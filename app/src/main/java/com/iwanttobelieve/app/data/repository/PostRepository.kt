package com.iwanttobelieve.app.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.iwanttobelieve.app.data.model.Post
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Repositório de Publicações (Posts)
 * Inclui upload de imagem + persistência no Firestore
 */
class PostRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val postsCollection = firestore.collection("posts")

    /**
     * Cria uma nova publicação com imagem
     * (Requisito principal do trabalho)
     */
    suspend fun createPost(descricao: String, imageUri: Uri): Result<Post> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("Usuário não autenticado"))

            // 1. Faz upload da imagem para o Firebase Storage
            val imageRef = storage.reference
                .child("posts/${currentUser.uid}/${UUID.randomUUID()}.jpg")

            val uploadTask = imageRef.putFile(imageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

            // 2. Pega o nome do autor (denormalizado)
            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()
            val nomeAutor = userDoc.getString("nome") ?: "Usuário"

            // 3. Monta o objeto Post
            val post = Post(
                uid = currentUser.uid,
                nomeAutor = nomeAutor,
                descricao = descricao,
                imageUrl = downloadUrl
            )

            // 4. Salva no Firestore
            val documentRef = postsCollection.add(post).await()

            Result.success(post.copy(id = documentRef.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Busca todas as publicações (da mais nova para a mais antiga)
     */
    suspend fun getAllPosts(): Result<List<Post>> {
        return try {
            val snapshot = postsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val posts = snapshot.toObjects(Post::class.java)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listener em tempo real para o Feed
     */
    fun listenToPosts(
        onUpdate: (List<Post>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.toObjects(Post::class.java) ?: emptyList()
                onUpdate(posts)
            }
    }

    /**
     * Deleta uma publicação (apenas o autor pode deletar)
     */
    suspend fun deletePost(postId: String): Result<Boolean> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuário não autenticado"))

            val postDoc = postsCollection.document(postId).get().await()
            val post = postDoc.toObject(Post::class.java)

            if (post?.uid != currentUserId) {
                return Result.failure(Exception("Você não tem permissão para deletar esta publicação"))
            }

            postsCollection.document(postId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}