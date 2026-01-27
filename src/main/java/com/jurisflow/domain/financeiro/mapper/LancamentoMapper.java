package com.jurisflow.domain.financeiro.mapper;

import com.jurisflow.domain.financeiro.dto.LancamentoDTO;
import com.jurisflow.domain.financeiro.entity.Lancamento;
import org.springframework.stereotype.Component;

@Component
public class LancamentoMapper {

    public LancamentoDTO toDTO(Lancamento lancamento) {
        if (lancamento == null) return null;

        return LancamentoDTO.builder()
                .id(lancamento.getId())
                .tipo(lancamento.getTipo())
                .categoria(lancamento.getCategoria())
                .descricao(lancamento.getDescricao())
                .valor(lancamento.getValor())
                .dataVencimento(lancamento.getDataVencimento())
                .dataPagamento(lancamento.getDataPagamento())
                .status(lancamento.getStatus())
                .formaPagamento(lancamento.getFormaPagamento())
                .numeroDocumento(lancamento.getNumeroDocumento())
                .numeroParcela(lancamento.getNumeroParcela())
                .totalParcelas(lancamento.getTotalParcelas())
                .recorrente(lancamento.getRecorrente())
                .observacoes(lancamento.getObservacoes())
                .comprovanteUrl(lancamento.getComprovanteUrl())
                .processoId(lancamento.getProcesso() != null ? lancamento.getProcesso().getId() : null)
                .processoNumero(lancamento.getProcesso() != null ? lancamento.getProcesso().getNumero() : null)
                .clienteId(lancamento.getCliente() != null ? lancamento.getCliente().getId() : null)
                .clienteNome(lancamento.getCliente() != null ? lancamento.getCliente().getNome() : null)
                .createdAt(lancamento.getCreatedAt())
                .build();
    }
}

