# Bud Jet

Aplicación Android para llevar el control de tus gastos, ingresos, presupuestos y objetivos de ahorro. Diseño sobrio, cálido y editorial: papel, tinta y verde pino.

> Estado: **boceto funcional v0.3.0**. Datos locales, copia de seguridad y desbloqueo biométrico.

| Resumen | Objetivos | Movimientos |
|---|---|---|
| ![Resumen](docs/screenshots/01-resumen.png) | ![Objetivos](docs/screenshots/03-objetivos.png) | ![Movimientos](docs/screenshots/04-movimientos.png) |

| Presupuestos | Análisis | Detalle de categoría |
|---|---|---|
| ![Presupuestos](docs/screenshots/05-presupuestos.png) | ![Análisis](docs/screenshots/06-analisis.png) | ![Detalle de categoría](docs/screenshots/07-categoria.png) |

| Añadir movimiento | Movimientos recurrentes | Ajustes |
|---|---|---|
| ![Nuevo movimiento](docs/screenshots/08-nuevo-movimiento.png) | ![Recurrentes](docs/screenshots/10-recurrentes.png) | ![Ajustes](docs/screenshots/09-ajustes.png) |

| Datos y copia de seguridad | Modo oscuro | Bloqueo biométrico |
|---|---|---|
| ![Datos](docs/screenshots/09b-ajustes-datos.png) | ![Resumen en modo oscuro](docs/screenshots/12-resumen-oscuro.png) | ![Bloqueo](docs/screenshots/13-bloqueo.png) |

## Qué incluye

- **Inicio** — saldo total, evolución de 6 meses, ingresos/gastos del mes, presupuesto, objetivos y últimos movimientos.
- **Movimientos** — historial agrupado por día, filtros, búsqueda, edición y borrado con deshacer.
- **Presupuestos** — límite mensual general y por categoría, con ritmo de gasto diario.
- **Análisis** — desglose por categorías, comparativa de 6 meses, estadísticas e ideas automáticas.
- **Detalle de categoría** — toca un segmento del donut o su leyenda para ver la evolución, las métricas y los movimientos de esa categoría; desde ahí puedes editar cada gasto.
- **Objetivos de ahorro** — crea metas, aporta o retira dinero y consulta el progreso y la sugerencia mensual. Es una “hucha virtual”: organiza tu ahorro sin mover el saldo.
- **Movimientos recurrentes** — nómina, alquiler o suscripciones mensuales; se apuntan solos al abrir la app el día que toca, se pueden pausar y editar.
- **Copia de seguridad** — exporta todo a un JSON versionado y restáuralo desde el selector de archivos de Android. La importación sustituye los datos actuales tras una confirmación con el resumen de la copia.
- **Bloqueo biométrico** — pide huella o PIN al abrir la app y al volver tras más de 30 segundos en segundo plano. Incluye “Probar desbloqueo” en Ajustes.
- **Ajustes** — nombre, moneda (EUR/USD/GBP/MXN), tema claro/oscuro/sistema, categorías, cuentas y exportación CSV.
- **Añadir movimiento** — teclado numérico propio, importes rápidos, categorías, cuenta, fecha y nota.

Todo se guarda en el dispositivo (`files/budjet.json`); no hay cuentas ni nube.

## Stack

- Kotlin + Jetpack Compose (Material 3)
- ViewModel + StateFlow
- Kotlinx Serialization para persistencia y backups
- AndroidX Biometric + FragmentActivity
- Gradle 9.8 · AGP 9.4 · compileSdk 37 · targetSdk 37 · minSdk 26
- Tipografías DM Serif Display + Inter (SIL OFL)

## Compilar

```bash
./gradlew :app:assembleDebug
```

APK en `app/build/outputs/apk/debug/app-debug.apk`.

Requiere JDK 17+ y Android SDK 37.

## Probar el boceto

1. Instala `BudJet-0.3.0-boceto.apk` en un Android 8.0 o superior (o compila con Gradle).
2. La app arranca con datos de ejemplo de los últimos 6 meses para que puedas moverte por todas las pantallas.
3. En **Ajustes → Datos** puedes exportar una copia JSON, restaurarla, cargar el ejemplo o borrar los movimientos.
4. El bloqueo biométrico se activa desde **Ajustes → Seguridad**. Usa **Probar desbloqueo** y la checklist de [`docs/BIOMETRIC_TEST.md`](docs/BIOMETRIC_TEST.md).

La APK de la raíz es una build *debug* firmada con la clave de desarrollo: sirve para probar, no para publicar en Google Play.

## Tests

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

- **30 tests de lógica** — finanzas, recurrencias, objetivos, backup/importación, validación y mapeo de errores biométricos.
- **2 tests de captura** con Robolectric/Roborazzi que renderizan la app completa y generan 14 pantallas.
- **Lint: 0 errores**.

## Estructura

```
app/src/main/java/com/budjet/app/
├── data/         modelos, demo, recurrencias, backup, repositorio y cálculos
├── ui/           navegación, ViewModel, pantallas y componentes
├── ui/theme/     paleta, tipografía y tema
└── util/         formatos, CSV y autenticación biométrica
```

## Límites conocidos del boceto

- Las recurrencias son mensuales y se procesan al abrir la app; si no abres la app durante un mes, ese cargo no se genera.
- Los objetivos son una asignación virtual: no mueven dinero entre cuentas ni descuentan del saldo.
- La copia de seguridad **sustituye** todos los datos; todavía no hay fusión ni importación incremental.
- El archivo de copia es un JSON legible, no cifrado: guárdalo en un lugar seguro. El cifrado está previsto para una versión posterior.
- El bloqueo biométrico está integrado y probado en JVM/UI; falta la firma en un dispositivo real con huella y PIN.
- Sin copia en la nube ni sincronización entre dispositivos.

## Próximos pasos

- [ ] Validar la checklist biométrica en tu móvil (BUD-26).
- [ ] Recurrencias semanales/anuales y avisos de próximos cargos (BUD-10).
- [ ] Notificaciones de presupuesto y cargos (BUD-2).
- [ ] Aportaciones automáticas a objetivos (BUD-14).
- [ ] Copia de seguridad cifrada y copia automática.
- [ ] Widget y notificaciones.

El backlog está creado en Jira:

- Proyecto: [BUD · Bud Jet](https://jaxsullivan7.atlassian.net/jira/software/projects/BUD/summary)
- 5 épicas (BUD-1, BUD-3, BUD-4, BUD-5, BUD-6) y 24 historias/tareas (BUD-2, BUD-7…BUD-29).
- Mapa de claves: [`docs/jira-created.md`](docs/jira-created.md).
- Snapshot importable: [`docs/jira-import.csv`](docs/jira-import.csv).
