-- ============================================================================
-- PROJETO DIMDIM - 2º CHECKPOINT 2º SEMESTRE
-- DEVOPS TOOLS & CLOUD COMPUTING - FIAP
-- Professor: João Menk
-- Equipe: Gabriel Maciel (RM562795), Vitória Martins (RM565160),
--         Augusto Bonomo (RM565155), Thomas Fontes (RM562254), Matheus Molina (RM563399)
-- Banco de Dados: Azure SQL Database (PaaS) - Microsoft SQL Server
-- ============================================================================

-- Colunas de texto livre usam NVARCHAR: VARCHAR na collation padrão do Azure SQL
-- (SQL_Latin1_General_CP1_CI_AS) não armazena emojis/Unicode e os grava como '?'.
-- Execute este script no Query Editor do Azure SQL ANTES do primeiro start da aplicação.

-- 1. DROP DAS TABELAS SE EXISTIREM (Ordem inversa das chaves estrangeiras)
IF OBJECT_ID('dbo.tb_dimdim_transacao', 'U') IS NOT NULL
    DROP TABLE dbo.tb_dimdim_transacao;
GO

IF OBJECT_ID('dbo.tb_dimdim_categoria', 'U') IS NOT NULL
    DROP TABLE dbo.tb_dimdim_categoria;
GO

-- ============================================================================
-- 2. TABELA 1: CATEGORIAS FINANCEIRAS (tb_dimdim_categoria)
-- ============================================================================
CREATE TABLE dbo.tb_dimdim_categoria (
    id              BIGINT IDENTITY(1,1) NOT NULL,
    nome           NVARCHAR(100)        NOT NULL,
    tipo            VARCHAR(20)          NOT NULL,
    icone          NVARCHAR(30)         NULL,
    descricao      NVARCHAR(255)        NULL,
    data_criacao    DATETIME2(7)         NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT pk_tb_dimdim_categoria PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uq_categoria_nome UNIQUE (nome),
    CONSTRAINT ck_categoria_tipo CHECK (tipo IN ('RECEITA', 'DESPESA'))
);
GO

-- ============================================================================
-- 3. TABELA 2: TRANSAÇÕES FINANCEIRAS (tb_dimdim_transacao)
--    Relacionamento: N Transações pertencem a 1 Categoria (1:N)
-- ============================================================================
CREATE TABLE dbo.tb_dimdim_transacao (
    id                BIGINT IDENTITY(1,1) NOT NULL,
    descricao        NVARCHAR(150)        NOT NULL,
    valor             DECIMAL(15, 2)       NOT NULL,
    data_transacao    DATE                 NOT NULL,
    tipo              VARCHAR(20)          NOT NULL,
    metodo_pagamento  VARCHAR(50)          NOT NULL,
    categoria_id      BIGINT               NOT NULL,
    observacoes      NVARCHAR(255)        NULL,
    status            VARCHAR(20)          NOT NULL DEFAULT 'CONCLUIDA',
    data_registro     DATETIME2(7)         NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT pk_tb_dimdim_transacao PRIMARY KEY CLUSTERED (id),
    CONSTRAINT fk_transacao_categoria FOREIGN KEY (categoria_id) 
        REFERENCES dbo.tb_dimdim_categoria (id) 
        ON DELETE NO ACTION,
    CONSTRAINT ck_transacao_tipo CHECK (tipo IN ('RECEITA', 'DESPESA')),
    CONSTRAINT ck_transacao_valor CHECK (valor > 0),
    CONSTRAINT ck_transacao_status CHECK (status IN ('CONCLUIDA', 'PENDENTE', 'CANCELADA'))
);
GO

-- ============================================================================
-- 4. ÍNDICES DE PERFORMANCE PARA CONSULTAS EM NUVEM
-- ============================================================================
CREATE NONCLUSTERED INDEX idx_transacao_categoria 
    ON dbo.tb_dimdim_transacao (categoria_id);
GO

CREATE NONCLUSTERED INDEX idx_transacao_tipo 
    ON dbo.tb_dimdim_transacao (tipo, status);
GO

CREATE NONCLUSTERED INDEX idx_transacao_data 
    ON dbo.tb_dimdim_transacao (data_transacao DESC);
GO

-- ============================================================================
-- 5. CARGA INICIAL DE DADOS (SEEDS DO CASO DIMDIM / STEVE JOBS)
-- ============================================================================
INSERT INTO dbo.tb_dimdim_categoria (nome, tipo, icone, descricao) VALUES
(N'Consultoria Cloud & Inovação', 'RECEITA', N'💡', N'Receitas de consultoria estratégica para Steve Jobs e Apple'),
('Assinaturas DimDim Pro', 'RECEITA', N'💳', 'Receitas SaaS recorrentes de clientes da plataforma DimDim'),
('Infraestrutura Cloud Azure', 'DESPESA', N'☁️', 'Custos mensais de Web App, Azure SQL Database e Application Insights'),
('P&D e Ferramentas DevOps', 'DESPESA', N'🛠️', N'Licenciamento de esteiras CI/CD e automações de engenharia');
GO

INSERT INTO dbo.tb_dimdim_transacao 
(descricao, valor, data_transacao, tipo, metodo_pagamento, categoria_id, observacoes, status) VALUES
('Contrato Consultoria Cloud - Steve Jobs', 45000.00, DATEADD(DAY, -5, CAST(GETDATE() AS DATE)), 'RECEITA', 'TRANSFERENCIA', 1, N'Primeira parcela modernização PaaS', 'CONCLUIDA'),
('Faturamento Mensal DimDim Pro SaaS', 18250.00, DATEADD(DAY, -3, CAST(GETDATE() AS DATE)), 'RECEITA', 'PIX', 2, N'Assinaturas ativas no período', 'CONCLUIDA'),
('Custos Microsoft Azure PaaS (App Service + SQL)', 3420.50, DATEADD(DAY, -2, CAST(GETDATE() AS DATE)), 'DESPESA', 'CARTAO_CREDITO', 3, N'Serviços gerenciados Azure', 'CONCLUIDA'),
(N'Licenças GitHub Enterprise & Runners', 1200.00, DATEADD(DAY, -1, CAST(GETDATE() AS DATE)), 'DESPESA', 'BOLETO', 4, 'Esteira automatizada CI/CD', 'CONCLUIDA');
GO

-- ============================================================================
-- 6. CONSULTAS DE VALIDAÇÃO (EVIDÊNCIAS DE PERSISTÊNCIA)
-- ============================================================================
-- Listar Categorias
SELECT * FROM dbo.tb_dimdim_categoria;

-- Listar Transações com JOIN na Categoria
SELECT 
    t.id,
    t.data_transacao,
    t.descricao,
    c.nome AS categoria_nome,
    t.tipo,
    t.metodo_pagamento,
    t.valor,
    t.status
FROM dbo.tb_dimdim_transacao t
INNER JOIN dbo.tb_dimdim_categoria c ON t.categoria_id = c.id
ORDER BY t.data_transacao DESC;

-- Balanço Financeiro Consolidado
SELECT 
    SUM(CASE WHEN tipo = 'RECEITA' THEN valor ELSE 0 END) AS TotalReceitas,
    SUM(CASE WHEN tipo = 'DESPESA' THEN valor ELSE 0 END) AS TotalDespesas,
    SUM(CASE WHEN tipo = 'RECEITA' THEN valor ELSE -valor END) AS SaldoLiquido
FROM dbo.tb_dimdim_transacao
WHERE status = 'CONCLUIDA';
GO
