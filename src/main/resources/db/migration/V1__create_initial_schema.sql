-- ═══════════════════════════════════════════════════════════════════════════
-- JURISFLOW - INITIAL SCHEMA
-- V1 - Create initial tables
-- ═══════════════════════════════════════════════════════════════════════════

-- ─────────────────────────────────────────────────────────────────────────
-- ESCRITORIOS (Multi-tenant base)
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE escritorios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(200) NOT NULL,
    razao_social VARCHAR(200),
    cnpj VARCHAR(18) UNIQUE,
    inscricao_estadual VARCHAR(20),
    email VARCHAR(100),
    telefone VARCHAR(20),
    celular VARCHAR(20),
    website VARCHAR(500),
    
    -- Endereço
    cep VARCHAR(10),
    logradouro VARCHAR(200),
    numero VARCHAR(20),
    complemento VARCHAR(100),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    uf VARCHAR(2),
    
    -- OAB
    numero_oab VARCHAR(20),
    seccional_oab VARCHAR(2),
    
    -- Configurações
    logo_url VARCHAR(500),
    cor_primaria VARCHAR(7) DEFAULT '#1a56db',
    cor_secundaria VARCHAR(7) DEFAULT '#7c3aed',
    timezone VARCHAR(50) DEFAULT 'America/Sao_Paulo',
    ativo BOOLEAN DEFAULT TRUE,
    
    -- Plano
    plano VARCHAR(20) DEFAULT 'TRIAL',
    stripe_customer_id VARCHAR(100),
    stripe_subscription_id VARCHAR(100),
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);

-- ─────────────────────────────────────────────────────────────────────────
-- USUARIOS
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escritorio_id UUID NOT NULL REFERENCES escritorios(id),
    
    nome VARCHAR(200) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) UNIQUE,
    telefone VARCHAR(20),
    celular VARCHAR(20),
    foto_url VARCHAR(500),
    
    -- Dados profissionais
    numero_oab VARCHAR(20),
    seccional_oab VARCHAR(2),
    especialidade VARCHAR(100),
    valor_hora DECIMAL(10, 2),
    
    -- Segurança
    role VARCHAR(20) NOT NULL DEFAULT 'ADVOGADO',
    ativo BOOLEAN DEFAULT TRUE,
    email_verificado BOOLEAN DEFAULT FALSE,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(100),
    ultimo_login TIMESTAMP,
    tentativas_login INTEGER DEFAULT 0,
    bloqueado_ate TIMESTAMP,
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_usuario_email ON usuarios(email);
CREATE INDEX idx_usuario_escritorio ON usuarios(escritorio_id);

-- ─────────────────────────────────────────────────────────────────────────
-- CLIENTES
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE clientes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escritorio_id UUID NOT NULL REFERENCES escritorios(id),
    
    tipo_pessoa VARCHAR(10) NOT NULL DEFAULT 'FISICA',
    nome VARCHAR(200) NOT NULL,
    cpf_cnpj VARCHAR(18),
    rg VARCHAR(20),
    orgao_emissor VARCHAR(20),
    inscricao_estadual VARCHAR(20),
    data_nascimento DATE,
    estado_civil VARCHAR(20),
    profissao VARCHAR(100),
    nacionalidade VARCHAR(100),
    
    -- Contato
    email VARCHAR(200),
    email_secundario VARCHAR(200),
    telefone VARCHAR(20),
    celular VARCHAR(20),
    whatsapp BOOLEAN DEFAULT FALSE,
    
    -- Endereço
    cep VARCHAR(10),
    logradouro VARCHAR(200),
    numero VARCHAR(20),
    complemento VARCHAR(100),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    uf VARCHAR(2),
    
    -- Informações adicionais
    como_conheceu VARCHAR(100),
    indicado_por VARCHAR(200),
    observacoes TEXT,
    foto_url VARCHAR(500),
    ativo BOOLEAN DEFAULT TRUE,
    
    -- Portal do cliente
    portal_ativo BOOLEAN DEFAULT FALSE,
    portal_senha VARCHAR(255),
    portal_ultimo_acesso TIMESTAMP,
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_cliente_cpf_cnpj ON clientes(cpf_cnpj);
CREATE INDEX idx_cliente_escritorio ON clientes(escritorio_id);
CREATE INDEX idx_cliente_nome ON clientes(nome);

