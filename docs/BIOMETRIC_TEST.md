# Prueba del bloqueo biométrico (BUD-26)

El flujo está integrado y compilado. Esta checklist es la parte que hay que firmar en un móvil real: el emulador de este equipo no tiene virtualización activa y no puede mostrar el diálogo biométrico.

## Antes de empezar

- Android 8.0 o superior.
- Huella o rostro registrado y PIN de seguridad configurado.
- APK `BudJet-0.3.0-boceto.apk`.
- Better: desactivar temporalmente “No molestar” para ver el diálogo con claridad.

## Casos

| # | Acción | Resultado esperado |
|---|---|---|
| 1 | Ajustes → Seguridad → activar “Bloqueo biométrico” | Aparece el diálogo del sistema; al confirmar, el interruptor queda activo y se muestra “Bloqueo biométrico activado”. |
| 2 | Con el bloqueo activo, pulsar “Probar desbloqueo” | Se abre el diálogo; al autenticarte, aparece “Desbloqueo verificado”. |
| 3 | Cerrar la app desde Recientes y volver a abrirla | Aparece la pantalla de bloqueo; al autenticarte, entra en Inicio. |
| 4 | Dejar la app en segundo plano más de 30 segundos y volver | Aparece la pantalla de bloqueo. |
| 5 | Volver en menos de 30 segundos | No se pide autenticación. |
| 6 | Cancelar el diálogo y volver a intentarlo | Se muestra “Autenticación cancelada” y el botón “Desbloquear” sigue disponible. |
| 7 | Fallar la huella y usar el PIN del dispositivo (Android 10+) | El PIN permite desbloquear; no se queda bloqueado. |
| 8 | Desactivar el bloqueo desde Ajustes y reabrir la app | Entra directamente, sin pantalla de bloqueo. |
| 9 | Importar una copia de seguridad | El bloqueo biométrico **no** se activa solo por importar (por seguridad). |
| 10 | Activar el bloqueo sin huella ni PIN configurados | La app avisa y no activa el interruptor. |

## Si algo falla

Anota:

- Modelo y versión de Android.
- Caso de la tabla.
- ¿Aparece el diálogo del sistema o falla antes?
- ¿Se queda en la pantalla de bloqueo? ¿Con qué mensaje?
- Captura de pantalla si es posible.

Con eso puedo ajustar `BiometricAuth` y el flujo de bloqueo sin adivinar.
