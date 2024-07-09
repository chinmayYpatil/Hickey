package com.example.finalhicky

import Routes
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun add(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val scrollState = rememberScrollState()
    val itemsList = remember { mutableStateListOf(
        "Microsoft Bluetooth, USB Surface Arc Mouse",
        "Gizga Essentials 3-in-1 Phone Cleaner",
        "Portronics Hydra 10 Mechanical Wireless Gaming Keyboard"
    ) }

    val urls = remember {
        listOf(
            "https://amzn.to/4cswhsA",
            "https://amzn.to/45WqPfe",
            "https://amzn.to/45Pr0c4"
        )
    }

    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Products",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 50.sp
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(itemsList) { item ->
                val index = itemsList.indexOf(item)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = item,
                        modifier = Modifier
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                uriHandler.openUri(urls[index])
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(text = "View Product")
                        }
                        Button(
                            onClick = {
                                try {
                                    val encodedUrl = Uri.encode(urls[index])
                                    navController.navigate(Routes.CreateReel.replace("{productLink}", encodedUrl))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "Error navigating to Create Reel", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(text = "Create Reel")
                        }

                    }
                }
            }
        }
    }
}


