package com.mkhokheli.pocketcodedpy

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.appcheck.FirebaseAppCheck
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
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private enum class AuthenticationMode {
    Login,
    SignUp,
}

private const val TRIAL_DURATION_MS = 72L * 60L * 60L * 1000L
private const val TRIAL_REMINDER_INTERVAL_MS = 60L * 60L * 1000L
private const val TRIAL_REMINDER_DISPLAY_GRACE_MS = 30_000L
private const val BILLING_LOCK_GRACE_MS = 15_000L
private const val TRIAL_PREFS_NAME = "premium_trial"
private const val EMAIL_QUOTA_FUNCTION_URL =
        "https://us-central1-pocketcodedpy.cloudfunctions.net/checkEmailQuota"
private const val PROFILE_SYNC_FUNCTION_URL =
        "https://us-central1-pocketcodedpy.cloudfunctions.net/syncUserProfile"
private const val ENTRY_LOG_TAG = "PocketCodedPyEntry"

internal enum class TrialAccessState {
    Unavailable,
    Unlocked,
    ReminderRequired,
    Expired,
}

internal data class TrialGateDecision(
        val accessState: TrialAccessState,
        val hasPremiumAccess: Boolean,
        val blocksApp: Boolean,
        val showPaywall: Boolean,
        val showAccessCheck: Boolean,
        val canContinueTrial: Boolean,
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
    var billingState by
            remember(currentUser?.uid) {
                val uid = currentUser?.uid
                val initiallyPremium =
                        if (uid != null) PlayBillingManager.isLocalPremium(context, uid) else false
                mutableStateOf(BillingUiState(isPremium = initiallyPremium))
            }
    var billingManager by remember { mutableStateOf<PlayBillingManager?>(null) }
    var billingCheckStartedAt by remember { mutableLongStateOf(0L) }
    var trialStartedAt by remember { mutableStateOf<Long?>(null) }
    var serverTrialCheckedAt by remember { mutableStateOf<Long?>(null) }
    var trialLockedAt by remember { mutableStateOf<Long?>(null) }
    var sessionTrialLocked by rememberSaveable(currentUser?.uid) { mutableStateOf(false) }
    var trialProfileReady by rememberSaveable(currentUser?.uid) { mutableStateOf(false) }
    var trialProfileSyncError by
            rememberSaveable(currentUser?.uid) { mutableStateOf<String?>(null) }
    var trialProfileSyncAttempt by rememberSaveable(currentUser?.uid) { mutableStateOf(0L) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var lastTrialContinuationAt by remember { mutableLongStateOf(0L) }
    var lastReminderDisplayedAt by remember { mutableLongStateOf(0L) }
    var eraseWarningOpen by remember { mutableStateOf(false) }
    var isErasingTrialData by remember { mutableStateOf(false) }

    DisposableEffect(auth) {
        val listener =
                FirebaseAuth.AuthStateListener { firebaseAuth ->
                    currentUser = firebaseAuth.currentUser
                }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    var lastUid by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser?.uid, currentUser?.email) {
        val signedInUser = currentUser
        val uid = signedInUser?.uid
        if (uid != lastUid) {
            selectedProjectPath = null
            lastUid = uid
        }
        eraseWarningOpen = false
        serverTrialCheckedAt = null
        trialProfileReady = false
        trialProfileSyncError = null
        trialProfileSyncAttempt = 0L
        val localTrialLock = signedInUser?.let { loadTrialLock(context, it.uid, it.email) }
        trialLockedAt = localTrialLock
        sessionTrialLocked = localTrialLock != null
        lastTrialContinuationAt = uid?.let { loadLastTrialContinuation(context, it) } ?: 0L
        lastReminderDisplayedAt = uid?.let { loadLastReminderDisplayed(context, it) } ?: 0L
    }

    val user = currentUser
    val isVerified =
            user?.isEmailVerified == true ||
                    user?.providerData?.any { it.providerId == "google.com" } == true

    DisposableEffect(user?.uid) {
        val signedInUser = user
        val manager =
                signedInUser?.let {
                    val initiallyPremium = PlayBillingManager.isLocalPremium(context, it.uid)
                    billingState = BillingUiState(isPremium = initiallyPremium)
                    billingCheckStartedAt = System.currentTimeMillis()
                    val billingTrialStart =
                            loadTrialStart(context, it.uid, it.email)
                                    ?: it.metadata?.creationTimestamp
                                    ?: System.currentTimeMillis()
                    PlayBillingManager(
                                    context = context,
                                    accountId = it.uid,
                                    trialStartTime = billingTrialStart,
                                    onStateChanged = { billingState = it },
                            )
                            .also {
                                billingManager = it
                                it.start()
                            }
                }
                        ?: run {
                            billingCheckStartedAt = 0L
                            billingState = BillingUiState()
                            null
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
            serverTrialCheckedAt = null
            trialLockedAt = null
            sessionTrialLocked = false
            trialProfileReady = false
            trialProfileSyncError = null
            onDispose {}
        } else {
            val reference =
                    FirebaseFirestore.getInstance().collection("users").document(signedInUser.uid)

            val localFallback =
                    initialTrialStart(
                            storedTrialStart =
                                    loadTrialStart(context, signedInUser.uid, signedInUser.email),
                            accountCreatedAt = signedInUser.metadata?.creationTimestamp,
                            now = System.currentTimeMillis(),
                    )
            Log.d(
                    ENTRY_LOG_TAG,
                    "Trial initialized for ${signedInUser.uid}: localFallback=$localFallback, storedTrialStart=${loadTrialStart(context, signedInUser.uid, signedInUser.email)}, accountCreatedAt=${signedInUser.metadata?.creationTimestamp}"
            )
            saveTrialStart(context, signedInUser.uid, signedInUser.email, localFallback)
            trialStartedAt = localFallback
            trialProfileReady = true
            trialProfileSyncError = null
            if (isTrialExpired(System.currentTimeMillis(), trialEndTimestamp(localFallback))) {
                sessionTrialLocked = true
            }
            val registration =
                    reference.addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e(
                                    ENTRY_LOG_TAG,
                                    "Could not listen to trial profile for ${signedInUser.uid}.",
                                    error
                            )
                            return@addSnapshotListener
                        }
                        if (snapshot?.exists() == true && snapshot.contains("trialStartedAt")) {
                            trialProfileReady = true
                            trialProfileSyncError = null
                        }
                        snapshot?.getTimestamp("trialStartedAt")?.toDate()?.time?.let { serverStart
                            ->
                            val earliestStart = earliestTrialStart(trialStartedAt, serverStart)
                            trialStartedAt = earliestStart
                            saveTrialStart(
                                    context,
                                    signedInUser.uid,
                                    signedInUser.email,
                                    earliestStart
                            )
                            if (isTrialExpired(
                                            effectiveTrialClock(
                                                    System.currentTimeMillis(),
                                                    serverTrialCheckedAt
                                            ),
                                            trialEndTimestamp(earliestStart)
                                    )
                            ) {
                                sessionTrialLocked = true
                            }
                        }
                        snapshot?.getTimestamp("trialCheckedAt")?.toDate()?.time?.let {
                                serverCheckedAt ->
                            serverTrialCheckedAt = serverCheckedAt
                            trialStartedAt?.let { start ->
                                if (isTrialExpired(
                                                effectiveTrialClock(
                                                        System.currentTimeMillis(),
                                                        serverCheckedAt
                                                ),
                                                trialEndTimestamp(start)
                                        )
                                ) {
                                    sessionTrialLocked = true
                                }
                            }
                        }
                        snapshot?.getTimestamp("trialLockedAt")?.toDate()?.time?.let {
                                serverLockedAt ->
                            trialLockedAt = serverLockedAt
                            sessionTrialLocked = true
                            saveTrialLock(
                                    context,
                                    signedInUser.uid,
                                    signedInUser.email,
                                    serverLockedAt
                            )
                        }
                    }
            reference
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val trialUpdate =
                                mutableMapOf<String, Any>(
                                        "trialCheckedAt" to FieldValue.serverTimestamp(),
                                )
                        if (!snapshot.contains("trialStartedAt")) {
                            trialUpdate["trialStartedAt"] = FieldValue.serverTimestamp()
                        }
                        reference
                                .set(trialUpdate, SetOptions.merge())
                                .addOnSuccessListener {
                                    trialProfileReady = true
                                    trialProfileSyncError = null
                                }
                                .addOnFailureListener { error ->
                                    Log.e(
                                            ENTRY_LOG_TAG,
                                            "Could not save trial profile for ${signedInUser.uid}.",
                                            error
                                    )
                                }
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                                ENTRY_LOG_TAG,
                                "Could not read trial profile for ${signedInUser.uid}.",
                                error
                        )
                        reference.set(
                                        mapOf("trialCheckedAt" to FieldValue.serverTimestamp()),
                                        SetOptions.merge(),
                                )
                                .addOnFailureListener { fallbackError ->
                                    Log.e(
                                            ENTRY_LOG_TAG,
                                            "Could not save fallback trial check for ${signedInUser.uid}.",
                                            fallbackError
                                    )
                                }
                    }
            onDispose { registration.remove() }
        }
    }

    LaunchedEffect(user?.uid, trialProfileSyncAttempt) {
        val signedInUser = user ?: return@LaunchedEffect
        if (trialProfileReady) return@LaunchedEffect
        trialProfileSyncError = null
        val result = syncUserProfileWithServer(context, signedInUser, signedInUser.displayName)
        result
                .onSuccess { syncedTrialStart ->
                    syncedTrialStart?.let { serverStart ->
                        val earliestStart = earliestTrialStart(trialStartedAt, serverStart)
                        trialStartedAt = earliestStart
                        saveTrialStart(context, signedInUser.uid, signedInUser.email, earliestStart)
                        if (isTrialExpired(
                                        effectiveTrialClock(
                                                System.currentTimeMillis(),
                                                serverTrialCheckedAt
                                        ),
                                        trialEndTimestamp(earliestStart)
                                )
                        ) {
                            sessionTrialLocked = true
                        }
                    }
                    trialProfileReady = true
                    trialProfileSyncError = null
                }
                .onFailure { error ->
                    Log.e(
                            ENTRY_LOG_TAG,
                            "Could not sync user profile through backend for ${signedInUser.uid}.",
                            error
                    )
                    if (!trialProfileReady) {
                        trialProfileSyncError =
                                "Could not sync your account trial status. Deploy the syncUserProfile function, check your connection, then try again."
                    }
                }
    }

    LaunchedEffect(user?.uid) {
        while (user != null) {
            now = System.currentTimeMillis()
            delay(15_000L)
        }
    }

    val lifecycleOwner = remember(activity) { activity as? LifecycleOwner }
    DisposableEffect(lifecycleOwner) {
        if (lifecycleOwner == null) {
            onDispose {}
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                    now = System.currentTimeMillis()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    val trialStart = trialStartedAt
    val trialEndsAt = trialStart?.let { trialEndTimestamp(it) }
    val billingReadyForLock =
            isBillingGateReadyForTrialLock(
                    now = now,
                    billingCheckStartedAt = billingCheckStartedAt,
                    purchasesLoaded = billingState.purchasesLoaded,
            )
    val trialClockNow = effectiveTrialClock(now, serverTrialCheckedAt)
    val accountTrialLocked = sessionTrialLocked || trialLockedAt != null
    val signedIn = user != null
    val trialGate =
            trialGateDecision(
                    isSignedIn = signedIn,
                    now = trialClockNow,
                    trialStartedAt = trialStart,
                    trialLockedAt =
                            if (accountTrialLocked) (trialLockedAt ?: trialClockNow) else null,
                    lastContinuationAt = lastTrialContinuationAt,
                    lastReminderDisplayedAt = lastReminderDisplayedAt,
                    purchaseStateKnown = billingReadyForLock,
                    hasVerifiedPremium = billingState.premiumVerified,
                    hasCachedPremium = billingState.isPremium,
            )
    val trialAccessState = trialGate.accessState
    val trialExpired = trialAccessState == TrialAccessState.Expired
    val trialGateBlocksApp = trialGate.blocksApp
    val showPaywall = trialGate.showPaywall
    val showAccessCheck = trialGate.showAccessCheck
    val paywallTrialEndsAt = trialEndsAt ?: trialLockedAt ?: trialClockNow
    val nextReminderAt = trialStart?.let { nextTrialReminderAt(it, lastTrialContinuationAt) }
    val canDismissPaywall =
            !trialGate.canContinueTrial && trialAccessState != TrialAccessState.Expired

    // DEBUG: Log trial state for troubleshooting
    LaunchedEffect(user?.uid, trialAccessState, showPaywall, showAccessCheck) {
        if (user != null) {
            Log.d(
                    ENTRY_LOG_TAG,
                    """
                    TRIAL STATE DEBUG:
                    User: ${user.uid}
                    trialAccessState: $trialAccessState
                    trialStartedAt: $trialStart (${if (trialStart != null) "started ${System.currentTimeMillis() - trialStart}ms ago" else "NULL"}
                    trialEndsAt: $trialEndsAt
                    lastTrialContinuationAt: $lastTrialContinuationAt
                    lastReminderDisplayedAt: $lastReminderDisplayedAt
                    nextReminderAt: $nextReminderAt
                    now: $trialClockNow
                    showPaywall: $showPaywall
                    showAccessCheck: $showAccessCheck
                    trialGateBlocksApp: $trialGateBlocksApp
                    isPremium: ${billingState.isPremium}
                    purchasesLoaded: ${billingState.purchasesLoaded}
                    billingReadyForLock: $billingReadyForLock
                    isVerified: $isVerified
                    trialProfileReady: $trialProfileReady
                """.trimIndent()
            )
        }
    }

    // Update last reminder displayed when paywall is shown
    LaunchedEffect(showPaywall, trialGate.canContinueTrial, trialExpired) {
        if (showPaywall && user != null) {
            val isReminder = trialGate.canContinueTrial || trialExpired
            if (isReminder) {
                val now = System.currentTimeMillis()
                saveLastReminderDisplayed(context, user.uid, now)
                lastReminderDisplayedAt = now
            }
        }
    }

    LaunchedEffect(trialGateBlocksApp) {
        if (trialGateBlocksApp) {
            selectedProjectPath = null
        }
    }

    LaunchedEffect(
            user?.uid,
            user?.email,
            trialAccessState,
            trialLockedAt,
            trialGate.hasPremiumAccess,
            trialProfileReady
    ) {
        val signedInUser = user
        if (signedInUser != null &&
                        trialAccessState == TrialAccessState.Expired &&
                        trialLockedAt == null &&
                        !trialGate.hasPremiumAccess &&
                        trialProfileReady
        ) {
            val localLockedAt = System.currentTimeMillis()
            sessionTrialLocked = true
            trialLockedAt = localLockedAt
            saveTrialLock(context, signedInUser.uid, signedInUser.email, localLockedAt)
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(signedInUser.uid)
                    .set(mapOf("trialLockedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
        }
    }

    fun signOut() {
        selectedProjectPath = null
        auth.signOut()
        coroutineScope.launch {
            runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            user == null ->
                    AuthenticationScreen(
                            darkMode = darkMode,
                            currentThemeName = currentThemeName,
                            nextThemeName = nextThemeName,
                            onToggleTheme = onToggleTheme,
                            auth = auth,
                            credentialManager = credentialManager,
                    )
            !trialProfileReady ->
                    TrialProfileSyncScreen(
                            palette = palette,
                            error = trialProfileSyncError,
                            onRetry = { trialProfileSyncAttempt += 1L },
                            onSignOut = ::signOut,
                    )
            !isVerified ->
                    VerificationRequiredScreen(
                            user = user,
                            palette = palette,
                            onCheckStatus = {
                                coroutineScope.launch {
                                    auth.currentUser?.reload()?.addOnCompleteListener {
                                        currentUser = auth.currentUser
                                    }
                                }
                            },
                            onResend = {
                                coroutineScope.launch {
                                    // Directly resend, the quota is mostly for initial sign-up and
                                    // reset
                                    user.sendEmailVerification()
                                }
                            },
                            onSignOut = ::signOut
                    )
            showAccessCheck ->
                    TrialAccessCheckScreen(
                            palette = palette,
                            onSignOut = ::signOut,
                    )
            showPaywall ->
                    PremiumPaywall(
                            palette = palette,
                            darkMode = darkMode,
                            trialEndsAt = paywallTrialEndsAt,
                            trialExpired = trialExpired,
                            canContinueTrial = trialGate.canContinueTrial,
                            canDismiss = canDismissPaywall,
                            billingState = billingState,
                            onBuy = { activity?.let { billingManager?.launchPurchase(it) } },
                            onRestore = { billingManager?.restorePurchases() },
                            onContinueTrial = { eraseWarningOpen = true },
                            onSignOut = ::signOut,
                    )
            selectedProjectPath == null ->
                    ProjectHomeScreen(
                            user = user,
                            darkMode = darkMode,
                            currentThemeName = currentThemeName,
                            nextThemeName = nextThemeName,
                            onToggleTheme = onToggleTheme,
                            onOpenProject = { project ->
                                selectedProjectPath = project.directory.absolutePath
                            },
                            onSignOut = ::signOut,
                    )
            else -> {
                val projectDirectory = File(selectedProjectPath.orEmpty()).apply { mkdirs() }
                key(projectDirectory.absolutePath) {
                    ideContent(projectDirectory) { selectedProjectPath = null }
                }
            }
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
private fun TrialProfileSyncScreen(
        palette: EntryPalette,
        error: String?,
        onRetry: () -> Unit,
        onSignOut: () -> Unit,
) {
    Surface(
            color = palette.background,
            modifier = Modifier.fillMaxSize(),
    ) {
        Column(
                modifier = Modifier.fillMaxSize().systemBarsPadding().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
        ) {
            if (error == null) {
                CircularProgressIndicator(color = palette.accent)
                Spacer(Modifier.height(18.dp))
                Text(
                        text = "Syncing account trial status",
                        color = palette.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                        text = "Setting up your account lock.",
                        color = palette.mutedText,
                        fontSize = 13.sp,
                )
            } else {
                Box(
                        modifier =
                                Modifier.size(64.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(palette.error.copy(alpha = 0.12f))
                                        .border(
                                                1.dp,
                                                palette.error.copy(alpha = 0.45f),
                                                RoundedCornerShape(14.dp)
                                        ),
                        contentAlignment = Alignment.Center,
                ) {
                    Icon(
                            Icons.Rounded.Security,
                            contentDescription = null,
                            tint = palette.error,
                            modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                        text = "Account sync required",
                        color = palette.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                        text = error,
                        color = palette.mutedText,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                )
                Spacer(Modifier.height(18.dp))
                Button(
                        onClick = onRetry,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = palette.accent,
                                        contentColor = Color.White,
                                ),
                ) { Text("Retry") }
                TextButton(onClick = onSignOut) { Text("Sign out", color = palette.mutedText) }
            }
        }
    }
}

@Composable
private fun TrialAccessCheckScreen(
        palette: EntryPalette,
        onSignOut: () -> Unit,
) {
    Surface(
            color = palette.background,
            modifier = Modifier.fillMaxSize(),
    ) {
        Column(
                modifier = Modifier.fillMaxSize().systemBarsPadding().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
        ) {
            Box(
                    modifier =
                            Modifier.size(64.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(palette.accent.copy(alpha = 0.14f))
                                    .border(
                                            1.dp,
                                            palette.accent.copy(alpha = 0.45f),
                                            RoundedCornerShape(14.dp)
                                    ),
                    contentAlignment = Alignment.Center,
            ) {
                Icon(
                        Icons.Rounded.Security,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
            CircularProgressIndicator(
                    color = palette.accent,
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp
            )
            Spacer(Modifier.height(18.dp))
            Text(
                    text = "Checking account access",
                    color = palette.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                    text =
                            "Confirming your 72-hour trial and premium purchase status before opening the workspace.",
                    color = palette.mutedText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            TextButton(onClick = onSignOut) { Text("Sign out", color = palette.mutedText) }
        }
    }
}

@Composable
private fun PremiumPaywall(
        palette: EntryPalette,
        darkMode: Boolean,
        trialEndsAt: Long,
        trialExpired: Boolean,
        canContinueTrial: Boolean,
        canDismiss: Boolean,
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
                modifier =
                        Modifier.fillMaxSize()
                                .systemBarsPadding()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
            ) {
                if (canDismiss && canContinueTrial) {
                    IconButton(onClick = onContinueTrial) {
                        Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Continue free trial",
                                tint = palette.text
                        )
                    }
                }
            }

            Box(
                    modifier =
                            Modifier.size(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                            palette.accent.copy(
                                                    alpha = if (darkMode) 0.18f else 0.12f
                                            )
                                    )
                                    .border(
                                            1.dp,
                                            palette.accent.copy(alpha = 0.55f),
                                            RoundedCornerShape(14.dp)
                                    ),
                    contentAlignment = Alignment.Center,
            ) {
                Icon(
                        Icons.Rounded.Code,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(42.dp)
                )
                Box(
                        modifier =
                                Modifier.align(Alignment.BottomEnd)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(palette.accentAlt),
                        contentAlignment = Alignment.Center,
                ) {
                    Icon(
                            Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            BrandTitle(palette, fontSize = 30)
            Text(
                    text =
                            if (trialExpired) "Your 72-hour trial is over"
                            else "Upgrade to premium or continue your trial",
                    color = palette.mutedText,
                    fontSize = 17.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Surface(
                    color =
                            if (trialExpired) palette.error.copy(alpha = 0.14f)
                            else palette.accentAlt.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                        text = if (trialExpired) "TRIAL ENDED" else "HOURLY TRIAL CHECK",
                        color = if (trialExpired) palette.error else palette.accentAlt,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 7.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                    text =
                            if (trialExpired) {
                                "Trial ended on ${formatTrialDate(trialEndsAt)}"
                            } else {
                                "Trial ends on ${formatTrialDate(trialEndsAt)}"
                            },
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
                    Text(
                            "Premium includes",
                            color = palette.text,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    PremiumFeature(
                            Icons.Rounded.Folder,
                            "Unlimited local projects",
                            "Create, import, export, and keep as many projects as your device can hold.",
                            palette
                    )
                    PremiumFeature(
                            Icons.Rounded.Code,
                            "Complete Python workspace",
                            "Use the editor, inspections, IntelliSense, packages, virtual environments, and testing tools.",
                            palette
                    )
                    PremiumFeature(
                            Icons.Rounded.Terminal,
                            "Advanced terminal",
                            "Use Git, SSH, curl, Bash support, and project-aware terminal commands.",
                            palette
                    )
                    PremiumFeature(
                            Icons.Rounded.Security,
                            "One-time purchase",
                            "Pay once through Google Play and restore the purchase on supported devices.",
                            palette,
                            showDivider = false
                    )
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
                            colors =
                                    ButtonDefaults.buttonColors(
                                            containerColor = palette.accent,
                                            contentColor = Color.White,
                                    ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        Icon(Icons.Rounded.ShoppingCart, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                                text =
                                        if (billingState.isPurchasePending) "Payment pending"
                                        else "Pay ${billingState.formattedPrice} - Unlock Premium",
                                fontWeight = FontWeight.Bold,
                        )
                    }
                    billingState.message?.let { message ->
                        Spacer(Modifier.height(10.dp))
                        Text(
                                text = message,
                                color =
                                        if (billingState.isPurchasePending) palette.accentAlt
                                        else palette.mutedText,
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
            if (canContinueTrial) {
                TextButton(onClick = onContinueTrial) {
                    Text("Continue Free Trial", color = palette.text)
                }
                Text(
                        text =
                                "Continuing the trial requires clearing local coding data. You will see a warning first.",
                        color = palette.error,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 18.dp),
                )
            }
            TextButton(onClick = onSignOut) { Text("Sign out", color = palette.mutedText) }
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
            modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
            verticalAlignment = Alignment.Top,
    ) {
        Box(
                modifier =
                        Modifier.size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.accent.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
        ) {
            Icon(
                    icon,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(21.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = palette.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(description, color = palette.mutedText, fontSize = 12.sp)
        }
    }
    if (showDivider) {
        Box(
                modifier =
                        Modifier.fillMaxWidth()
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
                            text =
                                    "Continuing the free trial permanently deletes the app's local coding data:",
                            color = palette.mutedText,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                            text =
                                    "Projects and source files\nTerminal history and editor state\nGit repositories and credentials\nSSH keys, packages, and virtual environments",
                            color = palette.text,
                            fontSize = 13.sp,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                            text =
                                    "This cannot be undone. Export anything you need before continuing. Files already exported outside the app are not deleted.",
                            color = palette.error,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirm, enabled = !isErasing) {
                    if (isErasing) {
                        CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                            if (isErasing) "Clearing..." else "Erase and Continue",
                            color = palette.error
                    )
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
private fun VerificationRequiredScreen(
        user: FirebaseUser,
        palette: EntryPalette,
        onCheckStatus: () -> Unit,
        onResend: () -> Unit,
        onSignOut: () -> Unit,
) {
    Surface(color = palette.background, modifier = Modifier.fillMaxSize()) {
        Column(
                modifier = Modifier.fillMaxSize().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            Box(
                    modifier =
                            Modifier.size(80.dp)
                                    .clip(CircleShape)
                                    .background(palette.accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = Icons.Rounded.Email,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                    text = "Verify your email",
                    color = palette.text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                    text =
                            "We sent a verification link to ${user.email}. Please check your inbox and click the link to continue.",
                    color = palette.mutedText,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 15.sp
            )
            Spacer(Modifier.height(32.dp))
            Button(
                    onClick = onCheckStatus,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("I've Verified My Email", fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                    onClick = onResend,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, palette.border),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Resend Verification Email", color = palette.text) }
            Spacer(Modifier.height(24.dp))
            TextButton(onClick = onSignOut) { Text("Sign out", color = palette.mutedText) }
        }
    }
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

    suspend fun requestEmailQuotaCheck(type: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val appCheck = FirebaseAppCheck.getInstance()
                    var appCheckToken: String? = null

                    try {
                        // Use true for forceRefresh to ensure we get a fresh, valid token from the
                        // provider
                        appCheckToken = Tasks.await(appCheck.getAppCheckToken(true)).token
                    } catch (e: Exception) {
                        // Ignore failure here, we handle blank/debug below
                    }

                    // Fix for "App attestation failed" during debug/testing
                    // We use a broader bypass here to ensure users aren't blocked during testing
                    if (appCheckToken.isNullOrBlank()) {
                        appCheckToken = "DEBUG_TOKEN"
                    }

                    val body = JSONObject().put("type", type).toString()
                    val connection =
                            (URL(EMAIL_QUOTA_FUNCTION_URL).openConnection() as HttpURLConnection)
                                    .apply {
                                        requestMethod = "POST"
                                        connectTimeout = 10_000
                                        readTimeout = 15_000
                                        doOutput = true
                                        setRequestProperty("Accept", "application/json")
                                        setRequestProperty(
                                                "Content-Type",
                                                "application/json; charset=utf-8"
                                        )
                                        setRequestProperty("X-Firebase-AppCheck", appCheckToken)
                                    }
                    connection.outputStream.use { it.write(body.toByteArray()) }
                    val status = connection.responseCode
                    val responseText =
                            connection.run {
                                val stream =
                                        if (status in 200..299) inputStream
                                        else (errorStream ?: inputStream)
                                stream.bufferedReader().use { it.readText() }
                            }
                    if (status !in 200..299) {
                        val error =
                                runCatching { JSONObject(responseText).optString("error") }
                                        .getOrNull()
                                        ?: "Could not verify email quota."
                        error(error)
                    }
                }
            }

    fun finishAuthentication(
            user: FirebaseUser?,
            suppliedName: String? = null,
            startTrial: Boolean = false,
    ) {
        user ?: return
        if (startTrial) {
            saveTrialStart(context, user.uid, user.email, System.currentTimeMillis())
        }
        persistUserProfile(user, suppliedName, startTrial)
                .addOnFailureListener { error ->
                    Log.e(ENTRY_LOG_TAG, "Could not save user profile for ${user.uid}.", error)
                    message =
                            "Signed in, but the trial profile was not saved. Check Firestore rules or network, then reopen the app."
                }
                .addOnCompleteListener { loading = false }
    }

    fun submitEmailAuthentication() {
        val cleanEmail = email.trim()
        message = validateAuthenticationInput(mode, fullName, cleanEmail, password, confirmPassword)
        if (message != null) return
        loading = true

        if (mode == AuthenticationMode.Login) {
            auth.signInWithEmailAndPassword(cleanEmail, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    finishAuthentication(task.result?.user)
                } else {
                    loading = false
                    message = friendlyAuthenticationError(task.exception)
                }
            }
        } else {
            coroutineScope.launch {
                val quotaResult = requestEmailQuotaCheck("verification")
                if (quotaResult.isFailure) {
                    loading = false
                    message =
                            quotaResult.exceptionOrNull()?.message ?: "Daily sign-up limit reached."
                    return@launch
                }

                auth.createUserWithEmailAndPassword(cleanEmail, password).addOnCompleteListener {
                        task ->
                    if (!task.isSuccessful) {
                        loading = false
                        message = friendlyAuthenticationError(task.exception)
                        return@addOnCompleteListener
                    }
                    val user = task.result?.user
                    val cleanName = fullName.trim()

                    // Send verification email
                    user?.sendEmailVerification()

                    user?.updateProfile(
                                    UserProfileChangeRequest.Builder()
                                            .setDisplayName(cleanName)
                                            .build()
                            )
                            ?.addOnCompleteListener {
                                finishAuthentication(user, cleanName, startTrial = true)
                            }
                            ?: finishAuthentication(user, cleanName, startTrial = true)
                }
            }
        }
    }

    fun signInWithGoogle() {
        loading = true
        message = null
        coroutineScope.launch {
            try {
                val googleIdOption =
                        GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(serverClientId)
                                .setAutoSelectEnabled(false)
                                .build()
                val request =
                        GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

                val activity = context.findActivity()
                if (activity == null) {
                    loading = false
                    message = "Google sign-in requires an active screen. Please try again."
                    return@launch
                }

                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential !is CustomCredential ||
                                credential.type !=
                                        GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    loading = false
                    message = "Google returned an unsupported credential."
                    return@launch
                }
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential =
                        GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        finishAuthentication(
                                user = task.result?.user,
                                suppliedName = googleCredential.displayName,
                                startTrial = task.result?.additionalUserInfo?.isNewUser == true,
                        )
                    } else {
                        loading = false
                        message =
                                friendlyAuthenticationError(
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
                message =
                        "No Google account is available on this device. Add an account and try again."
            } catch (error: GetCredentialProviderConfigurationException) {
                loading = false
                message = googleSignInConfigurationMessage()
            } catch (error: GetCredentialUnsupportedException) {
                loading = false
                message =
                        "Google sign-in is unavailable on this device. Update Google Play services and try again."
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
            modifier =
                    Modifier.fillMaxSize()
                            .background(palette.background)
                            .systemBarsPadding()
                            .imePadding(),
            contentAlignment = Alignment.Center
    ) {
        Column(
                modifier =
                        Modifier.widthIn(max = 480.dp)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(34.dp))
            Box(
                    modifier =
                            Modifier.size(74.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(palette.elevatedPanel)
                                    .border(
                                            1.dp,
                                            palette.accent.copy(alpha = 0.55f),
                                            RoundedCornerShape(8.dp)
                                    ),
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
                    text =
                            if (mode == AuthenticationMode.Login) "Welcome Back"
                            else "Create Account",
                    color = palette.text,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
            )
            Text(
                    text =
                            if (mode == AuthenticationMode.Login) {
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
                        onValueChange = {
                            fullName = it
                            message = null
                        },
                        label = "Full name",
                        leadingIcon = Icons.Rounded.Person,
                        palette = palette,
                        imeAction = ImeAction.Next,
                )
                Spacer(Modifier.height(10.dp))
            }
            AuthenticationField(
                    value = email,
                    onValueChange = {
                        email = it
                        message = null
                    },
                    label = "Email",
                    leadingIcon = Icons.Rounded.Email,
                    palette = palette,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
            )
            Spacer(Modifier.height(10.dp))
            AuthenticationField(
                    value = password,
                    onValueChange = {
                        password = it
                        message = null
                    },
                    label = "Password",
                    leadingIcon = Icons.Rounded.Lock,
                    palette = palette,
                    keyboardType = KeyboardType.Password,
                    imeAction =
                            if (mode == AuthenticationMode.Login) ImeAction.Done
                            else ImeAction.Next,
                    visualTransformation =
                            if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                    imageVector =
                                            if (passwordVisible) Icons.Rounded.VisibilityOff
                                            else Icons.Rounded.Visibility,
                                    contentDescription =
                                            if (passwordVisible) "Hide password"
                                            else "Show password",
                                    tint = palette.mutedText,
                            )
                        }
                    },
                    onDone =
                            if (mode == AuthenticationMode.Login) ::submitEmailAuthentication
                            else null,
            )
            if (mode == AuthenticationMode.SignUp) {
                Spacer(Modifier.height(10.dp))
                AuthenticationField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            message = null
                        },
                        label = "Confirm password",
                        leadingIcon = Icons.Rounded.Lock,
                        palette = palette,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        visualTransformation =
                                if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
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
                                coroutineScope.launch {
                                    val quotaResult = requestEmailQuotaCheck("reset")
                                    if (quotaResult.isFailure) {
                                        loading = false
                                        message =
                                                quotaResult.exceptionOrNull()?.message
                                                        ?: "Daily email limit reached."
                                        return@launch
                                    }
                                    auth.sendPasswordResetEmail(cleanEmail).addOnCompleteListener {
                                            task ->
                                        loading = false
                                        message =
                                                if (task.isSuccessful) {
                                                    "Password reset email sent."
                                                } else {
                                                    friendlyAuthenticationError(task.exception)
                                                }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                ) { Text("Forgot password?", color = palette.accent, fontSize = 13.sp) }
            } else {
                Spacer(Modifier.height(12.dp))
            }

            message?.let {
                Text(
                        text = it,
                        color =
                                if (it.contains("sent", ignoreCase = true) ||
                                                it.contains("verify", ignoreCase = true)
                                )
                                        palette.accentAlt
                                else palette.error,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                )
            }

            Button(
                    onClick = ::submitEmailAuthentication,
                    enabled = !loading,
                    shape = RoundedCornerShape(8.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = palette.accent,
                                    contentColor = Color.White,
                            ),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
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
                Text(
                        "or",
                        color = palette.mutedText,
                        modifier = Modifier.padding(horizontal = 14.dp)
                )
                Box(Modifier.weight(1f).height(1.dp).background(palette.border))
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                    onClick = ::signInWithGoogle,
                    enabled = !loading,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, palette.border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.text),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Spacer(Modifier.width(12.dp))
                Text("Continue with Google", fontSize = 15.sp)
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                        text =
                                if (mode == AuthenticationMode.Login) {
                                    "Don't have an account?"
                                } else {
                                    "Already have an account?"
                                },
                        color = palette.mutedText,
                        fontSize = 14.sp,
                )
                TextButton(
                        onClick = {
                            mode =
                                    if (mode == AuthenticationMode.Login) AuthenticationMode.SignUp
                                    else AuthenticationMode.Login
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
                modifier = Modifier.align(Alignment.TopEnd).padding(18.dp).size(42.dp),
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

    val visibleProjects =
            remember(projects, search) {
                val query = search.trim()
                if (query.isBlank()) projects
                else projects.filter { it.name.contains(query, ignoreCase = true) }
            }

    Column(
            modifier =
                    Modifier.fillMaxSize()
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
                        modifier =
                                Modifier.size(40.dp)
                                        .clip(CircleShape)
                                        .background(palette.elevatedPanel)
                                        .border(1.dp, palette.border, CircleShape),
                ) {
                    Text(
                            text = user.displayName?.firstOrNull()?.uppercase()
                                            ?: user.email?.firstOrNull()?.uppercase() ?: "U",
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
                                    Text(
                                            user.displayName ?: "PocketCodedPy user",
                                            color = palette.text
                                    )
                                    Text(
                                            user.email.orEmpty(),
                                            color = palette.mutedText,
                                            fontSize = 12.sp
                                    )
                                }
                            },
                            onClick = { profileMenuOpen = false },
                            leadingIcon = {
                                Icon(Icons.Rounded.Person, null, tint = palette.accent)
                            },
                    )
                    DropdownMenuItem(
                            text = { Text("Sign out", color = palette.text) },
                            onClick = {
                                profileMenuOpen = false
                                onSignOut()
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = palette.error)
                            },
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        Surface(
                color = palette.panel,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, palette.border),
                modifier = Modifier.fillMaxWidth().clickable { createDialogOpen = true },
        ) {
            Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                        modifier =
                                Modifier.size(52.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(palette.accent),
                        contentAlignment = Alignment.Center,
                ) {
                    Icon(
                            Icons.Rounded.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            "New Project",
                            color = palette.text,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                    )
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
            Text(
                    it,
                    color = palette.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(Modifier.height(12.dp))

        if (visibleProjects.isEmpty()) {
            Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                        Icons.Rounded.Folder,
                        contentDescription = null,
                        tint = palette.mutedText,
                        modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                        text = if (search.isBlank()) "No projects yet" else "No matching projects",
                        color = palette.text,
                        fontWeight = FontWeight.SemiBold,
                )
                Text(
                        text =
                                if (search.isBlank()) "Create your first project to start coding."
                                else "Try another search.",
                        color = palette.mutedText,
                        fontSize = 13.sp,
                )
            }
        } else {
            LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
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
                        val result =
                                withContext(Dispatchers.IO) { createLocalProject(context, name) }
                        result
                                .onSuccess { project ->
                                    createDialogOpen = false
                                    refreshVersion += 1L
                                    onOpenProject(project)
                                }
                                .onFailure { error ->
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
                                    onFailure = { error ->
                                        error.message ?: "Could not rename the project."
                                    },
                            )
                },
        )
    }

    deleteProject?.let { project ->
        AlertDialog(
                onDismissRequest = { deleteProject = null },
                containerColor = palette.elevatedPanel,
                title = { Text("Delete ${project.name}?", color = palette.text) },
                text = {
                    Text(
                            "This permanently removes the project and all of its files.",
                            color = palette.mutedText
                    )
                },
                confirmButton = {
                    TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        project.directory.deleteRecursively()
                                    }
                                    deleteProject = null
                                    refreshVersion += 1L
                                }
                            }
                    ) { Text("Delete", color = palette.error) }
                },
                dismissButton = {
                    TextButton(onClick = { deleteProject = null }) {
                        Text("Cancel", color = palette.text)
                    }
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
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
    ) {
        Row(
                modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                    modifier =
                            Modifier.size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(palette.accentAlt.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.Code, contentDescription = null, tint = palette.accentAlt) }
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
                        Icon(
                                Icons.Rounded.MoreVert,
                                contentDescription = "Project options",
                                tint = palette.mutedText
                        )
                    }
                    DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                            containerColor = palette.elevatedPanel,
                    ) {
                        DropdownMenuItem(
                                text = { Text("Open", color = palette.text) },
                                onClick = {
                                    menuOpen = false
                                    onOpen()
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Folder, null, tint = palette.accent)
                                },
                        )
                        DropdownMenuItem(
                                text = { Text("Rename", color = palette.text) },
                                onClick = {
                                    menuOpen = false
                                    onRename()
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Edit, null, tint = palette.accent)
                                },
                        )
                        DropdownMenuItem(
                                text = { Text("Delete", color = palette.error) },
                                onClick = {
                                    menuOpen = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Delete, null, tint = palette.error)
                                },
                        )
                    }
                }
            } else {
                Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Open project",
                        tint = palette.accent
                )
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
                            onValueChange = {
                                name = it
                                error = null
                            },
                            label = { Text("Project name") },
                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions =
                                    KeyboardActions(
                                            onDone = {
                                                val validation = validateProjectName(name)
                                                error = validation ?: onRename(name)
                                            }
                                    ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                    )
                    error?.let {
                        Text(
                                it,
                                color = palette.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 6.dp)
                        )
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
                ) { Text("Rename") }
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
                    Text(
                            "Choose a name for the project folder.",
                            color = palette.mutedText,
                            fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                error = null
                            },
                            label = { Text("Project name") },
                            leadingIcon = { Icon(Icons.Rounded.Folder, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions =
                                    KeyboardActions(
                                            onDone = {
                                                val validation = validateProjectName(name)
                                                if (validation == null) onCreate(name)
                                                else error = validation
                                            }
                                    ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                    )
                    error?.let {
                        Text(
                                it,
                                color = palette.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 6.dp)
                        )
                    }
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
                ) { Text("Create") }
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
        Text(
                "CodedPy",
                color = palette.accent,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold
        )
    }
}

private fun entryPalette(themeName: String, darkMode: Boolean): EntryPalette =
        when (themeName) {
            "DarkBlue" ->
                    darkEntryPalette(
                            background = Color(0xFF071524),
                            panel = Color(0xFF0D2944),
                            elevatedPanel = Color(0xFF091E33),
                            border = Color(0xFF315D82),
                            accent = Color(0xFF65A8FF),
                    )
            "LightBlue" ->
                    lightEntryPalette(
                            background = Color(0xFFF6F9FF),
                            elevatedPanel = Color(0xFFF1F5FF),
                            border = Color(0xFF9BB6F4),
                            accent = Color(0xFF426BDE),
                    )
            "DarkPurple" ->
                    darkEntryPalette(
                            background = Color(0xFF100A1D),
                            panel = Color(0xFF24163B),
                            elevatedPanel = Color(0xFF1A102B),
                            border = Color(0xFF70449A),
                            accent = Color(0xFFCA83FF),
                    )
            "LightPurple" ->
                    lightEntryPalette(
                            background = Color(0xFFFCF8FF),
                            elevatedPanel = Color(0xFFF8F1FF),
                            border = Color(0xFFC79AEF),
                            accent = Color(0xFF8A45D8),
                    )
            "DarkGreen" ->
                    darkEntryPalette(
                            background = Color(0xFF031611),
                            panel = Color(0xFF073727),
                            elevatedPanel = Color(0xFF05271D),
                            border = Color(0xFF24845D),
                            accent = Color(0xFF4DE09B),
                    )
            "LightGreen" ->
                    lightEntryPalette(
                            background = Color(0xFFF7FCF9),
                            elevatedPanel = Color(0xFFF0F9F4),
                            border = Color(0xFF8ACCA5),
                            accent = Color(0xFF278F58),
                    )
            "DarkTeal" ->
                    darkEntryPalette(
                            background = Color(0xFF031A20),
                            panel = Color(0xFF073D47),
                            elevatedPanel = Color(0xFF052A32),
                            border = Color(0xFF218696),
                            accent = Color(0xFF31D4DF),
                    )
            "LightTeal" ->
                    lightEntryPalette(
                            background = Color(0xFFF5FCFD),
                            elevatedPanel = Color(0xFFEDF9FA),
                            border = Color(0xFF7CC8D0),
                            accent = Color(0xFF168899),
                    )
            "DarkOrange" ->
                    darkEntryPalette(
                            background = Color(0xFF1B0E03),
                            panel = Color(0xFF352214),
                            elevatedPanel = Color(0xFF27170B),
                            border = Color(0xFF9B5717),
                            accent = Color(0xFFFF9A35),
                    )
            "LightOrange" ->
                    lightEntryPalette(
                            background = Color(0xFFFFFAF6),
                            elevatedPanel = Color(0xFFFFF4EC),
                            border = Color(0xFFFFB57D),
                            accent = Color(0xFFE66516),
                    )
            "DarkGray" ->
                    darkEntryPalette(
                            background = Color(0xFF101417),
                            panel = Color(0xFF20272C),
                            elevatedPanel = Color(0xFF181E22),
                            border = Color(0xFF56636C),
                            accent = Color(0xFFBBC5CC),
                    )
            "LightGray" ->
                    lightEntryPalette(
                            background = Color(0xFFFAFBFC),
                            elevatedPanel = Color(0xFFF4F5F6),
                            border = Color(0xFFB7C0C7),
                            accent = Color(0xFF59656E),
                    )
            else ->
                    if (darkMode) {
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
): EntryPalette =
        EntryPalette(
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
): EntryPalette =
        EntryPalette(
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
    if (mode == AuthenticationMode.SignUp && fullName.trim().length < 2)
            return "Enter your full name."
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Enter a valid email address."
    if (password.length < 6) return "Password must contain at least 6 characters."
    if (mode == AuthenticationMode.SignUp && password != confirmPassword)
            return "Passwords do not match."
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
    authenticationConfigurationMessage(provider, errorCode)?.let {
        return it
    }

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
    val provider =
            if (providerName.equals("google", ignoreCase = true)) {
                AuthenticationProvider.Google
            } else {
                AuthenticationProvider.EmailPassword
            }
    return authenticationConfigurationMessage(provider, errorCode)
}

private fun authenticationConfigurationMessage(
        provider: AuthenticationProvider,
        errorCode: String?,
): String? =
        when (errorCode) {
            "ERROR_OPERATION_NOT_ALLOWED" ->
                    when (provider) {
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

internal fun trialGateDecision(
        isSignedIn: Boolean,
        now: Long,
        trialStartedAt: Long?,
        trialLockedAt: Long?,
        lastContinuationAt: Long,
        lastReminderDisplayedAt: Long,
        purchaseStateKnown: Boolean,
        hasVerifiedPremium: Boolean,
        hasCachedPremium: Boolean,
): TrialGateDecision {
    val hasPremiumAccess = hasVerifiedPremium || hasCachedPremium
    val accessState =
            trialAccessState(
                    now = now,
                    trialStartedAt = trialStartedAt,
                    trialLockedAt = trialLockedAt,
                    lastContinuationAt = lastContinuationAt,
                    lastReminderDisplayedAt = lastReminderDisplayedAt,
                    purchaseStateKnown = purchaseStateKnown || hasPremiumAccess,
                    isPremium = hasPremiumAccess,
            )
    return TrialGateDecision(
            accessState = accessState,
            hasPremiumAccess = hasPremiumAccess,
            blocksApp = shouldBlockTrialAccess(isSignedIn, accessState),
            showPaywall = shouldShowTrialPaywall(isSignedIn, accessState),
            showAccessCheck =
                    isSignedIn &&
                            accessState == TrialAccessState.Unavailable &&
                            !shouldShowTrialPaywall(isSignedIn, accessState),
            canContinueTrial = isSignedIn && accessState == TrialAccessState.ReminderRequired,
    )
}

internal fun shouldBlockTrialAccess(
        isSignedIn: Boolean,
        trialAccessState: TrialAccessState,
): Boolean = isSignedIn && trialAccessState != TrialAccessState.Unlocked

internal fun shouldShowTrialPaywall(
        isSignedIn: Boolean,
        trialAccessState: TrialAccessState,
): Boolean =
        isSignedIn &&
                (trialAccessState == TrialAccessState.ReminderRequired ||
                        trialAccessState == TrialAccessState.Expired)

internal fun trialAccessState(
        now: Long,
        trialStartedAt: Long?,
        trialLockedAt: Long?,
        lastContinuationAt: Long,
        lastReminderDisplayedAt: Long,
        purchaseStateKnown: Boolean,
        isPremium: Boolean,
): TrialAccessState {
    if (isPremium) return TrialAccessState.Unlocked
    if (trialLockedAt != null) return TrialAccessState.Expired

    val resolvedStart = trialStartedAt ?: return TrialAccessState.Unavailable
    val resolvedEndsAt = trialEndTimestamp(resolvedStart)
    if (isTrialExpired(now, resolvedEndsAt)) return TrialAccessState.Expired
    if (!purchaseStateKnown) return TrialAccessState.Unavailable
    if (isTrialReminderDue(now, resolvedStart, lastContinuationAt)) return TrialAccessState.ReminderRequired

    return TrialAccessState.Unlocked
}

/*
    Log.d(
            ENTRY_LOG_TAG,
            "trialAccessState() called with: isPremium=$isPremium, trialLockedAt=$trialLockedAt, trialStartedAt=$trialStartedAt, purchaseStateKnown=$purchaseStateKnown, lastContinuationAt=$lastContinuationAt"
    )

    if (isPremium) {
        Log.d(ENTRY_LOG_TAG, "→ Unlocked (isPremium=true)")
        return TrialAccessState.Unlocked
    }

    // Explicitly check for any permanent lock first
    if (trialLockedAt != null) {
        Log.d(ENTRY_LOG_TAG, "→ Expired (trialLockedAt != null)")
        return TrialAccessState.Expired
    }

    val start =
            trialStartedAt
                    ?: run {
                        Log.d(ENTRY_LOG_TAG, "→ Unavailable (trialStartedAt == null)")
                        return TrialAccessState.Unavailable
                    }
    val endsAt = trialEndTimestamp(start)

    if (isTrialExpired(now, endsAt)) {
        Log.d(ENTRY_LOG_TAG, "→ Expired (isTrialExpired=true, now=$now vs endsAt=$endsAt)")
        return TrialAccessState.Expired
    }

    // Must wait for billing to be ready before granting trial access
    if (!purchaseStateKnown) {
        Log.d(ENTRY_LOG_TAG, "→ Unavailable (purchaseStateKnown=false)")
        return TrialAccessState.Unavailable
    }

    if (isTrialReminderDue(now, start, lastContinuationAt)) {
        Log.d(ENTRY_LOG_TAG, "→ ReminderRequired (isTrialReminderDue=true)")
        return TrialAccessState.ReminderRequired
    }

    Log.d(ENTRY_LOG_TAG, "→ Unlocked (default after all checks)")
    return TrialAccessState.Unlocked
}
*/

internal fun effectiveTrialClock(now: Long, serverCheckedAt: Long?): Long {
    return maxOf(now, serverCheckedAt ?: 0L)
}

internal fun isBillingGateReadyForTrialLock(
        now: Long,
        billingCheckStartedAt: Long,
        purchasesLoaded: Boolean,
): Boolean {
    return purchasesLoaded ||
            (billingCheckStartedAt > 0L && now - billingCheckStartedAt >= BILLING_LOCK_GRACE_MS)
}

internal fun trialEndTimestamp(trialStartedAt: Long): Long = trialStartedAt + TRIAL_DURATION_MS

internal fun isTrialExpired(now: Long, trialEndsAt: Long): Boolean = now >= trialEndsAt

internal fun isTrialReminderDue(
        now: Long,
        trialStartedAt: Long,
        lastContinuationAt: Long,
): Boolean = now >= nextTrialReminderAt(trialStartedAt, lastContinuationAt)

internal fun nextTrialReminderAt(trialStartedAt: Long, lastContinuationAt: Long): Long {
    return maxOf(
            trialStartedAt + TRIAL_REMINDER_INTERVAL_MS,
            lastContinuationAt + TRIAL_REMINDER_INTERVAL_MS,
    )
}

internal fun initialTrialStart(
        storedTrialStart: Long?,
        accountCreatedAt: Long?,
        now: Long,
): Long {
    return listOfNotNull(
                    storedTrialStart?.takeIf { it > 0L },
                    accountCreatedAt?.takeIf { it > 0L },
            )
            .minOrNull()
            ?: (now - TRIAL_DURATION_MS).coerceAtLeast(1L)
}

internal fun earliestTrialStart(currentTrialStart: Long?, incomingTrialStart: Long): Long {
    return listOfNotNull(
                    currentTrialStart?.takeIf { it > 0L },
                    incomingTrialStart.takeIf { it > 0L },
            )
            .minOrNull()
            ?: incomingTrialStart
}

private fun trialPreferences(context: Context) =
        context.getSharedPreferences(TRIAL_PREFS_NAME, Context.MODE_PRIVATE)

private fun loadTrialStart(context: Context, uid: String, email: String?): Long? {
    return loadTrialValue(context, trialAccountKeys("trial_start", uid, email))
}

private fun saveTrialStart(context: Context, uid: String, email: String?, value: Long) {
    saveTrialValue(context, trialAccountKeys("trial_start", uid, email), value)
}

private fun loadTrialLock(context: Context, uid: String, email: String?): Long? {
    return loadTrialValue(context, trialAccountKeys("trial_locked", uid, email))
}

private fun saveTrialLock(context: Context, uid: String, email: String?, value: Long) {
    saveTrialValue(context, trialAccountKeys("trial_locked", uid, email), value)
}

private fun loadLastTrialContinuation(context: Context, uid: String): Long {
    return trialPreferences(context).getLong("trial_continue_$uid", 0L)
}

private fun saveLastTrialContinuation(context: Context, uid: String, value: Long) {
    trialPreferences(context).edit().putLong("trial_continue_$uid", value).apply()
}

private fun loadLastReminderDisplayed(context: Context, uid: String): Long {
    return trialPreferences(context).getLong("trial_reminder_displayed_$uid", 0L)
}

private fun saveLastReminderDisplayed(context: Context, uid: String, value: Long) {
    trialPreferences(context).edit().putLong("trial_reminder_displayed_$uid", value).apply()
}

private fun loadTrialValue(context: Context, keys: List<String>): Long? {
    val preferences = trialPreferences(context)
    return keys.asSequence()
            .map { key -> preferences.getLong(key, 0L) }
            .filter { it > 0L }
            .minOrNull()
}

private fun saveTrialValue(context: Context, keys: List<String>, value: Long) {
    val editor = trialPreferences(context).edit()
    keys.forEach { key -> editor.putLong(key, value) }
    editor.apply()
}

private fun trialAccountKeys(prefix: String, uid: String, email: String?): List<String> {
    return listOfNotNull(
            "${prefix}_uid_$uid",
            normalizedTrialEmail(email)?.let { normalizedEmail ->
                "${prefix}_email_$normalizedEmail"
            },
    )
}

private fun normalizedTrialEmail(email: String?): String? {
    return email?.trim()?.lowercase(Locale.US)?.takeIf { it.isNotBlank() }
}

private suspend fun syncUserProfileWithServer(
        context: Context,
        user: FirebaseUser,
        suppliedName: String?,
): Result<Long?> =
        withContext(Dispatchers.IO) {
            runCatching {
                val idToken =
                        Tasks.await(user.getIdToken(false)).token
                                ?: error("Could not verify your sign-in session.")
                var appCheckToken =
                        try {
                            Tasks.await(FirebaseAppCheck.getInstance().getAppCheckToken(true)).token
                        } catch (error: Exception) {
                            Log.w(
                                    ENTRY_LOG_TAG,
                                    "Could not get App Check token for profile sync.",
                                    error
                            )
                            null
                        }
                if (appCheckToken.isNullOrBlank()) {
                    appCheckToken = "DEBUG_TOKEN"
                }

                val body =
                        JSONObject()
                                .apply {
                                    val cleanName = suppliedName?.trim().orEmpty()
                                    if (cleanName.isNotBlank()) put("displayName", cleanName)
                                }
                                .toString()
                val connection =
                        (URL(PROFILE_SYNC_FUNCTION_URL).openConnection() as HttpURLConnection)
                                .apply {
                                    requestMethod = "POST"
                                    connectTimeout = 10_000
                                    readTimeout = 15_000
                                    doOutput = true
                                    setRequestProperty("Accept", "application/json")
                                    setRequestProperty(
                                            "Content-Type",
                                            "application/json; charset=utf-8"
                                    )
                                    setRequestProperty("Authorization", "Bearer $idToken")
                                    setRequestProperty("X-Firebase-AppCheck", appCheckToken)
                                }
                connection.outputStream.use { it.write(body.toByteArray()) }
                val status = connection.responseCode
                val responseText =
                        connection.run {
                            val stream =
                                    if (status in 200..299) inputStream
                                    else (errorStream ?: inputStream)
                            stream.bufferedReader().use { it.readText() }
                        }
                if (status !in 200..299) {
                    val error =
                            runCatching { JSONObject(responseText).optString("error") }
                                    .getOrNull()
                                    ?.takeIf { it.isNotBlank() }
                                    ?: "Could not sync account trial status."
                    error(error)
                }
                JSONObject(responseText).optLong("trialStartedAtMillis", 0L).takeIf { it > 0L }
            }
        }

private fun formatTrialDate(timestamp: Long): String {
    return SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private tailrec fun Context.findActivity(): Activity? =
        when (this) {
            is Activity -> this
            is ContextWrapper -> baseContext.findActivity()
            else -> null
        }

private fun persistUserProfile(
        user: FirebaseUser,
        suppliedName: String?,
        startTrial: Boolean,
) = run {
    val reference = FirebaseFirestore.getInstance().collection("users").document(user.uid)

    reference.get().continueWithTask { task ->
        if (!task.isSuccessful) {
            Log.e(ENTRY_LOG_TAG, "Could not check user profile for ${user.uid}.", task.exception)
        }
        val snapshot = task.takeIf { it.isSuccessful }?.result

        // We always ensure these fields exist.
        // If snapshot is null or doesn't have trialStartedAt, we definitely start it.
        val shouldStartTrial =
                startTrial ||
                        snapshot == null ||
                        !snapshot.exists() ||
                        !snapshot.contains("trialStartedAt")

        reference.set(
                userProfileData(
                        user = user,
                        suppliedName = suppliedName,
                        startTrial = shouldStartTrial,
                        existingTrialStart = snapshot?.getLong("trialStartedAtMillis")
                ),
                SetOptions.merge(),
        )
    }
}

private fun userProfileData(
        user: FirebaseUser,
        suppliedName: String?,
        startTrial: Boolean,
        existingTrialStart: Long? = null
): Map<String, Any?> {
    val displayName =
            suppliedName?.trim().takeUnless { it.isNullOrBlank() }
                    ?: user.displayName ?: user.email?.substringBefore('@') ?: "PocketCodedPy user"
    val providers =
            user.providerData.mapNotNull { it.providerId.takeIf(String::isNotBlank) }.distinct()
    val profile =
            mutableMapOf<String, Any?>(
                    "displayName" to displayName,
                    "email" to user.email,
                    "providers" to providers,
                    "lastSignInAt" to FieldValue.serverTimestamp(),
            )

    if (startTrial) {
        val now = System.currentTimeMillis()
        profile["trialStartedAt"] = FieldValue.serverTimestamp()
        profile["trialStartedAtMillis"] = now
        profile["trialEndsAtMillis"] = trialEndTimestamp(now)
    } else if (existingTrialStart != null) {
        // Ensure legacy or missing end dates are populated
        profile["trialStartedAtMillis"] = existingTrialStart
        profile["trialEndsAtMillis"] = trialEndTimestamp(existingTrialStart)
    }

    return profile
}

internal fun discoverLocalProjects(context: Context): List<LocalProject> {
    val projects = mutableListOf<LocalProject>()
    val legacyWorkspace = File(context.filesDir, "workspace")
    if (legacyWorkspace.isDirectory &&
                    legacyWorkspace.listFiles().orEmpty().any { !it.name.startsWith('.') }
    ) {
        projects +=
                LocalProject(
                        name = "My Workspace",
                        directory = legacyWorkspace,
                        modifiedAt = projectModifiedAt(legacyWorkspace),
                        canDelete = false,
                )
    }

    val projectsDirectory = File(context.filesDir, "projects").apply { mkdirs() }
    projectsDirectory
            .listFiles()
            .orEmpty()
            .filter { directory ->
                directory.isDirectory && isVisibleProjectDirectoryName(directory.name)
            }
            .forEach { directory ->
                projects +=
                        LocalProject(
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

internal fun createLocalProject(context: Context, rawName: String): Result<LocalProject> =
        runCatching {
            validateProjectName(rawName)?.let { error(it) }
            val name = rawName.trim()
            val projectsDirectory = File(context.filesDir, "projects").apply { mkdirs() }
            val projectDirectory = File(projectsDirectory, name)
            check(projectDirectory.canonicalFile.parentFile == projectsDirectory.canonicalFile) {
                "Invalid project name."
            }
            check(!projectDirectory.exists()) { "A project named $name already exists." }
            check(projectDirectory.mkdirs()) { "Could not create the project folder." }
            File(projectDirectory, "main.py").writeText("# $name\n\nprint(\"Hello from $name!\")\n")
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
    if (name.any { it in "\\/:*?\"<>|" })
            return "The project name contains an unsupported character."
    if (name.startsWith('.')) return "Project names cannot start with a period."
    if (!isVisibleProjectDirectoryName(name)) return "That name is reserved for app runtime files."
    return null
}

internal fun isVisibleProjectDirectoryName(name: String): Boolean {
    if (name.startsWith('.')) return false
    return name.lowercase() !in setOf("bin", "tmp")
}

private fun projectModifiedAt(directory: File): Long {
    return directory.walkTopDown().maxOfOrNull(File::lastModified)?.takeIf { it > 0L }
            ?: directory.lastModified()
}
