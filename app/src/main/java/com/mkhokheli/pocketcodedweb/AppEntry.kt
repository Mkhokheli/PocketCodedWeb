package com.mkhokheli.pocketcodedweb

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TRIAL_DURATION_MS = 72L * 60L * 60L * 1000L
private const val TRIAL_REMINDER_INTERVAL_MS = 60L * 60L * 1000L
private const val TRIAL_PREFS_NAME = "premium_trial"

private data class LocalUser(
    val id: String,
    val displayName: String,
    val email: String,
    val emailVerified: Boolean,
)

internal data class LocalProject(
    val name: String,
    val directory: File,
    val modifiedAt: Long,
    val canDelete: Boolean,
)

private data class EntryPalette(
    val background: Color,
    val panel: Color,
    val elevatedPanel: Color,
    val text: Color,
    val mutedText: Color,
    val border: Color,
    val accent: Color,
    val accentAlt: Color,
    val error: Color,
)

@Composable
internal fun PocketCodedWebEntry(
    darkMode: Boolean,
    currentThemeName: String,
    nextThemeName: String,
    onToggleTheme: () -> Unit,
    ideContent: @Composable (projectDirectory: File, onBackToProjects: () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val coroutineScope = rememberCoroutineScope()
    val palette = remember(currentThemeName, darkMode) { entryPalette(currentThemeName, darkMode) }
    val auth = remember {
        FirebaseAuth.getInstance().apply { useAppLanguage() }
    }
    var firebaseUser by remember { mutableStateOf(auth.currentUser) }
    val localUser = remember(firebaseUser?.uid, firebaseUser?.displayName, firebaseUser?.email, firebaseUser?.isEmailVerified) {
        firebaseUser?.takeIf { it.isVerifiedForAppAccess() }?.toLocalUser()
    }
    val accountId = localUser?.id
    var selectedProjectPath by rememberSaveable { mutableStateOf<String?>(null) }
    var billingState by remember { mutableStateOf(BillingUiState(purchasesLoaded = false)) }
    var billingManager by remember { mutableStateOf<PlayBillingManager?>(null) }
    var trialStartedAt by remember { mutableLongStateOf(0L) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var lastTrialContinuationAt by remember { mutableLongStateOf(0L) }
    var eraseWarningOpen by remember { mutableStateOf(false) }
    var isErasingTrialData by remember { mutableStateOf(false) }

    fun signOut() {
        selectedProjectPath = null
        auth.signOut()
        firebaseUser = null
        coroutineScope.launch { clearGoogleCredentialState(context) }
    }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            firebaseUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    DisposableEffect(accountId) {
        if (accountId == null) {
            billingManager = null
            billingState = BillingUiState(purchasesLoaded = false)
            onDispose {}
        } else {
            val cachedPremium = PlayBillingManager.isLocalPremium(context, accountId)
            val billingTrialStart = loadTrialStart(context, accountId)
                ?: firebaseUser?.metadata?.creationTimestamp
                ?: System.currentTimeMillis()
            val manager = PlayBillingManager(
                context = context,
                accountId = accountId,
                trialStartTime = billingTrialStart,
                onStateChanged = { billingState = it },
            ).also {
                billingState = BillingUiState(isPremium = cachedPremium, purchasesLoaded = cachedPremium)
                billingManager = it
                it.start()
            }
            onDispose {
                manager.close()
                if (billingManager === manager) billingManager = null
            }
        }
    }

    LaunchedEffect(accountId) {
        selectedProjectPath = null
        eraseWarningOpen = false
        isErasingTrialData = false
        val activeAccountId = accountId
        if (activeAccountId == null) {
            trialStartedAt = 0L
            lastTrialContinuationAt = 0L
        } else {
            val startedAt = loadTrialStart(context, activeAccountId)
                ?: System.currentTimeMillis().also { saveTrialStart(context, activeAccountId, it) }
            trialStartedAt = startedAt
            lastTrialContinuationAt = loadLastTrialContinuation(context, activeAccountId)
            now = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("users").document(activeAccountId).get()
                .addOnSuccessListener { snapshot ->
                    val serverStartedAt = snapshot.getTimestamp("trialStartedAt")?.toDate()?.time
                    if (serverStartedAt != null && serverStartedAt > 0L) {
                        saveTrialStart(context, activeAccountId, serverStartedAt)
                        trialStartedAt = serverStartedAt
                        now = System.currentTimeMillis()
                    }
                }
        }
    }

    DisposableEffect(activity, billingManager) {
        val activeBillingManager = billingManager
        val lifecycleOwner = activity as? LifecycleOwner
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                now = System.currentTimeMillis()
                activeBillingManager?.refreshPurchases()
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }

    LaunchedEffect(accountId, billingState.isPremium) {
        while (accountId != null && !billingState.isPremium) {
            now = System.currentTimeMillis()
            delay(15_000L)
        }
    }

    val trialEndsAt = trialStartedAt + TRIAL_DURATION_MS
    val trialExpired = accountId != null && trialStartedAt > 0L && isTrialExpired(now, trialEndsAt)
    val showPaywall = accountId != null &&
        trialStartedAt > 0L &&
        shouldShowTrialGate(
            now = now,
            trialStartedAt = trialStartedAt,
            lastContinuationAt = lastTrialContinuationAt,
            isPremium = billingState.isPremium,
        )

    BackHandler(enabled = showPaywall && !eraseWarningOpen) {}

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            firebaseUser == null -> FirebaseAuthScreen(
                palette = palette,
                darkMode = darkMode,
                nextThemeName = nextThemeName,
                onToggleTheme = onToggleTheme,
                auth = auth,
                onUserChanged = { firebaseUser = it },
            )

            firebaseUser?.isVerifiedForAppAccess() != true -> FirebaseVerificationScreen(
                palette = palette,
                darkMode = darkMode,
                nextThemeName = nextThemeName,
                onToggleTheme = onToggleTheme,
                auth = auth,
                user = firebaseUser,
                onUserChanged = { firebaseUser = it },
                onSignOut = { signOut() },
            )

            selectedProjectPath == null && localUser != null -> ProjectHomeScreen(
                user = localUser,
                darkMode = darkMode,
                currentThemeName = currentThemeName,
                nextThemeName = nextThemeName,
                onToggleTheme = onToggleTheme,
                onOpenProject = { project -> selectedProjectPath = project.directory.absolutePath },
                onSignOut = { signOut() },
            )

            else -> {
                val projectDirectory = File(selectedProjectPath.orEmpty()).apply { mkdirs() }
                key(projectDirectory.absolutePath) {
                    ideContent(projectDirectory) { selectedProjectPath = null }
                }
            }
        }

        if (showPaywall) {
            PremiumPaywall(
                palette = palette,
                darkMode = darkMode,
                trialEndsAt = trialEndsAt,
                trialExpired = trialExpired,
                billingState = billingState,
                onBuy = {
                    activity?.let { billingManager?.launchPurchase(it) }
                },
                onRestore = { billingManager?.restorePurchases() },
                onContinueTrial = {
                    if (!trialExpired) eraseWarningOpen = true
                },
            )
        }

        if (eraseWarningOpen) {
            FreeTrialEraseWarning(
                palette = palette,
                isErasing = isErasingTrialData,
                onDismiss = { if (!isErasingTrialData) eraseWarningOpen = false },
                onConfirm = {
                    val activeAccountId = accountId
                    if (!isErasingTrialData && activeAccountId != null) {
                        val confirmationTime = System.currentTimeMillis()
                        if (isTrialExpired(confirmationTime, trialEndsAt)) {
                            now = confirmationTime
                            eraseWarningOpen = false
                        } else {
                            isErasingTrialData = true
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) { clearLocalCodingData(context) }
                                val continuedAt = System.currentTimeMillis()
                                selectedProjectPath = null
                                saveLastTrialContinuation(context, activeAccountId, continuedAt)
                                lastTrialContinuationAt = continuedAt
                                now = continuedAt
                                trialStartedAt = loadTrialStart(context, activeAccountId)
                                    ?: continuedAt.also { saveTrialStart(context, activeAccountId, it) }
                                isErasingTrialData = false
                                eraseWarningOpen = false
                            }
                        }
                    }
                },
            )
        }
    }
}

