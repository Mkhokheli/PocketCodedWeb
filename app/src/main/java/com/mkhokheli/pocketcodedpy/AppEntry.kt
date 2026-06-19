package com.mkhokheli.pocketcodedpy

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class AuthenticationMode {
    Login,
    SignUp,
}

private const val TRIAL_DURATION_MS = 72L * 60L * 60L * 1000L
private const val TRIAL_REMINDER_INTERVAL_MS = 60L * 60L * 1000L
private const val TRIAL_PREFS_NAME = "premium_trial"

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
internal fun PocketCodedPyEntry(
    darkMode: Boolean,
    currentThemeName: String,
    nextThemeName: String,
    onToggleTheme: () -> Unit,
    ideContent: @Composable (projectDirectory: File, onBackToProjects: () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val auth = remember { FirebaseAuth.getInstance() }
    val credentialManager = remember { CredentialManager.create(context) }
    val coroutineScope = rememberCoroutineScope()
    val palette = remember(currentThemeName, darkMode) { entryPalette(currentThemeName, darkMode) }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var selectedProjectPath by rememberSaveable { mutableStateOf<String?>(null) }
    var billingState by remember { mutableStateOf(BillingUiState()) }
    var billingManager by remember { mutableStateOf<PlayBillingManager?>(null) }
    var trialStartedAt by remember { mutableStateOf<Long?>(null) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var lastTrialContinuationAt by remember { mutableLongStateOf(0L) }
    var eraseWarningOpen by remember { mutableStateOf(false) }
    var isErasingTrialData by remember { mutableStateOf(false) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    var lastUid by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != lastUid) {
            selectedProjectPath = null
            lastUid = uid
        }
        eraseWarningOpen = false
        lastTrialContinuationAt = uid?.let { loadLastTrialContinuation(context, it) } ?: 0L
    }

    val user = currentUser
    DisposableEffect(user?.uid) {
        val manager = user?.let { signedInUser ->
            billingState = BillingUiState()
            PlayBillingManager(
                context = context,
                accountId = signedInUser.uid,
                onStateChanged = { billingState = it },
            ).also {
                billingManager = it
                it.start()
            }
        }
        onDispose {
            manager?.close()
            if (billingManager === manager) billingManager = null
        }
    }

    DisposableEffect(user?.uid) {
        val signedInUser = user
        if (signedInUser == null) {
            trialStartedAt = null
            onDispose { }
        } else {
            val reference = FirebaseFirestore.getInstance()
                .collection("users")
                .document(signedInUser.uid)
            
            // Listen for Premium status from Firestore (Cross-device sync)
            val premiumRegistration = reference.addSnapshotListener { snapshot, _ ->
                val firestorePremium = snapshot?.getBoolean("isPremium") ?: false
                if (firestorePremium && !billingState.isPremium) {
                    billingState = billingState.copy(isPremium = true)
                }
            }

            val localFallback = loadTrialStart(context, signedInUser.uid)
                ?: System.currentTimeMillis().also { saveTrialStart(context, signedInUser.uid, it) }
            trialStartedAt = localFallback
            val registration = reference.addSnapshotListener { snapshot, _ ->
                snapshot?.getTimestamp("trialStartedAt")?.toDate()?.time?.let { serverStart ->
                    trialStartedAt = serverStart
                    saveTrialStart(context, signedInUser.uid, serverStart)
                }
            }
            reference.get().addOnSuccessListener { snapshot ->
                if (!snapshot.contains("trialStartedAt")) {
                    reference.set(
                        mapOf("trialStartedAt" to FieldValue.serverTimestamp()),
                        SetOptions.merge(),
                    )
                }
            }
            onDispose { 
                premiumRegistration.remove()
                registration.remove() 
            }
        }
    }

    LaunchedEffect(user?.uid, billingState.isPremium) {
        while (user != null && !billingState.isPremium) {
            now = System.currentTimeMillis()
            delay(15_000L)
        }
    }

    val trialStart = trialStartedAt
    val trialEndsAt = trialStart?.plus(TRIAL_DURATION_MS)
    val trialExpired = trialEndsAt?.let { isTrialExpired(now, it) } ?: false
    val nextReminderAt = trialStart?.let { start -> nextTrialReminderAt(start, lastTrialContinuationAt) }
    val reminderDue = nextReminderAt?.let { now >= it } ?: false
    val showPaywall = user != null && !billingState.isPremium && (trialExpired || reminderDue)

    fun signOut() {
        selectedProjectPath = null
        auth.signOut()
        coroutineScope.launch {
            runCatching {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            user == null -> AuthenticationScreen(
                darkMode = darkMode,
                currentThemeName = currentThemeName,
                nextThemeName = nextThemeName,
                onToggleTheme = onToggleTheme,
                auth = auth,
                credentialManager = credentialManager,
            )

            selectedProjectPath == null -> ProjectHomeScreen(
                user = user,
                darkMode = darkMode,
                currentThemeName = currentThemeName,
                nextThemeName = nextThemeName,
                onToggleTheme = onToggleTheme,
                onOpenProject = { project -> selectedProjectPath = project.directory.absolutePath },
                onSignOut = ::signOut,
            )

            else -> {
                val projectDirectory = File(selectedProjectPath.orEmpty()).apply { mkdirs() }
                key(projectDirectory.absolutePath) {
                    ideContent(projectDirectory) { selectedProjectPath = null }
                }
            }
        }

        if (showPaywall && user != null && trialEndsAt != null) {
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
                onContinueTrial = { eraseWarningOpen = true },
                onSignOut = ::signOut,
            )
        }

        if (eraseWarningOpen && user != null) {
            FreeTrialEraseWarning(
                palette = palette,
                isErasing = isErasingTrialData,
                onDismiss = { if (!isErasingTrialData) eraseWarningOpen = false },
                onConfirm = {
                    if (!isErasingTrialData) {
                        isErasingTrialData = true
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) { clearLocalCodingData(context) }
                            val continuedAt = System.currentTimeMillis()
                            selectedProjectPath = null
                            saveLastTrialContinuation(context, user.uid, continuedAt)
                            lastTrialContinuationAt = continuedAt
                            now = continuedAt
                            isErasingTrialData = false
                            eraseWarningOpen = false
                        }
                    }
                },
            )
        }
    }
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
    onSignOut: () -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (!trialExpired) {
                    IconButton(onClick = onContinueTrial) {
                        Icon(Icons.Rounded.Close, contentDescription = "Continue free trial", tint = palette.text)
                    }
                }
            }

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
                text = if (trialExpired) "Your free trial has ended" else "Unlock unlimited coding",
                color = palette.mutedText,
                fontSize = 17.sp,
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
                    PremiumFeature(Icons.Rounded.Folder, "Unlimited local projects", "Create, import, export, and keep as many projects as your device can hold.", palette)
                    PremiumFeature(Icons.Rounded.Code, "Complete Python workspace", "Use the editor, inspections, IntelliSense, packages, virtual environments, and testing tools.", palette)
                    PremiumFeature(Icons.Rounded.Terminal, "Advanced terminal", "Use Git, SSH, curl, Bash support, and project-aware terminal commands.", palette)
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
                    Button(
                        onClick = onBuy,
                        enabled = !billingState.isPurchasePending,
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
                            text = if (billingState.isPurchasePending) "Payment pending" else "Pay ${billingState.formattedPrice} - Unlock Premium",
                            fontWeight = FontWeight.Bold,
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            }
            TextButton(onClick = onSignOut) {
                Text("Sign out", color = palette.mutedText)
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
                    text = "Projects and source files\nTerminal history and editor state\nGit repositories and credentials\nSSH keys, packages, and virtual environments",
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
private fun AuthenticationScreen(
    darkMode: Boolean,
    currentThemeName: String,
    nextThemeName: String,
    onToggleTheme: () -> Unit,
    auth: FirebaseAuth,
    credentialManager: CredentialManager,
) {
    val context = LocalContext.current
    val serverClientId = stringResource(R.string.default_web_client_id)
    val coroutineScope = rememberCoroutineScope()
    val palette = remember(currentThemeName, darkMode) { entryPalette(currentThemeName, darkMode) }
    var mode by rememberSaveable { mutableStateOf(AuthenticationMode.Login) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    fun finishAuthentication(user: FirebaseUser?, suppliedName: String? = null) {
        user ?: return
        persistUserProfile(user, suppliedName)
        loading = false
    }

    fun submitEmailAuthentication() {
        val cleanEmail = email.trim()
        message = validateAuthenticationInput(mode, fullName, cleanEmail, password, confirmPassword)
        if (message != null) return
        loading = true

        if (mode == AuthenticationMode.Login) {
            auth.signInWithEmailAndPassword(cleanEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        finishAuthentication(task.result?.user)
                    } else {
                        loading = false
                        message = friendlyAuthenticationError(task.exception)
                    }
                }
        } else {
            auth.createUserWithEmailAndPassword(cleanEmail, password)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        loading = false
                        message = friendlyAuthenticationError(task.exception)
                        return@addOnCompleteListener
                    }
                    val user = task.result?.user
                    val cleanName = fullName.trim()
                    user?.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(cleanName)
                            .build()
                    )?.addOnCompleteListener {
                        finishAuthentication(user, cleanName)
                    } ?: finishAuthentication(user, cleanName)
                }
        }
    }

    fun signInWithGoogle() {
        loading = true
        message = null
        coroutineScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                
                val activity = context.findActivity()
                if (activity == null) {
                    loading = false
                    message = "Google sign-in requires an active screen. Please try again."
                    return@launch
                }

                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (
                    credential !is CustomCredential ||
                    credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    loading = false
                    message = "Google returned an unsupported credential."
                    return@launch
                }
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            finishAuthentication(task.result?.user, googleCredential.displayName)
                        } else {
                            loading = false
                            message = friendlyAuthenticationError(
                                error = task.exception,
                                provider = AuthenticationProvider.Google,
                            )
                        }
                    }
            } catch (error: GetCredentialCancellationException) {
                loading = false
                message = "Google sign-in was cancelled."
            } catch (error: NoCredentialException) {
                loading = false
                message = "No Google account is available on this device. Add an account and try again."
            } catch (error: GetCredentialProviderConfigurationException) {
                loading = false
                message = googleSignInConfigurationMessage()
            } catch (error: GetCredentialUnsupportedException) {
                loading = false
                message = "Google sign-in is unavailable on this device. Update Google Play services and try again."
            } catch (error: GetCredentialInterruptedException) {
                loading = false
                message = "Google sign-in was interrupted. Please try again."
            } catch (error: GetCredentialException) {
                loading = false
                message = googleSignInConfigurationMessage()
            } catch (error: Throwable) {
                loading = false
                message = error.message ?: "Google sign-in failed."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(34.dp))
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.elevatedPanel)
                    .border(1.dp, palette.accent.copy(alpha = 0.55f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Code,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(42.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            BrandTitle(palette)
            Text(
                text = "Code. Learn. Build. Anywhere.",
                color = palette.mutedText,
                fontSize = 15.sp,
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = if (mode == AuthenticationMode.Login) "Welcome Back" else "Create Account",
                color = palette.text,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (mode == AuthenticationMode.Login) {
                    "Sign in to continue coding"
                } else {
                    "Create your PocketCodedPy account"
                },
                color = palette.mutedText,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(22.dp))

            if (mode == AuthenticationMode.SignUp) {
                AuthenticationField(
                    value = fullName,
                    onValueChange = { fullName = it; message = null },
                    label = "Full name",
                    leadingIcon = Icons.Rounded.Person,
                    palette = palette,
                    imeAction = ImeAction.Next,
                )
                Spacer(Modifier.height(10.dp))
            }
            AuthenticationField(
                value = email,
                onValueChange = { email = it; message = null },
                label = "Email",
                leadingIcon = Icons.Rounded.Email,
                palette = palette,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            )
            Spacer(Modifier.height(10.dp))
            AuthenticationField(
                value = password,
                onValueChange = { password = it; message = null },
                label = "Password",
                leadingIcon = Icons.Rounded.Lock,
                palette = palette,
                keyboardType = KeyboardType.Password,
                imeAction = if (mode == AuthenticationMode.Login) ImeAction.Done else ImeAction.Next,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = palette.mutedText,
                        )
                    }
                },
                onDone = if (mode == AuthenticationMode.Login) ::submitEmailAuthentication else null,
            )
            if (mode == AuthenticationMode.SignUp) {
                Spacer(Modifier.height(10.dp))
                AuthenticationField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; message = null },
                    label = "Confirm password",
                    leadingIcon = Icons.Rounded.Lock,
                    palette = palette,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    onDone = ::submitEmailAuthentication,
                )
            }

            if (mode == AuthenticationMode.Login) {
                TextButton(
                    onClick = {
                        val cleanEmail = email.trim()
                        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                            message = "Enter your email address first."
                        } else {
                            loading = true
                            auth.sendPasswordResetEmail(cleanEmail).addOnCompleteListener { task ->
                                loading = false
                                message = if (task.isSuccessful) {
                                    "Password reset email sent."
                                } else {
                                    friendlyAuthenticationError(task.exception)
                                }
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Forgot password?", color = palette.accent, fontSize = 13.sp)
                }
            } else {
                Spacer(Modifier.height(12.dp))
            }

            message?.let {
                Text(
                    text = it,
                    color = if (it.contains("sent", ignoreCase = true)) palette.accentAlt else palette.error,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                )
            }

            Button(
                onClick = ::submitEmailAuthentication,
                enabled = !loading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accent,
                    contentColor = Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp),
                    )
                } else {
                    Text(
                        text = if (mode == AuthenticationMode.Login) "Sign In" else "Sign Up",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f).height(1.dp).background(palette.border))
                Text("or", color = palette.mutedText, modifier = Modifier.padding(horizontal = 14.dp))
                Box(Modifier.weight(1f).height(1.dp).background(palette.border))
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = ::signInWithGoogle,
                enabled = !loading,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, palette.border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.text),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Spacer(Modifier.width(12.dp))
                Text("Continue with Google", fontSize = 15.sp)
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (mode == AuthenticationMode.Login) {
                        "Don't have an account?"
                    } else {
                        "Already have an account?"
                    },
                    color = palette.mutedText,
                    fontSize = 14.sp,
                )
                TextButton(
                    onClick = {
                        mode = if (mode == AuthenticationMode.Login) AuthenticationMode.SignUp else AuthenticationMode.Login
                        message = null
                    }
                ) {
                    Text(
                        text = if (mode == AuthenticationMode.Login) "Sign Up" else "Sign In",
                        color = palette.accent,
                        fontWeight = FontWeight.SemiBold,
                    )
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

@Composable
private fun AuthenticationField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    palette: EntryPalette,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
    onDone: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(leadingIcon, contentDescription = null, tint = palette.mutedText)
        },
        trailingIcon = trailingIcon,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ProjectHomeScreen(
    user: FirebaseUser,
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
                        text = user.displayName?.firstOrNull()?.uppercase() ?: user.email?.firstOrNull()?.uppercase() ?: "U",
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
                                Text(user.displayName ?: "PocketCodedPy user", color = palette.text)
                                Text(user.email.orEmpty(), color = palette.mutedText, fontSize = 12.sp)
                            }
                        },
                        onClick = { profileMenuOpen = false },
                        leadingIcon = { Icon(Icons.Rounded.Person, null, tint = palette.accent) },
                    )
                    DropdownMenuItem(
                        text = { Text("Sign out", color = palette.text) },
                        onClick = {
                            profileMenuOpen = false
                            onSignOut()
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = palette.error) },
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
                    Text("Create a new Python project", color = palette.mutedText, fontSize = 13.sp)
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
        title = { Text("New Python Project", color = palette.text) },
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
        Text("CodedPy", color = palette.accent, fontSize = fontSize.sp, fontWeight = FontWeight.Bold)
    }
}

