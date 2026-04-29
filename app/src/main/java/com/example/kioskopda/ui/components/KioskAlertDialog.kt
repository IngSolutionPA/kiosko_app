package com.example.kioskopda.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KioskAlertDialog(
    title: String,
    message: String,
    isSuccess: Boolean = true,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSuccess) Color(0xFFF9963B) else Color(0xFFF9963B)
                )
            ) {
                Text(
                    text = "Entendido",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF222222)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color(0xFF555555)
                )
            }
        },
        containerColor = Color(0xFFF3F3F3),
        shape = RoundedCornerShape(22.dp)
    )
}