-- ─────────────────────────────────────────────────────────────────────────
-- PROCESSOS
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE processos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escritorio_id UUID NOT NULL REFERENCES escritorios(id),
    cliente_id UUID NOT NULL REFERENCES clientes(id),
    advogado_responsavel_id UUID REFERENCES usuarios(id),
    
    numero VARCHAR(25) NOT NULL,
    numero_antigo VARCHAR(25),
    titulo VARCHAR(300) NOT NULL,
    descricao TEXT,
    tipo_acao VARCHAR(30) NOT NULL,
    area_direito VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ANDAMENTO',
    
    tribunal VARCHAR(100),
    vara VARCHAR(100),
    comarca VARCHAR(100),
    uf VARCHAR(2),
    
    valor_causa DECIMAL(15, 2),
    valor_estimado DECIMAL(15, 2),
    data_distribuicao DATE,
    data_encerramento DATE,
    
    observacoes TEXT,
    prioridade VARCHAR(10) DEFAULT 'MEDIA',
    segredo_justica BOOLEAN DEFAULT FALSE,
    pasta_fisica VARCHAR(50),
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_processo_numero ON processos(numero);
CREATE INDEX idx_processo_escritorio ON processos(escritorio_id);
CREATE INDEX idx_processo_cliente ON processos(cliente_id);
CREATE INDEX idx_processo_status ON processos(status);

-- ─────────────────────────────────────────────────────────────────────────
-- PROCESSO TAGS
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE processo_tags (
    processo_id UUID NOT NULL REFERENCES processos(id) ON DELETE CASCADE,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (processo_id, tag)
);

-- ─────────────────────────────────────────────────────────────────────────
-- PARTES
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE partes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    processo_id UUID NOT NULL REFERENCES processos(id) ON DELETE CASCADE,
    
    nome VARCHAR(200) NOT NULL,
    cpf_cnpj VARCHAR(18),
    tipo_parte VARCHAR(30) NOT NULL,
    polo VARCHAR(10) NOT NULL DEFAULT 'ATIVO',
    email VARCHAR(200),
    telefone VARCHAR(20),
    endereco VARCHAR(300),
    
    -- Advogado da parte
    advogado_nome VARCHAR(200),
    advogado_oab VARCHAR(20),
    advogado_email VARCHAR(200),
    advogado_telefone VARCHAR(20),
    
    observacoes TEXT,
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);

-- ─────────────────────────────────────────────────────────────────────────
-- ANDAMENTOS
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE andamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    processo_id UUID NOT NULL REFERENCES processos(id) ON DELETE CASCADE,
    
    data_movimentacao TIMESTAMP NOT NULL,
    descricao TEXT NOT NULL,
    tipo VARCHAR(30) NOT NULL DEFAULT 'OUTROS',
    fonte VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    codigo_movimentacao VARCHAR(50),
    documento_url VARCHAR(500),
    observacoes TEXT,
    lido BOOLEAN DEFAULT FALSE,
    importante BOOLEAN DEFAULT FALSE,
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_andamento_processo ON andamentos(processo_id);
CREATE INDEX idx_andamento_data ON andamentos(data_movimentacao);

-- ─────────────────────────────────────────────────────────────────────────
-- PRAZOS
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE prazos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escritorio_id UUID NOT NULL REFERENCES escritorios(id),
    processo_id UUID REFERENCES processos(id) ON DELETE CASCADE,
    responsavel_id UUID REFERENCES usuarios(id),
    
    titulo VARCHAR(300) NOT NULL,
    descricao TEXT,
    tipo_prazo VARCHAR(30) NOT NULL,
    data_inicio DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    dias_prazo INTEGER,
    tipo_contagem VARCHAR(20) NOT NULL DEFAULT 'DIAS_UTEIS',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    prioridade VARCHAR(10) DEFAULT 'MEDIA',
    
    -- Cumprimento
    data_cumprimento TIMESTAMP,
    numero_protocolo VARCHAR(50),
    observacoes_cumprimento TEXT,
    documento_url VARCHAR(500),
    
    -- Alertas
    alerta_7_dias BOOLEAN DEFAULT FALSE,
    alerta_3_dias BOOLEAN DEFAULT FALSE,
    alerta_1_dia BOOLEAN DEFAULT FALSE,
    alerta_no_dia BOOLEAN DEFAULT FALSE,
    
    observacoes TEXT,
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_prazo_processo ON prazos(processo_id);
CREATE INDEX idx_prazo_vencimento ON prazos(data_vencimento);
CREATE INDEX idx_prazo_escritorio ON prazos(escritorio_id);
CREATE INDEX idx_prazo_status ON prazos(status);

-- ─────────────────────────────────────────────────────────────────────────
-- INSERT DEFAULT DATA
-- ─────────────────────────────────────────────────────────────────────────

-- Insert default escritório for testing
INSERT INTO escritorios (id, nome, email, plano) 
VALUES ('00000000-0000-0000-0000-000000000001', 'Escritório Demo', 'demo@jurisflow.com.br', 'TRIAL');

-- Insert default admin user (password: admin123)
INSERT INTO usuarios (id, escritorio_id, nome, email, senha, role, email_verificado)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    'Administrador',
    'admin@jurisflow.com.br',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.sPLfK3n4W9OKGqGpSe',
    'ADMIN',
    TRUE
);


