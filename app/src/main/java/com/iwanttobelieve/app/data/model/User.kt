package com.iwanttobelieve.app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Modelo de Usuário
 * Coleção: "users" no Firestore
 */
data class User(
    @DocumentId
    val uid: String = "",

    val nome: String = "",
    val email: String = "",

    val photoUrl: String? = null,

    @ServerTimestamp
    val createdAt: Date? = null
)