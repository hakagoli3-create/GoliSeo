package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.model.TaskStatus
import com.example.ui.AppNavDestination
import com.example.ui.GliseoViewModel
import com.example.ui.components.DiffViewerDialog
import com.example.ui.components.GliseoBottomNav
import com.example.ui.components.GliseoTopBar
import com.example.ui.screens.ContentStudioScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LogsSnapshotsScreen
import com.example.ui.screens.QueueAutopilotScreen
import com.example.ui.screens.SettingsSecurityScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GliseoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Full RTL (راست‌به‌چپ) support for Persian language
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MyApplicationTheme {
                    GliseoApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun GliseoApp(viewModel: GliseoViewModel) {
    val currentNav by viewModel.currentNav.collectAsState()
    val items by viewModel.items.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val queueTasks by viewModel.queueTasks.collectAsState()
    val brandProfile by viewModel.brandProfile.collectAsState()
    val siteConfig by viewModel.siteConfig.collectAsState()
    val userSession by viewModel.userSession.collectAsState()
    val autopilotConfig by viewModel.autopilotConfig.collectAsState()
    val budgetTracker by viewModel.budgetTracker.collectAsState()
    val circuitBreakerActive by viewModel.circuitBreakerTriggered.collectAsState()

    val selectedItem by viewModel.selectedItem.collectAsState()
    val aiOptimizing by viewModel.aiOptimizing.collectAsState()
    val aiResult by viewModel.aiResult.collectAsState()
    val dryRunPreview by viewModel.dryRunPreviewItem.collectAsState()
    val testingConnection by viewModel.testingConnection.collectAsState()
    val connectionResult by viewModel.connectionResult.collectAsState()
    val activeDiffLog by viewModel.activeDiffLog.collectAsState()

    // Phase 2 State Collections
    val brandKnowledgeBase by viewModel.brandKnowledgeBase.collectAsState()
    val freeformPlan by viewModel.freeformPlan.collectAsState()
    val validationResult by viewModel.activeValidationResult.collectAsState()
    val productPackage by viewModel.productStudioPackage.collectAsState()
    val topicSuggestions by viewModel.topicSuggestions.collectAsState()
    val articlePreview by viewModel.generatedArticlePreview.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Collect UI messages
    LaunchedEffect(Unit) {
        viewModel.uiMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg.text)
        }
    }

    // Handle system back navigation
    BackHandler(enabled = selectedItem != null || currentNav != AppNavDestination.DASHBOARD) {
        if (selectedItem != null) {
            viewModel.selectItem(null)
        } else if (currentNav != AppNavDestination.DASHBOARD) {
            viewModel.navigateTo(AppNavDestination.DASHBOARD)
        }
    }

    Scaffold(
        topBar = {
            GliseoTopBar(
                userSession = userSession,
                isCircuitBreakerActive = circuitBreakerActive,
                onEmergencyStop = { viewModel.emergencyStop() }
            )
        },
        bottomBar = {
            val pendingReviews = queueTasks.count { it.status == TaskStatus.NEEDS_REVIEW }
            GliseoBottomNav(
                currentDestination = currentNav,
                pendingReviewsCount = pendingReviews,
                onNavigate = { dest ->
                    viewModel.selectItem(null)
                    viewModel.navigateTo(dest)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        when (currentNav) {
            AppNavDestination.DASHBOARD -> {
                DashboardScreen(
                    items = items,
                    siteConfig = siteConfig,
                    autopilotConfig = autopilotConfig,
                    budgetTracker = budgetTracker,
                    auditLogs = auditLogs,
                    circuitBreakerActive = circuitBreakerActive,
                    onNavigate = { dest -> viewModel.navigateTo(dest) },
                    onTestConnection = { viewModel.runConnectionTest() },
                    onTestHardLock = { viewModel.testAttemptHardLockViolation() },
                    onViewLog = { log -> viewModel.viewDiffLog(log) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppNavDestination.CONTENT_STUDIO -> {
                ContentStudioScreen(
                    items = items,
                    selectedItem = selectedItem,
                    aiOptimizing = aiOptimizing,
                    aiResult = aiResult,
                    dryRunPreviewItem = dryRunPreview,
                    brandKnowledgeBase = brandKnowledgeBase,
                    freeformPlan = freeformPlan,
                    validationResult = validationResult,
                    productPackage = productPackage,
                    topicSuggestions = topicSuggestions,
                    articlePreview = articlePreview,
                    onSelectItem = { item -> viewModel.selectItem(item) },
                    onGenerateAiSeo = { viewModel.generateSeoWithGemini() },
                    onRunDryRun = { changes -> viewModel.runDryRunSimulation(changes) },
                    onDismissDryRun = { viewModel.dismissDryRun() },
                    onCommitUpdate = { changes, reason -> viewModel.commitItemUpdate(changes, reason) },
                    onTestHardLock = { viewModel.testAttemptHardLockViolation() },
                    onEnqueueTask = { targetId, taskType -> viewModel.enqueueTask(targetId, taskType) },
                    onExecuteFreeform = { prompt, isDryRun -> viewModel.executeFreeform(prompt, isDryRun) },
                    onDismissFreeform = { viewModel.dismissFreeformPlan() },
                    onPrepareProductPackage = { prodId -> viewModel.prepareProductPackage(prodId) },
                    onApplyProductPackage = { prodId -> viewModel.applyProductPackage(prodId) },
                    onDismissProductPackage = { viewModel.dismissProductPackage() },
                    onBatchOptimizeProducts = { viewModel.batchOptimizeProducts() },
                    onSelectArticleTopic = { topic -> viewModel.generateArticleFromTopic(topic) },
                    onPublishArticle = { article -> viewModel.publishGeneratedArticle(article) },
                    onDismissArticlePreview = { viewModel.dismissArticlePreview() },
                    onScheduleFuture = { targetId, days, occasion -> viewModel.schedulePostFuture(targetId, days, occasion) },
                    onValidatePrePublish = { item -> viewModel.validatePrePublish(item) },
                    onDismissValidation = { viewModel.dismissValidationResult() },
                    onLearnUserEdit = { original, approved, category -> viewModel.learnUserEdit(original, approved, category) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppNavDestination.QUEUE_AUTOPILOT -> {
                QueueAutopilotScreen(
                    autopilotConfig = autopilotConfig,
                    tasks = queueTasks,
                    circuitBreakerActive = circuitBreakerActive,
                    onUpdateAutopilot = { cfg -> viewModel.updateAutopilot(cfg) },
                    onEmergencyStop = { viewModel.emergencyStop() },
                    onResetCircuitBreaker = { viewModel.resetCircuitBreaker() },
                    onApproveReview = { taskId -> viewModel.approveHumanReview(taskId) },
                    onRejectReview = { taskId -> viewModel.rejectHumanReview(taskId) },
                    onClearCompleted = { viewModel.clearCompletedTasks() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppNavDestination.LOGS_SNAPSHOTS -> {
                LogsSnapshotsScreen(
                    logs = auditLogs,
                    onSelectLog = { log -> viewModel.viewDiffLog(log) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppNavDestination.SETTINGS_SECURITY -> {
                SettingsSecurityScreen(
                    siteConfig = siteConfig,
                    userSession = userSession,
                    brandProfile = brandProfile,
                    testingConnection = testingConnection,
                    connectionResult = connectionResult,
                    onSaveSiteConfig = { cfg -> viewModel.updateSiteConfig(cfg) },
                    onRunConnectionTest = { viewModel.runConnectionTest() },
                    onSaveBrandProfile = { brand -> viewModel.saveBrandProfile(brand) },
                    onSwitchRole = { role -> viewModel.switchRole(role) },
                    onVerify2FA = { code -> viewModel.verifyTwoFactorCode(code) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        // Diff and Rollback Modal
        if (activeDiffLog != null) {
            DiffViewerDialog(
                log = activeDiffLog!!,
                onDismiss = { viewModel.viewDiffLog(null) },
                onUndo = { log -> viewModel.undoSnapshot(log) }
            )
        }
    }
}
