package br.com.fiap.dimdim.repository;

import br.com.fiap.dimdim.model.StatusTransacao;
import br.com.fiap.dimdim.model.TipoOperacao;
import br.com.fiap.dimdim.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByCategoriaId(Long categoriaId);

    boolean existsByCategoriaId(Long categoriaId);

    long countByCategoriaId(Long categoriaId);

    /** Retorna pares [categoriaId, quantidade] numa única consulta agregada. */
    @Query("SELECT t.categoria.id, COUNT(t) FROM Transacao t GROUP BY t.categoria.id")
    List<Object[]> contarPorCategoria();

    List<Transacao> findByTipo(TipoOperacao tipo);

    List<Transacao> findByStatus(StatusTransacao status);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.tipo = :tipo AND t.status = 'CONCLUIDA'")
    BigDecimal sumValorByTipoAndConcluida(@Param("tipo") TipoOperacao tipo);
}
