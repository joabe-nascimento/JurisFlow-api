package com.jurisflow.controller;

import com.jurisflow.common.ApiResponse;
import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.documento.dto.DocumentoDTO;
import com.jurisflow.domain.documento.dto.DocumentoUploadDTO;
import com.jurisflow.domain.documento.entity.Documento.CategoriaDocumento;
import com.jurisflow.domain.documento.service.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Controller para gestão de documentos.
 */
@RestController
@RequestMapping("/v1/documentos")
@RequiredArgsConstructor
@Tag(name = "Documentos", description = "Gestão de documentos do escritório")
public class DocumentoController {

    private final DocumentoService documentoService;

    @GetMapping
    @Operation(summary = "Listar documentos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<PageResponse<DocumentoDTO>>> listar(
            @RequestParam(required = false) CategoriaDocumento categoria,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<DocumentoDTO> response = documentoService.listar(categoria, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar documento por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<DocumentoDTO>> buscarPorId(@PathVariable UUID id) {
        DocumentoDTO response = documentoService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/processo/{processoId}")
    @Operation(summary = "Listar documentos de um processo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO')")
    public ResponseEntity<ApiResponse<List<DocumentoDTO>>> listarPorProcesso(@PathVariable UUID processoId) {
        List<DocumentoDTO> response = documentoService.listarPorProcesso(processoId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar documentos de um cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<List<DocumentoDTO>>> listarPorCliente(@PathVariable UUID clienteId) {
        List<DocumentoDTO> response = documentoService.listarPorCliente(clienteId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de documento")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA')")
    public ResponseEntity<ApiResponse<DocumentoDTO>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart("dados") DocumentoUploadDTO dto
    ) throws IOException {
        DocumentoDTO response = documentoService.upload(file, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Documento enviado com sucesso"));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download de documento")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO', 'ESTAGIARIO', 'SECRETARIA')")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) throws IOException {
        DocumentoDTO documento = documentoService.buscarPorId(id);
        byte[] bytes = documentoService.download(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(documento.getTipo()));
        headers.setContentDispositionFormData("attachment", documento.getNomeOriginal());
        headers.setContentLength(bytes.length);

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir documento")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable UUID id) {
        documentoService.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Documento excluído com sucesso"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Estatísticas de documentos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCIO', 'ADVOGADO')")
    public ResponseEntity<ApiResponse<DocumentoService.DocumentoStats>> getStats() {
        DocumentoService.DocumentoStats stats = documentoService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}

