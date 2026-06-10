package com.example.tvmaze.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvmaze.data.model.Show

@Composable
fun DetailScreen(
    showId: Int,
    viewModel: DetailViewModel,
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFavourite by viewModel.isFavourite.collectAsState()

    // Загружаем детали при первом запуске
    LaunchedEffect(showId) {
        viewModel.loadShowDetails(showId)
    }

    when (uiState) {
        is DetailUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is DetailUiState.Error -> {
            val errorState = uiState as DetailUiState.Error
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = errorState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(onClick = { viewModel.retry(showId) }) {
                        Text("Повторить")
                    }
                }
            }
        }

        is DetailUiState.Success -> {
            val show = (uiState as DetailUiState.Success).show
            DetailContent(
                show = show,
                isFavourite = isFavourite,
                onFavouriteClick = { viewModel.toggleFavourite(show) },
                onBackClick = onBackClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailContent(
    show: Show,
    isFavourite: Boolean,
    onFavouriteClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        show.name,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text(
                            text = "←",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onFavouriteClick) {
                        Icon(
                            imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                            contentDescription = if (isFavourite) "Удалить из избранного" else "Добавить в избранное",
                            tint = if (isFavourite) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                // Убрали colors - используем стандартные цвета
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Жанры
            if (show.genres.isNotEmpty()) {
                DetailInfoRow("Жанры", show.genres.joinToString(", "))
            }

            // Рейтинг
            if (show.rating?.average != null) {
                DetailInfoRow("Рейтинг", "${show.rating.average}/10")
            }

            // Статус
            if (show.status != null) {
                DetailInfoRow("Статус", show.status)
            }

            // Язык
            if (show.language != null) {
                DetailInfoRow("Язык", show.language)
            }

            // Дата премьеры
            if (show.premiered != null) {
                DetailInfoRow("Премьера", show.premiered)
            }

            // Дата окончания
            if (show.ended != null) {
                DetailInfoRow("Окончание", show.ended)
            }

            // Официальный сайт
            if (show.officialSite != null) {
                DetailInfoRow("Официальный сайт", show.officialSite)
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Описание
            Text(
                text = "Описание",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            val cleanSummary = show.summary?.replace(Regex("<[^>]*>"), "") ?: "Нет описания"
            Text(
                text = cleanSummary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp
        )
    }
}