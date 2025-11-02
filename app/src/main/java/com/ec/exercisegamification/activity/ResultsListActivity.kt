package com.ec.exercisegamification.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ec.exercisegamification.helper.ResultData
import com.ec.exercisegamification.helper.getResultsList
import com.ec.exercisegamification.helper.loadResultFromFile
import com.ec.exercisegamification.ui.components.ListComponent
import com.ec.exercisegamification.ui.components.MiniResultView
import com.ec.exercisegamification.ui.theme.ExerciseGamificationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultsListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val routineName = intent.getStringExtra("routineName") ?: return

        setContent {
            ExerciseGamificationTheme {
                ResultsListScreen(routineName)
            }
        }
    }
}

@Composable
fun ResultsListScreen(routineName: String) {
    val context = LocalContext.current
    var results by remember { mutableStateOf(emptyList<Pair<Long, String>>()) }
    var expandedResult by remember { mutableStateOf<String?>(null) }
    var selectedResult by remember { mutableStateOf<ResultData?>(null) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(routineName) {
        val timestamps = getResultsList(context, routineName)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        results = timestamps.map { ts -> ts to formatter.format(Date(ts)) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No results yet.")
            }
            return@Surface
        }

        ListComponent(
            itemList = results.map { it.second },
            expandedListItem = expandedResult,
            onItemExpand = { itemName ->
                if (expandedResult == itemName) {
                    expandedResult = null
                    selectedResult = null
                } else {
                    expandedResult = itemName
                    selectedResult = null
                    loading = true
                }
            },
            actions = { emptyList() },
            expandedContent = { itemName ->
                if (expandedResult == itemName) {
                    if (loading) {
                        LinearProgressIndicator()
                    } else {
                        selectedResult?.let { result ->
                            MiniResultView(result)
                        } ?: Text("ERROR: Result not found!")
                    }
                }
            }
        )
    }

    // Load the selected result asynchronously
    LaunchedEffect(expandedResult) {
        expandedResult?.let { itemName ->
            val timestamp = results.first { it.second == itemName }.first
            val loaded = loadResultFromFile(context, routineName, timestamp)
            selectedResult = loaded
            loading = false
        } ?: run {
            selectedResult = null
            loading = false
        }
    }
}


