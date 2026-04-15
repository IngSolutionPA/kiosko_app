# KioskoPDA - Modo kiosko

Esta app implementa un flujo de kiosko con dos niveles:

- **Completo (recomendado)**: `Device Owner` + `Lock Task`.
- **Limitado**: inmersivo + bloqueo de back dentro de la actividad.

## Lo que ya hace

- Lista solo apps permitidas por whitelist en `KioskConfig.allowedPackages`.
- Intenta mantener pantalla en modo inmersivo (sin barra de estado/navegacion).
- Consume `back` para evitar cierre de actividad.
- Pide PIN para salir de `Lock Task`.
- Se auto-lanza al iniciar el equipo por `BOOT_COMPLETED`.

## Archivos clave

- `app/src/main/java/com/example/kioskopda/MainActivity.kt`
- `app/src/main/java/com/example/kioskopda/kiosk/KioskManager.kt`
- `app/src/main/java/com/example/kioskopda/kiosk/KioskConfig.kt`
- `app/src/main/java/com/example/kioskopda/admin/KioskDeviceAdminReceiver.kt`
- `app/src/main/java/com/example/kioskopda/boot/BootReceiver.kt`
- `app/src/main/res/xml/device_admin_receiver.xml`

## Provisionar Device Owner (obligatorio para bloqueo fuerte)

> Requiere dispositivo reseteado de fabrica y sin cuentas configuradas.

1. Instala la app en el dispositivo.
2. Ejecuta el siguiente comando ADB:

```powershell
adb shell dpm set-device-owner com.example.kioskopda/.admin.KioskDeviceAdminReceiver
```

3. Abre la app y verifica estado de kiosko completo.

## Configuracion rapida

- PIN de salida: `KioskConfig.exitPin`.
- Apps permitidas: `KioskConfig.allowedPackages`.

## Notas importantes

- Sin Device Owner, Android no garantiza bloquear completamente ajustes/notificaciones/home en todos los equipos.
- Para endurecer mas, agrega restricciones adicionales en `KioskManager.applyKioskPolicies`.

