package com.example.locationtrackingappv2.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.locationtrackingappv2.presentation.viewmodel.PlaceSuggestionUi

@Composable
fun OriginField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<PlaceSuggestionUi>,
    onSuggestionSelected: (PlaceSuggestionUi) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onClear: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = it.isNotBlank()
            },
            placeholder = { Text("Origin") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Row {
                    // Clear button
                    if (value.isNotEmpty()) {
                        IconButton(onClick = {
                            onClear()
                            expanded = false
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }

                    // Use Current Location
                    IconButton(onClick = onUseCurrentLocation) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Use current location")
                    }
                }
            }
        )

        if (expanded && suggestions.isNotEmpty()) {
            LazyColumn {
                items(suggestions.size) { i ->
                    val s = suggestions[i]
                    Text(
                        s.description,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSuggestionSelected(s)
                                expanded = false
                            }
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}
