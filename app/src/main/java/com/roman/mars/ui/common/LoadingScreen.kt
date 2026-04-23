package com.roman.mars.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roman.mars.R
import com.roman.mars.ui.theme.MarsColors

@Composable
fun LoadingScreen(
    text: String = "Подключаемся к Марсу…"
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.mars_bg),
            contentDescription = "Фон Mars",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MarsColors.OverlaySoft,
                            MarsColors.OverlayStrong
                        )
                    )
                )
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MARS",
                color = MarsColors.AccentBright,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(18.dp))
            CircularProgressIndicator(
                color = MarsColors.Accent,
                trackColor = MarsColors.TextMuted.copy(alpha = 0.25f)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = text,
                color = MarsColors.TextSecondary,
                fontSize = 15.sp
            )
        }
    }
}