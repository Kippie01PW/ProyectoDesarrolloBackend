# Microservicio de seguridad

Servicio Spring Boot para usuarios, roles, permisos, sesiones y autenticación.

## Endpoints de autenticación

- `POST /api/auth/register` o `POST /api/users`: registro local.
- `POST /api/auth/login`: login con correo y contraseña.
- `POST /api/auth/2fa/verify`: valida un desafío 2FA por correo.
- `PUT /api/auth/2fa-settings`: activa o desactiva 2FA.
- `POST /api/auth/oauth`: login con un ID token de Firebase.
- `GET /api/auth/me`: devuelve el usuario, roles y permisos actuales.
- `POST /api/auth/logout`: revoca la sesión y el JWT presentado.
- `POST /api/auth/password-recovery`: solicita recuperación sin revelar si el correo existe.
- `POST /api/auth/password-reset`: cambia la contraseña usando un token de un solo uso.

Las sesiones se registran en la base de datos. Un JWT firmado no es suficiente:
también debe corresponder a una sesión activa y no expirada.

## Configuración

Define `JWT_SECRET` con al menos 32 bytes. Para MySQL se usan las propiedades
`spring.datasource.*` de `src/main/resources/application.properties`.

El correo para 2FA y recuperación se configura mediante:

```text
EMAIL_SERVICE_URL=http://localhost:5000
EMAIL_SERVICE_API_KEY=...
```

Para OAuth con Firebase se requiere `FIREBASE_PROJECT_ID`. El backend verifica
la firma RS256 y las claims del ID token usando las claves públicas de Firebase;
no se debe guardar una cuenta de servicio en el repositorio.

## Validación

```powershell
.\mvnw.cmd test
```

Las pruebas usan H2 en memoria y no requieren MySQL ni un servicio de correo real.
