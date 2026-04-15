package com.example.kioskopda.kiosk

object KioskConfig {
    // Cambia este PIN antes de producción.
    const val exitPin = "2580"

    // Solo estas apps podrán abrirse dentro del kiosko cuando haya Device Owner.
    // Agrega paquetes de apps permitidas para tu operación.
    val allowedPackages: Set<String> = setOf(
        "com.imaapp.proyectoappcedula"
    )
}

