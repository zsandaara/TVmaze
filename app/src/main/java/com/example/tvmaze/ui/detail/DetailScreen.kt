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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tvmaze.data.model.Show

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    showId: Int,
    viewModel: DetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFavourite by viewModel.isFavourite.collectAsState()

    LaunchedEffect(showId) {
        viewModel.loadShowDetails(showId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("detail_screen")
    ) {
        when (uiState) {
            is DetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("detail_loading"),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DetailUiState.Error -> {
                val errorState = uiState as DetailUiState.Error
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("detail_error"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorState.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(
                            onClick = { viewModel.retry(showId) },
                            modifier = Modifier.testTag("detail_retry_button")
                        ) {
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
        modifier = Modifier.testTag("detail_screen_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        show.name,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("detail_title")
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
                    IconButton(
                        onClick = onFavouriteClick,
                        modifier = Modifier.testTag("favourite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                            contentDescription = if (isFavourite) "Удалить из избранного" else "Добавить в избранное",
                            tint = if (isFavourite) Color.Red else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("favourite_icon")
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("detail_content"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (show.genres.isNotEmpty()) {
                DetailInfoRow(
                    label = "Жанры",
                    value = show.genres.joinToString(", "),
                    tag = "detail_genres"
                )
            }

            if (show.rating?.average != null) {
                DetailInfoRow(
                    label = "Рейтинг",
                    value = "${show.rating.average}/10",
                    tag = "detail_rating"
                )
            }

            if (show.status != null) {
                DetailInfoRow(
                    label = "Статус",
                    value = show.status,
                    tag = "detail_status"
                )
            }

            if (show.language != null) {
                DetailInfoRow(
                    label = "Язык",
                    value = show.language,
                    tag = "detail_language"
                )
            }

            if (show.premiered != null) {
                DetailInfoRow(
                    label = "Премьера",
                    value = show.premiered,
                    tag = "detail_premiered"
                )
            }

            if (show.ended != null) {
                DetailInfoRow(
                    label = "Окончание",
                    value = show.ended,
                    tag = "detail_ended"
                )
            }

            if (show.officialSite != null) {
                DetailInfoRow(
                    label = "Официальный сайт",
                    value = show.officialSite,
                    tag = "detail_official_site"
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Описание",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("detail_description_label")
            )

            Spacer(modifier = Modifier.height(4.dp))

            val cleanSummary = show.summary?.replace(Regex("<[^>]*>"), "") ?: "Нет описания"
            Text(
                text = cleanSummary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.testTag("detail_summary")
            )
        }
    }
}

@Composable
fun DetailInfoRow(label: String, value: String, tag: String = "") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("${tag}_label")
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            modifier = Modifier.testTag("${tag}_value")
        )
    }
}