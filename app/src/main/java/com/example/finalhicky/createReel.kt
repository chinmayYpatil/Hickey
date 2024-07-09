package com.example.finalhicky

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CreateReelScreen(navController: NavController, productLink: String) {
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }
    val context = LocalContext.current

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        videoUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { videoPickerLauncher.launch("video/*") },
            modifier = Modifier
                .width(160.dp)
                .height(60.dp),
            enabled = !isUploading
        ) {
            Text("Add Video")
        }

        // Video preview area
        Box(
            modifier = Modifier
                .size(300.dp, 534.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
        ) {
            VideoPreview(videoUri)
        }

        // Upload button and progress bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Bottom
        ) {
            Button(
                onClick = {
                    videoUri?.let { uri ->
                        isUploading = true
                        uploadVideoToFirebase(uri, productLink) { progress ->
                            uploadProgress = progress
                            if (progress >= 1f) {
                                isUploading = false
                            }
                        }
                    }
                },
                enabled = !isUploading && videoUri != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upload Reel")
            }

            if (isUploading) {
                LinearProgressIndicator(
                    progress = uploadProgress,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun VideoPreview(videoUri: Uri?) {
    videoUri?.let {
        val context = LocalContext.current
        val player = remember {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(it))
                repeatMode = ExoPlayer.REPEAT_MODE_ALL
                prepare()
                playWhenReady = true  // Start playing the video automatically
            }
        }

        DisposableEffect(context) {
            onDispose {
                player.release()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                }
        ) {
            AndroidView(
                factory = {
                    PlayerView(context).apply {
                        this.player = player
                        useController = false  // Remove all controllers
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    } ?: run {
        Text(
            "No video selected",
            modifier = Modifier.fillMaxSize(),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

private fun uploadVideoToFirebase(videoUri: Uri, productLink: String, onProgressUpdate: (Float) -> Unit) {
    val storage = FirebaseStorage.getInstance()
    val videoRef = storage.reference.child("reels/${System.currentTimeMillis()}.mp4")

    videoRef.putFile(videoUri)
        .addOnProgressListener { taskSnapshot ->
            val progress = taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount
            onProgressUpdate(progress)
        }
        .addOnSuccessListener { taskSnapshot ->
            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUrl ->
                saveReelInfoToFirestore(downloadUrl.toString(), productLink)
            }?.addOnFailureListener { e ->
                // Handle failure to get download URL
                e.printStackTrace()
            }
        }
        .addOnFailureListener { e ->
            // Handle failure during video upload
            e.printStackTrace()
        }
}

private fun saveReelInfoToFirestore(videoUrl: String, productLink: String) {
    val db = FirebaseFirestore.getInstance()
    val reelInfo = hashMapOf(
        "videoUrl" to videoUrl,
        "productLink" to productLink,
        "timestamp" to com.google.firebase.Timestamp.now()
    )

    db.collection("reels")
        .add(reelInfo)
        .addOnSuccessListener { documentReference ->
            // Reel info saved successfully
            println("DocumentSnapshot added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            // Handle failure
            e.printStackTrace()
        }
}
