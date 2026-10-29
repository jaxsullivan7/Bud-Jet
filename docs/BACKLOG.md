# Backlog · Bud Jet

Propuesta de backlog para el proyecto **BUD** de Jira. Las historias están escritas para poder copiarse tal cual y priorizadas para las siguientes iteraciones.

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
| BUD-10 | **Movimientos recurrentes**: crear nómina, alquiler y suscripciones que se registran automáticamente cada mes. | Must |
| BUD-11 | **Aviso de presupuesto**: notificación local al superar el 80 % y el 100 % de un presupuesto. | Must |
| BUD-12 | **Plantillas rápidas**: guardar movimientos frecuentes para apuntarlos en un toque. | Should |
| BUD-13 | **Búsqueda avanzada**: filtrar por rango de fechas, importe, cuenta y categoría. | Should |
| BUD-14 | **Adjuntar ticket**: asociar una foto o PDF a un movimiento. | Could |

## Épica 3 · Ahorro y objetivos

| Historia | Criterios de aceptación | Prioridad |
|---|---|---|
| BUD-20 | **Objetivos de ahorro**: crear un objetivo con importe y fecha, y ver su progreso. | Must |
| BUD-21 | **Transferencias entre cuentas**: mover dinero sin contar como gasto o ingreso. | Must |
| BUD-22 | **Patrimonio**: evolución del saldo total y de cada cuenta en el tiempo. | Should |
| BUD-23 | **Regla 50/30/20**: comparar gastos fijos, variables y ahorro con la recomendación. | Could |

## Épica 4 · Análisis

| Historia | Criterios de aceptación | Prioridad |
|---|---|---|
| BUD-30 | **Detalle por categoría**: tocar un segmento del donut y ver sus movimientos. | Should |
| BUD-31 | **Comparativa interanual**: mismo mes del año anterior y estacionalidad. | Should |
| BUD-32 | **Suscripciones**: detectar cargos repetidos y avisar de su coste anual. | Should |
| BUD-33 | **Insights**: recomendaciones basadas en hábitos (sin IA generativa al principio). | Could |

## Épica 5 · Producto y confianza

| Historia | Criterios de aceptación | Prioridad |
|---|---|---|
| BUD-40 | **Copia de seguridad**: exportar e importar un JSON cifrado. | Must |
| BUD-41 | **Bloqueo biométrico**: pedir huella al abrir la app. | Should |
| BUD-42 | **Widget**: saldo del mes y acceso rápido a añadir gasto. | Should |
| BUD-43 | **Onboarding**: 3 pantallas para nombre, moneda y primer presupuesto. | Should |
| BUD-44 | **Modo familia**: varios perfiles locales (sin nube en v1). | Could |

## Deuda técnica

- Migrar la persistencia JSON a Room cuando el volumen de movimientos crezca.
- Añadir pruebas de UI de flujos completos (crear, editar, borrar, deshacer).
- Internacionalización ES/EN y formatos regionales.
- Baseline profile para arranque en frío.
- Reducir el tamaño del APK sustituyendo `material-icons-extended` por iconos propios.

## Criterios de diseño

- Luz cálida y contraste alto; nada de sombras duras.
- Un solo acento (verde pino) y tierra para los gastos.
- Cifras tabulares, jerarquía editorial y aire entre secciones.
- Cada pantalla debe responder a una pregunta: *¿cuánto tengo?*, *¿en qué se va?*, *¿voy bien?*.
