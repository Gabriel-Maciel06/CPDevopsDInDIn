package br.com.fiap.dimdim.service;

import br.com.fiap.dimdim.dto.CategoriaRequestDto;
import br.com.fiap.dimdim.dto.CategoriaResponseDto;
import br.com.fiap.dimdim.model.Categoria;
import br.com.fiap.dimdim.model.TipoOperacao;
import br.com.fiap.dimdim.repository.CategoriaRepository;
import br.com.fiap.dimdim.repository.TransacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoriaService {

    private static final String ICONE_PADRAO = "📁";

    private final CategoriaRepository categoriaRepository;
    private final TransacaoRepository transacaoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, TransacaoRepository transacaoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDto> listarTodas() {
        return toResponseList(categoriaRepository.findAll());
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDto buscarPorId(Long id) {
        Categoria categoria = buscarEntidade(id);
        return new CategoriaResponseDto(categoria, transacaoRepository.countByCategoriaId(id));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDto> listarPorTipo(TipoOperacao tipo) {
        return toResponseList(categoriaRepository.findByTipo(tipo));
    }

    @Transactional
    public CategoriaResponseDto criar(CategoriaRequestDto dto) {
        String nome = dto.getNome().trim();
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma categoria cadastrada com o nome: " + nome);
        }

        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setTipo(dto.getTipo());
        categoria.setIcone(isPreenchido(dto.getIcone()) ? dto.getIcone() : ICONE_PADRAO);
        categoria.setDescricao(dto.getDescricao());

        return new CategoriaResponseDto(categoriaRepository.save(categoria), 0);
    }

    @Transactional
    public CategoriaResponseDto atualizar(Long id, CategoriaRequestDto dto) {
        Categoria categoria = buscarEntidade(id);

        String nome = dto.getNome().trim();
        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe outra categoria com o nome: " + nome);
        }

        categoria.setNome(nome);
        categoria.setTipo(dto.getTipo());
        if (isPreenchido(dto.getIcone())) {
            categoria.setIcone(dto.getIcone());
        }
        categoria.setDescricao(dto.getDescricao());

        Categoria atualizada = categoriaRepository.save(categoria);
        return new CategoriaResponseDto(atualizada, transacaoRepository.countByCategoriaId(id));
    }

    /**
     * Exclui a categoria somente se não houver transações vinculadas.
     * Excluir em cascata apagaria histórico financeiro sem aviso, por isso a regra retorna 409.
     */
    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarEntidade(id);
        if (transacaoRepository.existsByCategoriaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A categoria possui transações vinculadas. Exclua ou mova as transações antes de removê-la.");
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria buscarEntidade(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada com o ID: " + id));
    }

    /** Monta a resposta com a contagem de transações obtida em uma única consulta agregada. */
    private List<CategoriaResponseDto> toResponseList(List<Categoria> categorias) {
        Map<Long, Long> totais = new HashMap<>();
        for (Object[] linha : transacaoRepository.contarPorCategoria()) {
            totais.put((Long) linha[0], (Long) linha[1]);
        }
        return categorias.stream()
                .map(c -> new CategoriaResponseDto(c, totais.getOrDefault(c.getId(), 0L)))
                .toList();
    }

    private static boolean isPreenchido(String valor) {
        return valor != null && !valor.isBlank();
    }
}
