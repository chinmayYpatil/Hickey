package com.example.finalhicky

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun LoginScreen(navController: NavController) {
      var email by remember { mutableStateOf("") }
      var password by remember { mutableStateOf("") }

      Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
      ) {
            Image(
                  painter = painterResource(id = R.drawable.logo),
                  contentDescription = "Logo",
                  modifier = Modifier.size(200.dp)
            )

            Text(text = "Welcome To Hickey", fontWeight = FontWeight.Bold, fontSize = 25.sp)

            Spacer(modifier = Modifier.height(1.dp))

            Text(text = "Login to Your Account")
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                  value = email,
                  onValueChange = { email = it },
                  label = { Text(text = "Email Address") }
            )
            Spacer(modifier = Modifier.height(1.dp))
            OutlinedTextField(
                  value = password,
                  onValueChange = { password = it },
                  label = { Text(text = "Password") },
                  visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {
                  navController.navigate(Routes.ViewPageScreen)
            }) {
                  Text(text = "Login")
            }

            Spacer(modifier = Modifier.height(15.dp))
            Text(text = "Forgot Password?", modifier = Modifier.clickable { })
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "or sign-in with")

            Row(
                  modifier = Modifier
                        .fillMaxWidth()
                        .padding(55.dp),
                  horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                  Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "google",
                        modifier = Modifier
                              .size(50.dp)
                              .clickable { }
                  )
                  Image(
                        painter = painterResource(id = R.drawable.facebook),
                        contentDescription = "Facebook",
                        modifier = Modifier
                              .size(50.dp)
                              .clickable { }
                  )
            }
      }
}