package com.ec.exercisegamification.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.ec.exercisegamification.activity.PlayActivity
import com.ec.exercisegamification.activity.ResultsListActivity
import com.ec.exercisegamification.helper.ExerciseDataType
import com.ec.exercisegamification.helper.RoutineData
import com.ec.exercisegamification.helper.RoutinePose
import com.ec.exercisegamification.helper.deleteFile
import com.ec.exercisegamification.helper.getList
import com.ec.exercisegamification.helper.saveRoutineToFile
import com.ec.exercisegamification.ui.components.ListComponent
import com.ec.exercisegamification.ui.components.ListDialogAdd
import com.ec.exercisegamification.ui.components.ListDialogDelete
import com.ec.exercisegamification.ui.components.ListItemAction
import com.ec.exercisegamification.ui.theme.ExerciseGamificationTheme

data class RoutineExerciseInput(
    var name: String = "",
    var repetitions: Int = 1
)

@Composable
fun RoutineScreen() {
    val context = LocalContext.current
    var routineList by remember { mutableStateOf(emptyList<String>()) }
    var expandedRoutine by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var routineToDelete by remember { mutableStateOf<String?>(null) }

    fun refreshList() {
        routineList = getList(context, ExerciseDataType.ROUTINE)
    }

    fun deleteAndRefresh(name: String?) {
        val deleted = deleteFile(context, name, ExerciseDataType.ROUTINE)
        if (deleted) {
            refreshList()
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

    ExerciseGamificationTheme {
        Box(modifier = Modifier.Companion.fillMaxSize()) {
            ListComponent(
                itemList = routineList,
                expandedListItem = expandedRoutine,
                onItemExpand = { name ->
                    expandedRoutine = if (expandedRoutine == name) null else name
                },
                actions = { name ->
                    listOf(
                        ListItemAction(
                            icon = painterResource(R.drawable.play_arrow),
                            contentDescription = "Play $name",
                            onClick = {
                                val intent = Intent(context, PlayActivity::class.java).apply {
                                    putExtra("routineName", name)
                                }
                                context.startActivity(intent)
                            },
                        ),
                        ListItemAction(
                            icon = painterResource(R.drawable.results),
                            contentDescription = "Show $name results",
                            onClick = {
                                val intent = Intent(context, ResultsListActivity::class.java).apply {
                                    putExtra("routineName", name)
                                }
                                context.startActivity(intent)
                            },
                        ),
                        ListItemAction(
                            icon = painterResource(R.drawable.delete),
                            contentDescription = "Delete $name",
                            onClick = {
                                routineToDelete = name
                                showDeleteDialog = true
                            },
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
                Icon(painterResource(R.drawable.add), contentDescription = "Add Routine")
            }

            if (showAddDialog) {
                RoutineAddDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name, loops, exercises ->
                        showAddDialog = false

                        // Convert to RoutineData and save it
                        val routineData = RoutineData(
                            routineName = name,
                            poses = exercises.map {
                                RoutinePose(name = it.name, repetitions = it.repetitions)
                            }
                        )

                        if (routineList.contains(name.trim())) {
                            Toast.makeText(context, "Routine '$name' already exists.", Toast.LENGTH_SHORT).show()
                        } else {
                            saveRoutineToFile(context, name, routineData.poses)
                            refreshList()
                        }
                    }
                )
            }

            if (showDeleteDialog && routineToDelete != null) {
                ListDialogDelete(
                    itemName = routineToDelete!!,
                    onDismiss = {
                        showDeleteDialog = false
                        routineToDelete = null
                    },
                    onConfirm = {
                        deleteAndRefresh(routineToDelete)
                        showDeleteDialog = false
                        routineToDelete = null
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoutineScreenPreview() {
    val dummyList = listOf("Arms", "Legs", "Back")
    var expandedRoutine by remember { mutableStateOf<String?>(dummyList.first()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var routineToDelete by remember { mutableStateOf<String?>(null) }

    ExerciseGamificationTheme {
        Box(modifier = Modifier.Companion.fillMaxSize()) {
            ListComponent(
                itemList = dummyList,
                expandedListItem = expandedRoutine,
                onItemExpand = { name ->
                    expandedRoutine = if (expandedRoutine == name) null else name
                },
                actions = { name ->
                    listOf(
                        ListItemAction(
                            icon = painterResource(R.drawable.play_arrow),
                            contentDescription = "Play $name",
                            onClick = { },
                        ),
                        ListItemAction(
                            icon = painterResource(R.drawable.results),
                            contentDescription = "Show $name results",
                            onClick = { },
                        ),
                        ListItemAction(
                            icon = painterResource(R.drawable.delete),
                            contentDescription = "Delete $name",
                            onClick = { },
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
                Icon(painterResource(R.drawable.add), contentDescription = "Add Routine")
            }

            if (showAddDialog) {
                ListDialogAdd(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name ->
                        showAddDialog = false
                    },
                    itemType = "Routine"
                )
            }

            if (showDeleteDialog && routineToDelete != null) {
                ListDialogDelete(
                    itemName = routineToDelete!!,
                    onDismiss = {
                        showDeleteDialog = false
                        routineToDelete = null
                    },
                    onConfirm = {
                        println("Delete $routineToDelete")
                        showDeleteDialog = false
                        routineToDelete = null
                    }
                )
            }
        }
    }
}

@Composable
fun RoutineAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (routineName: String, loops: Int, exercises: List<RoutineExerciseInput>) -> Unit
) {
    var routineName by remember { mutableStateOf("") }
    var loops by remember { mutableStateOf("1") }
    var exercises by remember { mutableStateOf(emptyList<RoutineExerciseInput>()) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showExercisePicker by remember { mutableStateOf(false) }
    val availableExercises = getList(LocalContext.current, ExerciseDataType.POSE)
    var showValidationDialog by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }

    // Scaffold for Snackbar support
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    val loopInt = loops.toIntOrNull() ?: 1
                    val validExercises = exercises.filter { it.name.isNotBlank() && it.repetitions > 0 }

                    if (routineName.isBlank()) {
                        validationMessage = "Please enter a routine name."
                        showValidationDialog = true
                        return@TextButton
                    }

                    if (validExercises.isEmpty()) {
                        validationMessage = "Please add at least one valid exercise."
                        showValidationDialog = true
                        return@TextButton
                    }

                    onConfirm(routineName.trim(), loopInt, validExercises)
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            },
            title = { Text("Create Routine") },
            text = {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .fillMaxHeight(0.8f)
                ) {
                    OutlinedTextField(
                        value = routineName,
                        onValueChange = { routineName = it },
                        label = { Text("Routine Name") }
                    )

                    // Loop count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = loops,
                            onValueChange = { loops = it.filter { char -> char.isDigit() } },
                            label = { Text("Loops") },
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                val loopInt = loops.toIntOrNull() ?: 1
                                if (loopInt > 1) {
                                    loops = (loopInt - 1).toString()
                                }
                            },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(painterResource(R.drawable.remove), "Subtract Loops")
                        }

                        IconButton(
                            onClick = {
                                val loopInt = loops.toIntOrNull() ?: 1
                                loops = (loopInt + 1).toString()
                            },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(painterResource(R.drawable.add), "Add Loops")
                        }
                    }

                    // List of exercises
                    exercises.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = item.name,
                                onValueChange = { new ->
                                    exercises = exercises.toMutableList().apply {
                                        this[index] = this[index].copy(name = new)
                                    }
                                },
                                label = { Text("Exercise") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = item.repetitions.toString(),
                                onValueChange = { new ->
                                    val reps = new.toIntOrNull() ?: 0
                                    exercises = exercises.toMutableList().apply {
                                        this[index] = this[index].copy(repetitions = reps)
                                    }
                                },
                                label = { Text("Reps") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )
                        }
                    }

                    TextButton(
                        onClick = { showExercisePicker = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("+ Add Exercise")
                    }

                    if (showExercisePicker) {
                        ExercisePickerDialog(
                            exerciseList = availableExercises,
                            onDismiss = { showExercisePicker = false },
                            onExerciseSelected = { selectedExercise ->
                                exercises = exercises + RoutineExerciseInput(name = selectedExercise)
                                showExercisePicker = false
                            }
                        )
                    }

                    if (showValidationDialog) {
                        AlertDialog(
                            onDismissRequest = { showValidationDialog = false },
                            confirmButton = {
                                TextButton(onClick = { showValidationDialog = false }) {
                                    Text("OK")
                                }
                            },
                            title = { Text("Invalid Input") },
                            text = { Text(validationMessage) }
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun ExercisePickerDialog(
    exerciseList: List<String>,
    onDismiss: () -> Unit,
    onExerciseSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick an Exercise") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxHeight(0.5f)
            ) {
                exerciseList.forEach { exercise ->
                    TextButton(
                        onClick = { onExerciseSelected(exercise) } ,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(exercise)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
