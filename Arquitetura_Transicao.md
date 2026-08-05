# Estratégia e Arquitetura de Transição (Migração de Legado)

Este documento descreve a estratégia de migração e a **Arquitetura de Transição** proposta para migrar um sistema monolítico/legado de Fluxo de Caixa para a nova arquitetura baseada em microsserviços e orientada a eventos (EDA + CQRS), garantindo **zero downtime** e sem risco de perda de dados.

---

## 1. Padrão Estratégico: *Strangler Fig Pattern* (Padrão Estrangulamento)

Para evitar os riscos de uma migração *Big Bang* (troca abrupta de todo o sistema), adotamos o padrão **Strangler Fig Pattern**. As funcionalidades serão migradas gradualmente do monolito legado para os novos microsserviços (`transaction-service` e `consolidation-service`).

---

## 2. Fases da Arquitetura de Transição

### **Fase 1: Coexistência e Roteamento (Proxy/Gateway)**