# Estimativa de Custos com Infraestrutura e Licenças

Este documento detalha o dimensionamento financeiro e a estimativa de custos para sustentação da solução **Fluxo de Caixa Diário** em ambiente de produção de nuvem (AWS - Amazon Web Services).

---

## 1. Premissas de Dimensionamento
* **Volume Esperado:** Carga contínua com picos de 50 requisições por segundo (req/s).
* **Volumetria diária:** ~1,5 milhão de transações/dia.
* **Retenção de Dados:** 5 anos para histórico de relatórios consolidados e lançamentos.
* **Estratégia de Licenciamento:** Utilização 100% de tecnologias **Open Source** (Java/Spring Boot, PostgreSQL, RabbitMQ, Prometheus/Grafana) para eliminação de custos com licenças de software.

---

## 2. Estimativa de Custos Mensais (AWS US-East-1 - N. Virginia)

| Componente | Recurso AWS | Especificação / Sizing | Custo Mensal Estimado (USD) |
| :--- | :--- | :--- | :--- |
| **Computação** | AWS EKS (Kubernetes) | Control Plane Gerenciado | $73.00 |
| **Nodes de Computação** | EC2 (Spot/On-Demand) | 3x `t3.medium` (2 vCPU, 4GB RAM) em Auto Scaling | $80.00 |
| **Mensageria** | Amazon MQ (RabbitMQ) | `mq.m5.large` Single Instance (ou EKS Cluster) | $75.00 |
| **Banco de Dados** | AWS RDS PostgreSQL | `db.t4g.medium` (Multi-AZ para alta disponibilidade) + 100GB Storage | $110.00 |
| **Balanceador de Carga** | AWS ALB (Application Load Balancer) | 1 ALB + LCU (Load Balancer Capacity Units) | $25.00 |
| **Observabilidade e Logs** | Amazon CloudWatch / S3 | Logs de aplicação, métricas e retenção em S3 Glacier | $30.00 |
| **Licenciamento de Software**| Open Source | Java, PostgreSQL, RabbitMQ, Prometheus | **$0.00** |
| **TOTAL ESTIMADO** | | | **~$393.00 / mês** |

---

## 3. Estratégias de Otimização de Custos (FinOps)
1. **Uso de Instâncias ARM (AWS Graviton):** Utilização de instâncias do tipo `t4g` para o banco de dados RDS, proporcionando até 20% de economia em relação à arquitetura x86.
2. **Auto Scaling Dinâmico:** O cluster EKS escala horizontalmente conforme a demanda do dia, reduzindo o consumo nos horários de menor movimento (noite/madrugada).
3. **Lifecycle no S3:** Arquivamento automático de logs e dados consolidados antigos para classe de armazenamento *S3 Glacier Flexible Retrieval*, reduzindo o custo de armazenamento persistente.