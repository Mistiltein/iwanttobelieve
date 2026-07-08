package com.iwanttobelieve.app.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val currentUser = auth.currentUser

    var userName by remember { mutableStateOf("Carregando...") }
    var userEmail by remember { mutableStateOf(currentUser?.email ?: "") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Carrega dados do usuário do Firestore
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                userName = doc.getString("nome") ?: "Nome não encontrado"
                photoUrl = doc.getString("photoUrl")
            } catch (e: Exception) {
                userName = "Erro ao carregar dados"
            }
        }
    }

    // Launcher para escolher foto
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isUploading = true
                try {
                    // Upload da foto
                    val ref = storage.reference
                        .child("profile_images/${currentUser?.uid}/${UUID.randomUUID()}.jpg")

                    val uploadTask = ref.putFile(it).await()
                    val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

                    // Atualiza no Firestore
                    currentUser?.uid?.let { uid ->
                        firestore.collection("users").document(uid)
                            .update("photoUrl", downloadUrl)
                            .await()
                    }

                    photoUrl = downloadUrl
                    message = "Foto de perfil atualizada com sucesso!"
                } catch (e: Exception) {
                    message = "Erro ao atualizar foto: ${e.message}"
                } finally {
                    isUploading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Meu Perfil", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(24.dp))

        // Foto de perfil
        AsyncImage(
            model = photoUrl ?: "https://via.placeholder.com/150",
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { imagePicker.launch("image/*") },
            enabled = !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Alterar Foto de Perfil")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nome", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(userName, style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(16.dp))

                Text("E-mail", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(userEmail, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sair da Conta (Logout)")
        }

        message?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}