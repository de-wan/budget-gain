package co.ke.foxlysoft.budgetgain.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.ke.foxlysoft.budgetgain.utils.PaginationState
import kotlinx.coroutines.launch

@Composable
fun <T> BGPaginatedList(
    pageSize: Int = 10,
    onGetKey: (T) -> Any,
    onGetItem: @Composable (T) -> Unit,
    onGetSpacer: (@Composable () -> Unit)? = null,
    onGetItems: suspend (limit: Int, offset: Int) -> List<T>,
    ) {
    val coroutineScope = rememberCoroutineScope()

    val itemsList = remember { mutableStateListOf<T>() }
    var page by remember { mutableStateOf(0) }
    var pagingState  by remember { mutableStateOf(PaginationState.LOADING) }
    var canPaginate by mutableStateOf(false)

    val lazyColumnListState = rememberLazyListState()

    val initialPage = 0

    fun clearPaging() {
        page = 0
        pagingState = PaginationState.LOADING
        canPaginate = false
    }

    fun getItemsProxy() {
        if (page == initialPage || (canPaginate) && pagingState == PaginationState.REQUEST_INACTIVE) {
            pagingState = if (page == initialPage) {
                PaginationState.LOADING
            } else {
                PaginationState.PAGINATING
            }
        }

        coroutineScope.launch {
            try {
                val result = onGetItems(
                    pageSize, page * pageSize
                )
                canPaginate = result.size == pageSize

                if (page == initialPage) {
                    if (result.isEmpty()) {
                        pagingState = PaginationState.EMPTY
                        return@launch
                    }
                    itemsList.clear()
                    itemsList.addAll(result)
                } else {
                    itemsList.addAll(result)
                }

                pagingState = PaginationState.REQUEST_INACTIVE

                if (canPaginate) {
                    page++
                }

                if (!canPaginate) {
                    pagingState = PaginationState.PAGINATION_EXHAUST
                }
            } catch (e: Exception) {
                pagingState = if (page == initialPage) {
                    PaginationState.ERROR
                } else {
                    PaginationState.PAGINATION_EXHAUST
                }
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        clearPaging()
        getItemsProxy()
    }

    val shouldPaginate = remember {
        derivedStateOf {
            canPaginate && (
                    lazyColumnListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                        ?: -5
                    ) >= (lazyColumnListState.layoutInfo.totalItemsCount - 3)
        }
    }

    LaunchedEffect(key1 = shouldPaginate.value) {
        if (shouldPaginate.value && pagingState == PaginationState.REQUEST_INACTIVE) {
            getItemsProxy()
        }
    }

    LazyColumn(
        state = lazyColumnListState,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(
            itemsList.size,
            key = { onGetKey(itemsList[it]) },
        ) { index ->
            onGetItem(itemsList[index])
            if (onGetSpacer != null) {
                onGetSpacer()
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    when (pagingState) {
        PaginationState.LOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        PaginationState.REQUEST_INACTIVE -> {
//                Text(text = "Request Inactive")
        }
        PaginationState.PAGINATING -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        PaginationState.ERROR -> {
//                Text(text = "Error")
        }
        PaginationState.PAGINATION_EXHAUST -> {
//                Text(text = "Pagination Exhaust")
        }
        PaginationState.EMPTY -> {
//                Text(text = "Empty")
        }
    }
}