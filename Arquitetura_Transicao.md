# 🏗️ Documento de Arquitetura de Transição: Sistema de Fluxo de Caixa

## 1. Introdução e Contexto

A modernização do sistema de **Fluxo de Caixa** visa evoluir uma arquitetura legada (monolítica ou de processamento em lote) para uma arquitetura distribuída, resiliente e orientada a eventos (*Event-Driven Architecture*), dividida em dois microsserviços principais:

* **`transaction-service`**: Responsável pelo registro síncrono e de alta vazão das movimentações financeiras (Crédito e Débito).
* **`consolidation-service`**: Responsável pelo processamento assíncrono do saldo consolidado diário para os comerciantes.

Este documento detalha o plano de migração, estratégias de coexistência e as fases de transição do ambiente legado para a nova arquitetura baseada em **Spring Boot 3**, **RabbitMQ** e **PostgreSQL**.

---

## 2. Princípios de Arquitetura de Transição

1. **Zero Downtime:** A migração não deve causar indisponibilidade no registro de lançamentos dos comerciantes.
2. **Consistência Eventual Aceitável:** O saldo diário consolidado pode apresentar uma latência de milissegundos a poucos segundos em relação ao lançamento síncrono, garantindo alto desempenho na gravação.
3. **Reversibilidade (Rollback Seguro):** Cada etapa da migração possui mecanismos para desviar o tráfego de volta ao sistema legado caso anomalias sejam detectadas.
4. **Desacoplamento por Mensageria:** Nenhuma dependência direta via HTTP existirá entre o serviço de lançamentos e o de consolidação.

---

## 3. Fases da Migração Propostas

```text
┌────────────────┐     ┌────────────────┐     ┌────────────────┐     ┌────────────────┐
│    FASE 1      │ ──> │    FASE 2      │ ──> │    FASE 3      │ ──> │    FASE 4      │
│  Infraestrutura│     │ Escrita Dupla  │     │ Leitura no Novo│     │ Desativação do │
│    & Shadow    │     │   (Dual-Write) │     │  Microsserviço │     │     Legado     │
└────────────────┘     └────────────────┘     └────────────────┘     └────────────────┘
```
### 📍 Fase 1: Implantação de Infraestrutura e Leitura Sombra (*Shadow Operations*)
* **Objetivo:** Subir o novo ambiente (RabbitMQ, PostgreSQL, `transaction-service` e `consolidation-service`) sem afetar o fluxo de produção do sistema antigo.
* **Estratégia:**
  * Subir os containers do RabbitMQ e PostgreSQL via Docker Compose/Kubernetes.
  * Configurar o sistema legado para espelhar (via *CDC - Change Data Capture* ou replicador de eventos) uma cópia dos lançamentos para a exchange do RabbitMQ (`transaction.exchange`).
  * O `consolidation-service` consome os eventos e popula sua própria base de dados de saldos consolidados em modo de teste/validação.
* **Critério de Sucesso:** Comparação automatizada entre o saldo gerado pelo legado e o saldo consolidado pelo `consolidation-service` atingir 100% de paridade.

---

### 📍 Fase 2: Escrita Dupla e Migração de Tráfego de Entrada (*Dual-Write / Canary Deployment*)
* **Objetivo:** Redirecionar gradualmente a gravação dos lançamentos dos comerciantes para o novo `transaction-service`.
* **Estratégia:**
  * Inserir um **API Gateway** na frente dos serviços.
  * Direcionar uma porcentagem inicial de tráfego (`10% -> 50% -> 100%`) das requisições `POST /api/v1/transactions` para o `transaction-service`.
  * Ao receber o lançamento, o `transaction-service` grava no PostgreSQL local e dispara a mensagem `TransactionMessageDTO` para o RabbitMQ.
  * Para manter o legado atualizado durante a transição, um *adapter* temporário escuta a fila do RabbitMQ e sincroniza o banco do sistema antigo.
* **Critério de Sucesso:** Gravação de transações com tempo de resposta < 100ms e zero perda de mensagens na fila.

---

### 📍 Fase 3: Migração do Tráfego de Consulta (*Read Switch*)
* **Objetivo:** Alterar a API de consulta de saldo diário dos comerciantes para consumir o novo `consolidation-service`.
* **Estratégia:**
  * Redirecionar o tráfego do endpoint `GET /api/v1/consolidation/daily` no API Gateway para o `consolidation-service` na porta `8081`.
  * Como a base do `consolidation-service` já foi populada e validada nas Fases 1 e 2, a transição para os clientes finais é transparente e instantânea.
* **Critério de Sucesso:** Consultas de saldo sendo respondidas exclusivamente pelo novo serviço com tempo de resposta < 50ms.

---

### 📍 Fase 4: Desativação do Legado (*Decommissioning*)
* **Objetivo:** Desconectar os componentes legados e remover adaptadores temporários de coexistência.
* **Estratégia:**
  * Remover o *adapter* de sincronização do RabbitMQ para o banco legado.
  * Desligar e arquivar os bancos de dados e servidores do monolito antigo.
  * A solução passa a rodar 100% sobre a nova arquitetura de microsserviços.

---

## 4. Plano de Contingência e Rollback

| Cenário de Falha | Ação Imensurável de Contingência |
| :--- | :--- |
| **Queda do RabbitMQ** | O `transaction-service` armazena a transação localmente no banco e ativa mecanismo de *Outbox Pattern* para retransmitir os eventos quando o broker restabelecer. |
| **Consolidação Incorreta** | Reprocessamento das mensagens da fila através de consumidor de *DLQ (Dead Letter Queue)* ou re-execução do script de consolidação a partir dos dados brutos do `transaction-service`. |
| **Erros na Fase 2 (Escrita)** | Alterar a regra do API Gateway para rotear 100% das chamadas de volta ao sistema legado enquanto se analisa o problema. |

---

## 5. Matriz de Qualidade e Observabilidade na Transição

Durante todo o período de transição, as seguintes métricas devem ser monitoradas via **Prometheus / Grafana**:

1. **Taxa de Erro HTTP (Actuator):** Exigido < 0.1% em ambos os serviços.
2. **Profundidade da Fila (RabbitMQ):** Alertas configurados se a fila `transaction.consolidation.queue` acumular mais de 10.000 mensagens sem processamento.
3. **Latência p99:** Lançamentos < 150ms, Consolidação < 50ms.
