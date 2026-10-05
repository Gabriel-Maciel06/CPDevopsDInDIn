package br.com.fiap.dimdim.service;

import br.com.fiap.dimdim.dto.ResumoFinanceiroDto;
import br.com.fiap.dimdim.dto.TransacaoRequestDto;
import br.com.fiap.dimdim.dto.TransacaoResponseDto;
import br.com.fiap.dimdim.model.Categoria;
import br.com.fiap.dimdim.model.StatusTransacao;
import br.com.fiap.dimdim.model.TipoOperacao;
import br.com.fiap.dimdim.model.Transacao;
import br.com.fiap.dimdim.repository.CategoriaRepository;
import br.com.fiap.dimdim.repository.TransacaoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final CategoriaRepository categoriaRepository;

    public TransacaoService(TransacaoRepository transacaoRepository, CategoriaRepository categoriaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponseDto> listarTodas() {
        return transacaoRepository.findAll(Sort.by(Sort.Direction.DESC, "dataTransacao", "id")).stream()
                .map(TransacaoResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransacaoResponseDto buscarPorId(Long id) {
        Transacao transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada com o ID: " + id));
        return new TransacaoResponseDto(transacao);
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponseDto> listarPorCategoria(Long categoriaId) {
        return transacaoRepository.findByCategoriaId(categoriaId).stream()
                .map(TransacaoResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransacaoResponseDto criar(TransacaoRequestDto dto) {
        Transacao transacao = new Transacao();
        aplicarDados(transacao, dto);
        transacao.setStatus(dto.getStatus() != null ? dto.getStatus() : StatusTransacao.CONCLUIDA);

        return new TransacaoResponseDto(transacaoRepository.save(transacao));
    }

    @Transactional
    public TransacaoResponseDto atualizar(Long id, TransacaoRequestDto dto) {
        Transacao transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada com o ID: " + id));

        aplicarDados(transacao, dto);
        if (dto.getStatus() != null) {
            transacao.setStatus(dto.getStatus());
        }

        return new TransacaoResponseDto(transacaoRepository.save(transacao));
    }

    /**
     * Copia os campos editáveis do DTO para a entidade, validando a categoria.
     * Uma RECEITA não pode cair numa categoria de DESPESA (e vice-versa), senão o painel
     * financeiro agrupado por categoria fica inconsistente.
     */
    private void aplicarDados(Transacao transacao, TransacaoRequestDto dto) {
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria inexistente com o ID: " + dto.getCategoriaId()));

        if (categoria.getTipo() != dto.getTipo()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O tipo da transação (" + dto.getTipo() + ") deve ser igual ao tipo da categoria (" + categoria.getTipo() + ")");
        }

        transacao.setDescricao(dto.getDescricao().trim());
        transacao.setValor(dto.getValor());
        transacao.setDataTransacao(dto.getDataTransacao());
        transacao.setTipo(dto.getTipo());
        transacao.setMetodoPagamento(dto.getMetodoPagamento());
        transacao.setCategoria(categoria);
        transacao.setObservacoes(dto.getObservacoes());
    }

    @Transactional
    public void excluir(Long id) {
        Transacao transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada com o ID: " + id));
        transacaoRepository.delete(transacao);
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiroDto obterResumoFinanceiro() {
        BigDecimal receitas = transacaoRepository.sumValorByTipoAndConcluida(TipoOperacao.RECEITA);
        BigDecimal despesas = transacaoRepository.sumValorByTipoAndConcluida(TipoOperacao.DESPESA);
        if (receitas == null) receitas = BigDecimal.ZERO;
        if (despesas == null) despesas = BigDecimal.ZERO;

        BigDecimal saldo = receitas.subtract(despesas);
        long totalTransacoes = transacaoRepository.count();
        long totalCategorias = categoriaRepository.count();

        return new ResumoFinanceiroDto(receitas, despesas, saldo, totalTransacoes, totalCategorias);
    }
}
