# BudJet · checklist de publicación

## Decisiones cerradas

- **Nombre:** BudJet
- **Package:** `com.budjet.app`
- **Modelo:** gratis, sin anuncios
- **Idiomas:** español + inglés
- **Soporte:** supportbudjet@gmail.com
- **Cuenta de Play:** personal verificada · nombre público NewProjects
- **Legal:** New Project · España
- **Analítica:** ninguna en v1.0; solo Play Console Vitals

## Estado técnico

- [x] Build de release con R8 y `shrinkResources`.
- [x] Firma de release configurada con un keystore de subida.
- [x] Versión **1.0.0** (`versionCode 14`).
- [x] AAB 1.0.0 firmado (`BudJet-1.0.0-release.aab`, 6,83 MB).
- [x] APK 1.0.0 firmada (`BudJet-1.0.0-release.apk`, 3,89 MB).
- [x] Firma y versión verificadas con `apksigner` y `aapt`.
- [x] Nombre visible de la app: BudJet.
- [x] Cuenta de Google Play creada y verificada.
- [x] Inglés completo de la interfaz.
  - [x] Bloque 1: navegación, Inicio, fechas y formato de importes.
  - [x] Bloque 2: Movimientos, orden, búsqueda y selección múltiple.
  - [x] Bloque 3: Presupuestos y Análisis.
  - [x] Bloque 4: Patrimonio, deudas, plan de deudas, cartera y calculadora.
  - [x] Bloque 5: Ajustes, editor, importación, recurrentes y objetivos.
  - [x] Bloque 6: widget, notificaciones, captura rápida, bloqueo y accesibilidad.
  - [x] Bloque 7: categorías, cuentas y datos de ejemplo por ID.
  - [x] Notas generadas, CSV y mensajes internos de validación de copias.
- [ ] QA en dispositivos reales.
- [ ] Política de privacidad y términos publicados.
- [ ] Ficha de tienda (icono, capturas, textos).
- [ ] Closed testing con 12 testers durante 14 días.
- [ ] Producción con rollout escalonado.

## Keystore de subida

- Keystore: `C:\Users\jeanm\Documents\BudJet-release\budjet-upload.jks`
- Propiedades locales: `keystore.properties` (ignorado por git)
- Alias: `budjet-upload`

**Importante:** guarda una copia de seguridad del keystore y de `keystore.properties` en un lugar seguro y cifrado. No los subas al repositorio ni los compartas. Si se pierde el keystore de subida, se puede solicitar un reset a Google Play, pero es un proceso lento.

## Siguiente tarea

1. QA de la build release en dispositivo real.
2. Política de privacidad, términos y descargo financiero.
3. Ficha de tienda, capturas e iconos.
4. Build 1.0.0 y subida a Internal testing.
4. Política de privacidad, términos y ficha de tienda.
