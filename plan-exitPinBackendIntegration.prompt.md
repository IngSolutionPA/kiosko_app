# Plan: Integrar API backend en ExitPinScreen

Reemplazar la validación local del PIN en `ExitPinScreen` por una llamada real al endpoint `POST /kiosco/login/` usando Retrofit + Coroutines. Se agrega un `ViewModel` con estados `Loading/Success/Error`, se manejan certificados SSL autofirmados y se muestran los mensajes del API directamente en la UI existente.

---

## Pasos

### 1. Agregar dependencias
En [`app/build.gradle.kts`](app/build.gradle.kts) agregar:
- Retrofit 2
- Gson converter
- OkHttp logging interceptor
- `lifecycle-viewmodel-compose`

### 2. Crear modelos de datos
Nuevo archivo `ui/screens/login/LoginModels.kt`:

```kotlin
data class LoginRequest(val pin: String, val imei: String)

data class LoginSuccessResponse(
    val titulo: String,
    val mensaje: String,
    val data: List<DeviceData>
)

data class DeviceData(
    val id: Int,
    val modelo: String,
    val numeracion: String,
    val imei: String,
    val estado: Int,
    val ubicado: String
)

data class LoginErrorResponse(
    val titulo: String,
    val mensaje: String,
    val intentos: Int,
    val max_intentos: Int
)
```

### 3. Crear `KioscoApiService`
Nuevo archivo `network/KioscoApiService.kt`:
- Interfaz Retrofit con `@POST("kiosco/login/") suspend fun login(@Body request: LoginRequest): Response<LoginSuccessResponse>`
- `RetrofitClient` singleton que configure `OkHttpClient` con:
  - `TrustManager` permisivo (o certificado pinneado) para SSL autofirmado en puerto 30443
  - `connectTimeout` y `readTimeout` de 15 segundos

Base URL: `https://api.ima.gob.pa:30443/`

### 4. Crear `ExitPinViewModel`
Nuevo archivo `ui/screens/ExitPinViewModel.kt`:
- `ViewModel` con `StateFlow<PinUiState>`
- Sealed class de estados:
  ```kotlin
  sealed class PinUiState {
      object Idle : PinUiState()
      object Loading : PinUiState()
      object Success : PinUiState()
      data class Error(val mensaje: String, val intentosRestantes: Int) : PinUiState()
  }
  ```
- Método `validatePin(pin: String, imei: String)`:
  - Llama al repositorio en `viewModelScope`
  - HTTP 200 → emite `Success`
  - HTTP 400/401 → parsea `LoginErrorResponse`, emite `Error(mensaje, max_intentos - intentos)`

### 5. Modificar `ExitPinScreen.kt`
- Recibir `viewModel: ExitPinViewModel` como parámetro
- Colectar `uiState` con `collectAsState()`
- Mostrar `CircularProgressIndicator` cuando `Loading`
- Mostrar `mensaje` y `"Quedan X intentos"` en la caja de error existente cuando `Error`
- En el botón "Autorizar": llamar `viewModel.validatePin(pin, imei)` en lugar de comparar contra `KioskConfig.exitPin`
- En estado `Success`: invocar `onPinOk()`

### 6. Actualizar `KioskScreen.kt`
- Crear el `ViewModel` con `viewModel()` y pasarlo a `ExitPinScreen`
- El callback `onExitKiosk` en `MainActivity` ya maneja `stopLockTask` — no requiere cambios adicionales

---

## Consideraciones adicionales

1. **SSL autofirmado**: Se puede usar un `TrustManager` que acepte todos los certificados (válido para intranet) o configurar un Network Security Config XML con el certificado del servidor.

2. **Timeout y reintentos**: El servidor usa puerto no estándar (30443) en red interna; configurar `connectTimeout` y `readTimeout` de 10–15 s en OkHttp para evitar que la pantalla quede bloqueada en `Loading`.

3. **`KioskConfig.exitPin` vs. API**: Decidir si se elimina la validación local por PIN hardcodeado o se mantiene como fallback offline si el servidor no responde.

---

## Archivos a crear/modificar

| Archivo | Acción |
|---|---|
| `app/build.gradle.kts` | Modificar — agregar dependencias |
| `app/src/main/java/com/example/kioskopda/network/KioscoApiService.kt` | Crear — Retrofit client + API interface |
| `app/src/main/java/com/example/kioskopda/ui/screens/login/LoginModels.kt` | Crear — modelos de datos |
| `app/src/main/java/com/example/kioskopda/ui/screens/ExitPinViewModel.kt` | Crear — ViewModel con estados |
| `app/src/main/java/com/example/kioskopda/ui/screens/ExitPinScreen.kt` | Modificar — conectar ViewModel y mostrar errores |
| `app/src/main/java/com/example/kioskopda/ui/screens/KioskScreen.kt` | Modificar — instanciar y pasar ViewModel |
| `app/src/main/res/xml/network_security_config.xml` | Crear (opcional) — config SSL para dominio |
| `app/src/main/AndroidManifest.xml` | Modificar (opcional) — referenciar networkSecurityConfig |

