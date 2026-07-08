package com.iwanttobelieve.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iwanttobelieve.app.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreatePostViewModel(
    private val repository: PostRepository = PostRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreatePostUiState>(CreatePostUiState.Idle)
    val uiState: StateFlow<CreatePostUiState> = _uiState

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    fun selectImage(uri: Uri) {
        _selectedImageUri.value = uri
    }

    fun clearSelectedImage() {
        _selectedImageUri.value = null
    }

    fun createPost(descricao: String) {
        val imageUri = _selectedImageUri.value
        if (descricao.isBlank() || imageUri == null) {
            _uiState.value = CreatePostUiState.Error("Descrição e imagem são obrigatórias")
            return
        }

        _uiState.value = CreatePostUiState.Loading

        viewModelScope.launch {
            val result = repository.createPost(descricao, imageUri)

            result.onSuccess {
                _uiState.value = CreatePostUiState.Success
                clearSelectedImage()
            }.onFailure { e ->
                _uiState.value = CreatePostUiState.Error(e.message ?: "Erro ao publicar")
            }
        }
    }

    fun resetState() {
        _uiState.value = CreatePostUiState.Idle
    }
}

sealed class CreatePostUiState {
    object Idle : CreatePostUiState()
    object Loading : CreatePostUiState()
    object Success : CreatePostUiState()
    data class Error(val message: String) : CreatePostUiState()
}