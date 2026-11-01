# Backlog creado en Jira

- **Proyecto:** BUD · Bud Jet
- **URL:** https://jaxsullivan7.atlassian.net/jira/software/projects/BUD/summary
- **Fecha:** 30 de octubre de 2026 (backlog); ejecución registrada el 1 de noviembre de 2026.
- **Resultado:** 29 issues originales (5 épicas + 24 historias de backlog), 3 épicas nuevas de ejecución y 97 incidencias de sprint (`BUD-33` a `BUD-129`), todas con etiqueta `bud-jet`.
- **Estado local (v0.6.0):** BUD-20 implementado; BUD-26 endurecido y pendiente de validación en dispositivo real. Ver [`BIOMETRIC_TEST.md`](BIOMETRIC_TEST.md). Las gamas de color, el saldo inicial, los movimientos programados, las categorías nuevas, el rango histórico, el balance inicial del mes y la cartera de inversión con calculadora forman parte de v0.6.0.

## Épicas

| Clave | Épica | Hijas |
|---|---|---|
| BUD-1 | Control del día a día | BUD-2, BUD-7, BUD-8, BUD-9, BUD-10 |
| BUD-3 | Ahorro y objetivos | BUD-11, BUD-14, BUD-15 |
| BUD-4 | Análisis | BUD-12, BUD-13, BUD-16, BUD-17, BUD-18, BUD-19 |
| BUD-5 | Producto y confianza | BUD-20, BUD-21, BUD-22, BUD-23 |
| BUD-6 | Deuda técnica | BUD-24, BUD-25, BUD-26, BUD-27, BUD-28, BUD-29 |
| BUD-30 | Plataforma y diseño | BUD-33–BUD-129 (fundaciones, diseño, navegación y ajustes) |
| BUD-31 | Inversión y patrimonio | BUD-108–BUD-129 (cartera y calculadora) |
| BUD-32 | Equipo, calidad y entrega | BUD-33–BUD-129 (ceremonias, QA, ausencias y releases) |

## Sprints

| Sprint | Fechas | Objetivo | Estado |
|---|---|---|---|
| Sprint 0 · Inception | 7–11 sep 2026 | Arquitectura, sistema visual y backlog | Cerrado |
| Sprint 1 · Fundaciones y v0.1 | 14–25 sep 2026 | Primera versión usable | Cerrado |
| Sprint 2 · v0.2 y v0.3 | 28 sep–9 oct 2026 | Recurrentes, objetivos, backup y biometría | Cerrado |
| Sprint 3 · v0.4 y v0.5 | 12–23 oct 2026 | Saldo inicial, paletas, programados y análisis | Cerrado |
| Sprint 4 · Cartera y release | 26 oct–6 nov 2026 | Cartera, calculadora y entrega v0.6.0 | Activo |

El detalle de capacidad, worklogs, ausencias y retrasos está en [`SPRINTS.md`](SPRINTS.md).

## Historias y tareas

| Clave | Tipo | Épica | Resumen |
|---|---|---|---|
| BUD-2 | Story | BUD-1 | Aviso al acercarse al límite de presupuesto |
| BUD-7 | Story | BUD-1 | Plantillas de movimientos frecuentes |
| BUD-8 | Story | BUD-1 | Búsqueda avanzada de movimientos |
| BUD-9 | Story | BUD-1 | Adjuntar ticket a un movimiento |
| BUD-10 | Story | BUD-1 | Recurrencias semanales y anuales |
| BUD-11 | Story | BUD-3 | Transferencias entre cuentas |
| BUD-12 | Story | BUD-4 | Evolución del patrimonio |
| BUD-13 | Story | BUD-4 | Regla 50/30/20 |
| BUD-14 | Story | BUD-3 | Aportaciones automáticas a objetivos |
| BUD-15 | Story | BUD-3 | Objetivo vinculado a una cuenta de ahorro |
| BUD-16 | Story | BUD-4 | Comparativa con el mismo mes del año anterior |
| BUD-17 | Story | BUD-4 | Detección de suscripciones repetidas |
| BUD-18 | Story | BUD-4 | Insights de hábitos de gasto |
| BUD-19 | Story | BUD-4 | Drill-down de categoría a comercio |
| BUD-20 | Story | BUD-5 | Copia de seguridad exportar e importar |
| BUD-21 | Story | BUD-5 | Widget de saldo y acceso rápido |
| BUD-22 | Story | BUD-5 | Onboarding de nombre, moneda y presupuesto |
| BUD-23 | Story | BUD-5 | Modo familia con perfiles locales |
| BUD-24 | Task | BUD-6 | Migrar la persistencia JSON a Room |
| BUD-25 | Task | BUD-6 | Pruebas de UI de flujos completos |
| BUD-26 | Task | BUD-6 | Probar biometría en dispositivos reales |
| BUD-27 | Task | BUD-6 | Internacionalización ES/EN |
| BUD-28 | Task | BUD-6 | Reducir el tamaño del APK |
| BUD-29 | Task | BUD-6 | Baseline profile para arranque en frío |

El CSV original usado para la importación se conserva en [`jira-import.csv`](jira-import.csv).