private enum class AuthMode {
    SignIn,
    SignUp,
}

@Composable
private fun FirebaseAuthScreen(
    palette: EntryPalette,
    darkMode: Boolean,
    nextThemeName: String,
    onToggleTheme: () -> Unit,
    auth: FirebaseAuth,
    onUserChanged: (FirebaseUser?) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var mode by rememberSaveable { mutableStateOf(AuthMode.SignIn) }
    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val isSignUp = mode == AuthMode.SignUp
    val colors = remember(palette) { authColors(palette) }

    fun submit() {
        val cleanEmail = email.trim()
        when {
            cleanEmail.isBlank() || "@" !in cleanEmail -> message = "Enter a valid email address."
            password.length < 6 -> message = "Use at least 6 characters for your password."
            isSignUp && password != confirmPassword -> message = "Passwords do not match."
            else -> {
                isBusy = true
                message = null
                if (isSignUp) {
                    auth.createUserWithEmailAndPassword(cleanEmail, password)
                        .addOnCompleteListener { createTask ->
                            if (!createTask.isSuccessful) {
                                isBusy = false
                                message = friendlyAuthMessage(createTask.exception, "Could not create your account.")
                                onUserChanged(auth.currentUser)
                                return@addOnCompleteListener
                            }

                            val createdUser = auth.currentUser
                            fun sendVerificationEmail() {
                                createdUser?.sendEmailVerification()
                                    ?.addOnCompleteListener { verifyTask ->
                                        isBusy = false
                                        message = if (verifyTask.isSuccessful) {
                                            "Verification email sent to $cleanEmail."
                                        } else {
                                            friendlyAuthMessage(verifyTask.exception, "Account created, but verification email could not be sent.")
                                        }
                                        syncUserProfileToFirestore(auth.currentUser)
                                        onUserChanged(auth.currentUser)
                                    }
                                    ?: run {
                                        isBusy = false
                                        message = "Account created. Sign in again to send verification."
                                        syncUserProfileToFirestore(auth.currentUser)
                                        onUserChanged(auth.currentUser)
                                    }
                            }

                            val cleanName = displayName.trim()
                            if (cleanName.isNotBlank() && createdUser != null) {
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(cleanName)
                                    .build()
                                createdUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener { sendVerificationEmail() }
                            } else {
                                sendVerificationEmail()
                            }
                        }
                } else {
                    auth.signInWithEmailAndPassword(cleanEmail, password)
                        .addOnCompleteListener { signInTask ->
                            if (!signInTask.isSuccessful) {
                                isBusy = false
                                message = friendlyAuthMessage(signInTask.exception, "Could not sign in.")
                                onUserChanged(auth.currentUser)
                                return@addOnCompleteListener
                            }

                            auth.currentUser?.reload()
                                ?.addOnCompleteListener {
                                    isBusy = false
                                    val refreshedUser = auth.currentUser
                                    syncUserProfileToFirestore(refreshedUser)
                                    onUserChanged(refreshedUser)
                                }
                                ?: run {
                                    isBusy = false
                                    val signedInUser = auth.currentUser
                                    syncUserProfileToFirestore(signedInUser)
                                    onUserChanged(signedInUser)
                                }
                        }
                }
            }
        }
    }

    fun continueWithGoogle() {
        isBusy = true
        message = null
        coroutineScope.launch {
            signInWithGoogle(context, auth)
                .onSuccess { user ->
                    isBusy = false
                    syncUserProfileToFirestore(user)
                    onUserChanged(user)
                }
                .onFailure { error ->
                    isBusy = false
                    message = friendlyGoogleSignInMessage(error)
                    onUserChanged(auth.currentUser)
                }
        }
    }

    AuthScreenSurface(colors) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.background)
                .systemBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            val compactHeight = maxHeight < 760.dp
            val tightHeight = maxHeight < 680.dp
            val narrowWidth = maxWidth < 380.dp
            val horizontalPadding = if (narrowWidth) 24.dp else 28.dp
            val contentMaxWidth = if (maxWidth < 520.dp) {
                maxWidth - (horizontalPadding * 2)
            } else {
                420.dp
            }
            val logoSize = when {
                tightHeight -> 56.dp
                compactHeight -> 62.dp
                else -> 68.dp
            }
            val brandSize = when {
                tightHeight -> 24
                compactHeight -> 27
                else -> 29
            }
            val titleSize = if (compactHeight) 21 else 22
            val fieldHeight = if (tightHeight) 56.dp else 58.dp
            val buttonHeight = if (tightHeight) 46.dp else 50.dp
            val fieldTextSize = if (tightHeight) 14 else 15
            val topSpacer = when {
                tightHeight -> 12.dp
                compactHeight -> 24.dp
                else -> 34.dp
            }
            val titleGap = when {
                tightHeight -> 20.dp
                isSignUp -> 26.dp
                else -> 32.dp
            }
            val formGap = if (compactHeight) 18.dp else 22.dp
            val fieldGap = if (tightHeight) 8.dp else 10.dp
            val sectionGap = if (tightHeight) 12.dp else 16.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(topSpacer))
                Box(
                    modifier = Modifier
                        .size(logoSize)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.elevatedPanel)
                        .border(1.dp, palette.accent.copy(alpha = 0.55f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Code,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(logoSize * 0.56f),
                    )
                }
                Spacer(Modifier.height(12.dp))
                BrandTitle(palette, fontSize = brandSize)
                Text(
                    text = "Code. Learn. Build. Anywhere.",
                    color = palette.mutedText,
                    fontSize = if (tightHeight) 12.sp else 14.sp,
                    letterSpacing = 0.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(titleGap))
                Text(
                    text = if (isSignUp) "Create Account" else "Welcome Back",
                    color = palette.text,
                    fontSize = titleSize.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isSignUp) "Create your PocketCodedWeb account" else "Sign in to continue coding",
                    color = palette.mutedText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(formGap))
                Column(
                    modifier = Modifier
                        .widthIn(max = contentMaxWidth)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (isSignUp) {
                        AuthTextField(
                            value = displayName,
                            onValueChange = {
                                displayName = it
                                message = null
                            },
                            enabled = !isBusy,
                            placeholder = "Full name",
                            icon = Icons.Rounded.Person,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            colors = colors,
                            height = fieldHeight,
                            fontSize = fieldTextSize,
                        )
                        Spacer(Modifier.height(fieldGap))
                    }
                    AuthTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            message = null
                        },
                        enabled = !isBusy,
                        placeholder = "Email",
                        icon = Icons.Rounded.Email,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        colors = colors,
                        height = fieldHeight,
                        fontSize = fieldTextSize,
                    )
                    Spacer(Modifier.height(fieldGap))
                    AuthPasswordField(
                        value = password,
                        onValueChange = {
                            password = it
                            message = null
                        },
                        enabled = !isBusy,
                        label = "Password",
                        imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done,
                        onDone = { if (!isSignUp) submit() },
                        colors = colors,
                        height = fieldHeight,
                        fontSize = fieldTextSize,
                    )
                    if (isSignUp) {
                        Spacer(Modifier.height(fieldGap))
                        AuthPasswordField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                message = null
                            },
                            enabled = !isBusy,
                            label = "Confirm password",
                            imeAction = ImeAction.Done,
                            onDone = { submit() },
                            colors = colors,
                            height = fieldHeight,
                            fontSize = fieldTextSize,
                        )
                    }
                    if (!isSignUp) {
                        TextButton(
                            onClick = {
                                val cleanEmail = email.trim()
                                if (cleanEmail.isBlank() || "@" !in cleanEmail) {
                                    message = "Enter your email address first."
                                } else {
                                    isBusy = true
                                    message = null
                                    auth.sendPasswordResetEmail(cleanEmail)
                                        .addOnCompleteListener { task ->
                                            isBusy = false
                                            message = if (task.isSuccessful) {
                                                "Password reset email sent."
                                            } else {
                                                friendlyAuthMessage(task.exception, "Could not send password reset email.")
                                            }
                                        }
                                }
                            },
                            enabled = !isBusy,
                            modifier = Modifier.align(Alignment.End),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        ) {
                            Text("Forgot password?", color = palette.accent, fontSize = 13.sp)
                        }
                    } else {
                        Spacer(Modifier.height(12.dp))
                    }
                    message?.let {
                        val success = it.contains("sent", ignoreCase = true) ||
                            it.contains("verify", ignoreCase = true)
                        Text(
                            text = it,
                            color = if (success) colors.success else colors.error,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                        )
                    }
                    AuthPrimaryButton(
                        text = if (isSignUp) "Sign Up" else "Sign In",
                        isBusy = isBusy,
                        colors = colors,
                        height = buttonHeight,
                        onClick = { submit() },
                    )
                    Spacer(Modifier.height(sectionGap))
                    AuthDivider(colors)
                    Spacer(Modifier.height(sectionGap))
                    GoogleAuthButton(
                        isBusy = isBusy,
                        colors = colors,
                        height = buttonHeight,
                        onClick = { continueWithGoogle() },
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = if (isSignUp) "Already have an account?" else "Don't have an account?",
                            color = palette.mutedText,
                            fontSize = 14.sp,
                        )
                        TextButton(
                            onClick = {
                                mode = if (isSignUp) AuthMode.SignIn else AuthMode.SignUp
                                message = null
                            },
                            enabled = !isBusy,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = if (isSignUp) "Sign In" else "Sign Up",
                                color = palette.accent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            ThemeCycleButton(
                darkMode = darkMode,
                nextThemeName = nextThemeName,
                buttonColor = palette.panel,
                borderColor = palette.border,
                contentColor = palette.text,
                onClick = onToggleTheme,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(18.dp)
                    .size(42.dp),
            )
        }
    }
}

