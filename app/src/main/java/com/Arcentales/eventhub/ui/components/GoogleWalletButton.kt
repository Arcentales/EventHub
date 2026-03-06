package com.Arcentales.eventhub.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Arcentales.eventhub.ui.theme.Navy900

@Composable
fun GoogleWalletButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Button(
        onClick        = onClick,
        modifier       = modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(12.dp),
        colors         = ButtonDefaults.buttonColors(containerColor = Navy900),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(18.dp),
                color       = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector        = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier           = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text       = "Add to Google Wallet",
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp
            )
        }
    }
}