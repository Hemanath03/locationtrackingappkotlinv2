package com.example.locationtrackingappv2.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.locationtrackingappv2.presentation.viewmodel.PlaceSuggestionUi

@Composable
fun AutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<PlaceSuggestionUi>,
    onSuggestionSelected: (PlaceSuggestionUi) -> Unit,
    placeholder: String,
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
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.White,
                focusedContainerColor = Color.DarkGray,
                unfocusedContainerColor = Color(0xFF3A3A3A),
                disabledContainerColor = Color(0xFF3A3A3A),
                focusedIndicatorColor = Color.DarkGray,
                unfocusedIndicatorColor = Color.DarkGray,
                disabledIndicatorColor = Color.DarkGray,
                focusedPlaceholderColor = Color.White,
                unfocusedPlaceholderColor = Color.White,
                disabledPlaceholderColor = Color.DarkGray,
                focusedTrailingIconColor = Color.White,
                unfocusedTrailingIconColor = Color.White,
                disabledTrailingIconColor = Color.White
            ),
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = {
                        onClear()
                        expanded = false
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color.White
                        )
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