@Composable
private fun FirebaseVerificationScreen(
    palette: EntryPalette,
    darkMode: Boolean,
    nextThemeName: String,
    onToggleTheme: () -> Unit,
    auth: FirebaseAuth,
    user: FirebaseUser?,
    onUserChanged: (FirebaseUser?) -> Unit,
    onSignOut: () -> Unit,
) {
    var isBusy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val email = user?.email.orEmpty()

    fun refreshUser() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onUserChanged(null)
            return
        }
        isBusy = true
        message = null
        currentUser.reload()
            .addOnCompleteListener { task ->
                isBusy = false
                val refreshedUser = auth.currentUser
                syncUserProfileToFirestore(refreshedUser)
                onUserChanged(refreshedUser)
                message = when {
                    !task.isSuccessful -> friendlyAuthMessage(task.exception, "Could not check verification.")
                    refreshedUser?.isEmailVerified == true -> "Email verified."
                    else -> "Still waiting for email verification."
                }
            }
    }

    fun resendVerification() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onUserChanged(null)
            return
        }
        isBusy = true
        message = null
        currentUser.sendEmailVerification()
            .addOnCompleteListener { task ->
                isBusy = false
                message = if (task.isSuccessful) {
                    "Verification email sent."
                } else {
                    friendlyAuthMessage(task.exception, "Could not send verification email.")
                }
            }
    }

    Surface(color = palette.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                ThemeCycleButton(
                    darkMode = darkMode,
                    nextThemeName = nextThemeName,
                    buttonColor = palette.panel,
                    borderColor = palette.border,
                    contentColor = palette.text,
                    onClick = onToggleTheme,
                )
            }
            Spacer(Modifier.height(42.dp))
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.accent.copy(alpha = if (darkMode) 0.18f else 0.12f))
                    .border(1.dp, palette.accent.copy(alpha = 0.55f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.MarkEmailRead, contentDescription = null, tint = palette.accent, modifier = Modifier.size(42.dp))
            }
            Spacer(Modifier.height(18.dp))
            Text("Verify your email", color = palette.text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(email.ifBlank { "Your Firebase account" }, color = palette.mutedText, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Surface(
                color = palette.panel,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, palette.border),
                modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Open the verification email from Firebase, then come back and check again.",
                        color = palette.mutedText,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                    )
                    message?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = it,
                            color = if (it.contains("sent", ignoreCase = true) || it.contains("verified", ignoreCase = true)) palette.accentAlt else palette.error,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { refreshUser() },
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("I verified my email", fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { resendVerification() }, enabled = !isBusy) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, tint = palette.accent)
                        Spacer(Modifier.width(6.dp))
                        Text("Resend email", color = palette.accent)
                    }
                    TextButton(onClick = onSignOut, enabled = !isBusy) {
                        Text("Use another account", color = palette.text)
                    }
                }
            }
        }
    }
}

