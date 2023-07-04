package dev.medzik.librepass.android.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

object Remember {
    /**
     * Remember Snackbar Host State.
     */
    @Composable
    fun rememberSnackbarHostState() = remember { SnackbarHostState() }

    /**
     * Remember loading state.
     */
    @Composable
    fun rememberLoadingState(value: Boolean = false) = remember { mutableStateOf(value) }

    /**
     * Remember string data (Default value is empty string).
     */
    @Composable
    fun rememberStringData(value: String = "") = remember { mutableStateOf(value) }
}
