package com.example.locationtrackingappv2.controller

import android.app.Activity
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.locationtrackingappv2.R
import com.example.locationtrackingappv2.domain.models.Prediction   // <-- CHECK THIS
import com.example.locationtrackingappv2.presentation.viewmodel.TripViewModel

class AutoCompleteController(
    private val context: Context,
    private val vm: TripViewModel
) {

    var onFromTextChanged: (String) -> Unit = {}
    var onToTextChanged: (String) -> Unit = {}
    var onFromSelected: (Prediction) -> Unit = {}
    var onToSelected: (Prediction) -> Unit = {}

    private val etFrom = (context as Activity).findViewById<EditText>(R.id.etFrom)
    private val etTo = context.findViewById<EditText>(R.id.etTo)

    private val fromPopup = ListPopupWindow(context).apply {
        anchorView = etFrom
        setOnItemClickListener { _, _, pos, _ ->
            val p = vm.predictions.value[pos]
            etFrom.setText(p.description)
            onFromSelected(p)
            dismiss()
        }
    }

    private val toPopup = ListPopupWindow(context).apply {
        anchorView = etTo
        setOnItemClickListener { _, _, pos, _ ->
            val p = vm.predictions.value[pos]
            etTo.setText(p.description)
            onToSelected(p)
            dismiss()
        }
    }

    init {
        etFrom.addTextChangedListener { onFromTextChanged(it.toString()) }
        etTo.addTextChangedListener { onToTextChanged(it.toString()) }
    }

    fun showPredictions(preds: List<Prediction>) {
        if (preds.isEmpty()) {
            fromPopup.dismiss()
            toPopup.dismiss()
            return
        }

        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_list_item_1,
            preds.map { it.description }
        )

        when {
            etFrom.isFocused -> {
                fromPopup.setAdapter(adapter)
                fromPopup.show()
            }
            etTo.isFocused -> {
                toPopup.setAdapter(adapter)
                toPopup.show()
            }
        }
    }

    fun showEvent(event: TripViewModel.UiEvent) {
        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
    }
}
