package com.example.cpen321application

import android.os.Bundle
import android.content.Context
import android.net.wifi.WifiManager
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
import retrofit2.awaitResponse
import java.net.NetworkInterface
import java.net.Inet4Address

class MainActivity : ComponentActivity() {
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiService = ApiClient.create(applicationContext)
        enableEdgeToEdge()
        setContent {
            CPEN321ApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        apiBaseUrl = BuildConfig.API_BASE_URL,
                        modifier = Modifier.padding(innerPadding),
                        apiService = apiService
                    )
                }
            }
        }
    }
}

data class LoginInfo (
    val serverIp: String = "",
    val clientIp: String = "",
    val serverTime: String = "",
    val clientTime: String = "",
    val backendOwnerName: String = "",
    val googleUserName: String = "",
)

@Composable
fun Greeting(apiBaseUrl: String, modifier: Modifier = Modifier, apiService: ApiService) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loginInfo by remember { mutableStateOf<LoginInfo?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        MyButton("Login") {
            scope.launch {
                isLoading = true
                errorMsg = null
                try {
                    val googleName = googleSignIn(context)

                    val clientIp = getClientIp()
                    val clientTime = getClientTime()

                    val nameResponse = apiService.getName().awaitResponse()
                    val ipResponse = apiService.getServerIp().awaitResponse()
                    val timeResponse = apiService.getServerTime().awaitResponse()

                    loginInfo = LoginInfo(
                        serverIp = ipResponse.body()?.ip ?: "Unknown",
                        clientIp = clientIp,
                        serverTime = timeResponse.body()?.time ?: "Unknown",
                        clientTime = clientTime,
                        backendOwnerName = "${nameResponse.body()?.firstName} ${nameResponse.body()?.lastName}",
                        googleUserName = googleName
                    )
                } catch (e: Exception) {
                    errorMsg = e.message ?: "Unknown error"
                } finally {
                    isLoading = false
                }
            }
        }

        if (isLoading) {
            Text("Loading...")
        }

        errorMsg?.let { Text(text = it, color = Color.Red) }

        loginInfo?.let { info ->
            Column {
                Text("Server IP: ${info.serverIp}")
                Text("Client IP: ${info.clientIp}")
                Text("Server Time: ${info.serverTime}")
                Text("Client Time: ${info.clientTime}")
                Text("Backend Owner: ${info.backendOwnerName}")
                Text("Logged in as: ${info.googleUserName}")
            }
        }
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
        onClick = onClick,
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

// HELPERS

suspend fun googleSignIn(context: Context): String { //check if right context was imported
    val credentialManager = CredentialManager.create(context)
    val googleIdOption = GetSignInWithGoogleOption.Builder(
        serverClientId = "417938142085-ngdhrek8t9vggugpbcadcjbcrn7vb5r3.apps.googleusercontent.com"
    ).build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    try {
        val response = credentialManager.getCredential(request = request, context = context)
        val credential = response.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return "${googleIdTokenCredential.givenName} ${googleIdTokenCredential.familyName}"
        }

    } catch (e: GetCredentialException) {
        Log.e("CredentialManagerException", "Sign in failed", e)
    }

    return "unknown"
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

fun getClientIp(): String {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (netInterface in interfaces) {
            Log.d("IP_DEBUG", "Interface: ${netInterface.name}, isLoopback: ${netInterface.isLoopback}, isUp: ${netInterface.isUp}")
            if (netInterface.isLoopback || netInterface.isUp ) continue
            for (address in netInterface.inetAddresses) {
                if (address is Inet4Address) {
                    return address.hostAddress ?: "Unknown"
                }
            }
        }
    } catch (e: Exception) {
            return "unknown"
        }
    return "unknown"
}

fun getClientTime(): String {
    val now = java.util.Calendar.getInstance()
    val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
    val timeStr = sdf.format(now.time)

    val offsetms  = now.get(java.util.Calendar.ZONE_OFFSET) + now.get(java.util.Calendar.DST_OFFSET)
    val offsetHours = offsetms / (1000 * 60 * 60)
    val sign = if (offsetHours >= 0) "+" else "-"
    val offsetStr = String.format("%02d", kotlin.math.abs(offsetHours))

    return "$timeStr GMT$sign$offsetStr"
}