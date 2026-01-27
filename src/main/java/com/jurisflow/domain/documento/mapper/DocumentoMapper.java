package com.jurisflow.domain.documento.mapper;

import com.jurisflow.domain.documento.dto.DocumentoDTO;
import com.jurisflow.domain.documento.entity.Documento;
import org.springframework.stereotype.Component;

@Component
public class DocumentoMapper {

    public DocumentoDTO toDTO(Documento documento) {
        if (documento == null) return null;

        return DocumentoDTO.builder()
                .id(documento.getId())
                .nome(documento.getNome())
                .nomeOriginal(documento.getNomeOriginal())
                .descricao(documento.getDescricao())
                .tipo(documento.getTipo())
                .tamanho(documento.getTamanho())
                .url(documento.getUrl())
                .categoria(documento.getCategoria())
                .versao(documento.getVersao())
                .confidencial(documento.getConfidencial())
                .processoId(documento.getProcesso() != null ? documento.getProcesso().getId() : null)
                .processoNumero(documento.getProcesso() != null ? documento.getProcesso().getNumero() : null)
                .clienteId(documento.getCliente() != null ? documento.getCliente().getId() : null)
                .clienteNome(documento.getCliente() != null ? documento.getCliente().getNome() : null)
                .createdAt(documento.getCreatedAt())
                .updatedAt(documento.getUpdatedAt())
                .build();
    }
}

