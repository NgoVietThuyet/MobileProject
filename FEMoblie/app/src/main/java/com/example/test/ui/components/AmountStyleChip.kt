package com.example.test.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) Color(0xFF4C6FFF) else Color.White,
        border = BorderStroke(1.dp, if (selected) Color(0xFF4C6FFF) else Color(0xFFE3E3E7)),
        modifier = Modifier
            .height(28.dp)
            .clickable { onClick() }
    ) {
        Box(Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
            Text(label, fontSize = 12.sp, color = if (selected) Color.White else Color(0xFF333333))
        }
    }
}
