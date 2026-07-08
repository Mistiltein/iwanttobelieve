package com.iwanttobelieve.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iwanttobelieve.app.data.model.User
import kotlinx.coroutines.tasks.await

/**
 * Repositório responsável por toda a lógica de Autenticação + criação do perfil no Firestore
 */
class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Login com email e senha
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Usuário não encontrado"))

            // Busca os dados completos do usuário no Firestore
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = userDoc.toObject(User::class.java) ?: User(
                uid = firebaseUser.uid,
                nome = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: ""
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cadastro + criação automática do documento na coleção "users"
     */
    suspend fun register(nome: String, email: String, password: String): Result<User> {
        return try {
            // 1. Cria usuário no Firebase Authentication
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Falha ao criar usuário"))

            // 2. Cria o documento do perfil na coleção "users" do Firestore
            val user = User(
                uid = firebaseUser.uid,
                nome = nome,
                email = email
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Faz logout do usuário
     */
    fun logout() {
        auth.signOut()
    }

}