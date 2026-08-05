# ADR 0001: Arquitetura Orientada a Eventos com CQRS para Fluxo de Caixa Diário

* **Status:** Aceito
* **Data:** Agosto de 2026
* **Autor:** Mauro Perroni (Arquiteto de Soluções)

---

## 1. Contexto e Problema

O sistema de **Fluxo de Caixa Diário** precisa atender a duas capacidades de negócio principais:
1. **Lançamentos:** Registro contínuo de movimentações financeiras (débitos e créditos) feitas por comerciantes.
2. **Consolidação Diária:** Disponibilização de um relatório com o saldo diário consolidado para consulta.

### Requisitos Não-Funcionais Críticos:
* **Disponibilidade e Resiliência:** O serviço de controle de lançamentos **não pode ficar indisponível** se o sistema de consolidação diária falhar ou cair.
* **Desempenho e Vazão:** O consolidado diário deve suportar picos de **50 requisições por segundo (req/s)** com no máximo **5% de perda de requisições** sob alta carga.
* **Auditabilidade e Consistência:** Registro imutável de transações com consistência eventual para os relatórios consolidados.

---

## 2. Decisão Arquitetural

Decidimos adotar uma **Arquitetura de Microsserviços Orientada a Eventos (EDA - Event-Driven Architecture)** combinada com o padrão **CQRS (Command Query Responsibility Segregation)**, suportada por infraestrutura na **AWS**.

### Detalhamento da Solução:
1. **Segregação de Responsabilidades (CQRS):**
   * **`transaction-service` (Command Side / BC Lançamentos - Ledger):** Microsserviço de escrita para validar e registrar os lançamentos. Ele aceita as requisições HTTP, persiste a transação localmente e publica o evento `transaction-created` de forma assíncrona.
   * **`consolidation-service` (Query Side / BC Consolidação):** Microsserviço de leitura que consome os eventos e mantém a visão agregada dos saldos diários (`totalCredit`, `totalDebit`, `finalBalance`).

2. **Mensageria Assíncrona (RabbitMQ / Amazon MQ):**
   * A comunicação entre os microsserviços ocorre via eventos utilizando o protocolo **AMQP/AMQPS**.
   * O uso de filas no RabbitMQ atua como um *buffer* de retenção, garantindo que mesmo se o `consolidation-service` estiver offline, o `transaction-service` continuará recebendo e registrando transações sem nenhuma interrupção.

3. **Plataforma e Infraestrutura (AWS):**
   * **Computação:** AWS EKS (Kubernetes) com Auto Scaling horizontal de pods/nodes EC2 (`t3.medium`).
   * **Banco de Dados:** AWS RDS PostgreSQL com criptografia em repouso via KMS.
   * **Segurança e Borda:** AWS ALB + API Gateway com autenticação OAuth2/JWT e limitação de taxa (*Rate Limiting*).

---

## 3. Padrões Considerados

| Opção Arquitetural | Prós | Contras | Resultado |
| :--- | :--- | :--- | :--- |
| **Monolito com Comunicação Síncrona** | Simplicidade inicial de desenvolvimento e deployment. | Ponto único de falha; queda na consolidação afeta o lançamento; fraca escalabilidade para picos. | **Rejeitado** |
| **Microsserviços com Comunicação REST (HTTP Síncrono)** | Desacoplamento de código e implantação independente. | Acoplamento temporal; se o serviço de consolidação cair, chamadas diretas falharão. | **Rejeitado** |
| **Microsserviços EDA + CQRS com Mensageria Assíncrona (RabbitMQ)** | **Alto desacoplamento, resiliência total a quedas, alta vazão para picos (50 req/s), consistência eventual.** | Complexidade operacional aumentada e necessidade de gestão de consistência eventual. | **Aceito** |

---

## 4. Consequências

### Positivas (+):
* **Resiliência e Tolerância a Falhas:** Atende 100% ao requisito de negócio. A queda do serviço de consolidação não afeta a captura de lançamentos.
* **Escalabilidade Independente:** O `consolidation-service` pode ser escalado isoladamente nos horários de pico sem impactar o dimensionamento do `transaction-service`.
* **Desempenho:** Respostas imediatas ao cliente na gravação do lançamento (modelo não-bloqueante).

### Negativas / Riscos (-):
* **Consistência Eventual:** O saldo diário consolidado pode levar alguns milissegundos para refletir uma transação recém-criada.
* **Complexidade de Infraestrutura:** Requer governança e monitoramento proativo de brokers de mensageria (RabbitMQ), tracing distribuído (OpenTelemetry/Zipkin) e cluster Kubernetes.

---

## 5. Validação e Testes
* **Testes de Carga:** Simulação de carga com ferramentas de estresse (JMeter/K6) atingindo 50 req/s para validar a latência e a taxa de perda abaixo de 5%.
* **Teste de Caos (Resiliência):** Interrupção forçada do `consolidation-service` durante um fluxo contínuo de escrita no `transaction-service` para validar a retenção das mensagens na fila.