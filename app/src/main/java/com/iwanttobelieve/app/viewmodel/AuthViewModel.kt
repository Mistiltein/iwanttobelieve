package com.iwanttobelieve.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iwanttobelieve.app.data.model.User
import com.iwanttobelieve.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsável pelo fluxo de Autenticação
 */
class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        // Verifica se já existe usuário logado ao iniciar o ViewModel
        if (repository.isUserLoggedIn()) {
            // Você pode buscar os dados do Firestore aqui se quiser
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Preencha todos os campos")
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val result = repository.login(email, password)

            result.onSuccess { user ->
                _currentUser.value = user
                _uiState.value = AuthUiState.Success(user)
            }.onFailure { exception ->
                _uiState.value = AuthUiState.Error(exception.message ?: "Erro ao fazer login")
            }
        }
    }

    fun register(nome: String, email: String, password: String) {
        if (nome.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Preencha todos os campos")
            return
        }

        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("A senha deve ter pelo menos 6 caracteres")
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val result = repository.register(nome, email, password)

            result.onSuccess { user ->
                _currentUser.value = user
                _uiState.value = AuthUiState.Success(user)
            }.onFailure { exception ->
                _uiState.value = AuthUiState.Error(exception.message ?: "Erro ao criar conta")
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

/**
 * Estados da UI de Autenticação
 */
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}