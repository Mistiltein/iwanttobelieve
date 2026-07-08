package com.iwanttobelieve.app.ui.screens.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.iwanttobelieve.app.util.getFriendlyErrorMessage
import com.iwanttobelieve.app.viewmodel.CreatePostUiState
import com.iwanttobelieve.app.viewmodel.CreatePostViewModel
import kotlinx.coroutines.launch

@Composable
fun CreatePostScreen(
    onPostCreated: () -> Unit,
    viewModel: CreatePostViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var descricao by remember { mutableStateOf("") }

    // Launcher para escolher imagem
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectImage(it) }
    }

    // Observa o estado para mostrar Snackbar e navegar
    LaunchedEffect(uiState) {
        if (uiState is CreatePostUiState.Success) {
            onPostCreated()
            viewModel.resetState()
        }

        if (uiState is CreatePostUiState.Error) {
            scope.launch {
                val errorMessage = getFriendlyErrorMessage(
                    Exception((uiState as CreatePostUiState.Error).message)
                )
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    actionLabel = "OK",
                    duration = SnackbarDuration.Long
                )
                viewModel.resetState()
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Nova Publicação", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("O que você quer compartilhar?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Selecionar Imagem da Galeria")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preview da imagem
            selectedImageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Imagem selecionada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )

                TextButton(onClick = { viewModel.clearSelectedImage() }) {
                    Text("Remover imagem")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.createPost(descricao) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreatePostUiState.Loading && selectedImageUri != null
            ) {
                if (uiState is CreatePostUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Publicar")
                }
            }
        }
    }
}