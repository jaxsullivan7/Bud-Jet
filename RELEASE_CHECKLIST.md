# BudJet · checklist de publicación

## Decisiones cerradas

- **Nombre:** BudJet
- **Package:** `com.budjet.app`
- **Modelo:** gratis, sin anuncios
- **Idiomas:** español + inglés
- **Soporte:** newprojectsontheline@gmail.com
- **Legal:** New Project · España
- **Analítica:** ninguna en v1.0; solo Play Console Vitals

## Estado técnico

- [x] Build de release con R8 y `shrinkResources`.
- [x] Firma de release configurada con un keystore de subida.
- [x] AAB generado (`app/build/outputs/bundle/release/app-release.aab`).
- [x] APK de release firmada (`app/build/outputs/apk/release/app-release.apk`).
- [x] Nombre visible de la app: BudJet.
- [ ] Cuenta de Google Play creada y verificada.
- [ ] Inglés completo de la interfaz.
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

1. Crear la cuenta de Google Play con `newprojectsontheline@gmail.com`.
2. Elegir cuenta personal, pagar la cuota única y verificar identidad.
3. Configurar el nombre público del desarrollador: **New Project**.
4. Avisar cuando la cuenta esté verificada para subir el AAB a Internal testing.
