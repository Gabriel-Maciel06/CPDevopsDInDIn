package br.com.fiap.dimdim.dto;

import br.com.fiap.dimdim.model.StatusTransacao;
import br.com.fiap.dimdim.model.TipoOperacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransacaoRequestDto {

    @NotBlank(message = "A descrição da transação é obrigatória")
    @Size(min = 2, max = 150, message = "A descrição deve ter entre 2 e 150 caracteres")
    private String descricao;

    @NotNull(message = "O valor da transação é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    private BigDecimal valor;

    @NotNull(message = "A data da transação é obrigatória")
    private LocalDate dataTransacao;

    @NotNull(message = "O tipo da transação é obrigatório (RECEITA ou DESPESA)")
    private TipoOperacao tipo;

    @NotBlank(message = "O método de pagamento é obrigatório")
    private String metodoPagamento;

    @NotNull(message = "O ID da categoria vinculada é obrigatório")
    private Long categoriaId;

    @Size(max = 255, message = "As observações podem ter no máximo 255 caracteres")
    private String observacoes;

    private StatusTransacao status;

    public TransacaoRequestDto() {
    }

    public TransacaoRequestDto(String descricao, BigDecimal valor, LocalDate dataTransacao,
                               TipoOperacao tipo, String metodoPagamento, Long categoriaId,
                               String observacoes, StatusTransacao status) {
        this.descricao = descricao;
        this.valor = valor;
        this.dataTransacao = dataTransacao;
        this.tipo = tipo;
        this.metodoPagamento = metodoPagamento;
        this.categoriaId = categoriaId;
        this.observacoes = observacoes;
        this.status = status;
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
}
