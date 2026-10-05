let categoriasState = [];
let transacoesState = [];

document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    initTodayDate();
    loadHealthStatus();
    loadDashboard();
    loadCategorias();
    loadTransacoes();

    // Auto-refresh telemetry and dashboard every 30s
    setInterval(() => {
        loadHealthStatus();
        loadDashboard();
    }, 30000);
});

// Escapa texto vindo da API antes de interpolar em innerHTML (previne XSS armazenado:
// nome/descrição/observações são digitados pelo usuário e persistidos no banco).
function esc(value) {
    return String(value ?? '').replace(/[&<>"']/g, ch => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[ch]));
}

// Extrai a mensagem padronizada do GlobalExceptionHandler ({ message, errors[] }).
async function extrairMensagemErro(res, fallback) {
    const err = await res.json().catch(() => ({}));
    const detalhes = Array.isArray(err.errors) && err.errors.length ? ` (${err.errors.join('; ')})` : '';
    return (err.message || fallback) + detalhes;
}

// Toast notification helper
function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${type === 'success' ? '✅' : '⚠️'}</span> <span>${esc(message)}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(50px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// Formata moeda Real BRL
function formatMoney(value) {
    if (value === null || value === undefined) return 'R$ 0,00';
    return Number(value).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

// Formata data ISO (YYYY-MM-DD) para PT-BR
function formatDate(dateStr) {
    if (!dateStr) return '--/--/----';
    const parts = dateStr.split('-');
    if (parts.length === 3) return `${parts[2]}/${parts[1]}/${parts[0]}`;
    return dateStr;
}

// Set data de hoje no formulário
function initTodayDate() {
    const today = new Date().toISOString().split('T')[0];
    const dataInput = document.getElementById('transacao-data');
    if (dataInput) dataInput.value = today;
}

// TABS
function initTabs() {
    const buttons = document.querySelectorAll('.tab-btn');
    buttons.forEach(btn => {
        btn.addEventListener('click', () => {
            buttons.forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

            btn.classList.add('active');
            const targetId = btn.getAttribute('data-tab');
            const targetContent = document.getElementById(targetId);
            if (targetContent) targetContent.classList.add('active');
        });
    });
}

// HEALTH / TELEMETRY
async function loadHealthStatus() {
    try {
        const res = await fetch('/api/health');
        if (!res.ok) throw new Error('Erro na consulta de saúde');
        const data = await res.json();

        const healthText = document.getElementById('health-status-text');
        const healthLatency = document.getElementById('health-latency');
        const badgeDb = document.getElementById('badge-db-status');
        const badgeTelemetry = document.getElementById('badge-telemetry');

        if (data.bancoStatus === 'CONECTADO') {
            healthText.textContent = `Online: ${data.bancoTipo}`;
            healthText.style.color = 'var(--success)';
            healthLatency.textContent = `Latência Banco: ${data.bancoLatenciaMs || 0} ms | App: ${data.ambiente}`;
            badgeDb.classList.remove('badge-sql-error');
        } else {
            healthText.textContent = 'Atenção: Banco desconectado';
            healthText.style.color = 'var(--danger)';
            healthLatency.textContent = `Erro: ${data.bancoErro || 'Desconhecido'}`;
        }

        if (data.applicationInsightsAtivo) {
            badgeTelemetry.title = 'Application Insights Ativo e Coletando';
        }
    } catch (e) {
        console.warn('Health check falhou:', e);
    }
}

// DASHBOARD KPIS
async function loadDashboard() {
    try {
        const res = await fetch('/api/dashboard/resumo');
        if (!res.ok) throw new Error('Erro ao carregar resumo financeiro');
        const data = await res.json();

        const saldoEl = document.getElementById('kpi-saldo');
        saldoEl.textContent = formatMoney(data.saldoLiquido);
        if (Number(data.saldoLiquido) >= 0) {
            saldoEl.style.color = 'var(--success)';
        } else {
            saldoEl.style.color = 'var(--danger)';
        }

        document.getElementById('kpi-receitas').textContent = formatMoney(data.totalReceitas);
        document.getElementById('kpi-despesas').textContent = formatMoney(data.totalDespesas);
        document.getElementById('kpi-transacoes-count').textContent = data.totalTransacoes;
        document.getElementById('kpi-categorias-count').textContent = data.totalCategorias;
    } catch (e) {
        console.error('Falha ao carregar KPIs:', e);
    }
}

// CATEGORIAS (CRUD)
async function loadCategorias() {
    try {
        const res = await fetch('/api/categorias');
        if (!res.ok) throw new Error('Erro ao listar categorias');
        categoriasState = await res.json();
        renderCategoriasTable(categoriasState);
        atualizarSelectCategoriasNoModal();
    } catch (e) {
        console.error('Erro ao carregar categorias:', e);
        document.getElementById('tbody-categorias').innerHTML = `
            <tr><td colspan="8" class="text-center py-6 text-red">Erro ao carregar categorias do Azure SQL.</td></tr>
        `;
    }
}

function renderCategoriasTable(categorias) {
    const tbody = document.getElementById('tbody-categorias');
    if (!categorias || categorias.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center py-6">Nenhuma categoria cadastrada.</td></tr>`;
        return;
    }

    tbody.innerHTML = categorias.map(c => `
        <tr>
            <td><code>#${c.id}</code></td>
            <td style="font-size: 1.4rem;">${esc(c.icone || '📁')}</td>
            <td><strong>${esc(c.nome)}</strong></td>
            <td>
                <span class="status-pill ${c.tipo === 'RECEITA' ? 'pill-receita' : 'pill-despesa'}">
                    ${c.tipo}
                </span>
            </td>
            <td style="color: var(--text-secondary); max-width: 280px;">${esc(c.descricao || '-')}</td>
            <td><span class="category-tag">${c.totalTransacoes} transações</span></td>
            <td style="font-size: 0.8rem; color: var(--text-muted);">${c.dataCriacao ? c.dataCriacao.split('T')[0] : '-'}</td>
            <td class="text-right">
                <button class="btn-icon" onclick="editarCategoria(${c.id})" title="Editar">✏️</button>
                <button class="btn-icon delete" onclick="excluirCategoria(${c.id})" title="Excluir">🗑️</button>
            </td>
        </tr>
    `).join('');
}

function abrirModalCategoria() {
    document.getElementById('categoria-id').value = '';
    document.getElementById('categoria-nome').value = '';
    document.getElementById('categoria-tipo').value = 'RECEITA';
    document.getElementById('categoria-icone').value = '💡';
    document.getElementById('categoria-descricao').value = '';
    document.getElementById('modal-categoria-title').textContent = 'Nova Categoria DimDim';
    document.getElementById('modal-categoria').classList.add('open');
}

function fecharModalCategoria() {
    document.getElementById('modal-categoria').classList.remove('open');
}

function editarCategoria(id) {
    const cat = categoriasState.find(c => c.id === id);
    if (!cat) return;

    document.getElementById('categoria-id').value = cat.id;
    document.getElementById('categoria-nome').value = cat.nome;
    document.getElementById('categoria-tipo').value = cat.tipo;
    document.getElementById('categoria-icone').value = cat.icone || '📁';
    document.getElementById('categoria-descricao').value = cat.descricao || '';
    document.getElementById('modal-categoria-title').textContent = `Editar Categoria #${cat.id}`;
    document.getElementById('modal-categoria').classList.add('open');
}

async function salvarCategoria(e) {
    e.preventDefault();
    const id = document.getElementById('categoria-id').value;
    const payload = {
        nome: document.getElementById('categoria-nome').value.trim(),
        tipo: document.getElementById('categoria-tipo').value,
        icone: document.getElementById('categoria-icone').value,
        descricao: document.getElementById('categoria-descricao').value.trim()
    };

    const btn = document.getElementById('btn-salvar-categoria');
    btn.disabled = true;
    btn.textContent = 'Salvando no Azure SQL...';

    try {
        const url = id ? `/api/categorias/${id}` : '/api/categorias';
        const method = id ? 'PUT' : 'POST';
        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            throw new Error(await extrairMensagemErro(res, 'Falha ao salvar categoria'));
        }

        fecharModalCategoria();
        showToast(id ? 'Categoria atualizada no Azure SQL!' : 'Categoria cadastrada com sucesso!');
        await loadCategorias();
        await loadDashboard();
    } catch (err) {
        alert('Erro: ' + err.message);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Salvar Categoria';
    }
}

async function excluirCategoria(id) {
    const cat = categoriasState.find(c => c.id === id);
    const confirmMsg = `Deseja realmente excluir a categoria "${cat ? cat.nome : id}" do Azure SQL?`;
    if (!confirm(confirmMsg)) return;

    try {
        const res = await fetch(`/api/categorias/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error(await extrairMensagemErro(res, 'Não foi possível excluir a categoria'));
        showToast('Categoria excluída do Azure SQL com sucesso!');
        await loadCategorias();
        await loadTransacoes();
        await loadDashboard();
    } catch (e) {
        alert('Erro ao excluir: ' + e.message);
    }
}

// TRANSAÇÕES (CRUD)
async function loadTransacoes() {
    try {
        const res = await fetch('/api/transacoes');
        if (!res.ok) throw new Error('Erro ao listar transações');
        transacoesState = await res.json();
        filtrarTransacoes();
    } catch (e) {
        console.error('Erro ao carregar transações:', e);
        document.getElementById('tbody-transacoes').innerHTML = `
            <tr><td colspan="9" class="text-center py-6 text-red">Erro ao carregar transações do Azure SQL.</td></tr>
        `;
    }
}

function filtrarTransacoes() {
    const filtro = document.getElementById('filter-tipo-transacao').value;
    let filtradas = transacoesState;
    if (filtro !== 'TODOS') {
        filtradas = transacoesState.filter(t => t.tipo === filtro);
    }
    renderTransacoesTable(filtradas);
}

function renderTransacoesTable(transacoes) {
    const tbody = document.getElementById('tbody-transacoes');
    if (!transacoes || transacoes.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center py-6">Nenhuma transação encontrada no Azure SQL.</td></tr>`;
        return;
    }

    tbody.innerHTML = transacoes.map(t => {
        const isReceita = t.tipo === 'RECEITA';
        const valorClass = isReceita ? 'text-green' : 'text-red';
        const valorPrefix = isReceita ? '+ ' : '- ';

        let pillStatusClass = 'pill-concluida';
        if (t.status === 'PENDENTE') pillStatusClass = 'pill-pendente';
        if (t.status === 'CANCELADA') pillStatusClass = 'pill-cancelada';

        return `
            <tr>
                <td><code>#${t.id}</code></td>
                <td>${formatDate(t.dataTransacao)}</td>
                <td>
                    <strong>${esc(t.descricao)}</strong>
                    ${t.observacoes ? `<div style="font-size: 0.75rem; color: var(--text-muted);">${esc(t.observacoes)}</div>` : ''}
                </td>
                <td>
                    <span class="category-tag">
                        <span>${esc(t.categoriaIcone || '📁')}</span>
                        <span>${esc(t.categoriaNome || 'Geral')}</span>
                    </span>
                </td>
                <td>
                    <span class="status-pill ${isReceita ? 'pill-receita' : 'pill-despesa'}">
                        ${t.tipo}
                    </span>
                </td>
                <td style="font-size: 0.82rem; color: var(--text-secondary);">${esc(t.metodoPagamento)}</td>
                <td style="font-family: 'JetBrains Mono', monospace; font-weight: 700;" class="${valorClass}">
                    ${valorPrefix}${formatMoney(t.valor)}
                </td>
                <td>
                    <span class="status-pill ${pillStatusClass}">${t.status}</span>
                </td>
                <td class="text-right">
                    <button class="btn-icon" onclick="editarTransacao(${t.id})" title="Editar">✏️</button>
                    <button class="btn-icon delete" onclick="excluirTransacao(${t.id})" title="Excluir">🗑️</button>
                </td>
            </tr>
        `;
    }).join('');
}

function atualizarSelectCategoriasNoModal() {
    filtrarCategoriasPorTipoNoModal();
}

function filtrarCategoriasPorTipoNoModal() {
    const select = document.getElementById('transacao-categoria');
    const tipo = document.getElementById('transacao-tipo').value;

    const filtradas = categoriasState.filter(c => c.tipo === tipo);
    if (filtradas.length === 0) {
        select.innerHTML = `<option value="">Nenhuma categoria de ${tipo} cadastrada</option>`;
        return;
    }

    select.innerHTML = filtradas.map(c => `
        <option value="${c.id}">${esc(c.icone || '📁')} ${esc(c.nome)}</option>
    `).join('');
}

function abrirModalTransacao() {
    document.getElementById('transacao-id').value = '';
    document.getElementById('transacao-descricao').value = '';
    document.getElementById('transacao-valor').value = '';
    initTodayDate();
    document.getElementById('transacao-tipo').value = 'RECEITA';
    filtrarCategoriasPorTipoNoModal();
    document.getElementById('transacao-pagamento').value = 'PIX';
    document.getElementById('transacao-status').value = 'CONCLUIDA';
    document.getElementById('transacao-obs').value = '';
    document.getElementById('modal-transacao-title').textContent = 'Nova Transação DimDim';
    document.getElementById('modal-transacao').classList.add('open');
}

function fecharModalTransacao() {
    document.getElementById('modal-transacao').classList.remove('open');
}

function editarTransacao(id) {
    const t = transacoesState.find(item => item.id === id);
    if (!t) return;

    document.getElementById('transacao-id').value = t.id;
    document.getElementById('transacao-descricao').value = t.descricao;
    document.getElementById('transacao-valor').value = t.valor;
    document.getElementById('transacao-data').value = t.dataTransacao;
    document.getElementById('transacao-tipo').value = t.tipo;
    filtrarCategoriasPorTipoNoModal();
    document.getElementById('transacao-categoria').value = t.categoriaId;
    document.getElementById('transacao-pagamento').value = t.metodoPagamento;
    document.getElementById('transacao-status').value = t.status;
    document.getElementById('transacao-obs').value = t.observacoes || '';
    document.getElementById('modal-transacao-title').textContent = `Editar Transação #${t.id}`;
    document.getElementById('modal-transacao').classList.add('open');
}

async function salvarTransacao(e) {
    e.preventDefault();
    const id = document.getElementById('transacao-id').value;
    const catId = document.getElementById('transacao-categoria').value;
    if (!catId) {
        alert('Selecione uma categoria válida para a transação');
        return;
    }

    const payload = {
        descricao: document.getElementById('transacao-descricao').value.trim(),
        valor: parseFloat(document.getElementById('transacao-valor').value),
        dataTransacao: document.getElementById('transacao-data').value,
        tipo: document.getElementById('transacao-tipo').value,
        categoriaId: parseInt(catId),
        metodoPagamento: document.getElementById('transacao-pagamento').value,
        status: document.getElementById('transacao-status').value,
        observacoes: document.getElementById('transacao-obs').value.trim()
    };

    const btn = document.getElementById('btn-salvar-transacao');
    btn.disabled = true;
    btn.textContent = 'Gravando no Azure SQL...';

    try {
        const url = id ? `/api/transacoes/${id}` : '/api/transacoes';
        const method = id ? 'PUT' : 'POST';
        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            throw new Error(await extrairMensagemErro(res, 'Falha ao salvar transação'));
        }

        fecharModalTransacao();
        showToast(id ? 'Transação atualizada no Azure SQL!' : 'Transação persistida no Azure SQL!');
        await loadTransacoes();
        await loadCategorias();
        await loadDashboard();
        await loadHealthStatus();
    } catch (err) {
        alert('Erro: ' + err.message);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Salvar no Azure SQL';
    }
}

async function excluirTransacao(id) {
    const t = transacoesState.find(item => item.id === id);
    const confirmMsg = `Deseja realmente excluir a transação "${t ? t.descricao : id}" do Azure SQL?`;
    if (!confirm(confirmMsg)) return;

    try {
        const res = await fetch(`/api/transacoes/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error(await extrairMensagemErro(res, 'Não foi possível excluir a transação'));
        showToast('Transação excluída do Azure SQL com sucesso!');
        await loadTransacoes();
        await loadCategorias();
        await loadDashboard();
        await loadHealthStatus();
    } catch (e) {
        alert('Erro ao excluir: ' + e.message);
    }
}