private data class AuthColors(
    val background: Color,
    val panel: Color,
    val text: Color,
    val muted: Color,
    val border: Color,
    val fieldBorder: Color,
    val primary: Color,
    val primaryText: Color,
    val error: Color,
    val success: Color,
    val googleBlue: Color = Color(0xFF4285F4),
)

private fun authColors(palette: EntryPalette) = AuthColors(
    background = palette.background,
    panel = palette.panel,
    text = palette.text,
    muted = palette.mutedText,
    border = palette.border,
    fieldBorder = palette.accent.copy(alpha = 0.72f),
    primary = palette.accent,
    primaryText = Color.White,
    error = palette.error,
    success = palette.accentAlt,
)

@Composable
private fun AuthScreenSurface(colors: AuthColors, content: @Composable () -> Unit) {
    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        content()
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    colors: AuthColors,
    height: androidx.compose.ui.unit.Dp,
    fontSize: Int,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = { Text(placeholder, color = colors.muted, fontSize = fontSize.sp) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size((fontSize + 5).dp)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        shape = RoundedCornerShape(8.dp),
        colors = authTextFieldColors(colors),
        textStyle = LocalTextStyle.current.copy(color = colors.text, fontSize = fontSize.sp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = height),
    )
}

@Composable
private fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    label: String,
    imeAction: ImeAction,
    onDone: () -> Unit,
    colors: AuthColors,
    height: androidx.compose.ui.unit.Dp,
    fontSize: Int,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = { Text(label, color = colors.muted, fontSize = fontSize.sp) },
        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size((fontSize + 5).dp)) },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = enabled) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    modifier = Modifier.size((fontSize + 6).dp),
                )
            }
        },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        shape = RoundedCornerShape(8.dp),
        colors = authTextFieldColors(colors),
        textStyle = LocalTextStyle.current.copy(color = colors.text, fontSize = fontSize.sp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = height),
    )
}

@Composable
private fun authTextFieldColors(colors: AuthColors) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = colors.text,
    unfocusedTextColor = colors.text,
    disabledTextColor = colors.muted.copy(alpha = 0.55f),
    cursorColor = colors.text,
    focusedBorderColor = colors.fieldBorder,
    unfocusedBorderColor = colors.fieldBorder,
    disabledBorderColor = colors.fieldBorder.copy(alpha = 0.45f),
    focusedLeadingIconColor = colors.muted,
    unfocusedLeadingIconColor = colors.muted,
    disabledLeadingIconColor = colors.muted.copy(alpha = 0.45f),
    focusedTrailingIconColor = colors.muted,
    unfocusedTrailingIconColor = colors.muted,
    disabledTrailingIconColor = colors.muted.copy(alpha = 0.45f),
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
)

@Composable
private fun AuthPrimaryButton(
    text: String,
    isBusy: Boolean,
    colors: AuthColors,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = !isBusy,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.primaryText,
            disabledContainerColor = colors.primary.copy(alpha = 0.65f),
            disabledContentColor = colors.primaryText.copy(alpha = 0.85f),
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
    ) {
        if (isBusy) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.primaryText)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AuthDivider(colors: AuthColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(colors.border)
        )
        Text("or", color = colors.muted, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 18.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(colors.border)
        )
    }
}

