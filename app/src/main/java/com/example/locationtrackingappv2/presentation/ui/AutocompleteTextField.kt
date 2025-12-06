package com.example.locationtrackingappv2.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.locationtrackingappv2.presentation.viewmodel.PlaceSuggestionUi

@Composable
fun AutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<PlaceSuggestionUi>,
    onSuggestionSelected: (PlaceSuggestionUi) -> Unit,
    placeholder: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = it.isNotBlank()
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) }
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
