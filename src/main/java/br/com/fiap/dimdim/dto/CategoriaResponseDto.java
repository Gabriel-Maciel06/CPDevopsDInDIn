package br.com.fiap.dimdim.dto;

import br.com.fiap.dimdim.model.Categoria;
import br.com.fiap.dimdim.model.TipoOperacao;

import java.time.LocalDateTime;

public class CategoriaResponseDto {

    private Long id;
    private String nome;
    private TipoOperacao tipo;
    private String icone;
    private String descricao;
    private LocalDateTime dataCriacao;
    private int totalTransacoes;

    public CategoriaResponseDto() {
    }

    /**
     * @param totalTransacoes quantidade já agregada no banco; evita inicializar a coleção lazy
     *                        de transações (N+1) apenas para contá-la.
     */
    public CategoriaResponseDto(Categoria categoria, long totalTransacoes) {
        this.id = categoria.getId();
        this.nome = categoria.getNome();
        this.tipo = categoria.getTipo();
        this.icone = categoria.getIcone();
        this.descricao = categoria.getDescricao();
        this.dataCriacao = categoria.getDataCriacao();
        this.totalTransacoes = (int) totalTransacoes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoOperacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoOperacao tipo) {
        this.tipo = tipo;
    }

    public String getIcone() {
        return icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public int getTotalTransacoes() {
        return totalTransacoes;
    }

    public void setTotalTransacoes(int totalTransacoes) {
        this.totalTransacoes = totalTransacoes;
    }
}
