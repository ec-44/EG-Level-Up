package com.ec.exercisegamification.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ec.exercisegamification.R
import com.ec.exercisegamification.activity.PoseRecorderActivity
import com.ec.exercisegamification.activity.TestActivity
import com.ec.exercisegamification.helper.ExerciseDataType
import com.ec.exercisegamification.helper.deleteFile
import com.ec.exercisegamification.helper.getList
import com.ec.exercisegamification.ui.components.ListComponent
import com.ec.exercisegamification.ui.components.ListDialogAdd
import com.ec.exercisegamification.ui.components.ListDialogDelete
import com.ec.exercisegamification.ui.components.ListItemAction
import com.ec.exercisegamification.ui.theme.ExerciseGamificationTheme

@Composable
    fun ExerciseListScreen() {
        val context = LocalContext.current

        var exerciseList by remember { mutableStateOf(emptyList<String>()) }
        var expandedExercise by remember { mutableStateOf<String?>(null) }

        var showAddDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var exerciseToDelete by remember { mutableStateOf<String?>(null) }

        fun refreshList() {
            exerciseList = getList(context)
        }

        fun deleteAndRefresh(name: String?) {
            val deleted = deleteFile(context, name, ExerciseDataType.POSE)
            if (deleted) {
                refreshList()
            }
        }

        fun addNewExercise(name: String) {
            if (name.isBlank()) return

            if (exerciseList.contains(name)) {
                Toast.makeText(context, "Exercise '$name' already exists.", Toast.LENGTH_SHORT).show()
                showAddDialog = false
            } else {
                val intent = Intent(context, PoseRecorderActivity::class.java)
                intent.putExtra("exerciseName", name)
                context.startActivity(intent)
            }
        }

        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    refreshList()
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        // Main UI
        Box(modifier = Modifier.Companion.fillMaxSize()) {

            ListComponent(
                itemList = exerciseList,
                expandedListItem = expandedExercise,
                onItemExpand = { name ->
                    expandedExercise = if (expandedExercise == name) null else name
                },
                actions = { name ->
                    listOf(
                        ListItemAction(
                            onClick = {
                                val intent = Intent(context, TestActivity::class.java)
                                intent.putExtra("exerciseName", name)
                                context.startActivity(intent)
                            },
                            icon = painterResource(R.drawable.play_arrow),
                            contentDescription = "Play $name"
                        ),
                        ListItemAction(
                            onClick = {
                                exerciseToDelete = name
                                showDeleteDialog = true
                            },
                            icon = painterResource(R.drawable.delete),
                            contentDescription = "Delete $name"
                        )
                    )
                }
            )

            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.Companion
                    .align(Alignment.Companion.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(painterResource(R.drawable.add), contentDescription = "Add Exercise")
            }

            // Modal dialogs
            if (showAddDialog) {
                ListDialogAdd(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name ->
                        addNewExercise(name)
                        showAddDialog = false
                    },
                    itemType = "Exercise"
                )
            }

            if (showDeleteDialog && exerciseToDelete != null) {
                ListDialogDelete(
                    itemName = exerciseToDelete!!,
                    onDismiss = {
                        showDeleteDialog = false
                        exerciseToDelete = null
                    },
                    onConfirm = {
                        deleteAndRefresh(exerciseToDelete)
                        showDeleteDialog = false
                        exerciseToDelete = null
                    }
                )
            }

        }
    }

    @Preview(showBackground = true)
    @Composable
    fun ExerciseListScreenPreview() {
        val dummyList = listOf("Push Ups", "Jumping Jacks", "Squats")
        var expandedExercise by remember { mutableStateOf<String?>(dummyList.first()) }
        var showAddDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var exerciseToDelete by remember { mutableStateOf<String?>(null) }

        ExerciseGamificationTheme {
            Box(modifier = Modifier.Companion.fillMaxSize()) {
                ListComponent(
                    itemList = dummyList,
                    expandedListItem = expandedExercise,
                    onItemExpand = { name ->
                        expandedExercise = if (expandedExercise == name) null else name
                    },
                    actions = { name ->
                        listOf(
                            ListItemAction(
                                icon = painterResource(R.drawable.play_arrow),
                                contentDescription = "Play $name",
                                onClick = { println("bruh") },
                            ),
                            ListItemAction(
                                icon = painterResource(R.drawable.delete),
                                contentDescription = "Delete $name",
                                onClick = { println("bruh") },
                            )
                        )
                    }
                )

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.Companion
                        .align(Alignment.Companion.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(painterResource(R.drawable.add), contentDescription = "Add Exercise")
                }

                if (showAddDialog) {
                    ListDialogAdd(
                        onDismiss = { showAddDialog = false },
                        onConfirm = { name ->
                            showAddDialog = false
                        },
                        itemType = "Exercise"
                    )
                }

                if (showDeleteDialog && exerciseToDelete != null) {
                    ListDialogDelete(
                        itemName = exerciseToDelete!!,
                        onDismiss = {
                            showDeleteDialog = false
                            exerciseToDelete = null
                        },
                        onConfirm = {
                            println("Delete $exerciseToDelete")
                            showDeleteDialog = false
                            exerciseToDelete = null
                        }
                    )
                }
            }
        }
    }