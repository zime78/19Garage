package com.zime.garage.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.zime.garage.db.viewModel.Items3Model

/**
 * MVVM ViewModel for Items3 management.
 * Wraps Items3Model (data layer) and exposes Compose-friendly state.
 */
class Items3ViewModel : BaseViewModel {
    private val model = Items3Model()

    // Observable state for UI
    val classifications: SnapshotStateList<String> = mutableStateListOf()

    override fun load() {
        refresh()
    }

    fun refresh() {
        val list = model.loadClassificationFile().map { it.type }
        classifications.clear()
        classifications.addAll(list)
    }

    fun add(newType: String): Boolean {
        val success = model.addClassificationType(newType)
        if (success) refresh()
        return success
    }

    fun update(oldType: String, newType: String): Boolean {
        val success = model.updateClassificationType(oldType, newType)
        if (success) refresh()
        return success
    }

    fun delete(type: String): Boolean {
        val success = model.deleteClassificationType(type)
        if (success) refresh()
        return success
    }
}
