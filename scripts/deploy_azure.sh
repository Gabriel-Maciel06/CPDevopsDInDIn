#!/usr/bin/env bash
# ==============================================================================
# PROJETO DIMDIM - 2º CHECKPOINT 2º SEMESTRE (DEVOPS TOOLS & CLOUD COMPUTING)
# Script de Automação Total: Azure CLI + Web App + Azure SQL + Application Insights
# ==============================================================================

set -euo pipefail

echo "======================================================================"
echo "🏦 DIMDIM CLOUD FINANCIAL - DEPLOY AUTOMATIZADO NA MICROSOFT AZURE"
echo "   Professor: João Menk | FIAP 2TDSR"
echo "   Equipe: Gabriel Maciel (RM562795) & Grupo DimDim"
echo "======================================================================"

# --- 1. CONFIGURAÇÕES E PARÂMETROS ---
export LOCATION="${AZURE_LOCATION:-canadacentral}"
export RESOURCE_GROUP="${AZURE_RG:-rg-dimdim-webapp}"
export UNIQUE_SUFFIX="${UNIQUE_SUFFIX:-rm562795}"

export SQL_SERVER_NAME="sql-dimdim-${UNIQUE_SUFFIX}"
export SQL_DB_NAME="sqldb-dimdim"
export SQL_ADMIN_USER="${SQL_ADMIN_USER:-dimdimadmin}"
# Senha NUNCA versionada: informe via variável de ambiente ou digite quando solicitado.
if [ -z "${SQL_ADMIN_PASS:-}" ]; then
    read -rsp "Senha do administrador do Azure SQL (min. 8 chars, maiúsc./minúsc./número/símbolo): " SQL_ADMIN_PASS
    echo ""
fi
: "${SQL_ADMIN_PASS:?Senha do Azure SQL não informada (export SQL_ADMIN_PASS='...')}"
export SQL_ADMIN_PASS

export APP_PLAN_NAME="plan-dimdim-${UNIQUE_SUFFIX}"
export WEBAPP_NAME="app-dimdim-${UNIQUE_SUFFIX}"
export WORKSPACE_NAME="law-dimdim-${UNIQUE_SUFFIX}"
export APP_INSIGHTS_NAME="appi-dimdim-${UNIQUE_SUFFIX}"

echo "[*] Verificando pré-requisitos..."
command -v az >/dev/null 2>&1 || { echo "[!] Azure CLI (az) não encontrado."; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "[!] Maven (mvn) não encontrado."; exit 1; }
command -v java >/dev/null 2>&1 || { echo "[!] Java JDK não encontrado."; exit 1; }

# Validar login na Azure
AZ_ACCOUNT=$(az account show --query name -o tsv 2>/dev/null || echo "")
if [ -z "$AZ_ACCOUNT" ]; then
    echo "[!] Você não está autenticado na Azure. Execute 'az login' antes de continuar."
    exit 1
fi
echo "[+] Autenticado na conta Azure: $AZ_ACCOUNT"
echo "[+] Região de provisionamento: $LOCATION"

# --- 2. CRIAÇÃO DO GRUPO DE RECURSOS ---
echo ""
echo "[*] [Passo 1/7] Criando Grupo de Recursos ($RESOURCE_GROUP)..."
az group create --name "$RESOURCE_GROUP" --location "$LOCATION" --output table

# --- 3. PROVISIONAMENTO DO AZURE SQL SERVER E DATABASE (PaaS) ---
echo ""
echo "[*] [Passo 2/7] Provisionando Azure SQL Server ($SQL_SERVER_NAME)..."
if ! az sql server show --name "$SQL_SERVER_NAME" --resource-group "$RESOURCE_GROUP" >/dev/null 2>&1; then
    az sql server create \
        --name "$SQL_SERVER_NAME" \
        --resource-group "$RESOURCE_GROUP" \
        --location "$LOCATION" \
        --admin-user "$SQL_ADMIN_USER" \
        --admin-password "$SQL_ADMIN_PASS" \
        --output table
else
    echo "[i] Azure SQL Server já existe. Prosseguindo..."
fi