@Composable
private fun GoogleAuthButton(
    isBusy: Boolean,
    colors: AuthColors,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isBusy,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = colors.text,
            disabledContentColor = colors.muted.copy(alpha = 0.6f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
    ) {
        Text("G", color = colors.googleBlue, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(14.dp))
        Text("Continue with Google", color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun syncUserProfileToFirestore(user: FirebaseUser?) {
    if (user == null) return

    val reference = FirebaseFirestore.getInstance()
        .collection("users")
        .document(user.uid)

    reference.get()
        .addOnCompleteListener { task ->
            val snapshot = task.takeIf { it.isSuccessful }?.result
            val existingTrialStart = snapshot?.getLong("trialStartedAtMillis")
                ?: snapshot?.getTimestamp("trialStartedAt")?.toDate()?.time
            val shouldStartTrial = snapshot == null ||
                !snapshot.exists() ||
                !snapshot.contains("trialStartedAt")
            reference.set(
                userProfileData(
                    user = user,
                    startTrial = shouldStartTrial,
                    existingTrialStart = existingTrialStart,
                ),
                SetOptions.merge(),
            )
        }
}

private fun userProfileData(
    user: FirebaseUser,
    startTrial: Boolean,
    existingTrialStart: Long?,
): Map<String, Any?> {
    val displayName = user.displayName?.takeIf { it.isNotBlank() }
        ?: user.email?.substringBefore('@')?.takeIf { it.isNotBlank() }
        ?: "PocketCodedWeb user"
    val providers = user.providerData
        .mapNotNull { it.providerId.takeIf(String::isNotBlank) }
        .filterNot { it == "firebase" }
        .distinct()
    val profile = mutableMapOf<String, Any?>(
        "uid" to user.uid,
        "app" to "PocketCodedWeb",
        "email" to user.email,
        "displayName" to displayName,
        "emailVerified" to user.isEmailVerified,
        "providers" to providers,
        "lastSignInAt" to FieldValue.serverTimestamp(),
        "lastSyncedAt" to FieldValue.serverTimestamp(),
    )

    user.photoUrl?.toString()?.takeIf { it.isNotBlank() }?.let {
        profile["photoUrl"] = it
    }
    user.metadata?.creationTimestamp?.takeIf { it > 0L }?.let {
        profile["createdAtMillis"] = it
    }
    user.metadata?.lastSignInTimestamp?.takeIf { it > 0L }?.let {
        profile["lastSignInAtMillis"] = it
    }
    if (startTrial) {
        profile["trialStartedAt"] = FieldValue.serverTimestamp()
    }

    return profile
}

private fun FirebaseUser.toLocalUser(): LocalUser {
    val cleanEmail = email.orEmpty()
    val fallbackName = cleanEmail.substringBefore('@').takeIf { it.isNotBlank() } ?: "PocketCodedWeb user"
    return LocalUser(
        id = uid,
        displayName = displayName?.takeIf { it.isNotBlank() } ?: fallbackName,
        email = cleanEmail.ifBlank { "Firebase account" },
        emailVerified = isEmailVerified,
    )
}

private fun FirebaseUser.isVerifiedForAppAccess(): Boolean {
    return isEmailVerified || providerData.any { it.providerId == "google.com" }
}

private fun friendlyAuthMessage(exception: Exception?, fallback: String): String {
    val raw = exception?.localizedMessage?.takeIf { it.isNotBlank() } ?: return fallback
    return when {
        raw.contains("network", ignoreCase = true) -> "Check your connection and try again."
        raw.contains("password", ignoreCase = true) && raw.contains("invalid", ignoreCase = true) -> "The email or password is incorrect."
        raw.contains("email address is already", ignoreCase = true) -> "An account already exists for this email."
        raw.contains("no user record", ignoreCase = true) -> "No account exists for this email."
        else -> raw
    }
}

private suspend fun signInWithGoogle(
    context: Context,
    auth: FirebaseAuth,
): Result<FirebaseUser?> = runCatching {
    val serverClientId = context.getString(R.string.default_web_client_id)
        .takeIf { it.isNotBlank() }
        ?: error("Google sign-in is missing the Firebase web client id.")
    val activity = context.findActivity()
        ?: error("Google sign-in requires an active screen. Please try again.")
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(serverClientId)
        .setAutoSelectEnabled(false)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    val response = CredentialManager.create(context).getCredential(activity, request)
    val credential = response.credential
    if (credential !is CustomCredential ||
        credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        error("Google sign-in did not return a Google ID token.")
    }

    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
    auth.signInWithCredential(firebaseCredential).awaitTask()
    auth.currentUser
}

private suspend fun clearGoogleCredentialState(context: Context) {
    runCatching {
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
    }
}

private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (!continuation.isActive) return@addOnCompleteListener
        if (task.isSuccessful) {
            @Suppress("UNCHECKED_CAST")
            continuation.resume(task.result as T)
        } else {
            continuation.resumeWithException(task.exception ?: IllegalStateException("Task failed."))
        }
    }
}

private fun friendlyGoogleSignInMessage(error: Throwable): String = when (error) {
    is GetCredentialCancellationException -> "Google sign-in cancelled."
    is NoCredentialException -> "No Google account is available on this device. Add an account and try again."
    is GetCredentialProviderConfigurationException -> googleSignInConfigurationMessage()
    is GetCredentialUnsupportedException -> "Google sign-in is unavailable on this device. Update Google Play services and try again."
    is GetCredentialInterruptedException -> "Google sign-in was interrupted. Please try again."
    is GetCredentialException -> googleSignInConfigurationMessage()
    is GoogleIdTokenParsingException -> "Google sign-in returned an invalid token."
    else -> error.localizedMessage?.takeIf { it.isNotBlank() } ?: "Could not sign in with Google."
}

private fun googleSignInConfigurationMessage(): String {
    return "Google sign-in is not fully configured. Add this app's SHA-1 and SHA-256 in Firebase Project settings, then download a fresh google-services.json."
}

@Composable
private fun PremiumPaywall(
    palette: EntryPalette,
    darkMode: Boolean,
    trialEndsAt: Long,
    trialExpired: Boolean,
    billingState: BillingUiState,
    onBuy: () -> Unit,
    onRestore: () -> Unit,
    onContinueTrial: () -> Unit,
) {
    Surface(
        color = palette.background,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.accent.copy(alpha = if (darkMode) 0.18f else 0.12f))
                    .border(1.dp, palette.accent.copy(alpha = 0.55f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Code, contentDescription = null, tint = palette.accent, modifier = Modifier.size(42.dp))
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(palette.accentAlt),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(18.dp))
            BrandTitle(palette, fontSize = 30)
            Text(
                text = if (trialExpired) {
                    "72 hour trial is over"
                } else {
                    "Upgrade to Premium or continue your trial"
                },
                color = palette.mutedText,
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Surface(
                color = if (trialExpired) palette.error.copy(alpha = 0.14f) else palette.accentAlt.copy(alpha = 0.15f),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    text = if (trialExpired) "TRIAL ENDED" else "72 HOUR FREE TRIAL",
                    color = if (trialExpired) palette.error else palette.accentAlt,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 7.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Trial ${if (trialExpired) "ended" else "ends"} on ${formatTrialDate(trialEndsAt)}",
                color = palette.mutedText,
                fontSize = 13.sp,
            )
            if (!trialExpired) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Continuing clears local coding data and resumes access until the next hourly check.",
                    color = palette.error,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            }

            Spacer(Modifier.height(22.dp))
            Surface(
                color = palette.panel,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, palette.border),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Premium includes", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(Modifier.height(12.dp))
                    PremiumFeature(Icons.Rounded.Folder, "Unlimited Web projects", "Create, import, export, and keep as many projects as your device can hold.", palette)
                    PremiumFeature(Icons.Rounded.Code, "Complete Web workspace", "Use the editor, inspections, completions, terminal, npm-style package support, and AI help.", palette)
                    PremiumFeature(Icons.Rounded.Terminal, "Browser and in-app live preview", "Run HTML/CSS/JS projects in the device browser or the in-app WebView with terminal activity preserved.", palette)
                    PremiumFeature(Icons.Rounded.Security, "One-time purchase", "Pay once through Google Play and restore the purchase on supported devices.", palette, showDivider = false)
                }
            }

            Spacer(Modifier.height(14.dp))
            Surface(
                color = palette.panel,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, palette.border),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("One-time payment", color = palette.mutedText, fontSize = 14.sp)
                    Text(
                        text = billingState.formattedPrice,
                        color = palette.accent,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("No recurring subscription", color = palette.mutedText, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                    val purchaseButtonEnabled = billingState.connected &&
                        billingState.productDetailsLoaded &&
                        billingState.productAvailable &&
                        !billingState.isPurchasePending
                    val purchaseButtonText = when {
                        billingState.isPurchasePending -> "Payment pending"
                        !billingState.connected -> "Connecting to Google Play"
                        !billingState.productDetailsLoaded -> "Loading Google Play purchase"
                        !billingState.productAvailable -> "Premium unavailable"
                        else -> "Pay ${billingState.formattedPrice} - Unlock Premium"
                    }
                    Button(
                        onClick = onBuy,
                        enabled = purchaseButtonEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.accent,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Icon(Icons.Rounded.ShoppingCart, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = purchaseButtonText,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                    billingState.message?.let { message ->
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = message,
                            color = if (billingState.isPurchasePending) palette.accentAlt else palette.mutedText,
                            fontSize = 12.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onRestore) {
                Icon(Icons.Rounded.Restore, contentDescription = null, tint = palette.accent)
                Spacer(Modifier.width(6.dp))
                Text("Restore Purchase", color = palette.accent)
            }
            if (!trialExpired) {
                TextButton(onClick = onContinueTrial) {
                    Text("Continue Free Trial", color = palette.text)
                }
                Text(
                    text = "Continuing the trial requires clearing local coding data. You will see a warning first.",
                    color = palette.error,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PremiumFeature(
    icon: ImageVector,
    title: String,
    description: String,
    palette: EntryPalette,
    showDivider: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(palette.accent.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = palette.accent, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = palette.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(description, color = palette.mutedText, fontSize = 12.sp)
        }
    }
    if (showDivider) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 50.dp)
                .height(1.dp)
                .background(palette.border)
        )
    }
}

@Composable
private fun FreeTrialEraseWarning(
    palette: EntryPalette,
    isErasing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.elevatedPanel,
        title = { Text("Clear all coding data?", color = palette.text) },
        text = {
            Column {
                Text(
                    text = "Continuing the free trial permanently deletes the app's local coding data:",
                    color = palette.mutedText,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Projects and source files\nTerminal history and editor state\nImported folders and preview cache\nPackages and local workspace data",
                    color = palette.text,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "This cannot be undone. Export anything you need before continuing. Files already exported outside the app are not deleted.",
                    color = palette.error,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isErasing) {
                if (isErasing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (isErasing) "Clearing..." else "Erase and Continue", color = palette.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isErasing) {
                Text("Go Back", color = palette.text)
            }
        },
    )
}

@Composable
private fun ProjectHomeScreen(
    user: LocalUser,
    darkMode: Boolean,
    currentThemeName: String,
    nextThemeName: String,
    onToggleTheme: () -> Unit,
    onOpenProject: (LocalProject) -> Unit,
    onSignOut: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val palette = remember(currentThemeName, darkMode) { entryPalette(currentThemeName, darkMode) }
    var projects by remember { mutableStateOf<List<LocalProject>>(emptyList()) }
    var refreshVersion by remember { mutableLongStateOf(0L) }
    var search by rememberSaveable { mutableStateOf("") }
    var createDialogOpen by remember { mutableStateOf(false) }
    var deleteProject by remember { mutableStateOf<LocalProject?>(null) }
    var renameProject by remember { mutableStateOf<LocalProject?>(null) }
    var profileMenuOpen by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refreshVersion) {
        projects = withContext(Dispatchers.IO) { discoverLocalProjects(context) }
    }

    val visibleProjects = remember(projects, search) {
        val query = search.trim()
        if (query.isBlank()) projects else projects.filter { it.name.contains(query, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .systemBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BrandTitle(palette, fontSize = 27)
                Text("Select a project to get started", color = palette.mutedText, fontSize = 14.sp)
            }
            ThemeCycleButton(
                darkMode = darkMode,
                nextThemeName = nextThemeName,
                buttonColor = palette.panel,
                borderColor = palette.border,
                contentColor = palette.text,
                onClick = onToggleTheme,
            )
            Spacer(Modifier.width(8.dp))
            Box {
                IconButton(
                    onClick = { profileMenuOpen = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(palette.elevatedPanel)
                        .border(1.dp, palette.border, CircleShape),
                ) {
                    Text(
                        text = user.displayName.firstOrNull()?.uppercase() ?: "P",
                        color = palette.text,
                        fontWeight = FontWeight.Bold,
                    )
                }
                DropdownMenu(
                    expanded = profileMenuOpen,
                    onDismissRequest = { profileMenuOpen = false },
                    containerColor = palette.elevatedPanel,
                ) {
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(user.displayName, color = palette.text)
                                Text(user.email, color = palette.mutedText, fontSize = 12.sp)
                                if (user.emailVerified) {
                                    Text("Verified account", color = palette.accent, fontSize = 12.sp)
                                }
                            }
                        },
                        onClick = { profileMenuOpen = false },
                        leadingIcon = { Icon(Icons.Rounded.Person, null, tint = palette.accent) },
                    )
                    HorizontalDivider(color = palette.border)
                    DropdownMenuItem(
                        text = { Text("Sign out", color = palette.text) },
                        onClick = {
                            profileMenuOpen = false
                            onSignOut()
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = palette.accent) },
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        Surface(
            color = palette.panel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, palette.border),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { createDialogOpen = true },
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("New Project", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Create a new Web project", color = palette.mutedText, fontSize = 13.sp)
                }
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Create project",
                    tint = palette.accent,
                )
            }
        }
        Spacer(Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Existing Projects",
                color = palette.text,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Rounded.Search, contentDescription = null, tint = palette.mutedText)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search projects") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        message?.let {
            Text(it, color = palette.error, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(12.dp))

        if (visibleProjects.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Rounded.Folder, contentDescription = null, tint = palette.mutedText, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (search.isBlank()) "No projects yet" else "No matching projects",
                    color = palette.text,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (search.isBlank()) "Create your first project to start coding." else "Try another search.",
                    color = palette.mutedText,
                    fontSize = 13.sp,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(visibleProjects, key = { it.directory.absolutePath }) { project ->
                    ProjectRow(
                        project = project,
                        palette = palette,
                        onOpen = { onOpenProject(project) },
                        onRename = { renameProject = project },
                        onDelete = { deleteProject = project },
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }

    if (createDialogOpen) {
        CreateProjectDialog(
            palette = palette,
            onDismiss = { createDialogOpen = false },
            onCreate = { name ->
                coroutineScope.launch {
                    val result = withContext(Dispatchers.IO) { createLocalProject(context, name) }
                    result.onSuccess { project ->
                        createDialogOpen = false
                        refreshVersion += 1L
                        onOpenProject(project)
                    }.onFailure { error ->
                        message = error.message ?: "Could not create the project."
                    }
                }
            },
        )
    }

    renameProject?.let { project ->
        RenameProjectDialog(
            project = project,
            palette = palette,
            onDismiss = { renameProject = null },
            onRename = { name ->
                renameLocalProject(context, project, name)
                    .fold(
                        onSuccess = {
                            renameProject = null
                            message = null
                            refreshVersion += 1L
                            null
                        },
                        onFailure = { error -> error.message ?: "Could not rename the project." },
                    )
            },
        )
    }

    deleteProject?.let { project ->
        AlertDialog(
            onDismissRequest = { deleteProject = null },
            containerColor = palette.elevatedPanel,
            title = { Text("Delete ${project.name}?", color = palette.text) },
            text = { Text("This permanently removes the project and all of its files.", color = palette.mutedText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) { project.directory.deleteRecursively() }
                            deleteProject = null
                            refreshVersion += 1L
                        }
                    }
                ) {
                    Text("Delete", color = palette.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteProject = null }) { Text("Cancel", color = palette.text) }
            },
        )
    }
}

