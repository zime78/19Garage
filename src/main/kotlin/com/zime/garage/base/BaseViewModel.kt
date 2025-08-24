package com.zime.garage.base

/**
 * Base interface for simple ViewModels in this Compose Desktop project.
 * We intentionally avoid AndroidX Lifecycle dependencies to keep the
 * desktop module lightweight.
 */
interface BaseViewModel {
    /**
     * Load initial data into the ViewModel's state.
     */
    fun load()
}