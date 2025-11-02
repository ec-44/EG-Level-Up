package com.ec.exercisegamification.helper

import android.content.Context
import android.util.Log
import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class PoseData(
    val restPose: List<OffsetSerializable>,
    val finalPose: List<OffsetSerializable>
)

@Serializable
data class RoutinePose(
    val name: String,
    val repetitions: Int
)

@Serializable
data class RoutineData(
    val routineName: String,
    val poses: List<RoutinePose>
)

@Serializable
data class ResultData(
    val routineName: String,
    val timestamp: Long,
    val totalScore: Int,
    val maxMultiplier: Int,
    val exerciseScores: List<Int>
)

private const val TAG = "StorageManager"

@Serializable
data class OffsetSerializable(val x: Float, val y: Float)

fun Offset.toSerializable() = OffsetSerializable(x, y)

enum class ExerciseDataType(val folderName: String) {
    POSE("poses"),
    ROUTINE("routines"),
    RESULT("results")
}
// Get the directory from context
private fun getDir(context: Context, type: ExerciseDataType): File {
    val dir = File(context.filesDir, type.folderName)
    if (!dir.exists()) dir.mkdirs()
    return dir
}

fun savePoseToFile(
    context: Context,
    exerciseName: String,
    rest: List<Offset>,
    final: List<Offset>
) {
    val cleanName = exerciseName.trim()
    val filename = "$cleanName.json"
    val data = PoseData(
        restPose = rest.map { it.toSerializable() },
        finalPose = final.map { it.toSerializable() }
    )
    val json = Json.encodeToString(data)

    val file = File(getDir(context, ExerciseDataType.POSE), filename)
    file.writeText(json)

    Log.d(TAG, "Saved pose to: ${file.absolutePath}, size=${file.length()}")
}

fun loadPoseFromFile(
    context: Context,
    exerciseName: String
): PoseData? {
    val cleanName = exerciseName.trim()
    val file = File(getDir(context, ExerciseDataType.POSE), "$cleanName.json")

    return if (file.exists()) {
        val json = file.readText()
        val poseData = Json.decodeFromString<PoseData>(json)
        Log.d(TAG, "Successfully loaded pose from: ${file.absolutePath}, Sizes: ${poseData.restPose.size}, ${poseData.finalPose.size}")
        poseData
    } else {
        Log.w(TAG, "Pose file not found: ${file.absolutePath}")
        null
    }
}

fun deleteFile(
    context: Context,
    filename: String?,
    type: ExerciseDataType = ExerciseDataType.POSE,
): Boolean {
    if (filename.isNullOrBlank()) return false
    val file = File(getDir(context, type), "$filename.json")
    return file.exists() && file.delete()
}

fun getList(
    context: Context,
    type: ExerciseDataType = ExerciseDataType.POSE
): List<String> {
    return getDir(context, type)
        .takeIf { it.isDirectory }
        ?.listFiles()
        ?.map { it.nameWithoutExtension }
        ?.distinct()
        ?: emptyList()
}

fun saveRoutineToFile(
    context: Context,
    routineName: String,
    poses: List<RoutinePose>
) {
    val cleanName = routineName.trim()
    val filename = "$cleanName.json"
    val data = RoutineData(
        routineName = cleanName,
        poses = poses
    )
    val json = Json.encodeToString(data)

    val file = File(getDir(context, ExerciseDataType.ROUTINE), filename)
    file.writeText(json)

    Log.d(TAG, "Saved routine to: ${file.absolutePath}, pose count=${poses.size}")
}

fun loadRoutineFromFile(
    context: Context,
    routineName: String
): RoutineData? {
    val cleanName = routineName.trim()
    val file = File(getDir(context, ExerciseDataType.ROUTINE), "$cleanName.json")

    return if (file.exists()) {
        val json = file.readText()
        val routineData = Json.decodeFromString<RoutineData>(json)
        Log.d(TAG, "Loaded routine from: ${file.absolutePath}, pose count=${routineData.poses.size}")
        routineData
    } else {
        Log.w(TAG, "Routine file not found: ${file.absolutePath}")
        null
    }
}

private fun getResultDir(context: Context, routineName: String): File {
    val resultRoot = getDir(context, ExerciseDataType.RESULT)
    val routineDir = File(resultRoot, routineName.trim())
    if (!routineDir.exists()) routineDir.mkdirs()
    return routineDir
}

fun saveResultToFile(
    context: Context,
    resultData: ResultData
) {
    val routineDir = getResultDir(context, resultData.routineName)
    val filename = "${resultData.timestamp}.json"
    val json = Json.encodeToString(resultData)

    val file = File(routineDir, filename)
    file.writeText(json)

    Log.d(TAG, "Saved result: ${file.absolutePath}")
}

fun loadResultFromFile(
    context: Context,
    routineName: String,
    timestamp: Long
): ResultData? {
    val file = File(getResultDir(context, routineName), "$timestamp.json")
    return if (file.exists()) {
        val json = file.readText()
        Json.decodeFromString<ResultData>(json)
    } else {
        Log.w(TAG, "Result not found: ${file.absolutePath}")
        null
    }
}

fun getResultsList(
    context: Context,
    routineName: String
): List<Long> {
    return getResultDir(context, routineName)
        .listFiles()
        ?.mapNotNull { it.nameWithoutExtension.toLongOrNull() }
        ?.sortedDescending()
        ?: emptyList()
}