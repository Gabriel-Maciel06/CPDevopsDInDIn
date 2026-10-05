package br.com.fiap.dimdim.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_dimdim_transacao")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A descrição da transação é obrigatória")
    @Size(min = 2, max = 150, message = "A descrição deve ter entre 2 e 150 caracteres")
    @Column(name = "descricao", nullable = false, length = 150)
    private String descricao;

    @NotNull(message = "O valor da transação é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "A data da transação é obrigatória")
    @Column(name = "data_transacao", nullable = false)
    private LocalDate dataTransacao;

    @NotNull(message = "O tipo da transação é obrigatório (RECEITA ou DESPESA)")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoOperacao tipo;

    @NotBlank(message = "O método de pagamento é obrigatório")
    @Column(name = "metodo_pagamento", nullable = false, length = 50)
    private String metodoPagamento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(name = "observacoes", length = 255)
    private String observacoes;

    @NotNull(message = "O status da transação é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusTransacao status;

    @Column(name = "data_registro", nullable = false, updatable = false)
    private LocalDateTime dataRegistro;

    public Transacao() {
    }

    public Transacao(Long id, String descricao, BigDecimal valor, LocalDate dataTransacao,
                     TipoOperacao tipo, String metodoPagamento, Categoria categoria,
                     String observacoes, StatusTransacao status) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.dataTransacao = dataTransacao;
        this.tipo = tipo;
        this.metodoPagamento = metodoPagamento;
        this.categoria = categoria;
        this.observacoes = observacoes;
        this.status = status;
        this.dataRegistro = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.dataRegistro == null) {
            this.dataRegistro = LocalDateTime.now();
        }
        if (this.dataTransacao == null) {
            this.dataTransacao = LocalDate.now();
        }
        if (this.status == null) {
            this.status = StatusTransacao.CONCLUIDA;
        }
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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
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
