# Bud Jet

Aplicación Android para llevar el control de tus gastos, ingresos, presupuestos y objetivos de ahorro. Diseño sobrio, cálido y editorial, con tres gamas de color.

> Estado: **boceto funcional v0.9.0**. Datos locales, patrimonio con deudas y plan de pago, cartera, importación de extractos, avisos, copias cifradas, widget adaptado a la paleta y captura rápida.

| Resumen | Objetivos | Movimientos |
|---|---|---|
| ![Resumen](docs/screenshots/01-resumen.png) | ![Objetivos](docs/screenshots/03-objetivos.png) | ![Movimientos](docs/screenshots/04-movimientos.png) |

| Presupuestos | Análisis | Detalle de categoría |
|---|---|---|
| ![Presupuestos](docs/screenshots/05-presupuestos.png) | ![Análisis](docs/screenshots/06-analisis.png) | ![Detalle de categoría](docs/screenshots/07-categoria.png) |

| Cartera de inversión | Producto | Calculadora |
|---|---|---|
| ![Cartera](docs/screenshots/07b-cartera.png) | ![Producto](docs/screenshots/07c-producto.png) | ![Calculadora](docs/screenshots/07e-calculadora-volatilidad.png) |

| Patrimonio | Transferir | Importar extracto |
|---|---|---|
| ![Patrimonio](docs/screenshots/06b-patrimonio.png) | ![Transferir](docs/screenshots/06c-transferencia.png) | ![Importar](docs/screenshots/09c-importar.png) |

| Avisos | Copia cifrada | Copias automáticas |
|---|---|---|
| ![Avisos](docs/screenshots/09a-ajustes-avisos.png) | ![Copia cifrada](docs/screenshots/09d-copia-cifrada.png) | ![Copias automáticas](docs/screenshots/09f-copias-automaticas.png) |

| Pago de deuda | Selección múltiple | Captura desde el widget |
|---|---|---|
| ![Pago de deuda](docs/screenshots/06d-pago-deuda.png) | ![Selección múltiple](docs/screenshots/04c-movimientos-seleccion.png) | ![Captura rápida](docs/screenshots/21-widget-quickadd.png) |

| Plan de deudas |
|---|
| ![Plan de deudas](docs/screenshots/06e-plan-deudas.png) |

| Orden de movimientos | Orden por importe |
|---|---|
| ![Orden de movimientos](docs/screenshots/04e-orden-menu.png) | ![Orden por importe](docs/screenshots/04f-movimientos-importe.png) |

| Añadir movimiento | Movimientos recurrentes | Ajustes |
|---|---|---|
| ![Nuevo movimiento](docs/screenshots/08-nuevo-movimiento.png) | ![Recurrentes](docs/screenshots/14-recurrentes.png) | ![Ajustes](docs/screenshots/09-ajustes.png) |

| Datos y copia | Modo oscuro | Empezar de cero |
|---|---|---|
| ![Datos](docs/screenshots/09b-ajustes-datos.png) | ![Resumen en modo oscuro](docs/screenshots/16-resumen-oscuro.png) | ![Resumen vacío](docs/screenshots/18-resumen-vacio.png) |

| Búsqueda con cifras | Anulación de un pago |
|---|---|
| ![Búsqueda](docs/screenshots/04b-movimientos-busqueda.png) | ![Anulación](docs/screenshots/08c-anulacion.png) |

| Bloqueo biométrico |
|---|
| ![Bloqueo](docs/screenshots/17-bloqueo.png) |

## Gamas de color

Se eligen en **Ajustes → Apariencia**, debajo de Claro/Oscuro/Sistema. Cada gama tiene variante clara y oscura:

| Business | Navy | Boss |
|---|---|---|
| ![Business](docs/screenshots/01-resumen.png) | ![Navy](docs/screenshots/11-resumen-navy.png) | ![Boss](docs/screenshots/13-resumen-boss.png) |
| Papel, verde pino y terracota. | Beige, azul marino y marrón. | Gris/negro y rojo vino. |

Las categorías conservan sus colores en las tres gamas para no perder identidad; el verde sigue significando ingreso y el rojo/tierra gasto.

## Qué incluye

