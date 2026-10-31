# microservicio_seguridad

## Autenticación

Los endpoints de autenticación están disponibles bajo `/api/auth`:

- `POST /register` registra usuarios locales con contraseña segura.
- `POST /login` autentica con correo y contraseña. Si el usuario tiene 2FA, devuelve `requiresTwoFactor` y `challengeToken`.
- `POST /2fa/verify` valida el código enviado por correo y activa la sesión.
- `POST /oauth/firebase` valida un ID token emitido por Firebase (Google, GitHub, Microsoft u otro proveedor configurado en Firebase).
- `POST /password-recovery` y `POST /password-reset` gestionan la recuperación de usuarios locales.
- `POST /logout` invalida la sesión interna asociada al Bearer token.
- `PUT /2fa` habilita o deshabilita 2FA para el usuario autenticado.

Configura `FIREBASE_ENABLED=true` y `FIREBASE_SERVICE_ACCOUNT_JSON` con las credenciales del proyecto Firebase y
`NOTIFICATION_SERVICE_URL` con la URL del monolito de notificaciones. Por defecto se usa
`http://localhost:5000/send-email`, cuyo contrato recibe `{ "to": "...", "text": "..." }`.
También se pueden ajustar `TWO_FACTOR_CODE_DURATION_MINUTES` y
`PASSWORD_RESET_DURATION_MINUTES`.