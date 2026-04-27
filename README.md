Comando para ejecutar la desintalacion en el shell de andorid estudio:
#esto se hace porque si ya estas en modo device owner no te pemrite desintalar

$adb = "C:\Users\lvillarreal\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb shell am broadcast -a com.example.kioskopda.CLEAR_DEVICE_OWNER -n com.example.kioskopda/.admin.AdbUnlockReceiver
& $adb shell dpm remove-active-admin com.example.kioskopda/.admin.KioskDeviceAdminReceiver
& $adb shell am force-stop com.example.kioskopda
& $adb uninstall com.example.kioskopda
