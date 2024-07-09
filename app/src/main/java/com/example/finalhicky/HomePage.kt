package com.example.finalhicky

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.pager.PagerDefaults
import androidx.compose.animation.core.tween
import androidx.compose.runtime.saveable.rememberSaveable
import dev.chrisbanes.snapper.ExperimentalSnapperApi

data class Reel(
    val id: String,
    val videoUrl: String,
    val productLink: String,
    var likeCount: Int = 0
)

@OptIn(ExperimentalPagerApi::class, ExperimentalSnapperApi::class)
@Composable
fun HomePage(modifier: Modifier = Modifier) {
    var reels by rememberSaveable { mutableStateOf<List<Reel>>(emptyList()) }

    LaunchedEffect(Unit) {
        reels = fetchReelsFromFirestore()
    }

    val pagerState = rememberPagerState()

    VerticalPager(
        count = reels.size,
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        key = { reels[it].id },
        flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
    ) { page ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            key(reels[page].id) {
                ReelItem(reels[page], pagerState.currentPage == page) { updatedReel ->
                    reels = reels.map { if (it.id == updatedReel.id) updatedReel else it }
                    updateReelInFirestore(updatedReel)
                }
            }
        }
    }
}

@Composable
fun ReelItem(reel: Reel, isCurrentPage: Boolean, onLikeUpdated: (Reel) -> Unit) {
    val context = LocalContext.current
    var isLiked by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        VideoPlayer(videoUrl = reel.videoUrl, isCurrentPage = isCurrentPage)

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reel.productLink))
                    context.startActivity(intent)
                },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Shop Now",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            IconButton(
                onClick = {
                    isLiked = !isLiked
                    val updatedReel = reel.copy(likeCount = reel.likeCount + if (isLiked) 1 else -1)
                    onLikeUpdated(updatedReel)
                },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "${reel.likeCount}",
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun VideoPlayer(videoUrl: String, isCurrentPage: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
            playWhenReady = isCurrentPage
        }
    }

    var isPlaying by remember { mutableStateOf(isCurrentPage) }

    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) {
            exoPlayer.play()
            isPlaying = true
        } else {
            exoPlayer.pause()
            isPlaying = false
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                    isPlaying = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isCurrentPage && isPlaying) {
                        exoPlayer.play()
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                if (isPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
                isPlaying = !isPlaying
            }
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

suspend fun fetchReelsFromFirestore(): List<Reel> {
    val db = FirebaseFirestore.getInstance()
    val reelsCollection = db.collection("reels")
    val snapshot = reelsCollection.get().await()
    return snapshot.documents.mapNotNull { document ->
        val id = document.id
        val videoUrl = document.getString("videoUrl")
        val productLink = document.getString("productLink")
        val likeCount = document.getLong("likeCount")?.toInt() ?: 0
        if (videoUrl != null && productLink != null) {
            Reel(id, videoUrl, productLink, likeCount)
        } else {
            null
        }
    }
}

fun updateReelInFirestore(reel: Reel) {
    val db = FirebaseFirestore.getInstance()
    db.collection("reels").document(reel.id)
        .update("likeCount", reel.likeCount)
        .addOnFailureListener { e ->
            // Handle the error, maybe show a toast to the user
            println("Error updating like count: ${e.message}")
        }
}
