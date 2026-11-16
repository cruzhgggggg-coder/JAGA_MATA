package com.jagamata.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jagamata.app.R

private data class IntroPage(val title: Int, val desc: Int)

private val introPages =
    listOf(
        IntroPage(R.string.intro_title_1, R.string.intro_desc_1),
        IntroPage(R.string.intro_title_2, R.string.intro_desc_2),
        IntroPage(R.string.intro_title_3, R.string.intro_desc_3),
    )

@Composable
fun OnboardingFlow(onDone: () -> Unit) {
    var pageIndex by rememberSaveable { mutableIntStateOf(0) }
    val page = introPages[pageIndex]
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(page.title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(page.desc),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
            onClick = {
                if (pageIndex < introPages.lastIndex) pageIndex++ else onDone()
            },
        ) {
            Text(stringResource(R.string.onboarding_next))
        }
    }
}
