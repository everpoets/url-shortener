package com.everpoets.urlshortener.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.everpoets.urlshortener.R
import com.everpoets.urlshortener.ui.theme.SPACING_8
import com.everpoets.urlshortener.ui.theme.UrlShortenerTheme

const val TEXT_FIELD_TAG = "textFieldTag"
const val BUTTON_TAG = "buttonTag"

@Composable
fun InputTextButton(
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SPACING_8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var text by rememberSaveable { mutableStateOf("") }

        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.url)) },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .testTag(TEXT_FIELD_TAG)
        )

        IconButton(
            onClick = {
                onClick(text)
                text = ""
            },
            enabled = text.isNotEmpty(),
            modifier = Modifier.testTag(BUTTON_TAG)
        ) {
            Icon(
                imageVector = Icons.Outlined.Send,
                contentDescription = stringResource(R.string.content_description_send_button)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InputTextButtonPreview() {
    UrlShortenerTheme {
        InputTextButton(onClick = {})
    }
}
