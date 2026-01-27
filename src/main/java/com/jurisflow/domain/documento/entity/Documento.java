package com.jurisflow.domain.documento.entity;

import com.jurisflow.common.BaseEntity;
import com.jurisflow.domain.cliente.entity.Cliente;
import com.jurisflow.domain.escritorio.entity.Escritorio;
import com.jurisflow.domain.processo.entity.Processo;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entidade que representa um documento do escritório.
 */
@Entity
@Table(name = "documentos", indexes = {
    @Index(name = "idx_documento_escritorio", columnList = "escritorio_id"),
    @Index(name = "idx_documento_processo", columnList = "processo_id"),
    @Index(name = "idx_documento_cliente", columnList = "cliente_id"),
    @Index(name = "idx_documento_categoria", columnList = "categoria")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Documento extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 300)
    private String nome;

    @Column(name = "nome_original", nullable = false, length = 500)
    private String nomeOriginal;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 100)
    private String tipo; // MIME type

    @Column(nullable = false)
    private Long tamanho; // bytes

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "storage_key", length = 500)
    private String storageKey; // Key no storage (S3, etc)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CategoriaDocumento categoria = CategoriaDocumento.OUTROS;

    @Column(name = "versao")
    @Builder.Default
    private Integer versao = 1;

    @Column(name = "hash_arquivo", length = 64)
    private String hashArquivo; // SHA-256 para verificar duplicatas

    @Column(name = "confidencial")
    @Builder.Default
    private Boolean confidencial = false;

    // ═══════════════════════════════════════════════════════════════
    // RELACIONAMENTOS
    // ═══════════════════════════════════════════════════════════════
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escritorio_id", nullable = false)
    private Escritorio escritorio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id")
    private Processo processo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // ═══════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════
    public enum CategoriaDocumento {
        CONTRATO,
        PETICAO,
        PROCURACAO,
        EVIDENCIA,
        FINANCEIRO,
        CERTIDAO,
        SENTENCA,
        RECURSO,
        CORRESPONDENCIA,
        OUTROS
    }
}

