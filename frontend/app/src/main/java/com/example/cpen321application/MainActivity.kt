package com.example.cpen321application

import android.os.Bundle
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.material3.ListItemDefaults.containerColor
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.example.cpen321application.ui.theme.CPEN321ApplicationTheme
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CPEN321ApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        apiBaseUrl = BuildConfig.API_BASE_URL,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(apiBaseUrl: String, modifier: Modifier = Modifier) {
    var statusText by remember { mutableStateOf("Checking backend at $apiBaseUrl/health...") }

    LaunchedEffect(apiBaseUrl) {
        statusText = fetchHealthStatus(apiBaseUrl)
    }

    Column(modifier=modifier) {
        Text(text = statusText)
        LoginButton()
    }
}

private suspend fun fetchHealthStatus(apiBaseUrl: String): String = withContext(Dispatchers.IO) {
    val healthUrl = "${apiBaseUrl.trimEnd('/')}/health"
    try {
        val connection = (URL(healthUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5_000
            readTimeout = 5_000
        }

        when (val code = connection.responseCode) {
            HttpURLConnection.HTTP_OK -> {
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                "Backend healthy ($healthUrl): $body"
            }
            else -> {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                "Backend error ($healthUrl): HTTP $code${errorBody?.let { " — $it" } ?: ""}"
            }
        }
    } catch (e: Exception) {
        "Backend unreachable ($healthUrl): ${e.message ?: e.javaClass.simpleName}"
    }
}

@Composable
fun MyButton(buttonText: String, onClick: () -> Unit) {
    Button(
        onClick = {
            println("button clicked")
        },

        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Gray,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(text = buttonText)
    }
}

@Composable
fun LoginButton() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    MyButton("Login",
        onClick = {
            scope.launch{
                googleSignIn(context)
            }
        })
}

suspend fun googleSignIn(context: Context) { //check if right context was imported
    val credentialManager = CredentialManager.create(context)
    val googleIdOption = GetSignInWithGoogleOption.Builder(
        serverClientId = "417938142085-ngdhrek8t9vggugpbcadcjbcrn7vb5r3.apps.googleusercontent.com"
    ).build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    try {
        val response = credentialManager.getCredential(request = request, context = context)
        handleSignIn(response)

    } catch (e: GetCredentialException) {
        Log.e("CredentialManagerException", "Sign in failed", e)
    }
}

fun handleSignIn(result: GetCredentialResponse) {
    val credential = result.credential
    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
        try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            val idToken = googleIdTokenCredential.idToken
            val displayName = googleIdTokenCredential.displayName
            val givenName = googleIdTokenCredential.givenName
            val familyName = googleIdTokenCredential.familyName
            val email = googleIdTokenCredential.email

        } catch (e: GoogleIdTokenParsingException) {
            Log.e("CredentialManagerException", "Sign in failed", e)
        }

    } else {
        Log.e("CredentialManagerException", "Sign in failed")
    }

}


@Composable
fun LiveUpdateButton() {
    MyButton("Live Update",
        onClick = {
            println("button clicked")
        }
    )

}

@Composable
fun SurpriseButton() {
    MyButton("Surprise Me",
        onClick = {
            println("button clicked")
        }
    )
}