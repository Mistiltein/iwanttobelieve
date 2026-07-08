package com.iwanttobelieve.app.ui.screens.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.iwanttobelieve.app.data.model.Post
import com.iwanttobelieve.app.viewmodel.FeedUiState
import com.iwanttobelieve.app.viewmodel.FeedViewModel
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    viewModel: FeedViewModel = viewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Estados do diálogo de exclusão
    var showDeleteDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<Post?>(null) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreatePost,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Publicação")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is FeedUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is FeedUiState.Empty -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Nenhuma publicação ainda")
                        Text("Seja o primeiro a postar!")
                    }
                }

                is FeedUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(posts) { post ->
                            PostItem(
                                post = post,
                                onDeleteClick = {
                                    postToDelete = post
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }

                is FeedUiState.Error -> {
                    // Erro será mostrado via Snackbar (abaixo)
                }
            }
        }
    }

    // === Snackbar para mostrar erros ===
    LaunchedEffect(uiState) {
        if (uiState is FeedUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = (uiState as FeedUiState.Error).message,
                    actionLabel = "OK",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    // === AlertDialog de Confirmação para deletar ===
    if (showDeleteDialog && postToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Publicação") },
            text = { Text("Tem certeza que deseja excluir esta publicação? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        postToDelete?.let { post ->
                            viewModel.deletePost(post.id)
                        }
                        showDeleteDialog = false
                        postToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PostItem(
    post: Post,
    onDeleteClick: () -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isAuthor = post.uid == currentUserId

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.nomeAutor,
                    style = MaterialTheme.typography.titleMedium
                )

                if (isAuthor) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Deletar publicação",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (post.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentScale = ContentScale.Crop
                )
            }

            if (post.descricao.isNotBlank()) {
                Text(
                    text = post.descricao,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}