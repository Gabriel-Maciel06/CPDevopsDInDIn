package br.com.fiap.dimdim.config;

import br.com.fiap.dimdim.model.Categoria;
import br.com.fiap.dimdim.model.StatusTransacao;
import br.com.fiap.dimdim.model.TipoOperacao;
import br.com.fiap.dimdim.model.Transacao;
import br.com.fiap.dimdim.repository.CategoriaRepository;
import br.com.fiap.dimdim.repository.TransacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CategoriaRepository categoriaRepository;
    private final TransacaoRepository transacaoRepository;

    public DataInitializer(CategoriaRepository categoriaRepository, TransacaoRepository transacaoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() == 0) {
            log.info("Inicializando dados iniciais do Projeto DimDim no Azure SQL Database...");

            Categoria catReceita1 = new Categoria(null, "Consultoria Cloud & Inovação", TipoOperacao.RECEITA, "💡", "Receitas de consultoria estratégica para clientes enterprise como Steve Jobs");
            Categoria catReceita2 = new Categoria(null, "Assinaturas DimDim Pro", TipoOperacao.RECEITA, "💳", "Receitas recorrentes SaaS da plataforma DimDim");
            Categoria catDespesa1 = new Categoria(null, "Infraestrutura Cloud Azure", TipoOperacao.DESPESA, "☁️", "Custos de Web App, Azure SQL Database e Application Insights");
            Categoria catDespesa2 = new Categoria(null, "P&D e Ferramentas DevOps", TipoOperacao.DESPESA, "🛠️", "Licenças, automações CI/CD e ferramentas de engenharia");

            List<Categoria> categoriasSalvas = categoriaRepository.saveAll(List.of(catReceita1, catReceita2, catDespesa1, catDespesa2));

            Categoria catCons = categoriasSalvas.get(0);
            Categoria catSaaS = categoriasSalvas.get(1);
            Categoria catAzure = categoriasSalvas.get(2);
            Categoria catDevOps = categoriasSalvas.get(3);

            Transacao t1 = new Transacao(null, "Contrato Consultoria Cloud - Steve Jobs / Apple",
                    new BigDecimal("45000.00"), LocalDate.now().minusDays(5),
                    TipoOperacao.RECEITA, "TRANSFERENCIA", catCons,
                    "Primeira parcela do projeto de modernização PaaS", StatusTransacao.CONCLUIDA);

            Transacao t2 = new Transacao(null, "Faturamento Mensal DimDim Pro SaaS",
                    new BigDecimal("18250.00"), LocalDate.now().minusDays(3),
                    TipoOperacao.RECEITA, "PIX", catSaaS,
                    "Assinaturas de 350 clientes corporativos ativos", StatusTransacao.CONCLUIDA);

            Transacao t3 = new Transacao(null, "Custos Microsoft Azure PaaS (App Service + Azure SQL)",
                    new BigDecimal("3420.50"), LocalDate.now().minusDays(2),
                    TipoOperacao.DESPESA, "CARTAO_CREDITO", catAzure,
                    "Consumo de Web App B1 e Azure SQL Database S0", StatusTransacao.CONCLUIDA);

            Transacao t4 = new Transacao(null, "Licenças GitHub Enterprise & CI/CD Runners",
                    new BigDecimal("1200.00"), LocalDate.now().minusDays(1),
                    TipoOperacao.DESPESA, "BOLETO", catDevOps,
                    "Pipelines automatizados de deploy e segurança", StatusTransacao.CONCLUIDA);

            transacaoRepository.saveAll(List.of(t1, t2, t3, t4));
            log.info("Carga inicial de dados DimDim finalizada com sucesso! ({} categorias, {} transações)",
                    categoriaRepository.count(), transacaoRepository.count());
        } else {
            log.info("Base de dados DimDim já contém dados cadastrados (Categorias: {}, Transações: {}). Pulando seed.",
                    categoriaRepository.count(), transacaoRepository.count());
        }
    }
}
