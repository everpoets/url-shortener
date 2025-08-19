package com.everpoets.urlshortener.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everpoets.urlshortener.R
import com.everpoets.urlshortener.ShortenUiState
import com.everpoets.urlshortener.ui.theme.Gray

const val EMPTY_STATE_TAG = "emptyStateTag"
const val LOADING_STATE_TAG = "loadingStateTag"
const val LIST_STATE_TAG = "listStateTag"

private const val ROUNDED_CORNER_SHAPE = 10

@Composable
fun ShortenUrlList(
    uiState: ShortenUiState,
    modifier: Modifier = Modifier
) {
    println("ShortenUrlList: isLoading=${uiState.isLoading}, listSize=${uiState.list.size}")
    uiState.list.forEachIndexed { index, url ->
        println("ShortenUrlList: URL[$index] = alias=${url.alias}, shortLink=${url.shortLink}")
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag(LOADING_STATE_TAG)
            )
        }

        if (uiState.list.isEmpty()) {
            Text(
                text = stringResource(R.string.no_urls),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag(EMPTY_STATE_TAG)
            )
        } else {
            Text(
                text = stringResource(R.string.shortened_urls),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 10.dp)
            )
            LazyColumn(
                modifier = Modifier.testTag(LIST_STATE_TAG)
            ) {
                items(
                    items = uiState.list,
                    key = { url -> url.alias }
                ) { urlResponse ->
                    Column(
                        modifier = Modifier
                            .padding(2.dp)
                            .background(Gray, shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE))
                            .padding(5.dp)
                            .animateItem()
                    ) {
                        Text(urlResponse.shortLink)
                        Text(urlResponse.selfLink, fontSize = 12.sp, fontStyle = FontStyle.Italic)
                    }
                }
            }
        }
    }
}
