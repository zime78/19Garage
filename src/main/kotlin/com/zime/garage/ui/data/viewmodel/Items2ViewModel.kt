package com.zime.garage.ui.data.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.zime.garage.base.BaseViewModel
import com.zime.garage.ui.data.model.Items2Model

/**
 * MVVM ViewModel for Items2 management.
 * Wraps Items2Model (data layer) and exposes Compose-friendly state.
 */
class Items2ViewModel : BaseViewModel {
    private val model = Items2Model()

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
