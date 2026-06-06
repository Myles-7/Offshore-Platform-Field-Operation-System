package com.offshore.platform.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.offshore.platform.mobile.ui.view.login.*
import com.offshore.platform.mobile.ui.view.mine.MineScreen
import com.offshore.platform.mobile.ui.view.workorder.*
import com.offshore.platform.mobile.ui.view.record.*
import com.offshore.platform.mobile.ui.view.media.*
import com.offshore.platform.mobile.ui.view.signature.*
import com.offshore.platform.mobile.ui.view.sync.SyncCenterScreen
import com.offshore.platform.mobile.ui.view.material.MaterialUsageScreen
import com.offshore.platform.mobile.ui.view.qualification.QualificationStatusScreen
import com.offshore.platform.mobile.ui.view.ai.AiResultsScreen
import com.offshore.platform.mobile.ui.view.knowledge.KnowledgeScreen
import com.offshore.platform.mobile.ui.view.home.HomeScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val session by authViewModel.session.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                onNavigateToWorkOrderList = { navController.navigate(Routes.WORK_ORDER_LIST) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                onNavigateToLoginOffline = { navController.navigate(Routes.WORK_ORDER_LIST) { popUpTo(Routes.SPLASH) { inclusive = true } } }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { navController.navigate(Routes.WORK_ORDER_LIST) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                onOfflineLogin = { navController.navigate(Routes.WORK_ORDER_LIST) { popUpTo(Routes.SPLASH) { inclusive = true } } }
            )
        }

        composable(Routes.WORK_ORDER_LIST) {
            WorkOrderListScreen(
                onNavigateToDetail = { id -> navController.navigate(Routes.workOrderDetail(id)) },
                onNavigateToMine = { navController.navigate(Routes.MINE) }
            )
        }

        composable(route = Routes.WORK_ORDER_DETAIL, arguments = listOf(navArgument("workOrderId") { type = NavType.LongType })) { e ->
            val id = e.arguments?.getLong("workOrderId") ?: 0L
            WorkOrderDetailScreen(
                workOrderId = id,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRecords = { navController.navigate(Routes.WORK_ORDER_RECORDS.replace("{workOrderId}", id.toString())) },
                onNavigateToAttachments = { navController.navigate(Routes.WORK_ORDER_ATTACHMENTS.replace("{workOrderId}", id.toString())) },
                onNavigateToSignature = { navController.navigate(Routes.SIGNATURE.replace("{workOrderId}", id.toString())) },
                onNavigateToPdf = { navController.navigate(Routes.PDF_PREVIEW.replace("{workOrderId}", id.toString())) },
                onNavigateToAi = { navController.navigate(Routes.AI_RESULTS.replace("{workOrderId}", id.toString())) }
            )
        }

        composable(route = Routes.WORK_ORDER_RECORDS, arguments = listOf(navArgument("workOrderId") { type = NavType.LongType })) { e ->
            val woId = e.arguments?.getLong("workOrderId") ?: 0L
            WorkRecordListScreen(workOrderId = woId, onNavigateBack = { navController.popBackStack() },
                onCreateRecord = { navController.navigate(Routes.RECORD_CREATE.replace("{workOrderId}", woId.toString())) },
                onEditRecord = { rid -> navController.navigate(Routes.RECORD_EDIT.replace("{recordId}", rid.toString())) }
            )
        }

        composable(route = Routes.RECORD_CREATE, arguments = listOf(navArgument("workOrderId") { type = NavType.LongType })) { e ->
            val woId = e.arguments?.getLong("workOrderId") ?: 0L
            WorkRecordEditScreen(workOrderId = woId, editRecordId = null, onSaved = { navController.popBackStack() }, onNavigateBack = { navController.popBackStack() })
        }

        composable(route = Routes.RECORD_EDIT, arguments = listOf(navArgument("recordId") { type = NavType.LongType })) { e ->
            val rid = e.arguments?.getLong("recordId") ?: 0L
            WorkRecordEditScreen(workOrderId = 0L, editRecordId = rid, onSaved = { navController.popBackStack() }, onNavigateBack = { navController.popBackStack() })
        }

        composable(route = Routes.WORK_ORDER_ATTACHMENTS, arguments = listOf(navArgument("workOrderId") { type = NavType.LongType })) { e ->
            val woId = e.arguments?.getLong("workOrderId") ?: 0L
            AttachmentListScreen(workOrderId = woId, onNavigateBack = { navController.popBackStack() },
                onAddPhoto = { navController.navigate(Routes.CAMERA) })
        }

        composable(Routes.CAMERA) {
            CameraScreen(workOrderId = 0L, recordId = null, workOrderNo = "UNKNOWN", workLocation = null,
                onNavigateBack = { navController.popBackStack() }, onPhotoSaved = { navController.popBackStack() })
        }

        composable(route = Routes.VIDEO_RECORD, arguments = listOf(
            navArgument("workOrderId") { type = NavType.LongType },
            navArgument("recordId") { type = NavType.LongType },
            navArgument("workOrderNo") { type = NavType.StringType }
        )) { e ->
            VideoRecordScreen(
                workOrderId = e.arguments?.getLong("workOrderId") ?: 0L,
                recordId = e.arguments?.getLong("recordId")?.takeIf { it > 0 },
                workOrderNo = e.arguments?.getString("workOrderNo") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onVideoSaved = { navController.popBackStack() }
            )
        }

        composable(route = Routes.AUDIO_RECORD, arguments = listOf(
            navArgument("workOrderId") { type = NavType.LongType },
            navArgument("recordId") { type = NavType.LongType },
            navArgument("workOrderNo") { type = NavType.StringType }
        )) { e ->
            AudioRecordScreen(
                workOrderId = e.arguments?.getLong("workOrderId") ?: 0L,
                recordId = e.arguments?.getLong("recordId")?.takeIf { it > 0 },
                workOrderNo = e.arguments?.getString("workOrderNo") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onAudioSaved = { navController.popBackStack() }
            )
        }

        composable(route = Routes.PDF_PREVIEW, arguments = listOf(navArgument("workOrderId") { type = NavType.LongType })) { e ->
            val woId = e.arguments?.getLong("workOrderId") ?: 0L
            val pdfViewModel: PdfViewModel = hiltViewModel()
            LaunchedEffect(woId) { pdfViewModel.init(woId) }
            PdfPreviewScreen(pdfFilePath = null, workOrderNo = "WO-$woId",
                onNavigateBack = { navController.popBackStack() },
                onRegenerate = { /* handled by ViewModel */ },
                viewModel = pdfViewModel
            )
        }

        composable(route = Routes.SIGNATURE, arguments = listOf(navArgument("workOrderId") { type = NavType.LongType })) { e ->
            val woId = e.arguments?.getLong("workOrderId") ?: 0L
            SignatureScreen(workOrderId = woId,
                onNavigateBack = { navController.popBackStack() },
                onSignatureSaved = { navController.popBackStack() }
            )
        }

        // Route for acceptance_submit not defined separately — reuses signature flow

        composable(route = Routes.MATERIAL_USAGE, arguments = listOf(navArgument("workOrderId") { type = NavType.LongType })) { e ->
            val woId = e.arguments?.getLong("workOrderId") ?: 0L
            MaterialUsageScreen(workOrderId = woId, onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.QUALIFICATION_STATUS) { QualificationStatusScreen() }
        composable(route = Routes.AI_RESULTS, arguments = listOf(navArgument("workOrderId") { type = NavType.LongType })) { e ->
            val woId = e.arguments?.getLong("workOrderId") ?: 0L
            AiResultsScreen(workOrderId = woId, onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.KNOWLEDGE) { KnowledgeScreen() }
        composable(Routes.SYNC_CENTER) { SyncCenterScreen() }

        composable(Routes.MINE) {
            MineScreen(session = session, onLogout = { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } })
        }

        composable(Routes.HOME) { HomeScreen(versionName = "1.0.0", onNavigateToLogin = {}) }
    }
}
