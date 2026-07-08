package com.iwanttobelieve.app.util

fun getFriendlyErrorMessage(exception: Throwable?): String {
    return when {
        exception?.message?.contains("network", ignoreCase = true) == true ->
            "Sem conexão com a internet. Verifique sua rede."
        exception?.message?.contains("permission", ignoreCase = true) == true ->
            "Permissão negada. Verifique as configurações do app."
        exception?.message?.contains("storage", ignoreCase = true) == true ->
            "Erro ao enviar imagem. Tente novamente."
        else -> exception?.message ?: "Ocorreu um erro inesperado."
    }
}