# Especificação Técnica de Arquitetura — Fluxo de Caixa

## 1. Diretrizes de Arquitetura e Padrões
A solução adota o padrão **CQRS (Command Query Responsibility Segregation)** orientado a eventos (**EDA - Event-Driven Architecture**), desacoplando a gravação de transações de alta frequência da leitura do consolidado diário.

* **Linguagem / Framework:** Java 17 + Spring Boot 3
* **Mensageria / Eventos:** RabbitMQ (AMQP)
* **Persistência de Dados:** PostgreSQL (Database per Service)
* **Containerização:** Docker & Docker Compose

---

## 2. Requisitos Não-Funcionais (RNF)

| ID | Categoria | Descrição / Meta Técnica |
| :--- | :--- | :--- |
| **RNF-01** | **Disponibilidade / Isolamento** | O serviço de lançamentos (`transaction-service`) **deve permanecer operacional** mesmo se o serviço de consolidação (`consolidation-service`) ficar indisponível. |
| **RNF-02** | **Desempenho e Vazão** | Suportar picos de no mínimo **50 requisições por segundo (req/s)** com taxa máxima de perda de 5%. |
| **RNF-03** | **Consistência** | Consistência eventual (*Eventual Consistency*) garantida pelo barramento de mensagens RabbitMQ com confirmação de entrega (*ACK*). |
| **RNF-04** | **Escalabilidade** | Microsserviços stateless permitindo escalabilidade horizontal via containers (HPA no Kubernetes / Docker). |

---

## 3. Visão Geral dos Microsserviços

### 3.1. `transaction-service` (Write Side - Command)
* **Responsabilidade:** Receber e validar lançamentos financeiros (`POST /api/v1/transactions`), salvar no banco de escrita e publicar o evento `transaction.created` na fila do RabbitMQ.
* **Endpoints:**
  * `POST /api/v1/transactions`: Registra novo débito ou crédito.
* **Eventos Produzidos:** `transaction.created` (Exchange: `transaction-exchange`, Routing Key: `transaction.created`).

### 3.2. `consolidation-service` (Read Side - Query)
* **Responsabilidade:** Consumir eventos da fila `transaction-created`, atualizar os totais e saldo final diário na base de consolidação e expor endpoint REST para consulta.
* **Endpoints:**
  * `GET /api/v1/consolidation/daily?merchantId={id}&date={YYYY-MM-DD}`: Consulta o relatório consolidado.
* **Filas Consumidas:** `transaction-created`.

---

## 4. Contratos de API (Interfaces REST)

### 4.1. Criar Transação
* **HTTP Method:** `POST`
* **URL:** `/api/v1/transactions`
* **Request Body:**
```json
{
  "merchantId": "loja-123",
  "type": "CREDIT",
  "amount": 150.50,
  "description": "Venda Balcão"
}
```

### 4.2. Consultar Saldo Consolidado Diário
* **HTTP Method:** 'GET'

* **URL:** '/api/v1/consolidation/daily?merchantId=loja-123&date=2026-08-04'

* **Response (200 OK):**
```json
{
  "merchantId": "loja-123",
  "date": "2026-08-04",
  "totalCredit": 150.50,
  "totalDebit": 0.00,
  "finalBalance": 150.50
}
```
### 5. Estrutura de Dados (Modelagem de Banco de Dados)
Tabela: transactions (Banco transaction_db)

```SQL
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    merchant_id VARCHAR(50) NOT NULL,
    type VARCHAR(10) NOT NULL, -- 'CREDIT' ou 'DEBIT'
    amount NUMERIC(15, 2) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_transactions_merchant_date ON transactions(merchant_id, created_at);
```

### Tabela: daily_balances (Banco consolidation_db)
```SQL
CREATE TABLE daily_balances (
    id BIGSERIAL PRIMARY KEY,
    merchant_id VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    total_credit NUMERIC(15, 2) DEFAULT 0.00,
    total_debit NUMERIC(15, 2) DEFAULT 0.00,
    final_balance NUMERIC(15, 2) DEFAULT 0.00,
    CONSTRAINT uk_merchant_date UNIQUE (merchant_id, date)
);
```
