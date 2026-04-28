package com.example.kioskopda.ui.utils

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast

enum class KioskShortcutType {
    PDA,
    CAMERA,
    GALLERY,
    NOTES
}

fun openKioskShortcut(
    context: Context,
    shortcut: KioskShortcutType
) {
    when (shortcut) {
        KioskShortcutType.PDA -> {
            openAppByPackage(
                context,
                packageName = "com.imaapp.proyectoappcedula",
                errorMessage = "No se encontró la app PDA"
            )
        }

        KioskShortcutType.CAMERA -> {
            openAppByPackage(
               context = context,
               packageName = "com.hihonor.camera",
               errorMessage = "No se encontró la cámara"
            )
        }

        KioskShortcutType.GALLERY -> {
            openAppByPackage(
                context = context,
                packageName = "com.hihonor.photos",
                errorMessage = "No se encontró la galería"
            )
        }

        KioskShortcutType.NOTES -> {
            openAppByPackage(
                context,
                packageName = "com.hihonor.notepad",
                errorMessage = "No se encontró la app de notas"
            )
        }
    }
}

private fun openAppByPackage(
    context: Context,
    packageName: String,
    errorMessage: String
) {
    val intent = context.packageManager
        .getLaunchIntentForPackage(packageName)
        ?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    if (intent != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }
}

