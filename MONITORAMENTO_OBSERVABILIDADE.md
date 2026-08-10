# Estratégia de Monitoramento e Observabilidade

Para garantir a alta disponibilidade e suportar a vazão de picos sem degradação do serviço de consolidação, a solução foi projetada utilizando os **Três Pilares da Observabilidade** (Métricas, Traces e Logs).

---

## 1. Mapeamento da Arquitetura de Observabilidade

```text

┌───────────────────────────┐      ┌─────────────────────────┐
│ transaction/consolidation │ ───► │ Micrometer / Actuator   │
└───────────────────────────┘      └────────────┬────────────┘
                                                │ (Scrape)
                                                ▼
┌───────────────────────────┐      ┌─────────────────────────┐
│     Grafana Dashboards    │ ◄─── │   Prometheus Server     │
└───────────────────────────┘      └─────────────────────────┘

```


## 2. Coleta de Métricas (Prometheus + Grafana)
Cada microsserviço Spring Boot expõe endpoints de saúde e métricas nativas através do **Spring Boot Actuator** e **Micrometer** no caminho `/actuator/prometheus`.

### Métricas Chave Monitoradas (KPIs / SLOs):
* **`http_server_requests_seconds_count`**: Vazão de requisições por segundo (TPS).
* **`http_server_requests_seconds_bucket`**: Latência das respostas HTTP (p95, p99).
* **`rabbitmq_queue_messages`**: Profundidade/tamanho da fila de consolidação no RabbitMQ.
* **`jvm_gc_pause_seconds`**: Pausas de Garbage Collector da JVM Java.

---

## 3. Rastreamento Distribuído (Distributed Tracing)
* **Ferramenta:** **OpenTelemetry** + **Zipkin / Jaeger**.
* **Funcionamento:** Cada requisição que entra no `transaction-service` recebe um `traceId` único no cabeçalho HTTP (`X-B3-TraceId`). Esse mesmo `traceId` é propagado nas mensagens publicadas no RabbitMQ e mantido até o processamento no `consolidation-service`.
* **Benefício:** Permite identificar gargalos de latência exatos entre o envio da transação e a consolidação final.

---

## 4. Agregação e Centralização de Logs
* **Padrão de Log:** Estruturado em **JSON** via Logback (`logstash-logback-encoder`).
* **Stack sugerida:** ELK Stack (Elasticsearch, Logstash, Kibana) ou Grafana Loki.
* **Mínimo de Informação por Log:** `timestamp`, `level`, `service_name`, `trace_id`, `merchant_id` e `message`.

---
## 5. Alertas Críticos Configurados
1. **Fila com backlog acumulado:** Alerta acionado se a fila `transaction-created` possuir mais de 10.000 mensagens sem processamento por mais de 5 minutos.
2. **Taxa de Erro HTTP 5xx:** Alerta acionado se a taxa de erro for `> 1%` em um intervalo de 5 minutos.
3. **Consumo de Memória/CPU:** Alerta ao atingir `> 85%` de uso continuo nos containers.
