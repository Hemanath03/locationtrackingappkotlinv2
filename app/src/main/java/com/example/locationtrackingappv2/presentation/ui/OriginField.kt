package com.example.locationtrackingappv2.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.White,
                focusedContainerColor = Color.DarkGray,
                unfocusedContainerColor = Color(0xFF3A3A3A),
                disabledContainerColor = Color(0xFF3A3A3A),
                focusedIndicatorColor = Color.Gray,
                unfocusedIndicatorColor = Color.DarkGray,
                disabledIndicatorColor = Color.DarkGray,
                focusedPlaceholderColor = Color.White,
                unfocusedPlaceholderColor = Color.White,
                disabledPlaceholderColor = Color.White,
                focusedLeadingIconColor = Color.DarkGray,
                unfocusedLeadingIconColor = Color.DarkGray,
                disabledLeadingIconColor = Color.DarkGray,
                focusedTrailingIconColor = Color.DarkGray,
                unfocusedTrailingIconColor = Color.DarkGray,
                disabledTrailingIconColor = Color.DarkGray
            ),
            trailingIcon = {
                Row {
                    // Clear button
                    if (value.isNotEmpty()) {
                        IconButton(onClick = {
                            onClear()
                            expanded = false
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                        }
                    }

                    // Use Current Location
                    IconButton(onClick = onUseCurrentLocation) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Use current location", tint = Color.White)
                    }
                }
            }
        )

        if (expanded && suggestions.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .background(Color(0xFF3A3A3A))
            ) {
                items(suggestions) { s ->
                    Text(
                        text = s.description,
                        color = Color.White,
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