@Composable
private fun ProjectRow(
    project: LocalProject,
    palette: EntryPalette,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Surface(
        color = palette.panel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, palette.border),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.accentAlt.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Code, contentDescription = null, tint = palette.accentAlt)
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    color = palette.text,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (project.canDelete) {
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Project options", tint = palette.mutedText)
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        containerColor = palette.elevatedPanel,
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open", color = palette.text) },
                            onClick = { menuOpen = false; onOpen() },
                            leadingIcon = { Icon(Icons.Rounded.Folder, null, tint = palette.accent) },
                        )
                        DropdownMenuItem(
                            text = { Text("Rename", color = palette.text) },
                            onClick = { menuOpen = false; onRename() },
                            leadingIcon = { Icon(Icons.Rounded.Edit, null, tint = palette.accent) },
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = palette.error) },
                            onClick = { menuOpen = false; onDelete() },
                            leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = palette.error) },
                        )
                    }
                }
            } else {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Open project", tint = palette.accent)
                Spacer(Modifier.width(10.dp))
            }
        }
    }
}

@Composable
private fun RenameProjectDialog(
    project: LocalProject,
    palette: EntryPalette,
    onDismiss: () -> Unit,
    onRename: (String) -> String?,
) {
    var name by rememberSaveable(project.directory.absolutePath) { mutableStateOf(project.name) }
    var error by remember(project.directory.absolutePath) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.elevatedPanel,
        title = { Text("Rename Project", color = palette.text) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Project name") },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val validation = validateProjectName(name)
                            error = validation ?: onRename(name)
                        }
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let {
                    Text(it, color = palette.error, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validation = validateProjectName(name)
                    error = validation ?: onRename(name)
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = palette.text) }
        },
    )
}

