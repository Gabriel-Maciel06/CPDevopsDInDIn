package br.com.fiap.dimdim.dto;

import java.math.BigDecimal;

public class ResumoFinanceiroDto {

    private BigDecimal totalReceitas;
    private BigDecimal totalDespesas;
    private BigDecimal saldoLiquido;
    private long totalTransacoes;
    private long totalCategorias;

    public ResumoFinanceiroDto() {
    }

    public ResumoFinanceiroDto(BigDecimal totalReceitas, BigDecimal totalDespesas, BigDecimal saldoLiquido,
                               long totalTransacoes, long totalCategorias) {
        this.totalReceitas = totalReceitas;
        this.totalDespesas = totalDespesas;
        this.saldoLiquido = saldoLiquido;
        this.totalTransacoes = totalTransacoes;
        this.totalCategorias = totalCategorias;
    }

    public BigDecimal getTotalReceitas() {
        return totalReceitas;
    }

    public void setTotalReceitas(BigDecimal totalReceitas) {
        this.totalReceitas = totalReceitas;
    }

    public BigDecimal getTotalDespesas() {
        return totalDespesas;
    }

    public void setTotalDespesas(BigDecimal totalDespesas) {
        this.totalDespesas = totalDespesas;
    }

    public BigDecimal getSaldoLiquido() {
        return saldoLiquido;
    }

    public void setSaldoLiquido(BigDecimal saldoLiquido) {
        this.saldoLiquido = saldoLiquido;
    }

    public long getTotalTransacoes() {
        return totalTransacoes;
    }

    public void setTotalTransacoes(long totalTransacoes) {
        this.totalTransacoes = totalTransacoes;
    }

    public long getTotalCategorias() {
        return totalCategorias;
    }

    public void setTotalCategorias(long totalCategorias) {
        this.totalCategorias = totalCategorias;
    }
}
