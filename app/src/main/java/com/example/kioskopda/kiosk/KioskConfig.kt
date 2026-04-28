package com.example.kioskopda.kiosk

object KioskConfig {
    const val exitPin = "2580"

    val allowedPackages: Set<String> = setOf(
        "com.imaapp.proyectoappcedula", // PDA
        "com.hihonor.notepad", // Notas Honor
        "com.hihonor.camera", //Cámara
        "com.hihonor.photos", //Galería
    )
}