private fun entryPalette(themeName: String, darkMode: Boolean): EntryPalette = when (themeName) {
    "DarkBlue" -> darkEntryPalette(
        background = Color(0xFF071524),
        panel = Color(0xFF0D2944),
        elevatedPanel = Color(0xFF091E33),
        border = Color(0xFF315D82),
        accent = Color(0xFF65A8FF),
    )
    "LightBlue" -> lightEntryPalette(
        background = Color(0xFFF6F9FF),
        elevatedPanel = Color(0xFFF1F5FF),
        border = Color(0xFF9BB6F4),
        accent = Color(0xFF426BDE),
    )
    "DarkPurple" -> darkEntryPalette(
        background = Color(0xFF100A1D),
        panel = Color(0xFF24163B),
        elevatedPanel = Color(0xFF1A102B),
        border = Color(0xFF70449A),
        accent = Color(0xFFCA83FF),
    )
    "LightPurple" -> lightEntryPalette(
        background = Color(0xFFFCF8FF),
        elevatedPanel = Color(0xFFF8F1FF),
        border = Color(0xFFC79AEF),
        accent = Color(0xFF8A45D8),
    )
    "DarkGreen" -> darkEntryPalette(
        background = Color(0xFF031611),
        panel = Color(0xFF073727),
        elevatedPanel = Color(0xFF05271D),
        border = Color(0xFF24845D),
        accent = Color(0xFF4DE09B),
    )
    "LightGreen" -> lightEntryPalette(
        background = Color(0xFFF7FCF9),
        elevatedPanel = Color(0xFFF0F9F4),
        border = Color(0xFF8ACCA5),
        accent = Color(0xFF278F58),
    )
    "DarkTeal" -> darkEntryPalette(
        background = Color(0xFF031A20),
        panel = Color(0xFF073D47),
        elevatedPanel = Color(0xFF052A32),
        border = Color(0xFF218696),
        accent = Color(0xFF31D4DF),
    )
    "LightTeal" -> lightEntryPalette(
        background = Color(0xFFF5FCFD),
        elevatedPanel = Color(0xFFEDF9FA),
        border = Color(0xFF7CC8D0),
        accent = Color(0xFF168899),
    )
    "DarkOrange" -> darkEntryPalette(
        background = Color(0xFF1B0E03),
        panel = Color(0xFF352214),
        elevatedPanel = Color(0xFF27170B),
        border = Color(0xFF9B5717),
        accent = Color(0xFFFF9A35),
    )
    "LightOrange" -> lightEntryPalette(
        background = Color(0xFFFFFAF6),
        elevatedPanel = Color(0xFFFFF4EC),
        border = Color(0xFFFFB57D),
        accent = Color(0xFFE66516),
    )
    "DarkGray" -> darkEntryPalette(
        background = Color(0xFF101417),
        panel = Color(0xFF20272C),
        elevatedPanel = Color(0xFF181E22),
        border = Color(0xFF56636C),
        accent = Color(0xFFBBC5CC),
    )
    "LightGray" -> lightEntryPalette(
        background = Color(0xFFFAFBFC),
        elevatedPanel = Color(0xFFF4F5F6),
        border = Color(0xFFB7C0C7),
        accent = Color(0xFF59656E),
    )
    else -> if (darkMode) {
        darkEntryPalette(
            background = Color(0xFF071524),
            panel = Color(0xFF0D2944),
            elevatedPanel = Color(0xFF091E33),
            border = Color(0xFF315D82),
            accent = Color(0xFF65A8FF),
        )
    } else {
        lightEntryPalette(
            background = Color(0xFFF6F9FF),
            elevatedPanel = Color(0xFFF1F5FF),
            border = Color(0xFF9BB6F4),
            accent = Color(0xFF426BDE),
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

private fun validateAuthenticationInput(
    mode: AuthenticationMode,
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
): String? {
    if (mode == AuthenticationMode.SignUp && fullName.trim().length < 2) return "Enter your full name."
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Enter a valid email address."
    if (password.length < 6) return "Password must contain at least 6 characters."
    if (mode == AuthenticationMode.SignUp && password != confirmPassword) return "Passwords do not match."
    return null
}

private enum class AuthenticationProvider {
    EmailPassword,
    Google,
}

private fun friendlyAuthenticationError(
    error: Throwable?,
    provider: AuthenticationProvider = AuthenticationProvider.EmailPassword,
): String {
    val errorCode = (error as? FirebaseAuthException)?.errorCode
    authenticationConfigurationMessage(provider, errorCode)?.let { return it }

    return when (error) {
        is FirebaseAuthUserCollisionException -> "An account already exists for this email."
        is FirebaseAuthInvalidUserException -> "No account was found for this email."
        is FirebaseAuthInvalidCredentialsException -> "The email or password is incorrect."
        else -> error?.message ?: "Authentication failed. Check your connection and try again."
    }
}

internal fun authenticationConfigurationMessage(
    providerName: String,
    errorCode: String?,
): String? {
    val provider = if (providerName.equals("google", ignoreCase = true)) {
        AuthenticationProvider.Google
    } else {
        AuthenticationProvider.EmailPassword
    }
    return authenticationConfigurationMessage(provider, errorCode)
}

private fun authenticationConfigurationMessage(
    provider: AuthenticationProvider,
    errorCode: String?,
): String? = when (errorCode) {
    "ERROR_OPERATION_NOT_ALLOWED" -> when (provider) {
        AuthenticationProvider.EmailPassword ->
            "Email/password sign-in is disabled for this Firebase project. Enable Email/Password in Firebase Authentication > Sign-in method."

        AuthenticationProvider.Google -> googleSignInConfigurationMessage()
    }

    "ERROR_NETWORK_REQUEST_FAILED" ->
        "Firebase could not be reached. Check your internet connection and try again."

    "ERROR_TOO_MANY_REQUESTS" ->
        "Firebase temporarily blocked more attempts from this device. Wait a moment and try again."

    "ERROR_USER_DISABLED" ->
        "This account has been disabled. Contact the app administrator."

    else -> null
}

private fun googleSignInConfigurationMessage(): String =
    "Google sign-in is not fully configured. Enable Google in Firebase Authentication, add this app's SHA-1 in Firebase Project settings, then download a fresh google-services.json."

internal fun isTrialExpired(now: Long, trialEndsAt: Long): Boolean = now >= trialEndsAt

internal fun nextTrialReminderAt(trialStartedAt: Long, lastContinuationAt: Long): Long {
    return maxOf(
        trialStartedAt + TRIAL_REMINDER_INTERVAL_MS,
        lastContinuationAt + TRIAL_REMINDER_INTERVAL_MS,
    )
}

private fun trialPreferences(context: Context) =
    context.getSharedPreferences(TRIAL_PREFS_NAME, Context.MODE_PRIVATE)

private fun loadTrialStart(context: Context, uid: String): Long? {
    val value = trialPreferences(context).getLong("trial_start_$uid", 0L)
    return value.takeIf { it > 0L }
}

private fun saveTrialStart(context: Context, uid: String, value: Long) {
    trialPreferences(context).edit().putLong("trial_start_$uid", value).apply()
}

private fun loadLastTrialContinuation(context: Context, uid: String): Long {
    return trialPreferences(context).getLong("trial_continue_$uid", 0L)
}

private fun saveLastTrialContinuation(context: Context, uid: String, value: Long) {
    trialPreferences(context).edit().putLong("trial_continue_$uid", value).apply()
}

private fun formatTrialDate(timestamp: Long): String {
    return SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun persistUserProfile(user: FirebaseUser, suppliedName: String?) {
    val displayName = suppliedName?.trim().takeUnless { it.isNullOrBlank() }
        ?: user.displayName
        ?: user.email?.substringBefore('@')
        ?: "PocketCodedPy user"
    val providers = user.providerData.mapNotNull { it.providerId.takeIf(String::isNotBlank) }.distinct()
    val profile = mapOf(
        "displayName" to displayName,
        "email" to user.email,
        "providers" to providers,
        "lastSignInAt" to FieldValue.serverTimestamp(),
    )
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(user.uid)
        .set(profile, SetOptions.merge())
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
    File(projectDirectory, "main.py").writeText(
        "# $name\n\nprint(\"Hello from $name!\")\n"
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
