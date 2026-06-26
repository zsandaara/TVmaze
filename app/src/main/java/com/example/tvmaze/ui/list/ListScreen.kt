package com.example.tvmaze.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tvmaze.data.model.Show

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    onShowClick: (Int) -> Unit,
    viewModel: ListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf false
            val lastVisibleIndex = visibleItems.last().index
            val totalItems = layoutInfo.totalItemsCount
            lastVisibleIndex >= totalItems - 3 && totalItems > 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("list_screen")
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchShows(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("search_field"),
            placeholder = { Text("Поиск сериалов...") },
            singleLine = true
        )

        when (val currentState = uiState) {
            is ListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("loading_indicator"),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("error_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(
                            onClick = { viewModel.retry() },
                            modifier = Modifier.testTag("retry_button")
                        ) {
                            Text("Повторить")
                        }
                    }
                }
            }

            is ListUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("empty_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ничего не найдено",
                        fontSize = 18.sp,
                        modifier = Modifier.testTag("empty_message")
                    )
                }
            }

            is ListUiState.Success -> {
                val shows = currentState.shows
                val isLoadingMore = currentState.isLoadingMore
                val hasError = currentState.hasError

                LaunchedEffect(shouldLoadMore) {
                    if (shouldLoadMore && !isLoadingMore && !hasError) {
                        viewModel.nextPage()
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("shows_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = shows.distinctBy { it.id },
                        key = { show -> "show_${show.id}" }
                    ) { show ->
                        ShowCard(
                            show = show,
                            onClick = { onShowClick(show.id) }
                        )
                    }

                    if (isLoadingMore) {
                        item(key = "loading_more_item") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .testTag("loading_more"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Загрузка...",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (hasError) {
                        item(key = "load_error_item") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Ошибка загрузки",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 14.sp
                                    )
                                    TextButton(
                                        onClick = { viewModel.retryNextPage() }
                                    ) {
                                        Text("Повторить")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowCard(show: Show, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("show_card"),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = show.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("show_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (show.rating?.average != null) {
                Text(
                    text = "${show.rating.average}/10",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("show_rating")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (show.genres.isNotEmpty()) {
                Text(
                    text = "Жанры: ${show.genres.joinToString(", ")}",
                    fontSize = 13.sp,
                    modifier = Modifier.testTag("show_genres")
                )
            }

            if (show.premiered != null) {
                Text(
                    text = "Премьера: ${show.premiered}",
                    fontSize = 13.sp,
                    modifier = Modifier.testTag("show_premiered")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val cleanSummary = show.summary
                ?.replace(Regex("<[^>]*>"), "")
                ?.take(120)
            if (!cleanSummary.isNullOrEmpty()) {
                Text(
                    text = cleanSummary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("show_summary")
                )
            }
        }
    }
}