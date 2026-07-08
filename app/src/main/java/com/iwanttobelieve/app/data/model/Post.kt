package com.iwanttobelieve.app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Modelo de Publicação (Post)
 * Coleção: "posts" no Firestore
 */
data class Post(
    @DocumentId
    val id: String = "",

    val uid: String = "",           // ID do autor
    val nomeAutor: String = "",     // Nome do autor (denormalizado para facilitar exibição)

    val descricao: String = "",
    val imageUrl: String = "",

    @ServerTimestamp
    val timestamp: Date? = null
)