#!/usr/bin/env bash
# ==============================================================================
# PROJETO DIMDIM - Script para Exclusão dos Recursos Azure (Economia de Créditos)
# ==============================================================================

set -euo pipefail

RESOURCE_GROUP="${AZURE_RG:-rg-dimdim-webapp}"

echo "⚠️  ATENÇÃO: Você está prestes a excluir o grupo de recursos '$RESOURCE_GROUP'."
echo "   Todos os recursos (Web App, Azure SQL, Application Insights) serão removidos permanentemente."
read -p "Confirma a exclusão? (s/N): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Ss]$ ]]; then
    echo "[*] Excluindo Resource Group $RESOURCE_GROUP em segundo plano..."
    az group delete --name "$RESOURCE_GROUP" --yes --no-wait
    echo "[+] Comando de exclusão enviado com sucesso!"
else
    echo "[-] Operação cancelada pelo usuário."
fi