- **Inicio** — saldo total, evolución de 6 meses, ingresos/gastos del mes, presupuesto, objetivos y últimos movimientos.
- **Movimientos** — historial agrupado por día, filtros, búsqueda, edición y borrado con deshacer. Al buscar, el resumen de ingresos/gastos/balance se recalcula solo con los resultados.
- **Orden de movimientos** — por defecto, la fecha más reciente primero y, dentro de cada día, el último registro primero. Puedes cambiar a **orden de registro** o **importe** (mayor o menor).
- **Presupuestos** — límite mensual general y por categoría, con ritmo de gasto diario.
- **Análisis** — desglose por categorías, comparativa de 6 meses, estadísticas e ideas automáticas. Puedes fijar **desde qué fecha** empieza a contar el histórico.
- **Balance del mes** — cada mes arranca con el saldo real del cierre anterior (aunque la nómina llegue el último día), suma ingresos, resta gastos y muestra el saldo final.
- **Patrimonio neto** — efectivo + inversiones − deudas, con evolución de 6 meses, saldo por cuenta y edición de deudas (hipoteca, préstamo, tarjeta u otras). El saldo inicial se asigna a la primera cuenta.
- **Pagos parciales de deudas** — registra cuánto has pagado, desde qué cuenta y en qué fecha. El saldo de la deuda y el efectivo bajan a la vez, y el patrimonio neto se mantiene coherente.
- **Plan de deudas** — indica cuánto puedes pagar al mes y elige entre **bola de nieve** (menor saldo primero) y **avalancha** (mayor interés primero). Calcula fecha libre, intereses totales, orden de pago y compara ambas estrategias.
- **Transferencias internas** — mueve dinero entre cuentas o entre una cuenta y un producto de inversión sin contarlo como ingreso ni gasto. Una aportación desde una cuenta descuenta el efectivo y suma cartera; una retirada hace lo contrario.
- **Selección múltiple en Movimientos** — mantén pulsado un movimiento para seleccionar varios, cambiarles la fecha de una vez o eliminarlos en bloque.
- **Cartera de inversión** — productos de fondos, acciones, ETF, cripto, depósitos, planes de pensiones, inmuebles u otros; aportaciones, retiradas, rendimientos y ajustes de valor. Calcula valor actual, rentabilidad total, **rentabilidad anualizada (XIRR)**, distribución y evolución de 12 meses. Puede ir enlazada a tus cuentas o seguirse por separado.
- **Calculadora de interés compuesto** — capital inicial, aportación mensual, rentabilidad anual, años y capitalización mensual o anual. La **volatilidad anual** es opcional: con 0 % hace un cálculo fijo; con un valor simula 1.200 escenarios y muestra P10, P50 y P90.
- **Importar extractos** — CSV u OFX del banco, procesados en el dispositivo. Detección de columnas, mapeo manual si hace falta, vista previa, detección de duplicados, cambio de categoría/tipo por fila y reglas de categorización por comercio o texto.
- **Avisos locales** — presupuesto al 80 % y 100 %, cargos recurrentes del día siguiente, revisión mensual y recordatorio para actualizar la cartera. Se activan en Ajustes → Avisos y no necesitan servidor.
- **Copias cifradas** — exportación `.bjet` con contraseña (PBKDF2 + AES-256-GCM), restauración con contraseña y copias automáticas diarias cifradas con Android Keystore (últimas 14).
- **Widget y captura rápida** — widget con saldo y gasto del mes que usa la paleta y el modo claro/oscuro elegidos. El botón “+ Añadir” abre una captura compacta con desbloqueo biométrico, sin cargar la app completa. También puedes compartir texto con Bud Jet para abrir el editor con importe y nota.
- **Detalle de categoría** — toca un segmento del donut o su leyenda para ver la evolución, las métricas y los movimientos de esa categoría.
- **Objetivos de ahorro** — metas, aportaciones/retiradas, progreso y sugerencia mensual. Es una “hucha virtual”.
- **Movimientos recurrentes** — nómina, alquiler o suscripciones mensuales; se apuntan solos al abrir la app, se pueden pausar y editar.
- **Movimientos programados** — puedes fechar un ingreso o gasto en el futuro: se guarda y se muestra en la lista con la marca “Programado”, pero no afecta al saldo, resumen, presupuestos ni análisis hasta ese día.
- **Categorías nuevas** — **Belleza** (gasto), **Devolución** y **Anulación** (ingreso). Anulación busca el gasto original y crea un ingreso vinculado por el mismo importe, dejando el efecto neto a cero.
- **Fecha recordada** — al añadir un movimiento, la app propone la última fecha que usaste.
- **Importes con decimales** — todos los importes muestran céntimos; no se redondean a euros enteros.
- **Copia de seguridad** — exporta todo a un JSON versionado y restáuralo desde el selector de Android, con validación y resumen antes de sustituir.
- **Saldo inicial** — visible y editable en Ajustes → Datos. Ya no hay una cifra oculta de demo.
- **Empezar de cero** — borra movimientos, presupuestos, objetivos, recurrentes, cartera, transferencias y deudas; deja el saldo inicial a 0 y conserva categorías, cuentas, reglas y apariencia.
- **Bloqueo biométrico** — huella o PIN al abrir y tras 30 segundos en segundo plano, con “Probar desbloqueo”.
- **Ajustes** — nombre, moneda (EUR/USD/GBP/MXN), tema, gama de color, categorías, cuentas, avisos, importación, copias y exportación CSV.

