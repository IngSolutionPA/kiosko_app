package com.example.kioskopda.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KioskAlertDialog(
    title: String,
    message: String,
    isSuccess: Boolean = true,
    onDismiss: () -> Unit
) {
    val accentColor = if (isSuccess) Color(0xFF16A34A) else Color(0xFFE53E3E)
    val softAccent = if (isSuccess) Color(0xFFEAF7EF) else Color(0xFFFFF1F2)
    val icon = if (isSuccess) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF9963B),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Entendido",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(softAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 6.dp
    )
}