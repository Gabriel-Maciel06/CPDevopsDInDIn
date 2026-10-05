package br.com.fiap.dimdim.dto;

import br.com.fiap.dimdim.model.StatusTransacao;
import br.com.fiap.dimdim.model.TipoOperacao;
import br.com.fiap.dimdim.model.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransacaoResponseDto {

    private Long id;
    private String descricao;
    private BigDecimal valor;
    private LocalDate dataTransacao;
    private TipoOperacao tipo;
    private String metodoPagamento;
    private Long categoriaId;
    private String categoriaNome;
    private String categoriaIcone;
    private String observacoes;
    private StatusTransacao status;
    private LocalDateTime dataRegistro;

    public TransacaoResponseDto() {
    }

    public TransacaoResponseDto(Transacao t) {
        this.id = t.getId();
        this.descricao = t.getDescricao();
        this.valor = t.getValor();
        this.dataTransacao = t.getDataTransacao();
        this.tipo = t.getTipo();
        this.metodoPagamento = t.getMetodoPagamento();
        if (t.getCategoria() != null) {
            this.categoriaId = t.getCategoria().getId();
            this.categoriaNome = t.getCategoria().getNome();
            this.categoriaIcone = t.getCategoria().getIcone();
        }
        this.observacoes = t.getObservacoes();
        this.status = t.getStatus();
        this.dataRegistro = t.getDataRegistro();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getDataTransacao() {
        return dataTransacao;
    }

    public void setDataTransacao(LocalDate dataTransacao) {
        this.dataTransacao = dataTransacao;
    }

    public TipoOperacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoOperacao tipo) {
        this.tipo = tipo;
    }

    public String getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNome() {
        return categoriaNome;
    }

    public void setCategoriaNome(String categoriaNome) {
        this.categoriaNome = categoriaNome;
    }

    public String getCategoriaIcone() {
        return categoriaIcone;
    }

    public void setCategoriaIcone(String categoriaIcone) {
        this.categoriaIcone = categoriaIcone;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public StatusTransacao getStatus() {
        return status;
    }

    public void setStatus(StatusTransacao status) {
        this.status = status;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }
}