Todo se guarda en el dispositivo (`files/budjet.json`); no hay cuentas ni nube.

El histórico de sprint, la composición del equipo simulado y los impedimentos están en [`docs/SPRINTS.md`](docs/SPRINTS.md).

## Stack

- Kotlin + Jetpack Compose (Material 3)
- ViewModel + StateFlow
- Kotlinx Serialization para persistencia y backups
- AndroidX Biometric + FragmentActivity
- WorkManager para avisos locales y RemoteViews para el widget
- Android Keystore + AES-256-GCM para copias cifradas
- Gradle 9.8 · AGP 9.4 · compileSdk 37 · targetSdk 37 · minSdk 26
- Tipografías DM Serif Display + Inter (SIL OFL)

## Compilar

```bash
./gradlew :app:assembleDebug
```

APK en `app/build/outputs/apk/debug/app-debug.apk`.

Requiere JDK 17+ y Android SDK 37.

## Probar el boceto

1. Instala `BudJet-0.9.0-boceto.apk` en un Android 8.0 o superior.
2. La app arranca con datos de ejemplo de los últimos 6 meses.
3. Para empezar con todo a cero: **Ajustes → Datos → Empezar de cero**. El saldo total, ingresos, gastos, presupuestos, objetivos, recurrentes, cartera, transferencias y deudas quedarán a 0. Si quieres registrar el dinero que ya tenías, cambia **Saldo inicial** en la misma pantalla.
4. Prueba las gamas **Business / Navy / Boss** en **Ajustes → Apariencia**.
5. En **Análisis → Patrimonio** revisa efectivo, inversiones y deudas. Prueba **Transferir** entre cuentas y hacia un producto de la cartera; el saldo de las cuentas debe moverse sin alterar ingresos/gastos.
6. En una deuda, pulsa **Pagar** y registra un pago parcial: la deuda y el efectivo deben bajar a la vez.
7. En **Movimientos**, prueba **Ordenar por**: fecha, registro e importe (mayor o menor). Por defecto verás primero lo más reciente y, dentro del día, lo último registrado.
8. En **Patrimonio → Deudas → Plan**, ajusta el pago mensual y compara **bola de nieve** con **avalancha**. Comprueba la fecha libre, los intereses y el orden de pago.
9. En **Movimientos**, mantén pulsado un movimiento para seleccionar varios. Prueba **Fecha** para cambiarla en bloque y **Eliminar** para borrarlos juntos.
10. En **Ajustes → Datos → Importar extracto**, elige un CSV/OFX de prueba. Revisa columnas, categorías y duplicados antes de importar.
11. En **Ajustes → Avisos**, activa recordatorios y prueba los permisos de notificación de Android 13+.
12. En **Ajustes → Datos → Copia cifrada**, exporta un `.bjet` con contraseña y restáuralo. Revisa también **Copias automáticas**.
13. Añade el widget de Bud Jet: debe usar la gama y el modo claro/oscuro de la app. Prueba “+ Añadir”; si el bloqueo biométrico está activo, pedirá desbloqueo antes de mostrar la captura compacta.
14. En **Análisis → Cartera de inversión**, añade un producto, registra una aportación y prueba «Actualizar valor» y «Rendimiento». Después abre la calculadora y simula con volatilidad.
15. El bloqueo biométrico se activa en **Ajustes → Seguridad**; sigue [`docs/BIOMETRIC_TEST.md`](docs/BIOMETRIC_TEST.md).

La APK de la raíz es una build *debug* firmada con la clave de desarrollo: sirve para probar, no para publicar en Google Play.

## Tests

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

