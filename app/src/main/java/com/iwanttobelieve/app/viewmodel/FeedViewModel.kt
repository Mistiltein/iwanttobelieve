package com.iwanttobelieve.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iwanttobelieve.app.data.model.Post
import com.iwanttobelieve.app.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FeedViewModel(
    private val repository: PostRepository = PostRepository()
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState

    init {
        // Listener em tempo real (melhor experiência)
        repository.listenToPosts(
            onUpdate = { postList ->
                _posts.value = postList
                _uiState.value = if (postList.isEmpty()) FeedUiState.Empty else FeedUiState.Success
            },
            onError = { exception ->
                _uiState.value = FeedUiState.Error(exception.message ?: "Erro ao carregar publicações")
            }
        )
    }

    fun refreshPosts() {
        _uiState.value = FeedUiState.Loading
        viewModelScope.launch {
            val result = repository.getAllPosts()
            result.onSuccess { list ->
                _posts.value = list
                _uiState.value = if (list.isEmpty()) FeedUiState.Empty else FeedUiState.Success
            }.onFailure { e ->
                _uiState.value = FeedUiState.Error(e.message ?: "Erro ao atualizar")
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val result = repository.deletePost(postId)
            result.onFailure { exception ->
                println("Erro ao deletar: ${exception.message}")
            }
        }
    }
}

sealed class FeedUiState {
    object Loading : FeedUiState()
    object Success : FeedUiState()
    object Empty : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}