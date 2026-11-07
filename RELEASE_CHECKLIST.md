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
- [x] AAB generado (`app/build/outputs/bundle/release/app-release.aab`).
- [x] APK de release firmada (`app/build/outputs/apk/release/app-release.apk`).
- [x] Nombre visible de la app: BudJet.
- [x] Cuenta de Google Play creada y verificada.
- [ ] Inglés completo de la interfaz.
  - [x] Bloque 1: navegación, Inicio, fechas y formato de importes.
  - [x] Bloque 2: Movimientos, orden, búsqueda y selección múltiple.
  - [x] Bloque 3: Presupuestos y Análisis.
  - [x] Bloque 4: Patrimonio, deudas, plan de deudas, cartera y calculadora.
  - [x] Bloque 5: Ajustes, editor, importación, recurrentes y objetivos.
  - [x] Bloque 6: widget, notificaciones, captura rápida, bloqueo y accesibilidad.
  - [ ] Bloque 7: categorías, cuentas y datos de ejemplo por ID.
  - [ ] Traducir notas generadas, CSV y mensajes internos de validación de copias.
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

1. Bloque 7 de localización: categorías, cuentas y datos de ejemplo por ID.
2. Mantener el español como idioma base y el inglés en `values-en`.
3. QA de la build release en dispositivo real.
4. Política de privacidad, términos y ficha de tienda.