- **73 tests de lógica** — finanzas, balance inicial/final, transferencias y patrimonio, pagos y plan de deudas, movimientos futuros, fechas recordadas, recurrencias, objetivos, cartera y XIRR, calculadora determinista y Monte Carlo, importación CSV/OFX, copias cifradas, backup, migraciones, filtros y biometría.
- **3 tests de captura** con Robolectric/Roborazzi que renderizan 42 pantallas (incluida la captura rápida del widget).
- **Lint: 0 errores**.

## Estructura

```
app/src/main/java/com/budjet/app/
├── data/         modelos, demo, recurrencias, backup, cifrado, importación y cálculos
├── notifications/ avisos locales con WorkManager
├── widget/       widget de inicio y lectura de datos
├── ui/           navegación, ViewModel, pantallas y componentes
├── ui/theme/     paletas Business/Navy/Boss, tipografía y tema
└── util/         formatos, CSV y autenticación biométrica
```

## Límites conocidos del boceto

- Las recurrencias son mensuales y se procesan al abrir la app; si no abres la app durante un mes, ese cargo no se genera.
- Los movimientos futuros aparecen en la lista del mes en que caen y se marcan como “Programado”; no cuentan hasta su fecha.
- Los objetivos son una asignación virtual: no mueven dinero entre cuentas ni descuentan del saldo.
- La cartera de inversión puede ir enlazada a cuentas o seguirse por separado. Las aportaciones desde una cuenta sí mueven el efectivo; las externas no. Los valores se actualizan a mano: no hay cotizaciones automáticas.
- Las deudas se registran con su saldo actual, interés anual y cuota mínima; permiten pagos parciales, pero no generan un calendario de amortización contractual ni incluyen comisiones o seguros.
- El plan de deudas supone que el pago mensual se mantiene constante y que los intereses se calculan mensualmente sobre el saldo. Es una estimación, no una oferta financiera.
- La selección múltiple de movimientos no tiene deshacer: el borrado en bloque pide confirmación.
- La importación CSV/OFX es local y tolerante a formatos comunes, pero cada banco puede tener particularidades. Revisa siempre la vista previa antes de importar.
- Los avisos son locales y dependen de que Android permita notificaciones y no limite el trabajo en segundo plano.
- Las copias automáticas viven en el almacenamiento privado de la app y usan una clave de Android Keystore: si desinstalas la app, esa clave se pierde. Para conservarlas fuera del móvil, usa la copia cifrada con contraseña.
- El widget se actualiza cuando la app guarda datos; no es un servicio en tiempo real. El botón “+ Añadir” abre una actividad compacta, porque Android no permite formularios de texto dentro de un widget.
- La rentabilidad anualizada usa XIRR (dinero ponderado en el tiempo) y necesita al menos una entrada y una salida de dinero; si no puede calcularse, se muestra «—».
- La calculadora es una estimación: no incluye impuestos, comisiones ni inflación. Con volatilidad, los percentiles P10/P50/P90 salen de una simulación y no garantizan resultados futuros.
- **Anulación** conserva el gasto original y crea un ingreso vinculado por el mismo importe; el efecto neto es cero. No borra el gasto de la lista.
- El rango histórico solo afecta a las gráficas de Análisis y al detalle de categoría; el bloque de balance del mes usa el saldo real completo.
- La copia de seguridad **sustituye** todos los datos; todavía no hay fusión ni importación incremental.
- El archivo JSON de compatibilidad sigue siendo legible; para datos sensibles usa la copia `.bjet` cifrada.
- El bloqueo biométrico está integrado y probado en JVM/UI; falta la firma en un dispositivo real.
- Sin copia en la nube ni sincronización entre dispositivos.

## Próximos pasos

- [ ] Validar la checklist biométrica en tu móvil (BUD-26).
- [ ] Sincronización bancaria opcional o importación incremental (BUD-11/BUD-8).
- [ ] Recurrencias semanales/anuales (BUD-10).
- [ ] Aportaciones automáticas a objetivos (BUD-14).
- [ ] Historial de deudas y amortización.
- [ ] Cotizaciones automáticas de la cartera.

El backlog está creado en Jira:

- Proyecto: [BUD · Bud Jet](https://jaxsullivan7.atlassian.net/jira/software/projects/BUD/summary)
- 5 épicas (BUD-1, BUD-3, BUD-4, BUD-5, BUD-6) y 24 historias/tareas.
- Mapa de claves: [`docs/jira-created.md`](docs/jira-created.md).
- Snapshot importable: [`docs/jira-import.csv`](docs/jira-import.csv).
