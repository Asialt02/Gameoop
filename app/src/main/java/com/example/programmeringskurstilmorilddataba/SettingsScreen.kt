package com.example.programmeringskurstilmorilddataba

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.programmeringskurstilmorilddataba.ui.theme.InfoCardColor
import com.example.programmeringskurstilmorilddataba.ui.theme.PrimaryPurple

@Composable
fun SettingsScreen(
    navController: NavController = rememberNavController(),
) {

    val changeNameRoute = stringResource(R.string.change_display_name_route)
    val changePasswordRoute = stringResource(R.string.change_password_route)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(PrimaryPurple)
                .padding(16.dp)
                .padding(vertical = 32.dp)
        ) {
            SettingsScreenElement(
                label = "Change username",
                onClick = { navController.navigate(changeNameRoute) }
            )
            SettingsScreenElement(
                label = "Change password",
                onClick = { navController.navigate(changePasswordRoute) }
            )
        }
        BottomNavBar(navController)
    }
}

@Composable
fun SettingsScreenElement(
    label: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(InfoCardColor)
            .padding(16.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineMedium,
            color = PrimaryPurple
        )
        Text(
            text = "»",
            style = MaterialTheme.typography.headlineMedium,
            color = PrimaryPurple
        )
    }
}

@Preview
@Composable
fun SettingsPreview() {
    SettingsScreenElement("Change Username")
}