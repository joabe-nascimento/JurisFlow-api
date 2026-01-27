package com.jurisflow.domain.financeiro.service;

import com.jurisflow.common.PageResponse;
import com.jurisflow.domain.cliente.repository.ClienteRepository;
import com.jurisflow.domain.financeiro.dto.LancamentoCreateDTO;
import com.jurisflow.domain.financeiro.dto.LancamentoDTO;
import com.jurisflow.domain.financeiro.dto.ResumoFinanceiroDTO;
import com.jurisflow.domain.financeiro.entity.Lancamento;
import com.jurisflow.domain.financeiro.entity.Lancamento.*;
import com.jurisflow.domain.financeiro.mapper.LancamentoMapper;
import com.jurisflow.domain.financeiro.repository.LancamentoRepository;
import com.jurisflow.domain.processo.repository.ProcessoRepository;
import com.jurisflow.domain.usuario.service.UsuarioService;
import com.jurisflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FinanceiroService {

    private final LancamentoRepository lancamentoRepository;
    private final ProcessoRepository processoRepository;
    private final ClienteRepository clienteRepository;
    private final LancamentoMapper lancamentoMapper;
    private final UsuarioService usuarioService;

    /**
     * Lista lançamentos paginados
     */
    @Transactional(readOnly = true)
    public PageResponse<LancamentoDTO> listar(TipoLancamento tipo, Pageable pageable) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Page<Lancamento> page;

        if (tipo != null) {
            page = lancamentoRepository.findByTipo(escritorioId, tipo, pageable);
        } else {
            page = lancamentoRepository.findAllByEscritorioId(escritorioId, pageable);
        }

        List<LancamentoDTO> content = page.getContent().stream()
                .map(lancamentoMapper::toDTO)
                .collect(Collectors.toList());

        return PageResponse.<LancamentoDTO>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    /**
     * Busca lançamento por ID
     */
    @Transactional(readOnly = true)
    public LancamentoDTO buscarPorId(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Lancamento lancamento = lancamentoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento", id));
        return lancamentoMapper.toDTO(lancamento);
    }

    /**
     * Lista lançamentos por período
     */
    @Transactional(readOnly = true)
    public List<LancamentoDTO> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        return lancamentoRepository.findByPeriodo(escritorioId, inicio, fim).stream()
                .map(lancamentoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista lançamentos atrasados
     */
    @Transactional(readOnly = true)
    public List<LancamentoDTO> listarAtrasados() {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        return lancamentoRepository.findAtrasados(escritorioId).stream()
                .map(lancamentoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista lançamentos por processo
     */
    @Transactional(readOnly = true)
    public List<LancamentoDTO> listarPorProcesso(UUID processoId) {
        return lancamentoRepository.findByProcessoId(processoId).stream()
                .map(lancamentoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Criar lançamento
     */
    public LancamentoDTO criar(LancamentoCreateDTO dto) {
        // Se tem parcelas, criar múltiplos lançamentos
        if (dto.getNumeroParcelas() != null && dto.getNumeroParcelas() > 1) {
            return criarParcelado(dto);
        }

        return criarLancamentoUnico(dto, null, null);
    }

    private LancamentoDTO criarParcelado(LancamentoCreateDTO dto) {
        int parcelas = dto.getNumeroParcelas();
        List<LancamentoDTO> lancamentos = new ArrayList<>();

        for (int i = 1; i <= parcelas; i++) {
            lancamentos.add(criarLancamentoUnico(dto, i, parcelas));
        }

        // Retorna o primeiro lançamento
        return lancamentos.get(0);
    }

    private LancamentoDTO criarLancamentoUnico(LancamentoCreateDTO dto, Integer numeroParcela, Integer totalParcelas) {
        BigDecimal valor = dto.getValor();
        if (totalParcelas != null) {
            valor = dto.getValor().divide(BigDecimal.valueOf(totalParcelas), 2, java.math.RoundingMode.HALF_UP);
        }

        LocalDate dataVencimento = dto.getDataVencimento();
        if (numeroParcela != null && numeroParcela > 1) {
            dataVencimento = dto.getDataVencimento().plusMonths(numeroParcela - 1);
        }

        Lancamento lancamento = Lancamento.builder()
                .tipo(dto.getTipo())
                .categoria(dto.getCategoria())
                .descricao(dto.getDescricao() + (numeroParcela != null ? " - Parcela " + numeroParcela + "/" + totalParcelas : ""))
                .valor(valor)
                .dataVencimento(dataVencimento)
                .formaPagamento(dto.getFormaPagamento())
                .numeroDocumento(dto.getNumeroDocumento())
                .numeroParcela(numeroParcela)
                .totalParcelas(totalParcelas)
                .recorrente(dto.getRecorrente() != null ? dto.getRecorrente() : false)
                .observacoes(dto.getObservacoes())
                .escritorio(usuarioService.getEscritorioFromContext())
                .build();

        // Vincular processo
        if (dto.getProcessoId() != null) {
            lancamento.setProcesso(processoRepository.findById(dto.getProcessoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Processo", dto.getProcessoId())));
        }

        // Vincular cliente
        if (dto.getClienteId() != null) {
            lancamento.setCliente(clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", dto.getClienteId())));
        }

        lancamento = lancamentoRepository.save(lancamento);
        log.info("Lançamento {} criado", lancamento.getId());

        return lancamentoMapper.toDTO(lancamento);
    }

    /**
     * Registrar pagamento
     */
    public LancamentoDTO registrarPagamento(UUID id, LocalDate dataPagamento, FormaPagamento formaPagamento) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Lancamento lancamento = lancamentoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento", id));

        lancamento.setDataPagamento(dataPagamento);
        lancamento.setFormaPagamento(formaPagamento);
        lancamento.setStatus(StatusLancamento.PAGO);

        lancamento = lancamentoRepository.save(lancamento);
        log.info("Pagamento registrado para lançamento {}", id);

        return lancamentoMapper.toDTO(lancamento);
    }

    /**
     * Cancelar lançamento
     */
    public LancamentoDTO cancelar(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Lancamento lancamento = lancamentoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento", id));

        lancamento.setStatus(StatusLancamento.CANCELADO);
        lancamento = lancamentoRepository.save(lancamento);
        log.info("Lançamento {} cancelado", id);

        return lancamentoMapper.toDTO(lancamento);
    }

    /**
     * Excluir lançamento
     */
    public void excluir(UUID id) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();
        Lancamento lancamento = lancamentoRepository.findByIdAndEscritorioId(id, escritorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento", id));

        lancamento.softDelete();
        lancamentoRepository.save(lancamento);
        log.info("Lançamento {} excluído", id);
    }

    /**
     * Resumo financeiro
     */
    @Transactional(readOnly = true)
    public ResumoFinanceiroDTO getResumo(LocalDate inicio, LocalDate fim) {
        UUID escritorioId = usuarioService.getEscritorioIdFromContext();

        BigDecimal totalReceitas = lancamentoRepository.sumReceitasPagasPeriodo(escritorioId, inicio, fim);
        BigDecimal totalDespesas = lancamentoRepository.sumDespesasPagasPeriodo(escritorioId, inicio, fim);
        BigDecimal receitasPendentes = lancamentoRepository.sumPendentesByTipo(escritorioId, TipoLancamento.RECEITA);
        BigDecimal despesasPendentes = lancamentoRepository.sumPendentesByTipo(escritorioId, TipoLancamento.DESPESA);
        BigDecimal receitasAtrasadas = lancamentoRepository.sumAtrasadosByTipo(escritorioId, TipoLancamento.RECEITA);
        BigDecimal despesasAtrasadas = lancamentoRepository.sumAtrasadosByTipo(escritorioId, TipoLancamento.DESPESA);

        long totalLancamentos = lancamentoRepository.count();
        long lancamentosPendentes = lancamentoRepository.countByStatus(escritorioId, StatusLancamento.PENDENTE);
        long lancamentosAtrasados = lancamentoRepository.findAtrasados(escritorioId).size();

        return ResumoFinanceiroDTO.builder()
                .totalReceitas(totalReceitas)
                .totalDespesas(totalDespesas)
                .saldo(totalReceitas.subtract(totalDespesas))
                .receitasPendentes(receitasPendentes)
                .despesasPendentes(despesasPendentes)
                .receitasAtrasadas(receitasAtrasadas)
                .despesasAtrasadas(despesasAtrasadas)
                .totalLancamentos(totalLancamentos)
                .lancamentosPendentes(lancamentosPendentes)
                .lancamentosAtrasados(lancamentosAtrasados)
                .build();
    }
}

