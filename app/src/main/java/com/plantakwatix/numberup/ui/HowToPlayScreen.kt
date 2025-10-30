package com.plantakwatix.numberup.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.plantakwatix.numberup.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToPlayScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.how_to_play_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_button_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InstructionPoint(
                number = "1.",
                text = stringResource(id = R.string.how_to_play_point_1)
            )

            InstructionPoint(
                number = "2.",
                text = stringResource(id = R.string.how_to_play_point_2)
            )

            InstructionPoint(
                number = "3.",
                text = stringResource(id = R.string.how_to_play_point_3)
            )

            InstructionPoint(
                number = "4.",
                text = stringResource(id = R.string.how_to_play_point_4)
            )

            InstructionPoint(
                number = "5.",
                text = stringResource(id = R.string.how_to_play_point_5)
            )

            InstructionPoint(
                number = "6.",
                text = stringResource(id = R.string.how_to_play_point_6)
            )

            InstructionPoint(
                number = "7.",
                text = stringResource(id = R.string.how_to_play_point_7)
            )

            SectionTitle(title = stringResource(id = R.string.how_to_play_section_sixth_square))

            InstructionPoint(
                number = "8.",
                text = stringResource(id = R.string.how_to_play_point_8)
            )

            InstructionPoint(
                number = "9.",
                text = stringResource(id = R.string.how_to_play_point_9)
            )

            InstructionPoint(
                number = "10.",
                text = stringResource(id = R.string.how_to_play_point_10)
            )


            SectionTitle(title = stringResource(id = R.string.how_to_play_section_end_game))

            InstructionPoint(
                number = "11.",
                text = stringResource(id = R.string.how_to_play_point_11)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.good_luck_message),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun InstructionPoint(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = number,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(30.dp) // Pour l'alignement
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f) // Pour que le texte prenne le reste de la place
        )
    }
}