@Composable
private fun CreateProjectDialog(
    palette: EntryPalette,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.elevatedPanel,
        title = { Text("New Web Project", color = palette.text) },
        text = {
            Column {
                Text("Choose a name for the project folder.", color = palette.mutedText, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Project name") },
                    leadingIcon = { Icon(Icons.Rounded.Folder, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val validation = validateProjectName(name)
                            if (validation == null) onCreate(name) else error = validation
                        }
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let { Text(it, color = palette.error, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validation = validateProjectName(name)
                    if (validation == null) onCreate(name) else error = validation
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = palette.text) }
        },
    )
}

@Composable
private fun BrandTitle(palette: EntryPalette, fontSize: Int = 31) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Pocket", color = palette.text, fontSize = fontSize.sp, fontWeight = FontWeight.Bold)
        Text("CodedWeb", color = palette.accent, fontSize = fontSize.sp, fontWeight = FontWeight.Bold)
    }
}

private fun entryPalette(themeName: String, darkMode: Boolean): EntryPalette = when (themeName) {
    "CyberpunkElectricDark", "Cyberpunk", "MidnightNeon" -> darkEntryPalette(
        background = Color(0xFF070617),
        panel = Color(0xFF0A0C24),
        elevatedPanel = Color(0xFF11122D),
        border = Color(0xFFFF2D75).copy(alpha = 0.38f),
        accent = Color(0xFFFF2D75),
    )
    "CyberpunkElectricLight" -> lightEntryPalette(
        background = Color(0xFFFFFBF8),
        elevatedPanel = Color(0xFFFFE7F0),
        border = Color(0xFFFFC4DC),
        accent = Color(0xFFFF2D75),
    )
    "ForestNightDark", "ForestDark", "SoftMint" -> darkEntryPalette(
        background = Color(0xFF031611),
        panel = Color(0xFF042018),
        elevatedPanel = Color(0xFF062D21),
        border = Color(0xFF22C55E).copy(alpha = 0.38f),
        accent = Color(0xFF22C55E),
    )
    "ForestNightLight" -> lightEntryPalette(
        background = Color(0xFFF7FFF9),
        elevatedPanel = Color(0xFFECFDF5),
        border = Color(0xFFD1FAE5),
        accent = Color(0xFF22C55E),
    )
    "SunsetDuskDark" -> darkEntryPalette(
        background = Color(0xFF1E0F0B),
        panel = Color(0xFF180F18),
        elevatedPanel = Color(0xFF2A1510),
        border = Color(0xFFFB923C).copy(alpha = 0.38f),
        accent = Color(0xFFFB923C),
    )
    "SunsetDuskLight", "RosePine", "Solarized" -> lightEntryPalette(
        background = Color(0xFFFFFDF9),
        elevatedPanel = Color(0xFFFFF0D0),
        border = Color(0xFFFED7AA),
        accent = Color(0xFFFB923C),
    )
    "ArcticAuroraDark", "OceanicNext", "SereneBlue" -> darkEntryPalette(
        background = Color(0xFF031A24),
        panel = Color(0xFF052536),
        elevatedPanel = Color(0xFF073047),
        border = Color(0xFF06B6D4).copy(alpha = 0.38f),
        accent = Color(0xFF06B6D4),
    )
    "ArcticAuroraLight" -> lightEntryPalette(
        background = Color(0xFFF7FEFF),
        elevatedPanel = Color(0xFFF0FDFF),
        border = Color(0xFFBAE6FD),
        accent = Color(0xFF06B6D4),
    )
    "VioletHazeDark", "Amethyst", "Cloudy" -> darkEntryPalette(
        background = Color(0xFF1A123A),
        panel = Color(0xFF201540),
        elevatedPanel = Color(0xFF281A4F),
        border = Color(0xFFA78BFA).copy(alpha = 0.38f),
        accent = Color(0xFFA78BFA),
    )
    "VioletHazeLight" -> lightEntryPalette(
        background = Color(0xFFFAF5FF),
        elevatedPanel = Color(0xFFEDE9FE),
        border = Color(0xFFDDD6FE),
        accent = Color(0xFFA78BFA),
    )
    "MonochromeProDark", "Nordic" -> darkEntryPalette(
        background = Color(0xFF111827),
        panel = Color(0xFF111827),
        elevatedPanel = Color(0xFF1F2937),
        border = Color(0xFF64748B).copy(alpha = 0.42f),
        accent = Color(0xFF64748B),
    )
    "MonochromeProLight", "Paper" -> lightEntryPalette(
        background = Color(0xFFFFFFFF),
        elevatedPanel = Color(0xFFF3F4F6),
        border = Color(0xFFE5E7EB),
        accent = Color(0xFF64748B),
    )
    "VSCodeDefaultDark" -> darkEntryPalette(
        background = Color(0xFF1E1E1E),
        panel = Color(0xFF252526),
        elevatedPanel = Color(0xFF2D2D30),
        border = Color(0xFF3C3C3C),
        accent = Color(0xFF007ACC),
    )
    "VSCodeDefaultLight" -> lightEntryPalette(
        background = Color(0xFFFFFFFF),
        elevatedPanel = Color(0xFFF3F3F3),
        border = Color(0xFFE5E5E5),
        accent = Color(0xFF007ACC),
    )
    else -> if (darkMode) {
        darkEntryPalette(
            background = Color(0xFF070617),
            panel = Color(0xFF0A0C24),
            elevatedPanel = Color(0xFF11122D),
            border = Color(0xFFFF2D75).copy(alpha = 0.38f),
            accent = Color(0xFFFF2D75),
        )
    } else {
        lightEntryPalette(
            background = Color(0xFFFFFBF8),
            elevatedPanel = Color(0xFFFFE7F0),
            border = Color(0xFFFFC4DC),
            accent = Color(0xFFFF2D75),
        )
    }
}

private fun darkEntryPalette(
    background: Color,
    panel: Color,
    elevatedPanel: Color,
    border: Color,
    accent: Color,
): EntryPalette = EntryPalette(
    background = background,
    panel = panel,
    elevatedPanel = elevatedPanel,
    text = Color.White,
    mutedText = Color(0xFFD8DEEB),
    border = border,
    accent = accent,
    accentAlt = accent,
    error = Color(0xFFFF7B7B),
)

private fun lightEntryPalette(
    background: Color,
    elevatedPanel: Color,
    border: Color,
    accent: Color,
): EntryPalette = EntryPalette(
    background = background,
    panel = Color.White,
    elevatedPanel = elevatedPanel,
    text = Color(0xFF151A24),
    mutedText = Color(0xFF475569),
    border = border,
    accent = accent,
    accentAlt = accent,
    error = Color(0xFFB42318),
)

