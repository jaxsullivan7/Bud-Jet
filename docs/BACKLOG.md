# Backlog · Bud Jet

Proyecto de Jira: **[BUD · Bud Jet](https://jaxsullivan7.atlassian.net/jira/software/projects/BUD/summary)**  
Creado el 30 de octubre de 2026: **5 épicas + 24 historias/tareas**.

El proyecto de Jira no tiene campo de prioridad en la pantalla de creación, así que la prioridad propuesta queda indicada al principio de la descripción de cada issue.

## Hecho

### v0.5

- **Fecha recordada** en nuevos movimientos.
- **Categorías nuevas**: Belleza (gasto), Devolución y Anulación (ingreso).
- **Anulación vinculada**: busca el gasto original y crea un ingreso compensatorio por el mismo importe.
- **Todos los importes con decimales**, sin redondeo a euros enteros.
- **Resumen de búsqueda**: la barra de ingresos/gastos/balance refleja solo los resultados de la búsqueda.
- **Rango histórico en Análisis**: elige desde qué fecha se cuentan los datos.

### v0.4.1

- **Movimientos programados**: un ingreso o gasto con fecha futura se guarda, se marca como “Programado” y no afecta al saldo, resumen, presupuestos ni análisis hasta su fecha.
- El aviso aparece también en el editor y en las pantallas de resumen.

### v0.4

- **Tres gamas de color** (Business, Navy y Boss), cada una con variante clara y oscura, seleccionables en Ajustes → Apariencia.
- **Saldo inicial visible y editable** en Ajustes → Datos.
- **Empezar de cero**: borra movimientos, presupuestos, objetivos y recurrentes, y deja el saldo inicial a 0, manteniendo categorías, cuentas y apariencia.
- **Migración v3 → v4** para eliminar el saldo oculto de 3.850 € en instalaciones que ya habían borrado los movimientos.
- La pantalla de inicio muestra un estado vacío claro cuando no hay movimientos, en lugar de un gráfico a cero.

### v0.3

- **Copia de seguridad y restauración** (BUD-20): JSON versionado con todos los datos, validación antes de importar, confirmación con resumen y protección contra activar el bloqueo biométrico desde un archivo.
- **Bloqueo biométrico endurecido** (BUD-26): mapeo de errores (cancelación, bloqueo, sensor ausente), PIN de respaldo en Android 10+, botón “Probar desbloqueo” y checklist de validación en [`BIOMETRIC_TEST.md`](BIOMETRIC_TEST.md). Pendiente de firma en dispositivo real.

### v0.2

- **Movimientos recurrentes** mensuales: día del mes, pausa, edición, borrado y registro automático al abrir la app.
- **Objetivos de ahorro** con meta, fecha límite, aportaciones/retiradas, progreso y sugerencia mensual.
- **Detalle de categoría** desde el donut o la leyenda: evolución, métricas y movimientos editables.
- **Bloqueo biométrico** con huella/PIN al abrir y tras 30 segundos en segundo plano.
- Cargo de ejemplo de “Gimnasio” generado automáticamente para demostrar las recurrencias.

### v0.1

- Saldo total, evolución de 6 meses y resumen mensual.
- Alta, edición y borrado de movimientos con deshacer.
- Presupuesto general y por categorías.
- Análisis por categorías, comparativa y estadísticas.
- Tema claro/oscuro/sistema, monedas, categorías, cuentas y exportación CSV.

## Épica BUD-1 · Control del día a día

> Mejoras para el uso diario de Bud Jet: presupuestos, recurrencias, búsqueda y captura rápida.

| Clave | Historia | Prioridad propuesta |
|---|---|---|
| BUD-2 | Aviso al acercarse al límite de presupuesto (80 % y 100 %, sin repetir más de una vez al día). | Alta |
| BUD-7 | Plantillas de movimientos frecuentes para registrarlos en un toque. | Media |
| BUD-8 | Búsqueda avanzada por texto, fechas, importe, cuenta y categoría. | Media |
| BUD-9 | Adjuntar ticket (foto o PDF) a un movimiento. | Baja |
| BUD-10 | Recurrencias semanales y anuales además de las mensuales. | Media |

## Épica BUD-3 · Ahorro y objetivos

> Todo lo relacionado con apartar dinero, objetivos y transferencias entre cuentas.

| Clave | Historia | Prioridad propuesta |
|---|---|---|
| BUD-11 | Transferencias entre cuentas sin contabilizarlas como gasto o ingreso. | Alta |
| BUD-14 | Aportaciones automáticas mensuales a objetivos. | Media |
| BUD-15 | Objetivo vinculado a una cuenta de ahorro real. | Media |

## Épica BUD-4 · Análisis

> Comparativas, suscripciones, insights y detalle cada vez más fino del gasto.

| Clave | Historia | Prioridad propuesta |
|---|---|---|
| BUD-12 | Evolución del patrimonio total y por cuenta. | Media |
| BUD-13 | Comparación con la regla 50/30/20. | Baja |
| BUD-16 | Comparativa con el mismo mes del año anterior. | Media |
| BUD-17 | Detección de suscripciones repetidas y su coste anual. | Media |
| BUD-18 | Insights de hábitos de gasto basados en reglas. | Baja |
| BUD-19 | Drill-down de categoría a comercio o nota frecuente. | Baja |

## Épica BUD-5 · Producto y confianza

> Copia de seguridad, onboarding, widget y funciones de producto.

| Clave | Historia | Prioridad propuesta |
|---|---|---|
| BUD-21 | Widget con saldo del mes y acceso rápido a añadir gasto. | Media |
| BUD-22 | Onboarding de nombre, moneda y primer presupuesto. | Media |
| BUD-23 | Modo familia con perfiles locales. | Baja |

## Épica BUD-6 · Deuda técnica

> Calidad interna, pruebas, rendimiento y mantenimiento.

| Clave | Historia | Prioridad propuesta |
|---|---|---|
| BUD-24 | Migrar la persistencia JSON a Room con migración de datos. | Media |
| BUD-25 | Pruebas de UI de flujos completos (movimientos, objetivos y recurrencias). | Media |
| BUD-27 | Internacionalización ES/EN y formatos regionales. | Baja |
| BUD-28 | Reducir el tamaño del APK sustituyendo `material-icons-extended`. | Baja |
| BUD-29 | Baseline profile para arranque en frío. | Baja |

## Criterios de diseño

- Luz cálida y contraste alto; nada de sombras duras.
- Un solo acento (verde pino) y tierra para los gastos.
- Cifras tabulares, jerarquía editorial y aire entre secciones.
- Cada pantalla debe responder a una pregunta: *¿cuánto tengo?*, *¿en qué se va?*, *¿voy bien?*, *¿qué quiero conseguir?*.
