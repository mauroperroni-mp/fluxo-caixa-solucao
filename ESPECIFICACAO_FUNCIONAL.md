# Especificação Funcional — Sistema de Fluxo de Caixa Diário

## 1. Visão Geral do Produto
O **Sistema de Fluxo de Caixa Diário** tem como objetivo permitir que comerciantes registrem suas movimentações financeiras de débito e crédito ao longo do dia e consultem, de forma rápida e precisa, o relatório consolidado do saldo diário.

## 2. Atores do Sistema
* **Comerciante / Lojista:** Usuário final responsável por efetuar lançamentos financeiros e consultar o saldo consolidado de sua loja.
* **Sistemas Integradores / POS:** Aplicações de frente de caixa ou checkout que enviam transações de forma automatizada.

## 3. Mapeamento de Capacidades de Negócio
1. **Gestão de Lançamentos (Transactions Management):**
   * Registro e classificação de entradas (Créditos) e saídas (Débitos).
   * Rastreabilidade e histórico individual das movimentações.
2. **Consolidação Financeira (Financial Consolidation):**
   * Agrupamento e cálculo de saldos acumulados por comerciante e data.
   * Disponibilização de relatórios sintéticos de posição diária.

---

## 4. Requisitos Funcionais (RF)

| ID | Descrição | Regras de Negócio |
| :--- | :--- | :--- |
| **RF-01** | **Registrar Lançamento Financeiro** | Permite registrar uma transação financeira do tipo `CREDIT` ou `DEBIT`. Deve conter: `merchantId`, `amount`, `type` e `description`. |
| **RF-02** | **Validar Valor e Tipo do Lançamento** | O valor (`amount`) deve ser estritamente maior que zero (`> 0`). O tipo deve aceitar apenas `CREDIT` ou `DEBIT`. |
| **RF-03** | **Consultar Saldo Consolidado Diário** | Permite consultar o total de créditos, total de débitos e saldo final de um comerciante para uma data específica. |
| **RF-04** | **Consolidação Automática de Transações** | As transações registradas devem atualizar o saldo consolidado diário do comerciante em tempo próximo ao real (*near real-time*). |
| **RF-05** | **Consulta por Data Padrão** | Se nenhuma data for informada na consulta do saldo consolidado, o sistema deve assumir a data corrente (`hoje`). |

---

## 5. Fluxos de Uso (Casos de Uso)

### UC-01: Registrar Novo Lançamento
1. O Comerciante (ou POS) envia os dados da transação (ID do comerciante, tipo, valor e descrição).
2. O sistema valida os campos obrigatórios e os limites de valor.
3. O sistema confirma o recebimento da transação e retorna o identificador único gerado.
4. Em segundo plano, o saldo do dia é recalculado/atualizado.

### UC-02: Consultar Saldo Diário
1. O Comerciante solicita o relatório informando seu `merchantId` e a data desejada.
2. O sistema busca a posição consolidada (Total Crédito, Total Débito, Saldo Final).
3. O sistema exibe o resumo financeiro do dia solicitado.
4. Caso não existam lançamentos na data, o sistema retorna totais zerados (`0.00`).