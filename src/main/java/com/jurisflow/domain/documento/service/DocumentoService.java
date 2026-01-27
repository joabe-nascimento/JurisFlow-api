package com.jurisflow.domain.documento.service;

import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.cliente.repository.ClienteRepository;
import com.jurisflow.domain.documento.dto.DocumentoDTO;
import com.jurisflow.domain.documento.dto.DocumentoUploadDTO;
import com.jurisflow.domain.documento.entity.Documento;
import com.jurisflow.domain.documento.entity.Documento.CategoriaDocumento;
import com.jurisflow.domain.documento.mapper.DocumentoMapper;
import com.jurisflow.domain.documento.repository.DocumentoRepository;
import com.jurisflow.domain.processo.repository.ProcessoRepository;
import com.jurisflow.domain.usuario.service.UsuarioService;
import com.jurisflow.exception.BusinessException;
import com.jurisflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final ProcessoRepository processoRepository;
    private final ClienteRepository clienteRepository;
    private final DocumentoMapper documentoMapper;
    private final UsuarioService usuarioService;

    // Diretório de upload (em produção usar S3/MinIO)
    private static final String UPLOAD_DIR = "uploads/documentos/";

    /**
     * Lista documentos paginados
     */
    @Transactional(readOnly = true)
    public PageResponse<DocumentoDTO> listar(CategoriaDocumento categoria, String search, Pageable pageable) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Page<Documento> page;

        if (search != null && !search.isBlank()) {
            page = documentoRepository.searchByNome(escritorioId, search, pageable);
        } else if (categoria != null) {
            page = documentoRepository.findAllByEscritorioIdAndCategoria(escritorioId, categoria, pageable);
        } else {
            page = documentoRepository.findAllByEscritorioId(escritorioId, pageable);
        }

        List<DocumentoDTO> content = page.getContent().stream()
                .map(documentoMapper::toDTO)
                .collect(Collectors.toList());

        return PageResponse.<DocumentoDTO>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    /**
     * Busca documento por ID
     */
    @Transactional(readOnly = true)
    public DocumentoDTO buscarPorId(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Documento documento = documentoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", id));
        return documentoMapper.toDTO(documento);
    }

    /**
     * Lista documentos por processo
     */
    @Transactional(readOnly = true)
    public List<DocumentoDTO> listarPorProcesso(UUID processoId) {
        return documentoRepository.findAllByProcessoId(processoId).stream()
                .map(documentoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista documentos por cliente
     */
    @Transactional(readOnly = true)
    public List<DocumentoDTO> listarPorCliente(UUID clienteId) {
        return documentoRepository.findAllByClienteId(clienteId).stream()
                .map(documentoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Upload de documento
     */
    public DocumentoDTO upload(MultipartFile file, DocumentoUploadDTO dto) throws IOException {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();

        // Validar arquivo
        if (file.isEmpty()) {
            throw new BusinessException("Arquivo vazio");
        }

        // Calcular hash para verificar duplicatas
        String hash = calcularHash(file.getBytes());
        
        // Verificar se já existe
        documentoRepository.findByHashAndEscritorioId(hash, escritorioId)
                .ifPresent(d -> {
                    throw new BusinessException("Este documento já foi enviado anteriormente");
                });

        // Criar diretório se não existir
        Path uploadPath = Paths.get(UPLOAD_DIR, escritorioId.toString());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Gerar nome único
        String extensao = getExtensao(file.getOriginalFilename());
        String nomeArquivo = UUID.randomUUID() + extensao;
        Path filePath = uploadPath.resolve(nomeArquivo);

        // Salvar arquivo
        Files.copy(file.getInputStream(), filePath);

        // Criar documento
        Documento documento = Documento.builder()
                .nome(dto.getNome())
                .nomeOriginal(file.getOriginalFilename())
                .descricao(dto.getDescricao())
                .tipo(file.getContentType())
                .tamanho(file.getSize())
                .url("/api/v1/documentos/" + nomeArquivo + "/download")
                .storageKey(filePath.toString())
                .categoria(dto.getCategoria())
                .hashArquivo(hash)
                .confidencial(dto.getConfidencial() != null ? dto.getConfidencial() : false)
                .escritorio(usuarioService.getEscritorioFromContext())
                .build();

        // Vincular a processo se informado
        if (dto.getProcessoId() != null) {
            documento.setProcesso(processoRepository.findById(dto.getProcessoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Processo", dto.getProcessoId())));
        }

        // Vincular a cliente se informado
        if (dto.getClienteId() != null) {
            documento.setCliente(clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", dto.getClienteId())));
        }

        documento = documentoRepository.save(documento);
        log.info("Documento {} enviado com sucesso", documento.getId());

        return documentoMapper.toDTO(documento);
    }

    /**
     * Excluir documento (soft delete)
     */
    public void excluir(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Documento documento = documentoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", id));

        documento.softDelete();
        documentoRepository.save(documento);
        log.info("Documento {} excluído", id);
    }

    /**
     * Obter bytes do documento para download
     */
    @Transactional(readOnly = true)
    public byte[] download(UUID id) throws IOException {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Documento documento = documentoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", id));

        Path filePath = Paths.get(documento.getStorageKey());
        if (!Files.exists(filePath)) {
            throw new BusinessException("Arquivo não encontrado no storage");
        }

        return Files.readAllBytes(filePath);
    }

    /**
     * Estatísticas de documentos
     */
    @Transactional(readOnly = true)
    public DocumentoStats getStats() {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        long total = documentoRepository.countByEscritorioId(escritorioId);
        Long tamanhoTotal = documentoRepository.sumTamanhoByEscritorioId(escritorioId);

        return DocumentoStats.builder()
                .totalDocumentos(total)
                .tamanhoTotal(tamanhoTotal != null ? tamanhoTotal : 0L)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ═══════════════════════════════════════════════════════════════

    private String calcularHash(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao calcular hash", e);
        }
    }

    private String getExtensao(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }

    @lombok.Data
    @lombok.Builder
    public static class DocumentoStats {
        private long totalDocumentos;
        private long tamanhoTotal;
    }
}

