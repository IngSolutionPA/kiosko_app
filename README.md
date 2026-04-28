# COMANDOS IMPORTANTES PARA EL PROYECTO


> [!IMPORTANT]
>**COMANDO PARA EJECUTAR LA DESINSTALACION EN EL SHELL DE ANDROID STUDIO:**
> 

_#esto se hace porque si ya estas en modo device owner no te pemrite desintalar_

```
$adb = "C:\Users\lvillarreal\AppData\Local\Android\Sdk\platform-tools\adb.exe"
```
```
$adb shell am broadcast -a com.example.kioskopda.CLEAR_DEVICE_OWNER -n com.example.kioskopda/.admin.AdbUnlockReceiver
```
```
$adb shell dpm remove-active-admin com.example.kioskopda/.admin.KioskDeviceAdminReceiver
```
```
$adb shell am force-stop com.example.kioskopda
```
```
$adb uninstall com.example.kioskopda
```


> [!IMPORTANT]
>**COMANDO PARA HABILITAR EL MODO DEVICE OWNER**

```
$adb = "C:\Users\lvillarreal\AppData\Local\Android\Sdk\platform-tools\adb.exe"
```
```
$adb shell dpm set-device-owner com.example.kioskopda/.admin.KioskDeviceAdminReceiver
```
> [!IMPORTANT]
>**COMANDO PARA DESHABILITAR EL MODO DEVICE OWNER**

```
$adb = "C:\Users\lvillarreal\AppData\Local\Android\Sdk\platform-tools\adb.exe"
```
```
$adb shell dpm remove-active-admin com.example.kioskopda/.admin.KioskDeviceAdminReceiver
```