echo "[*] Configurando Firewall do Azure SQL Server para permitir serviços Azure..."
az sql server firewall-rule create \
    --resource-group "$RESOURCE_GROUP" \
    --server "$SQL_SERVER_NAME" \
    --name "AllowAllWindowsAzureIps" \
    --start-ip-address 0.0.0.0 \
    --end-ip-address 0.0.0.0 \
    --output table || true

MY_IP=$(curl -s https://api.ipify.org || echo "")
if [ -n "$MY_IP" ]; then
    echo "[*] Liberando IP da máquina atual no Firewall ($MY_IP)..."
    az sql server firewall-rule create \
        --resource-group "$RESOURCE_GROUP" \
        --server "$SQL_SERVER_NAME" \
        --name "AllowClientIp" \
        --start-ip-address "$MY_IP" \
        --end-ip-address "$MY_IP" \
        --output table || true
fi

echo "[*] Provisionando Azure SQL Database ($SQL_DB_NAME - Tier Basic PaaS)..."
if ! az sql db show --name "$SQL_DB_NAME" --server "$SQL_SERVER_NAME" --resource-group "$RESOURCE_GROUP" >/dev/null 2>&1; then
    az sql db create \
        --resource-group "$RESOURCE_GROUP" \
        --server "$SQL_SERVER_NAME" \
        --name "$SQL_DB_NAME" \
        --edition Basic \
        --output table
else
    echo "[i] Banco de Dados já existe. Prosseguindo..."
fi

# --- 4. PROVISIONAMENTO DO APPLICATION INSIGHTS (MONITORAÇÃO) ---
echo ""
echo "[*] [Passo 3/7] Provisionando Log Analytics e Application Insights..."
if ! az monitor log-analytics workspace show --resource-group "$RESOURCE_GROUP" --workspace-name "$WORKSPACE_NAME" >/dev/null 2>&1; then
    az monitor log-analytics workspace create \
        --resource-group "$RESOURCE_GROUP" \
        --workspace-name "$WORKSPACE_NAME" \
        --location "$LOCATION" \
        --output table
fi

WORKSPACE_ID=$(az monitor log-analytics workspace show --resource-group "$RESOURCE_GROUP" --workspace-name "$WORKSPACE_NAME" --query id -o tsv)

if ! az monitor app-insights component show --app "$APP_INSIGHTS_NAME" --resource-group "$RESOURCE_GROUP" >/dev/null 2>&1; then
    az monitor app-insights component create \
        --app "$APP_INSIGHTS_NAME" \
        --location "$LOCATION" \
        --resource-group "$RESOURCE_GROUP" \
        --workspace "$WORKSPACE_ID" \
        --output table
fi

APP_INSIGHTS_CONN_STR=$(az monitor app-insights component show \
    --app "$APP_INSIGHTS_NAME" \
    --resource-group "$RESOURCE_GROUP" \
    --query connectionString -o tsv)

echo "[+] Application Insights configurado com sucesso!"

# --- 5. PROVISIONAMENTO DO APP SERVICE PLAN E WEB APP (PaaS) ---
echo ""
echo "[*] [Passo 4/7] Provisionando App Service Plan Linux ($APP_PLAN_NAME)..."
if ! az appservice plan show --name "$APP_PLAN_NAME" --resource-group "$RESOURCE_GROUP" >/dev/null 2>&1; then
    az appservice plan create \
        --name "$APP_PLAN_NAME" \
        --resource-group "$RESOURCE_GROUP" \
        --location "$LOCATION" \
        --is-linux \
        --sku B1 \
        --output table
fi

echo "[*] Provisionando Web App Linux ($WEBAPP_NAME - Runtime Java 21)..."
if ! az webapp show --name "$WEBAPP_NAME" --resource-group "$RESOURCE_GROUP" >/dev/null 2>&1; then
    az webapp create \
        --name "$WEBAPP_NAME" \
        --resource-group "$RESOURCE_GROUP" \
        --plan "$APP_PLAN_NAME" \
        --runtime "JAVA:21-java21" \
        --output table
fi

# --- 6. CONFIGURAÇÃO DE APPLICATION SETTINGS (SEGURANÇA & BANCO) ---
echo ""
echo "[*] [Passo 5/7] Injetando variáveis de ambiente seguras no Web App..."

JDBC_URL="jdbc:sqlserver://${SQL_SERVER_NAME}.database.windows.net:1433;database=${SQL_DB_NAME};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"

az webapp config appsettings set \
    --name "$WEBAPP_NAME" \
    --resource-group "$RESOURCE_GROUP" \
    --settings \
        AZURE_SQL_URL="$JDBC_URL" \
        AZURE_SQL_USERNAME="$SQL_ADMIN_USER" \
        AZURE_SQL_PASSWORD="$SQL_ADMIN_PASS" \
        AZURE_SQL_DRIVER="com.microsoft.sqlserver.jdbc.SQLServerDriver" \
        SPRING_PROFILES_ACTIVE="azure" \
        APPLICATIONINSIGHTS_CONNECTION_STRING="$APP_INSIGHTS_CONN_STR" \
        ApplicationInsightsAgent_EXTENSION_VERSION="~3" \
    --output table

# --- 6.1 CRIAÇÃO DAS TABELAS (DDL) ---
# Se o sqlcmd estiver instalado (brew install sqlcmd), o DDL oficial é aplicado automaticamente.
# Caso contrário, execute scripts/ddl_tables.sql no Query Editor do portal (ver README).
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
if command -v sqlcmd >/dev/null 2>&1; then
    export SQLCMDPASSWORD="$SQL_ADMIN_PASS"
    SQLCMD_ARGS=(-S "${SQL_SERVER_NAME}.database.windows.net" -d "$SQL_DB_NAME" -U "$SQL_ADMIN_USER")
    # O DDL faz DROP/CREATE: só aplica se as tabelas ainda não existem, para não apagar dados num redeploy.
    TABELA_EXISTE=$(sqlcmd "${SQLCMD_ARGS[@]}" -h -1 -W -Q "SET NOCOUNT ON; SELECT CASE WHEN OBJECT_ID('dbo.tb_dimdim_categoria','U') IS NULL THEN 0 ELSE 1 END" | tr -d '[:space:]')
    if [ "$TABELA_EXISTE" = "0" ]; then
        echo "[*] Aplicando DDL (scripts/ddl_tables.sql) no Azure SQL via sqlcmd..."
        sqlcmd "${SQLCMD_ARGS[@]}" -i "$SCRIPT_DIR/ddl_tables.sql"
    else
        echo "[i] Tabelas já existem no Azure SQL. DDL não reaplicado (preserva os dados)."
    fi
    unset SQLCMDPASSWORD
else
    echo "[i] sqlcmd não encontrado: execute scripts/ddl_tables.sql no Query Editor do Azure SQL."
fi

# --- 7. COMPILAÇÃO DO CÓDIGO FONTE E GERAÇÃO DO JAR ---
echo ""
echo "[*] [Passo 6/7] Compilando aplicação Spring Boot (Maven)..."
cd "$SCRIPT_DIR/.."
mvn clean package -DskipTests

# --- 8. DEPLOY AUTOMATIZADO VIA AZURE CLI (az webapp deploy) ---
echo ""
echo "[*] [Passo 7/7] Realizando Deploy do pacote JAR no Web App Azure..."
az webapp deploy \
    --resource-group "$RESOURCE_GROUP" \
    --name "$WEBAPP_NAME" \
    --src-path target/dimdim-webapp-1.0.0.jar \
    --type jar \
    --clean true \
    --restart true

WEBAPP_URL=$(az webapp show --name "$WEBAPP_NAME" --resource-group "$RESOURCE_GROUP" --query defaultHostName -o tsv)

echo ""
echo "======================================================================"
echo "🎉 DEPLOY CONCLUÍDO COM SUCESSO NA MICROSOFT AZURE!"
echo "======================================================================"
echo "🌐 URL da Aplicação Web (Front End):   https://${WEBAPP_URL}"
echo "📘 Swagger UI (OpenAPI Docs):          https://${WEBAPP_URL}/swagger-ui.html"
echo "🩺 Health Check & Telemetria:           https://${WEBAPP_URL}/api/health"
echo "📊 Application Insights:               ${APP_INSIGHTS_NAME}"
echo "🗄️ Azure SQL Server:                  ${SQL_SERVER_NAME}.database.windows.net"
echo "======================================================================"