@Composable
private fun ThemeCycleButton(
    darkMode: Boolean,
    nextThemeName: String,
    buttonColor: Color,
    borderColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(40.dp),
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = buttonColor,
        border = BorderStroke(1.dp, borderColor),
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (darkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                contentDescription = "Cycle theme to $nextThemeName",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

internal fun isTrialExpired(now: Long, trialEndsAt: Long): Boolean = now >= trialEndsAt

internal fun trialEndTimestamp(trialStartedAt: Long): Long = trialStartedAt + TRIAL_DURATION_MS

internal fun nextTrialReminderAt(trialStartedAt: Long, lastContinuationAt: Long): Long {
    val baseline = if (lastContinuationAt > trialStartedAt) lastContinuationAt else trialStartedAt
    return baseline + TRIAL_REMINDER_INTERVAL_MS
}

internal fun shouldShowTrialGate(
    now: Long,
    trialStartedAt: Long,
    lastContinuationAt: Long,
    isPremium: Boolean,
): Boolean {
    if (isPremium) return false
    val trialEndsAt = trialStartedAt + TRIAL_DURATION_MS
    return isTrialExpired(now, trialEndsAt) ||
        now >= nextTrialReminderAt(trialStartedAt, lastContinuationAt)
}

private fun trialPreferences(context: Context) =
    context.getSharedPreferences(TRIAL_PREFS_NAME, Context.MODE_PRIVATE)

private fun trialStartKey(uid: String) = "trial_started_$uid"

private fun trialContinuationKey(uid: String) = "trial_continued_$uid"

private fun loadTrialStart(context: Context, uid: String): Long? {
    val value = trialPreferences(context).getLong(trialStartKey(uid), 0L)
    return value.takeIf { it > 0L }
}

private fun saveTrialStart(context: Context, uid: String, value: Long) {
    trialPreferences(context).edit().putLong(trialStartKey(uid), value).apply()
}

private fun loadLastTrialContinuation(context: Context, uid: String): Long {
    return trialPreferences(context).getLong(trialContinuationKey(uid), 0L)
}

private fun saveLastTrialContinuation(context: Context, uid: String, value: Long) {
    trialPreferences(context).edit().putLong(trialContinuationKey(uid), value).apply()
}

private fun formatTrialDate(timeMillis: Long): String {
    return SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.getDefault()).format(Date(timeMillis))
}

internal fun discoverLocalProjects(context: Context): List<LocalProject> {
    val projects = mutableListOf<LocalProject>()
    val legacyWorkspace = File(context.filesDir, "workspace")
    if (legacyWorkspace.isDirectory && legacyWorkspace.listFiles().orEmpty().any { !it.name.startsWith('.') }) {
        projects += LocalProject(
            name = "My Workspace",
            directory = legacyWorkspace,
            modifiedAt = projectModifiedAt(legacyWorkspace),
            canDelete = false,
        )
    }

    val projectsDirectory = File(context.filesDir, "projects").apply { mkdirs() }
    projectsDirectory.listFiles()
        .orEmpty()
        .filter { directory ->
            directory.isDirectory && isVisibleProjectDirectoryName(directory.name)
        }
        .forEach { directory ->
            projects += LocalProject(
                name = directory.name,
                directory = directory,
                modifiedAt = projectModifiedAt(directory),
                canDelete = true,
            )
        }
    return projects.sortedByDescending(LocalProject::modifiedAt)
}

internal fun renameLocalProject(
    context: Context,
    project: LocalProject,
    rawName: String,
): Result<LocalProject> = runCatching {
    check(project.canDelete) { "This project cannot be renamed." }
    validateProjectName(rawName)?.let { error(it) }

    val name = rawName.trim()
    val projectsDirectory = File(context.filesDir, "projects").apply { mkdirs() }.canonicalFile
    val source = project.directory.canonicalFile
    check(source.parentFile == projectsDirectory) { "This project is outside the projects folder." }
    check(source.isDirectory) { "The project folder no longer exists." }

    val target = File(projectsDirectory, name).canonicalFile
    check(target.parentFile == projectsDirectory) { "Invalid project name." }
    if (source == target) return@runCatching project
    check(!target.exists()) { "A project named $name already exists." }
    check(source.renameTo(target)) { "Could not rename the project folder." }

    LocalProject(
        name = name,
        directory = target,
        modifiedAt = projectModifiedAt(target),
        canDelete = true,
    )
}

internal fun createLocalProject(context: Context, rawName: String): Result<LocalProject> = runCatching {
    validateProjectName(rawName)?.let { error(it) }
    val name = rawName.trim()
    val projectsDirectory = File(context.filesDir, "projects").apply { mkdirs() }
    val projectDirectory = File(projectsDirectory, name)
    check(projectDirectory.canonicalFile.parentFile == projectsDirectory.canonicalFile) { "Invalid project name." }
    check(!projectDirectory.exists()) { "A project named $name already exists." }
    check(projectDirectory.mkdirs()) { "Could not create the project folder." }
    File(projectDirectory, "index.html").writeText(
        """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$name</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <main class="app">
        <h1>$name</h1>
        <p>Edit HTML, CSS, and JavaScript offline in PocketCoded Web.</p>
        <button id="actionButton" type="button">Run JavaScript</button>
        <p id="output"></p>
    </main>
    <script src="script.js"></script>
</body>
</html>
""".trimIndent()
    )
    File(projectDirectory, "style.css").writeText(
        """:root {
    color-scheme: light dark;
    font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
}

body {
    margin: 0;
    min-height: 100vh;
    display: grid;
    place-items: center;
    background: #f6f9ff;
    color: #151a24;
}

.app {
    width: min(92vw, 680px);
    padding: 24px;
}

button {
    border: 0;
    border-radius: 8px;
    padding: 12px 16px;
    background: #426bde;
    color: white;
    font-weight: 700;
}
""".trimIndent()
    )
    File(projectDirectory, "script.js").writeText(
        """const button = document.querySelector('#actionButton');
const output = document.querySelector('#output');

button?.addEventListener('click', () => {
    output.textContent = 'JavaScript is running in the PocketCoded Web preview.';
});
""".trimIndent()
    )
    LocalProject(
        name = name,
        directory = projectDirectory,
        modifiedAt = projectModifiedAt(projectDirectory),
        canDelete = true,
    )
}

internal fun validateProjectName(rawName: String): String? {
    val name = rawName.trim()
    if (name.isBlank()) return "Enter a project name."
    if (name.length > 60) return "Use a project name under 60 characters."
    if (name.any { it in "\\/:*?\"<>|" }) return "The project name contains an unsupported character."
    if (name.startsWith('.')) return "Project names cannot start with a period."
    if (!isVisibleProjectDirectoryName(name)) return "That name is reserved for app runtime files."
    return null
}

internal fun isVisibleProjectDirectoryName(name: String): Boolean {
    if (name.startsWith('.')) return false
    return name.lowercase() !in setOf("bin", "tmp")
}

private fun projectModifiedAt(directory: File): Long {
    return directory.walkTopDown()
        .maxOfOrNull(File::lastModified)
        ?.takeIf { it > 0L }
        ?: directory.lastModified()
}
