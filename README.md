# Bud Jet

Aplicación Android para llevar el control de tus gastos, ingresos y presupuestos personales. Diseño sobrio, cálido y editorial: papel, tinta y verde pino.

> Estado: **boceto funcional v0.1.0**. Datos locales de demostración incluidos.

| Resumen | Movimientos | Presupuestos |
|---|---|---|
| ![Resumen](docs/screenshots/01-resumen.png) | ![Movimientos](docs/screenshots/02-movimientos.png) | ![Presupuestos](docs/screenshots/03-presupuestos.png) |

| Análisis | Añadir movimiento | Ajustes |
|---|---|---|
| ![Análisis](docs/screenshots/04-analisis.png) | ![Nuevo movimiento](docs/screenshots/05-nuevo-movimiento.png) | ![Ajustes](docs/screenshots/06-ajustes.png) |

| Modo oscuro |
|---|
| ![Resumen en modo oscuro](docs/screenshots/08-resumen-oscuro.png) |

## Qué incluye

- **Inicio** — saldo total, evolución de 6 meses, ingresos/gastos del mes, presupuesto y últimos movimientos.
- **Movimientos** — historial agrupado por día, filtros, búsqueda, edición y borrado con deshacer.
- **Presupuestos** — límite mensual general y por categoría, con ritmo de gasto diario.
- **Análisis** — desglose por categorías, comparativa de 6 meses, estadísticas e ideas automáticas.
- **Ajustes** — nombre, moneda (EUR/USD/GBP/MXN), tema claro/oscuro/sistema, categorías, cuentas y exportación CSV.
- **Añadir movimiento** — teclado numérico propio, importes rápidos, categorías, cuenta, fecha y nota.

Todo se guarda en el dispositivo (`files/budjet.json`); no hay cuentas ni nube.

## Stack

- Kotlin + Jetpack Compose (Material 3)
- ViewModel + StateFlow
- Kotlinx Serialization para persistencia local
- Gradle 9.8 · AGP 9.4 · compileSdk 37 · minSdk 26
- Tipografías DM Serif Display + Inter (SIL OFL)

## Compilar

```bash
./gradlew :app:assembleDebug
```

APK en `app/build/outputs/apk/debug/app-debug.apk`.

Requiere JDK 17+ y Android SDK 37.

## Probar el boceto

1. Instala `BudJet-0.1.0-boceto.apk` en un Android 8.0 o superior (o compila con Gradle).
2. La app arranca con datos de ejemplo de los últimos 6 meses para que puedas moverte por todas las pantallas.
3. En **Ajustes → Datos** puedes restaurar el ejemplo o borrar los movimientos y empezar de cero.

La APK de la raíz es una build *debug* firmada con la clave de desarrollo: sirve para probar, no para publicar en Google Play.

## Tests

```bash
./gradlew :app:testDebugUnitTest
```

- `FinanceTest` — cálculos de balance, totales, presupuesto y reparto por categorías.
- `ScreenshotTest` — renderiza las pantallas reales con Robolectric y guarda capturas en `app/build/screenshots/`.

## Estructura

```
app/src/main/java/com/budjet/app/
├── data/         modelos, demo, repositorio y cálculos financieros
├── ui/           navegación, ViewModel, pantallas y componentes
├── ui/theme/     paleta, tipografía y tema
└── util/         formato de importes, fechas y textos
```

## Próximos pasos

- [ ] Movimientos recurrentes y suscripciones
- [ ] Objetivos de ahorro
- [ ] Gráficas interactivas con detalle por categoría
- [ ] Copia de seguridad / importación CSV
- [ ] Widget y notificaciones de presupuesto

El backlog priorizado está en [`docs/BACKLOG.md`](docs/BACKLOG.md). También hay un CSV listo para importar en Jira: [`docs/jira-import.csv`](docs/jira-import.csv).
