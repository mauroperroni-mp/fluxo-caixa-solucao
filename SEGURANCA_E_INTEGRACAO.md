# Critérios de Segurança para Consumo e Integração de Serviços

Este documento define as diretrizes e mecanismos de proteção adotados para garantir a confidencialidade, integridade e autenticidade na comunicação entre clientes, sistemas parceiros e os microsserviços do **Fluxo de Caixa**.

---

## 1. Segurança na Camada de Integração (APIs REST)

[ Cliente / POS / App ]
│
▼  mTLS / HTTPS (TLS 1.3)
┌──────────────────────────────────┐
│      API Gateway / Ingress       │ ──► Autenticação OAuth2 / JWT (Keycloak)
└─────────────────┬────────────────┘
│
▼ (Rede Privada VPC)
┌──────────────────────────────────┐
│  transaction / consolidation     │
└──────────────────────────────────┘


### 1.1. Autenticação e Autorização (OAuth2 + OIDC)
* **Padrão:** Controle de acesso baseado em **Tokens JWT (JSON Web Tokens)** via protocolo **OAuth 2.0**.
* **Mapeamento de Escopos (RBAC):**
  * `scope: transaction:write` — Permissão para registrar novos débitos e créditos.
  * `scope: consolidation:read` — Permissão exclusiva para consulta de saldo diário.
* **Validação:** As APIs validam a assinatura digital do token JWT (chave pública via JWKS) antes de processar qualquer chamada.

### 1.2. Proteção de Borda e Rate Limiting
* **Rate Limiting:** Aplicação de limites de requisições por API Key / Comerciante no API Gateway para prevenir ataques de negação de serviço (DDoS) ou sobrecarga acidental.
* **WAF (Web Application Firewall):** Proteção contra *OWASP Top 10* (Injection, Cross-Site Scripting, etc.).

---

## 2. Segurança na Comunicação Interna e Mensageria

### 2.1. Criptografia em Trânsito (In-Transit)
* **HTTP/REST:** Todo o tráfego externo e entre containers exige **HTTPS (TLS 1.3)**.
* **AMQP (RabbitMQ):** Conexão criptografada entre microsserviços e o broker via protocolo **AMQPS** (Porta 5671) com autenticação por certificado mTLS.

### 2.2. Criptografia em Repouso (At-Rest)
* **Banco de Dados (PostgreSQL):** Dados armazenados com criptografia nativa no volume de disco via **AES-256** (AWS KMS).
* **Campos Sensíveis:** Aplicação de mascaramento de dados (Data Masking) em logs para evitar vazamento de dados pessoais ou financeiros.

---

## 3. Gestão de Segredos e Credenciais
* **Proibição de Hardcode:** Nenhuma senha, token de API ou chave de banco de dados fica gravada no código-fonte ou em arquivos `.yml` commitados no Git.
* **Injeção de Segredos:** Utilização do **AWS Secrets Manager** ou **HashiCorp Vault** com injeção em tempo de execução via Variáveis de Ambiente (`ENV`) nos pods do Kubernetes.
