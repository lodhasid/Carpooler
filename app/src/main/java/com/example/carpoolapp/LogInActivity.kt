package com.example.carpoolapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.carpoolapp.ui.theme.CarpoolAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class LogInActivity : ComponentActivity() {
    @ExperimentalTextApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth: FirebaseAuth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
            login()
        } else {
            setContent {
                CarpoolAppTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(color = MaterialTheme.colors.background) {
                        LoginScreen() {
                            login()
                        }
                    }
                }
            }
        }
    }

    private fun login() {
        val intent = Intent(this, HomePageActivity::class.java)
        startActivity(intent)
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    onLogIn: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var logInShown by remember { mutableStateOf(true) }
    val auth: FirebaseAuth = Firebase.auth
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Carpool App!",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        Box {
            androidx.compose.animation.AnimatedVisibility(
                visible = logInShown,
                exit = slideOutHorizontally(
                    animationSpec = tween(500),
                    targetOffsetX = { fullWidth ->
                        -2 * fullWidth
                    }),
                enter = slideInHorizontally(
                    animationSpec = tween(500),
                    initialOffsetX = { fullWidth ->
                        3 * fullWidth
                    })
            ) {
                val context = LocalContext.current
                LoginCard(
                    email = email,
                    onEmailChanged = { email = it },
                    password = password,
                    onPasswordChanged = { password = it },
                    onSubmit = {
                        if (email == "" || password == "") {
                            Toast.makeText(context, "Login failed.", Toast.LENGTH_SHORT).show()
                            return@LoginCard
                        }
                        auth.signInWithEmailAndPassword(email.trim(), password)
                            .addOnCompleteListener() {
                                if (it.isSuccessful) {
                                    val user = auth.currentUser
                                    if (user != null) {
                                        onLogIn()
                                    }
                                } else {
                                    Log.w("a", "LOGIN FAILED")
                                    Toast.makeText(
                                        context,
                                        "Login failed.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    },
                    onSignUp = {
                        logInShown = false
                    }
                )
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = !logInShown,
                exit = slideOutHorizontally(
                    animationSpec = tween(500),
                    targetOffsetX = { fullWidth ->
                        -2 * fullWidth
                    }),
                enter = slideInHorizontally(
                    animationSpec = tween(500),
                    initialOffsetX = { fullWidth ->
                        3 * fullWidth
                    })
            ) {
                val context = LocalContext.current
                SignUpCard(
                    email = email,
                    onEmailChanged = { email = it },
                    password = signUpPassword,
                    onPasswordChanged = { signUpPassword = it },
                    name = name,
                    onNameChanged = { name = it },
                    onSubmit = {
                        auth.createUserWithEmailAndPassword(email.trim(), signUpPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "createUserWithEmail:success")
                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setDisplayName(name.trim()).build()
                                    task.result.user?.updateProfile(profileUpdates)
                                    onLogIn()
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                    Toast.makeText(
                                        context, "Please enter valid information.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    },

                    onLogIn = {
                        logInShown = true
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginCard(
    email: String,
    onEmailChanged: (String) -> Unit,
    password: String,
    onPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onSignUp: (Int) -> Unit
) {
    Card(
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colors.surface,
    ) {
        val focusManager = LocalFocusManager.current
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val keyboardController = LocalSoftwareKeyboardController.current
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChanged,
                label = { Text("Email") },
                maxLines = 1,
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null
                    )
                },
                modifier = Modifier.padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 16.dp,
                    bottom = 8.dp
                )
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChanged,
                label = { Text("Password") },
                maxLines = 1,
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                modifier = Modifier.padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 8.dp,
                    bottom = 16.dp
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
            )
            Button(
                onClick = onSubmit,
                Modifier.padding(bottom = 16.dp)
            ) {
                Text(text = "Log In")
            }
            val annotatedString = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colors.onSurface,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Don't have an account? Sign up")
                }
            }
            ClickableText(
                text = annotatedString,
                onClick = onSignUp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignUpCard(
    email: String,
    onEmailChanged: (String) -> Unit,
    password: String,
    onPasswordChanged: (String) -> Unit,
    name: String,
    onNameChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onLogIn: (Int) -> Unit
) {
    Card(
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colors.surface,
    ) {
        val focusManager = LocalFocusManager.current
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val keyboardController = LocalSoftwareKeyboardController.current
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChanged,
                label = { Text("Email") },
                maxLines = 1,
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null
                    )
                },
                modifier = Modifier.padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 16.dp,
                    bottom = 8.dp
                )
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChanged,
                label = { Text("Password") },
                maxLines = 1,
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                modifier = Modifier.padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 8.dp,
                    bottom = 16.dp
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChanged,
                label = { Text("Display Name") },
                maxLines = 1,
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 8.dp,
                    bottom = 16.dp
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null
                    )
                }
            )
            Button(
                onClick = onSubmit,
                Modifier.padding(bottom = 16.dp)
            ) {
                Text(text = "Register")
            }
            val annotatedString = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colors.onSurface,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Already have an account? Log in")
                }
            }
            ClickableText(
                text = annotatedString,
                onClick = onLogIn,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@ExperimentalTextApi
@Preview(showBackground = true)
@Composable
fun Preview() {
    CarpoolAppTheme(darkTheme = false) {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {
            LoginScreen() {}
        }
    }
}