# Backlog · Bud Jet

Propuesta de backlog para el proyecto **BUD** de Jira. Las historias están escritas para poder copiarse tal cual.

## Hecho en v0.2

| Historia | Entrega |
|---|---|
| BUD-10 | **Movimientos recurrentes** mensuales con día del mes, pausa, edición, borrado y registro automático al abrir la app. |
| BUD-20 | **Objetivos de ahorro** con meta, fecha límite, aportaciones/retiradas, progreso y sugerencia mensual. |
| BUD-30 | **Detalle de categoría** desde el donut o la leyenda: evolución 6 meses, métricas y movimientos editables. |
| BUD-41 | **Bloqueo biométrico** con huella/PIN al abrir y tras 30 s en segundo plano. |

## Épica 1 · Fundamentos (v0.1 — hecho)

| Historia | Descripción | Prioridad |
|---|---|---|
| BUD-1 | Como usuario quiero ver mi saldo total y la evolución reciente al abrir la app. | Must |
| BUD-2 | Como usuario quiero apuntar un gasto o ingreso en pocos toques con teclado numérico. | Must |
| BUD-3 | Como usuario quiero editar y eliminar movimientos y poder deshacer el borrado. | Must |
| BUD-4 | Como usuario quiero definir un presupuesto mensual general y por categorías. | Must |
| BUD-5 | Como usuario quiero ver en qué categorías se va mi dinero cada mes. | Must |
| BUD-6 | Como usuario quiero cambiar entre tema claro, oscuro o el del sistema. | Should |
| BUD-7 | Como usuario quiero exportar mis movimientos a CSV. | Should |
| BUD-8 | Como usuario quiero elegir mi moneda (EUR, USD, GBP, MXN). | Should |

## Épica 2 · Control del día a día

| Historia | Criterios de aceptación | Prioridad |
|---|---|---|
| BUD-11 | **Aviso de presupuesto**: notificación local al superar el 80 % y el 100 % de un presupuesto. | Must |
| BUD-12 | **Plantillas rápidas**: guardar movimientos frecuentes para apuntarlos en un toque. | Should |
| BUD-13 | **Búsqueda avanzada**: filtrar por rango de fechas, importe, cuenta y categoría. | Should |
| BUD-14 | **Adjuntar ticket**: asociar una foto o PDF a un movimiento. | Could |
| BUD-15 | **Recurrencias semanales/anuales** además de las mensuales. | Should |

## Épica 3 · Ahorro y objetivos

| Historia | Criterios de aceptación | Prioridad |
|---|---|---|
| BUD-21 | **Transferencias entre cuentas**: mover dinero sin contar como gasto o ingreso. | Must |
| BUD-22 | **Patrimonio**: evolución del saldo total y de cada cuenta en el tiempo. | Should |
| BUD-23 | **Regla 50/30/20**: comparar gastos fijos, variables y ahorro con la recomendación. | Could |
| BUD-24 | **Aportaciones automáticas a objetivos** cada mes. | Should |
| BUD-25 | **Objetivo vinculado a una cuenta**: apartar dinero real de la cuenta de ahorro. | Should |

## Épica 4 · Análisis

| Historia | Criterios de aceptación | Prioridad |
|---|---|---|
| BUD-31 | **Comparativa interanual**: mismo mes del año anterior y estacionalidad. | Should |
| BUD-32 | **Suscripciones**: detectar cargos repetidos y avisar de su coste anual. | Should |
| BUD-33 | **Insights**: recomendaciones basadas en hábitos (sin IA generativa al principio). | Could |
| BUD-34 | **Drill-down de dos niveles**: categoría → comercio/nota frecuente. | Could |

## Épica 5 · Producto y confianza

| Historia | Criterios de aceptación | Prioridad |
|---|---|---|
| BUD-40 | **Copia de seguridad**: exportar e importar un JSON cifrado. | Must |
| BUD-42 | **Widget**: saldo del mes y acceso rápido a añadir gasto. | Should |
| BUD-43 | **Onboarding**: 3 pantallas para nombre, moneda y primer presupuesto. | Should |
| BUD-44 | **Modo familia**: varios perfiles locales (sin nube en v1). | Could |

## Deuda técnica

- Migrar la persistencia JSON a Room cuando el volumen de movimientos crezca.
- Añadir pruebas de UI de flujos completos (crear, editar, borrar, deshacer, aportar a objetivo).
- Internacionalización ES/EN y formatos regionales.
- Baseline profile para arranque en frío.
- Reducir el tamaño del APK sustituyendo `material-icons-extended` por iconos propios.
- Probar el desbloqueo biométrico en dispositivos con huella y con PIN como alternativa.

## Criterios de diseño

- Luz cálida y contraste alto; nada de sombras duras.
- Un solo acento (verde pino) y tierra para los gastos.
- Cifras tabulares, jerarquía editorial y aire entre secciones.
- Cada pantalla debe responder a una pregunta: *¿cuánto tengo?*, *¿en qué se va?*, *¿voy bien?*, *¿qué quiero conseguir?*.
