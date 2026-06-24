package com.payalert.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import coil.compose.AsyncImage
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.ReviewException
import com.payalert.app.ads.AdMobConfig
import com.payalert.app.ads.InterstitialAdController
import com.payalert.app.billing.PayAlertBillingManager
import com.payalert.app.billing.ProBillingUiState
import com.payalert.app.data.AppPreferencesRepository
import com.payalert.app.data.BankCatalog
import com.payalert.app.data.CardsRepository
import com.payalert.app.data.CreditCardItem
import com.payalert.app.data.Monetization
import com.payalert.app.data.NotificationSettings
import com.payalert.app.data.NotificationSettingsRepository
import com.payalert.app.data.OnboardingRepository
import com.payalert.app.data.ProAccessRepository
import com.payalert.app.data.StatusStyle
import com.payalert.app.notifications.NotificationScheduler
import com.payalert.app.widget.PayAlertWidgetRenderer
import com.payalert.app.ui.theme.Accent
import com.payalert.app.ui.theme.BackgroundBottom
import com.payalert.app.ui.theme.BackgroundTop
import com.payalert.app.ui.theme.DarkBackgroundBottom
import com.payalert.app.ui.theme.DarkBackgroundTop
import com.payalert.app.ui.theme.PayAlertTheme
import com.payalert.app.ui.theme.CardSurface
import com.payalert.app.ui.theme.StatusGray
import com.payalert.app.ui.theme.StatusGreen
import com.payalert.app.ui.theme.StatusOrange
import com.payalert.app.ui.theme.StatusRed
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import kotlin.math.abs

private enum class FormMode {
    Add,
    Edit,
}

private enum class CardsFilter(val label: String) {
    All("Todas"),
    Pending("Pendientes"),
    Overdue("Vencidas"),
    Paid("Pagadas"),
}

private enum class DebtEntryMode(val title: String) {
    PartialPayment("Monto a adelantar"),
    ReplacementTotal("Nuevo total"),
}

private data class DebtChartEntry(
    val label: String,
    val amount: Double,
)

private data class DebtChartSlice(
    val label: String,
    val amount: Double,
    val color: Color,
    val startAngle: Float,
    val sweepAngle: Float,
)

private data class CardFormState(
    val id: String? = null,
    val selectedBankId: String = BankCatalog.banks.first().id,
    val selectedCardType: String = BankCatalog.banks.first().cardTypes.first(),
    val lastDigits: String = "",
    val totalDebt: String = "",
    val cutDateText: String = LocalDate.now().toString(),
    val dueDateText: String = LocalDate.now().plusDays(20).toString(),
    val isPaid: Boolean = false,
)

private data class OnboardingSlide(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String,
    val highlights: List<String>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayAlertApp() {
    val context = LocalContext.current
    val appPreferencesRepository = remember(context) { AppPreferencesRepository(context) }
    val isDarkMode by produceState(initialValue = false, appPreferencesRepository) {
        appPreferencesRepository.isDarkMode.collect { value = it }
    }
    val scope = rememberCoroutineScope()

    PayAlertTheme(darkTheme = isDarkMode) {
        PayAlertAppContent(
            isDarkMode = isDarkMode,
            onToggleDarkMode = {
                scope.launch {
                    appPreferencesRepository.setDarkMode(!isDarkMode)
                }
            },
            appPreferencesRepository = appPreferencesRepository,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayAlertAppContent(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    appPreferencesRepository: AppPreferencesRepository,
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val repository = remember(context) { CardsRepository(context) }
    val notificationSettingsRepository = remember(context) { NotificationSettingsRepository(context) }
    val onboardingRepository = remember(context) { OnboardingRepository(context) }
    val proAccessRepository = remember(context) { ProAccessRepository(context) }
    val billingManager = remember(context, proAccessRepository) {
        PayAlertBillingManager(context.applicationContext, proAccessRepository)
    }
    val notificationScheduler = remember(context) { NotificationScheduler(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var showSplashOverlay by rememberSaveable { mutableStateOf(true) }
    val cards by produceState<List<CreditCardItem>>(initialValue = emptyList(), repository) {
        repository.cards.collect { loadedCards ->
            val normalizedCards = normalizeCardsForToday(loadedCards)

            if (normalizedCards != loadedCards) {
                repository.saveCards(normalizedCards)
            }

            value = normalizedCards
        }
    }
    val notificationSettings by produceState(initialValue = NotificationSettings(), notificationSettingsRepository) {
        notificationSettingsRepository.settings.collect { value = it }
    }
    val isPro by produceState(initialValue = false, proAccessRepository) {
        proAccessRepository.isPro.collect { value = it }
    }
    val billingUiState by billingManager.uiState.collectAsState()
    LaunchedEffect(isPro) {
        PayAlertWidgetRenderer.syncAvailability(context, isPro)
    }
    val hasSeenOnboarding by onboardingRepository.hasSeenOnboarding.collectAsState(initial = false)
    val latestCards by rememberUpdatedState(cards)

    var formMode by rememberSaveable { mutableStateOf(FormMode.Add) }
    var formState by remember { mutableStateOf(CardFormState()) }
    var showForm by rememberSaveable { mutableStateOf(false) }
    var showNotificationSettings by rememberSaveable { mutableStateOf(false) }
    var showProPanel by rememberSaveable { mutableStateOf(false) }
    var showInfoPanel by rememberSaveable { mutableStateOf(false) }
    var deleteCandidateId by rememberSaveable { mutableStateOf<String?>(null) }
    var filter by rememberSaveable { mutableStateOf(CardsFilter.All) }
    var sortByDate by rememberSaveable { mutableStateOf(true) }
    var showTopBarMenu by rememberSaveable { mutableStateOf(false) }
    var paymentTargetId by rememberSaveable { mutableStateOf<String?>(null) }
    var paymentAmountText by rememberSaveable { mutableStateOf("") }
    var paymentEntryMode by rememberSaveable { mutableStateOf<DebtEntryMode?>(null) }
    var notificationPermissionGranted by remember { mutableStateOf(context.hasNotificationPermission()) }
    val openPrivacyPolicy = remember(context) {
        { context.openExternalLink("https://adrianrol87.com.mx/privacy/privacy.html") }
    }
    val openTerms = remember(context) {
        { context.openExternalLink("https://adrianrol87.com.mx/terms/terms.html") }
    }
    val appVersionName = remember(context) { context.appVersionName() }
    val onPaidActionCompleted: () -> Unit = {
        if (activity != null) {
            scope.launch {
                val paidCount = appPreferencesRepository.incrementPaidCount()
                if (paidCount == 5 || paidCount == 15 || paidCount == 30) {
                    launchInAppReview(activity)
                }
            }
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notificationPermissionGranted = granted
    }

    DisposableEffect(billingManager) {
        billingManager.start()
        onDispose {
            billingManager.dispose()
        }
    }

    LaunchedEffect(cards, notificationSettings) {
        notificationScheduler.rescheduleAll(cards, notificationSettings)
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1300)
        showSplashOverlay = false
    }

    DisposableEffect(lifecycleOwner, repository, scope) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val normalizedCards = normalizeCardsForToday(latestCards)
                if (normalizedCards != latestCards) {
                    scope.launch {
                        repository.saveCards(normalizedCards)
                    }
                }
                billingManager.refreshPurchases()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val visibleCards = remember(cards, filter, sortByDate) {
        buildVisibleCards(cards, filter, sortByDate)
    }
    val freeSlotsRemaining = (Monetization.freeCardsLimit - cards.size).coerceAtLeast(0)
    val openProPanel = {
        showForm = false
        showNotificationSettings = false
        showInfoPanel = false
        showProPanel = true
        Unit
    }
    val openInfoPanel = {
        showForm = false
        showNotificationSettings = false
        showProPanel = false
        showInfoPanel = true
        Unit
    }
    val openCardForm: (FormMode, CardFormState) -> Unit = { mode, state ->
        formMode = mode
        formState = state
        showNotificationSettings = false
        showProPanel = false
        showInfoPanel = false
        showForm = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        if (isDarkMode) DarkBackgroundTop else BackgroundTop,
                        if (isDarkMode) DarkBackgroundBottom else BackgroundBottom,
                    ),
                ),
            ),
    ) {
        if (hasSeenOnboarding) {
            Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("PayAlert", fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                "Tus tarjetas ya se guardan en el telefono",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.82f),
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showTopBarMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Abrir menu",
                                    tint = Color.White,
                                )
                            }
                            DropdownMenu(
                                expanded = showTopBarMenu,
                                onDismissRequest = { showTopBarMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        MenuItemLabel(
                                            icon = Icons.Filled.Star,
                                            text = if (isPro) "Pro" else "Upgrade",
                                        )
                                    },
                                    onClick = {
                                        showTopBarMenu = false
                                        if (showProPanel) {
                                            showProPanel = false
                                        } else {
                                            openProPanel()
                                        }
                                    },
                                )
                                DropdownMenuItem(
                                    text = {
                                        MenuItemLabel(
                                            icon = Icons.Filled.CalendarMonth,
                                            text = if (sortByDate) "Orden: fecha" else "Orden: manual",
                                        )
                                    },
                                    onClick = {
                                        showTopBarMenu = false
                                        sortByDate = !sortByDate
                                    },
                                )
                                DropdownMenuItem(
                                    text = {
                                        MenuItemLabel(
                                            icon = Icons.Filled.Notifications,
                                            text = "Avisos",
                                        )
                                    },
                                    onClick = {
                                        showTopBarMenu = false
                                        showProPanel = false
                                        showNotificationSettings = !showNotificationSettings
                                    },
                                )
                                DropdownMenuItem(
                                    text = {
                                        MenuItemLabel(
                                            icon = Icons.Filled.Tune,
                                            text = "Info",
                                        )
                                    },
                                    onClick = {
                                        showTopBarMenu = false
                                        openInfoPanel()
                                    },
                                )
                                DropdownMenuItem(
                                    text = {
                                        MenuItemLabel(
                                            icon = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                            text = if (isDarkMode) "Modo claro" else "Modo oscuro",
                                        )
                                    },
                                    onClick = {
                                        showTopBarMenu = false
                                        onToggleDarkMode()
                                    },
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    ),
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (!isPro && cards.size >= Monetization.freeCardsLimit) {
                            openProPanel()
                        } else {
                            if (showForm && formMode == FormMode.Add) {
                                showForm = false
                            } else {
                                openCardForm(FormMode.Add, CardFormState())
                            }
                        }
                    },
                    containerColor = Accent,
                    contentColor = Color.White,
                ) {
                    Text(if (showForm && formMode == FormMode.Add) "x" else "+")
                }
            },
            ) { innerPadding ->
                LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Spacer(Modifier.height(8.dp))
                    HomeDashboard(
                        cards = cards,
                        isPro = isPro,
                        freeSlotsRemaining = freeSlotsRemaining,
                        onUpgrade = openProPanel,
                    )
                }

                item {
                    FilterRow(selected = filter, onSelected = { filter = it })
                }

                if (cards.isEmpty()) {
                    item {
                        EmptyStateCard()
                    }
                } else if (visibleCards.isEmpty()) {
                    item {
                        EmptyStateCard("No hay tarjetas en este filtro", "Cambia el filtro o agrega una tarjeta nueva.")
                    }
                } else {
                    itemsIndexed(visibleCards, key = { _, item -> item.id }) { index, item ->
                        if (!isPro && index >= 0) {
                            InlineBannerAd()
                        }
                        CardRow(
                            item = item,
                            isManualMode = !sortByDate,
                            canMoveUp = visibleCards.indexOf(item) > 0,
                            canMoveDown = visibleCards.indexOf(item) < visibleCards.lastIndex,
                            onMarkPaid = {
                                if (item.hasDebt) {
                                    paymentTargetId = item.id
                                    paymentAmountText = ""
                                    paymentEntryMode = null
                                } else {
                                    val nextCards = cards.map { existing ->
                                        if (existing.id == item.id) existing.copy(isPaid = true, totalDebt = 0.0) else existing
                                    }
                                    scope.launch {
                                        repository.saveCards(nextCards)
                                    }
                                    onPaidActionCompleted()
                                }
                            },
                            onMarkPending = {
                                val nextCards = cards.map { existing ->
                                    if (existing.id == item.id) existing.copy(isPaid = false) else existing
                                }
                                scope.launch {
                                    repository.saveCards(nextCards)
                                }
                            },
                            onEdit = {
                                openCardForm(FormMode.Edit, cardFormStateFromItem(item))
                            },
                            onDelete = {
                                deleteCandidateId = item.id
                            },
                            onMoveUp = {
                                val reordered = moveManualCard(cards, item.id, -1)
                                scope.launch {
                                    repository.saveCards(reordered)
                                }
                            },
                            onMoveDown = {
                                val reordered = moveManualCard(cards, item.id, 1)
                                scope.launch {
                                    repository.saveCards(reordered)
                                }
                            },
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(20.dp))
                }
                }
            }

            if (showForm) {
                FullScreenMenuOverlay {
                    AddOrEditCardForm(
                        mode = formMode,
                        initialState = formState,
                        existingItems = cards,
                        onSave = { updatedFormState ->
                            val shouldShowInterstitial = !isPro
                            formState = updatedFormState
                            val item = updatedFormState.toCardItem()
                            val itemWithOrder = if (formMode == FormMode.Add) {
                                item.copy(manualOrder = nextManualOrder(cards))
                            } else {
                                item.copy(
                                    manualOrder = cards.firstOrNull { it.id == updatedFormState.id }?.manualOrder ?: item.manualOrder,
                                )
                            }

                            val nextCards = if (formMode == FormMode.Edit && updatedFormState.id != null) {
                                cards.map { existing ->
                                    if (existing.id == updatedFormState.id) itemWithOrder else existing
                                }
                            } else {
                                cards + itemWithOrder
                            }

                            scope.launch {
                                repository.saveCards(nextCards)
                            }
                            if (shouldShowInterstitial && activity != null) {
                                InterstitialAdController.showIfAvailable(activity)
                            }
                            showForm = false
                            formMode = FormMode.Add
                            formState = CardFormState()
                        },
                        onCancel = {
                            showForm = false
                            formMode = FormMode.Add
                            formState = CardFormState()
                        },
                    )
                }
            }

            if (showProPanel) {
                FullScreenMenuOverlay {
                    ProPanel(
                        isPro = isPro,
                        billingUiState = billingUiState,
                        onPurchase = {
                            activity?.let { currentActivity ->
                                billingManager.launchPurchase(currentActivity)
                            }
                        },
                        onRestorePurchase = {
                            billingManager.restorePurchases()
                        },
                        onClose = { showProPanel = false },
                    )
                }
            }

            if (showNotificationSettings) {
                FullScreenMenuOverlay {
                    NotificationSettingsPanel(
                        settings = notificationSettings,
                        permissionGranted = notificationPermissionGranted,
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                notificationPermissionGranted = true
                            }
                        },
                        onSettingsChanged = { updated ->
                            scope.launch {
                                notificationSettingsRepository.save(updated)
                            }
                        },
                        onClose = { showNotificationSettings = false },
                    )
                }
            }

            if (showInfoPanel) {
                FullScreenMenuOverlay {
                    InfoPanel(
                        versionName = appVersionName,
                        onOpenPrivacy = openPrivacyPolicy,
                        onOpenTerms = openTerms,
                        onClose = { showInfoPanel = false },
                    )
                }
            }
        } else {
            OnboardingFlow(
                onRequestNotifications = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onFinish = {
                    scope.launch {
                        onboardingRepository.setHasSeenOnboarding(true)
                    }
                },
            )
        }

        if (showSplashOverlay) {
            PayAlertSplashOverlay()
        }
    }

    if (deleteCandidateId != null) {
        AlertDialog(
            onDismissRequest = { deleteCandidateId = null },
            title = { Text("Eliminar tarjeta") },
            text = { Text("Esta accion quitara la tarjeta guardada del dispositivo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val nextCards = cards.filterNot { it.id == deleteCandidateId }
                        scope.launch {
                            repository.saveCards(nextCards)
                        }
                        deleteCandidateId = null
                    },
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidateId = null }) {
                    Text("Cancelar")
                }
            },
        )
    }

    val paymentItem = cards.firstOrNull { it.id == paymentTargetId }
    if (paymentItem != null && paymentEntryMode == null) {
        AlertDialog(
            onDismissRequest = {
                paymentTargetId = null
                paymentAmountText = ""
                paymentEntryMode = null
            },
            title = { Text("Gestionar deuda") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("La tarjeta ${paymentItem.bankName} ${paymentItem.cardType} tiene una deuda registrada. Elige como quieres dejar ese monto.")
                    Button(
                        onClick = {
                            paymentEntryMode = DebtEntryMode.PartialPayment
                            paymentAmountText = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                    ) {
                        Text("Adelantar pago")
                    }
                    Button(
                        onClick = {
                            val nextCards = cards.map { existing ->
                                if (existing.id == paymentItem.id) existing.copy(isPaid = true, totalDebt = 0.0) else existing
                            }
                            scope.launch {
                                repository.saveCards(nextCards)
                            }
                            onPaidActionCompleted()
                            paymentTargetId = null
                            paymentAmountText = ""
                            paymentEntryMode = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                    ) {
                        Text("Liquidar deuda y marcar pagada")
                    }
                    Button(
                        onClick = {
                            paymentEntryMode = DebtEntryMode.ReplacementTotal
                            paymentAmountText = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                    ) {
                        Text("Marcar pagada y capturar nuevo total")
                    }
                    Button(
                        onClick = {
                            val nextCards = cards.map { existing ->
                                if (existing.id == paymentItem.id) existing.copy(isPaid = true) else existing
                            }
                            scope.launch {
                                repository.saveCards(nextCards)
                            }
                            onPaidActionCompleted()
                            paymentTargetId = null
                            paymentAmountText = ""
                            paymentEntryMode = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                    ) {
                        Text("Solo marcar pagada")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    paymentTargetId = null
                    paymentAmountText = ""
                    paymentEntryMode = null
                }) {
                    Text("Cancelar")
                }
            },
        )
    }
    if (paymentItem != null && paymentEntryMode != null) {
        val entryMode = paymentEntryMode!!
        val currentDebt = paymentItem.totalDebt ?: 0.0
        AlertDialog(
            onDismissRequest = {
                paymentTargetId = null
                paymentAmountText = ""
                paymentEntryMode = null
            },
            title = { Text(entryMode.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tarjeta ${paymentItem.bankName} ${paymentItem.cardType} • ${paymentItem.lastDigits}")
                    Text(
                        "Deuda actual: ${paymentItem.totalDebt?.let { "$${"%.2f".format(it)}" } ?: "-"}",
                        color = Color(0xFF475569),
                    )
                    Text(
                        when (entryMode) {
                            DebtEntryMode.PartialPayment -> {
                                val previewAmount = paymentAmountText.parseDebtOrNull()
                                val remainingDebt = if (previewAmount != null && previewAmount > 0) {
                                    (currentDebt - previewAmount).coerceAtLeast(0.0)
                                } else {
                                    currentDebt
                                }
                                "Este adelanto reducira la deuda a $${"%.2f".format(remainingDebt)} y la tarjeta seguira en curso."
                            }
                            DebtEntryMode.ReplacementTotal ->
                                "Ingresa el total actualizado del siguiente estado de cuenta."
                        },
                        color = Color(0xFF64748B),
                    )
                    OutlinedTextField(
                        value = paymentAmountText,
                        onValueChange = { paymentAmountText = it },
                        label = {
                            Text(
                                if (entryMode == DebtEntryMode.PartialPayment) {
                                    "Monto a adelantar"
                                } else {
                                    "Nuevo total"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = paymentAmountText.parseDebtOrNull()
                        if (amount != null && amount > 0) {
                            val nextCards = cards.map { existing ->
                                if (existing.id == paymentItem.id) {
                                    when (entryMode) {
                                        DebtEntryMode.PartialPayment -> {
                                            val remainingDebt = (currentDebt - amount).coerceAtLeast(0.0)
                                            existing.copy(
                                                totalDebt = remainingDebt,
                                                isPaid = remainingDebt == 0.0,
                                            )
                                        }
                                        DebtEntryMode.ReplacementTotal ->
                                            existing.copy(
                                                totalDebt = amount,
                                                isPaid = true,
                                            )
                                    }
                                } else {
                                    existing
                                }
                            }
                            scope.launch {
                                repository.saveCards(nextCards)
                            }
                            val willBePaid = when (entryMode) {
                                DebtEntryMode.PartialPayment -> (currentDebt - amount).coerceAtLeast(0.0) == 0.0
                                DebtEntryMode.ReplacementTotal -> true
                            }
                            if (willBePaid) {
                                onPaidActionCompleted()
                            }
                        }
                        paymentTargetId = null
                        paymentAmountText = ""
                        paymentEntryMode = null
                    },
                ) {
                    Text(
                        if (entryMode == DebtEntryMode.PartialPayment) {
                            "Aplicar adelanto"
                        } else {
                            "Guardar total"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        paymentTargetId = null
                        paymentAmountText = ""
                        paymentEntryMode = null
                    },
                ) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun PayAlertSplashOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14203A)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Image(
                painter = painterResource(id = com.adrianrol87.payalert.R.drawable.ic_launcher_background),
                contentDescription = "PayAlert",
                modifier = Modifier.size(210.dp),
                contentScale = ContentScale.Fit,
            )
            Text(
                "Controla tus tarjetas sin estres",
                color = Color.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun OnboardingFlow(
    onRequestNotifications: () -> Unit,
    onFinish: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val slides = remember {
        listOf(
            OnboardingSlide(
                icon = Icons.Filled.Star,
                title = "Bienvenido a PayAlert",
                subtitle = "Organiza tus tarjetas, revisa fechas clave y manten tus pagos bajo control desde un solo lugar.",
                highlights = listOf(
                    "Guarda fecha de pago y fecha de corte",
                    "Consulta tus tarjetas activas rapidamente",
                    "Empieza en pocos segundos",
                ),
            ),
            OnboardingSlide(
                icon = Icons.Filled.CheckCircle,
                title = "Notificaciones",
                subtitle = "Recibe recordatorios antes de tu fecha limite para evitar olvidos y mantener mejor control de tus pagos.",
                highlights = listOf(
                    "Alertas antes del vencimiento",
                    "Configuracion simple",
                    "Ideal si manejas varias tarjetas",
                ),
            ),
            OnboardingSlide(
                icon = Icons.Filled.Lock,
                title = "PayAlert Pro",
                subtitle = "Desbloquea mas tarjetas, una vista premium y una experiencia sin anuncios.",
                highlights = listOf(
                    "Mas capacidad para tus tarjetas",
                    "Widgets exclusivos en Android",
                    "Dashboard premium",
                ),
            ),
        )
    }
    val pagerState = rememberPagerState(pageCount = { slides.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BackgroundTop, BackgroundBottom),
                ),
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                OnboardingPage(slide = slides[page])
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < slides.lastIndex) {
                        if (pagerState.currentPage == 1) {
                            onRequestNotifications()
                        }
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Text(if (pagerState.currentPage < slides.lastIndex) "Siguiente" else "Comenzar")
            }

            TextButton(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                Text(if (pagerState.currentPage < slides.lastIndex) "Omitir" else "Listo")
            }
        }
    }
}

@Composable
private fun OnboardingPage(slide: OnboardingSlide) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = com.adrianrol87.payalert.R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(18.dp))
        Icon(slide.icon, contentDescription = null, tint = Accent, modifier = Modifier.size(42.dp))
        Spacer(Modifier.height(18.dp))
        Text(slide.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(slide.subtitle, color = Color(0xFF475569), textAlign = TextAlign.Center)
        Spacer(Modifier.height(22.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            slide.highlights.forEach { highlight ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Accent)
                    Text(highlight)
                }
            }
        }
    }
}

@Composable
private fun InlineBannerAd() {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.92f),
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Publicidad",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF64748B),
            )
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = AdMobConfig.bannerAdUnitId
                        loadAd(AdRequest.Builder().build())
                    }
                },
            )
        }
    }
}

@Composable
private fun HomeDashboard(
    cards: List<CreditCardItem>,
    isPro: Boolean,
    freeSlotsRemaining: Int,
    onUpgrade: () -> Unit,
) {
    val activeCards = cards.filter { !it.isPaid }
    val totalDebt = activeCards.sumOf { it.totalDebt ?: 0.0 }
    var showDebtChart by rememberSaveable { mutableStateOf(isPro && totalDebt > 0.0) }
    val dueSoon = activeCards.count { it.daysRemaining in 0L..5L }
    val overdue = activeCards.count { it.daysRemaining < 0L }
    val nextDue = activeCards
        .filter { it.daysRemaining >= 0L }
        .sortedBy { it.dueDate }
        .firstOrNull()
    val debtChartEntries = remember(activeCards) { buildDebtChartEntries(activeCards) }
    val debtChartSlices = remember(debtChartEntries) { buildDebtChartSlices(debtChartEntries) }

    LaunchedEffect(totalDebt, isPro) {
        if (!isPro || totalDebt <= 0.0) {
            showDebtChart = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (!isPro) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFFFF4D6),
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Plan Free", fontWeight = FontWeight.Bold, color = Color(0xFF7C5A00))
                        Text(
                            "Te quedan $freeSlotsRemaining espacio(s). Pro desbloquea tarjetas ilimitadas, widgets y una vista premium.",
                            color = Color(0xFF7C5A00),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = onUpgrade) {
                        Text("Ver Pro")
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.14f),
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.10f),
                                Color.White.copy(alpha = 0.03f),
                            ),
                        ),
                    )
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Panorama del mes",
                        color = Color.White.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f),
                    )

                    if (isPro && debtChartSlices.isNotEmpty()) {
                        IconButton(onClick = { showDebtChart = !showDebtChart }) {
                            Icon(
                                imageVector = if (showDebtChart) Icons.Filled.Remove else Icons.Filled.Add,
                                contentDescription = if (showDebtChart) "Ocultar grafica" else "Mostrar grafica",
                                tint = Color.White,
                            )
                        }
                    }
                }

                if (isPro && showDebtChart && debtChartSlices.isNotEmpty()) {
                    DebtBreakdownChart(
                        totalDebt = totalDebt,
                        slices = debtChartSlices,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InsightPill(
                        icon = Icons.Filled.CreditCard,
                        label = "Activas",
                        value = activeCards.size.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    InsightPill(
                        icon = Icons.Filled.AccessTime,
                        label = "Por vencer",
                        value = dueSoon.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    InsightPill(
                        icon = Icons.Filled.Warning,
                        label = "Vencidas",
                        value = overdue.toString(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (nextDue != null) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.92f),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Siguiente pago clave",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF64748B),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${nextDue.bankName} ${nextDue.cardType}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Termina en ${nextDue.lastDigits} • vence el ${nextDue.dueDate}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF475569),
                        )
                    }

                    StatusChip(
                        text = if (nextDue.daysRemaining == 0L) "Hoy" else "${nextDue.daysRemaining} d",
                        style = nextDue.statusStyle,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProPanel(
    isPro: Boolean,
    billingUiState: ProBillingUiState,
    onPurchase: () -> Unit,
    onRestorePurchase: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.96f),
        tonalElevation = 10.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Accent)
                    Text("PayAlert Pro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                if (isPro) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Activo") },
                    )
                }
            }

            Text(
                "La version pensada para usuarios que ya administran varias tarjetas y quieren una experiencia mas premium.",
                color = Color(0xFF475569),
            )

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF7FAFC),
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(billingUiState.priceLabel, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Pago unico. Sin suscripcion mensual.", color = Color(0xFF64748B))

                    ProBenefit("Tarjetas ilimitadas")
                    ProBenefit("Widgets de PayAlert solo para Pro")
                    ProBenefit("Vista mas premium y enfocada en deuda")
                    ProBenefit("Restaurar compra desde Google Play")
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFEEF6FF),
                tonalElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Accent)
                    Text(
                        "PayAlert Pro usa una compra unica desde Google Play. Para probarla, sube la app al track interno y crea el producto en Play Console con el mismo ID configurado en el codigo.",
                        color = Color(0xFF24415C),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            billingUiState.message?.let { message ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF8FAFC),
                    tonalElevation = 0.dp,
                ) {
                    Text(
                        text = message,
                        color = Color(0xFF475569),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                    )
                }
            }

            if (!isPro) {
                Button(
                    onClick = onPurchase,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = billingUiState.isReady && !billingUiState.isBusy,
                ) {
                    Text(if (billingUiState.isBusy) "Abriendo Google Play..." else "Comprar PayAlert Pro")
                }
            } else {
                AssistChip(
                    onClick = {},
                    label = { Text("Tu compra Pro esta activa") },
                )
            }

            OutlinedActionButton(
                text = "Restaurar compra",
                onClick = onRestorePurchase,
            )

            TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar")
            }
        }
    }
}

@Composable
private fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEFF6FF),
            contentColor = Accent,
        ),
    ) {
        Text(text)
    }
}

@Composable
private fun FullScreenMenuOverlay(
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        BackgroundTop.copy(alpha = 0.98f),
                        BackgroundBottom.copy(alpha = 0.98f),
                    ),
                ),
            )
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = 52.dp,
                bottom = 20.dp,
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun ProBenefit(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusGreen)
        Text(text)
    }
}

@Composable
private fun InfoPanel(
    versionName: String,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.96f),
        tonalElevation = 10.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = null, tint = Accent)
                    Text("Info", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFFF7FAFC),
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("PayAlert", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Version $versionName", color = Color(0xFF475569))
                    Text(
                        "Aqui puedes revisar los enlaces legales de la app.",
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Button(onClick = onOpenPrivacy, modifier = Modifier.fillMaxWidth()) {
                Text("Privacidad")
            }

            Button(onClick = onOpenTerms, modifier = Modifier.fillMaxWidth()) {
                Text("Terminos")
            }

            TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar")
            }
        }
    }
}

@Composable
private fun MenuItemLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Accent)
        Text(text)
    }
}

@Composable
private fun DebtBreakdownChart(
    totalDebt: Double,
    slices: List<DebtChartSlice>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(0.9f),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(168.dp)) {
                slices.forEach { slice ->
                    drawArc(
                        color = slice.color,
                        startAngle = slice.startAngle,
                        sweepAngle = slice.sweepAngle,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 28.dp.toPx(),
                            cap = StrokeCap.Butt,
                        ),
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$${"%,.2f".format(totalDebt)}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "Total",
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Column(
            modifier = Modifier.weight(1.1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            slices.forEach { slice ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(slice.color),
                    )
                    Column {
                        Text(
                            slice.label,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                        Text(
                            "$${"%,.2f".format(slice.amount)}",
                            color = Color.White.copy(alpha = 0.74f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.10f),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.82f),
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    value,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                label,
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String = "Aun no hay tarjetas",
    message: String = "Toca el boton + para agregar la primera y empezar a llevar tus fechas de corte y pago.",
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.94f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(message, color = Color(0xFF475569))
        }
    }
}

@Composable
private fun FilterRow(
    selected: CardsFilter,
    onSelected: (CardsFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CardsFilter.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = { Text(filter.label) },
            )
        }
    }
}

@Composable
private fun CardRow(
    item: CreditCardItem,
    isManualMode: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMarkPaid: () -> Unit,
    onMarkPending: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    val context = LocalContext.current
    val assetPath = rememberAssetPath(context, item)
    val borderColor = when (item.statusStyle) {
        StatusStyle.Green -> StatusGreen.copy(alpha = 0.95f)
        StatusStyle.Orange -> StatusOrange.copy(alpha = 0.95f)
        StatusStyle.Red -> StatusRed.copy(alpha = 0.95f)
        StatusStyle.Gray -> StatusGray.copy(alpha = 0.9f)
    }
    val reorderThresholdPx = with(LocalDensity.current) { 72.dp.toPx() }
    val maxDragPreviewPx = reorderThresholdPx / 3f
    var dragOffsetY by remember(item.id, isManualMode) { mutableStateOf(0f) }
    var dragTravelY by remember(item.id, isManualMode) { mutableStateOf(0f) }

    Card(
        modifier = Modifier
            .pointerInput(item.id, isManualMode, canMoveUp, canMoveDown) {
                if (!isManualMode) return@pointerInput
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        dragOffsetY = 0f
                        dragTravelY = 0f
                    },
                    onDragEnd = {
                        dragOffsetY = 0f
                        dragTravelY = 0f
                    },
                    onDragCancel = {
                        dragOffsetY = 0f
                        dragTravelY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragTravelY += dragAmount.y
                        dragOffsetY = (dragOffsetY + dragAmount.y).coerceIn(-maxDragPreviewPx, maxDragPreviewPx)

                        if (dragTravelY <= -reorderThresholdPx) {
                            if (canMoveUp) {
                                onMoveUp()
                            }
                            dragTravelY = 0f
                            dragOffsetY = 0f
                        } else if (dragTravelY >= reorderThresholdPx) {
                            if (canMoveDown) {
                                onMoveDown()
                            }
                            dragTravelY = 0f
                            dragOffsetY = 0f
                        }
                    },
                )
            }
            .graphicsLayer {
                translationY = if (isManualMode) dragOffsetY else 0f
                alpha = if (isManualMode && abs(dragOffsetY) > 0f) 0.96f else 1f
            }
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface.copy(alpha = 0.95f)),
        border = BorderStroke(4.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isManualMode && abs(dragOffsetY) > 0f) 18.dp else 12.dp,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.58f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = assetPath?.let { "file:///android_asset/$it" },
                    contentDescription = "${item.bankName} ${item.cardType}",
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (item.hasDebt) "Deuda registrada ${item.totalDebt?.let { "• $${"%.2f".format(it)}" }.orEmpty()}" else "Sin deuda registrada",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("${item.bankName} ${item.cardType}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Terminacion ${item.lastDigits}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
                }
                StatusChip(item.statusText, item.statusStyle)
            }

            HorizontalDivider()

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailBlock("Corte", item.cutDate.toString())
                DetailBlock("Pago", item.dueDate.toString())
                DetailBlock("Restante", item.totalDebt?.let { "$${"%.2f".format(it)}" } ?: "-")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = if (item.isPaid) onMarkPending else onMarkPaid,
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isPaid) Color(0xFFF1F5F9) else Accent,
                        contentColor = if (item.isPaid) Color(0xFF334155) else Color.White,
                    ),
                ) {
                    Text(
                        when {
                            item.isPaid -> "Pagada"
                            item.hasDebt -> "Registrar pago"
                            else -> "Marcar pagada"
                        }
                    )
                }
                TextButton(onClick = onDelete, modifier = Modifier.weight(0.85f)) {
                    Text("Eliminar", color = StatusRed)
                }
            }

            if (isManualMode) {
                Text(
                    "Manten presionada la tarjeta y arrastrala para reordenar.",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun rememberAssetPath(context: Context, item: CreditCardItem): String? {
    return remember(item.id, item.preferredAssetDirectory, item.fallbackAssetDirectory) {
        AssetImageResolver.assetPathFor(
            context = context,
            preferredDirectory = item.preferredAssetDirectory,
            fallbackDirectory = item.fallbackAssetDirectory,
        )
    }
}

@Composable
private fun StatusChip(text: String, style: StatusStyle) {
    val color = when (style) {
        StatusStyle.Green -> StatusGreen
        StatusStyle.Orange -> StatusOrange
        StatusStyle.Red -> StatusRed
        StatusStyle.Gray -> StatusGray
    }

    AssistChip(
        onClick = {},
        label = { Text(text, color = Color.White) },
        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
            containerColor = color,
            labelColor = Color.White,
        ),
    )
}

@Composable
private fun DetailBlock(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF64748B))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOrEditCardForm(
    mode: FormMode,
    initialState: CardFormState,
    existingItems: List<CreditCardItem>,
    onSave: (CardFormState) -> Unit,
    onCancel: () -> Unit,
) {
    val banks = remember { BankCatalog.banks }
    var expandedBank by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }
    var formState by remember { mutableStateOf(initialState) }
    var validationMessage by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(initialState) {
        formState = initialState
        validationMessage = null
    }

    val selectedBank = banks.firstOrNull { it.id == formState.selectedBankId } ?: banks.first()
    val canSave = formState.lastDigits.length == 4

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.96f),
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (mode == FormMode.Add) Icons.Filled.Add else Icons.Filled.Edit,
                        contentDescription = null,
                        tint = Accent,
                    )
                    Text(
                        if (mode == FormMode.Add) "Agregar tarjeta" else "Editar tarjeta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                TextButton(onClick = onCancel) {
                    Text("Cerrar")
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF7FAFC),
                tonalElevation = 0.dp,
            ) {
                Text(
                    "Las tarjetas quedan guardadas localmente en el dispositivo. Captura banco, terminacion, deuda y fechas del ciclo actual.",
                    color = Color(0xFF475569),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                )
            }

            BankDropdown(
                label = "Banco",
                expanded = expandedBank,
                value = selectedBank.name,
                onExpandedChange = { expandedBank = it },
                items = banks,
                itemLabel = { it.name },
                onItemSelected = { bank ->
                    formState = formState.copy(
                        selectedBankId = bank.id,
                        selectedCardType = bank.cardTypes.first(),
                    )
                    expandedBank = false
                },
            )

            BankDropdown(
                label = "Tipo",
                expanded = expandedType,
                value = formState.selectedCardType,
                onExpandedChange = { expandedType = it },
                items = selectedBank.cardTypes,
                itemLabel = { it },
                onItemSelected = { cardType ->
                    formState = formState.copy(selectedCardType = cardType)
                    expandedType = false
                },
            )

            OutlinedTextField(
                value = formState.lastDigits,
                onValueChange = {
                    formState = formState.copy(lastDigits = it.filter(Char::isDigit).take(4))
                },
                label = { Text("Ultimos 4 digitos") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = formState.totalDebt,
                onValueChange = { formState = formState.copy(totalDebt = it) },
                label = { Text("Deuda total") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = formState.cutDateText,
                onValueChange = { formState = formState.copy(cutDateText = it) },
                label = { Text("Fecha de corte (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = formState.dueDateText,
                onValueChange = { formState = formState.copy(dueDateText = it) },
                label = { Text("Fecha limite (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
            )

            validationMessage?.let { message ->
                Text(message, color = StatusRed, style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val cutDate = runCatching { LocalDate.parse(formState.cutDateText) }.getOrNull()
                        val dueDate = runCatching { LocalDate.parse(formState.dueDateText) }.getOrNull()

                        when {
                            cutDate == null || dueDate == null -> {
                                validationMessage = "Revisa las fechas. Usa el formato YYYY-MM-DD."
                            }
                            dueDate.isBefore(cutDate) -> {
                                validationMessage = "La fecha limite no puede ser antes de la fecha de corte."
                            }
                            isDuplicate(formState, existingItems) -> {
                                validationMessage = "Ya existe una tarjeta con ese banco, tipo y terminacion."
                            }
                            else -> {
                                validationMessage = null
                                onSave(formState)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = canSave,
                ) {
                    Text(if (mode == FormMode.Add) "Guardar" else "Actualizar")
                }
            }
        }
    }
}

private fun isDuplicate(formState: CardFormState, existingItems: List<CreditCardItem>): Boolean {
    return existingItems.any { item ->
        item.id != formState.id &&
            item.bankCode == formState.selectedBankId &&
            item.cardType == formState.selectedCardType &&
            item.lastDigits == formState.lastDigits
    }
}

private fun CardFormState.toCardItem(): CreditCardItem {
    val bank = BankCatalog.banks.firstOrNull { it.id == selectedBankId } ?: BankCatalog.banks.first()
    return CreditCardItem(
        id = id ?: UUID.randomUUID().toString(),
        bankCode = bank.id,
        bankName = bank.name,
        cardType = selectedCardType,
        lastDigits = lastDigits,
        totalDebt = totalDebt.parseDebtOrNull(),
        cutDate = runCatching { LocalDate.parse(cutDateText) }.getOrDefault(LocalDate.now()),
        dueDate = runCatching { LocalDate.parse(dueDateText) }.getOrDefault(LocalDate.now().plusDays(20)),
        isPaid = isPaid,
    )
}

private fun cardFormStateFromItem(item: CreditCardItem): CardFormState {
    return CardFormState(
        id = item.id,
        selectedBankId = item.bankCode,
        selectedCardType = item.cardType,
        lastDigits = item.lastDigits,
        totalDebt = item.totalDebt?.let { "%.2f".format(it) }.orEmpty(),
        cutDateText = item.cutDate.toString(),
        dueDateText = item.dueDate.toString(),
        isPaid = item.isPaid,
    )
}

private fun String.parseDebtOrNull(): Double? {
    val sanitized = trim()
        .replace("$", "")
        .replace(",", "")

    return sanitized.toDoubleOrNull()
}

@Composable
private fun NotificationSettingsPanel(
    settings: NotificationSettings,
    permissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onSettingsChanged: (NotificationSettings) -> Unit,
    onClose: () -> Unit,
) {
    var hourText by remember(settings.notificationHour) { mutableStateOf(settings.notificationHour.toString().padStart(2, '0')) }
    var minuteText by remember(settings.notificationMinute) { mutableStateOf(settings.notificationMinute.toString().padStart(2, '0')) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.96f),
        tonalElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = Accent)
                Text("Ajustes de avisos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text(
                if (permissionGranted) "Las notificaciones estan autorizadas."
                else "Activa el permiso del sistema para que PayAlert pueda avisarte.",
                color = Color(0xFF475569),
            )

            if (!permissionGranted) {
                Button(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) {
                    Text("Activar permiso")
                }
            }

            SettingsToggleRow(Icons.Filled.Payments, "Recordatorios de pago", settings.dueEnabled) {
                onSettingsChanged(settings.copy(dueEnabled = it))
            }
            SettingsToggleRow(Icons.Filled.CalendarMonth, "Recordatorios de corte", settings.cutEnabled) {
                onSettingsChanged(settings.copy(cutEnabled = it))
            }
            SettingsToggleRow(Icons.Filled.Payments, "Mostrar monto en aviso", settings.includeAmount) {
                onSettingsChanged(settings.copy(includeAmount = it))
            }
            SettingsToggleRow(Icons.Filled.Tune, "Acciones rapidas", settings.quickActionsEnabled) {
                onSettingsChanged(settings.copy(quickActionsEnabled = it))
            }
            SettingsToggleRow(Icons.Filled.Notifications, "Resumen semanal", settings.weeklySummaryEnabled) {
                onSettingsChanged(settings.copy(weeklySummaryEnabled = it))
            }
            SettingsToggleRow(Icons.Filled.CheckCircle, "Seguimiento extra el dia del pago", settings.sameDayFollowUpEnabled) {
                onSettingsChanged(settings.copy(sameDayFollowUpEnabled = it))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.AccessTime, contentDescription = null, tint = Accent)
                Text("Hora de notificacion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = hourText,
                    onValueChange = {
                        hourText = it.filter(Char::isDigit).take(2)
                        val hour = hourText.toIntOrNull()
                        if (hour != null && hour in 0..23) {
                            onSettingsChanged(settings.copy(notificationHour = hour))
                        }
                    },
                    label = { Text("Hora") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = minuteText,
                    onValueChange = {
                        minuteText = it.filter(Char::isDigit).take(2)
                        val minute = minuteText.toIntOrNull()
                        if (minute != null && minute in 0..59) {
                            onSettingsChanged(settings.copy(notificationMinute = minute))
                        }
                    },
                    label = { Text("Min") },
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Payments, contentDescription = null, tint = Accent)
                Text("Dias antes para pago", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            SettingsToggleRow(Icons.Filled.Payments, "El mismo dia", settings.dueSameDay) { onSettingsChanged(settings.copy(dueSameDay = it)) }
            SettingsToggleRow(Icons.Filled.Payments, "1 dia antes", settings.due1Day) { onSettingsChanged(settings.copy(due1Day = it)) }
            SettingsToggleRow(Icons.Filled.Payments, "2 dias antes", settings.due2Days) { onSettingsChanged(settings.copy(due2Days = it)) }
            SettingsToggleRow(Icons.Filled.Payments, "3 dias antes", settings.due3Days) { onSettingsChanged(settings.copy(due3Days = it)) }
            SettingsToggleRow(Icons.Filled.Payments, "5 dias antes", settings.due5Days) { onSettingsChanged(settings.copy(due5Days = it)) }
            SettingsToggleRow(Icons.Filled.Payments, "7 dias antes", settings.due7Days) { onSettingsChanged(settings.copy(due7Days = it)) }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Accent)
                Text("Dias antes para corte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            SettingsToggleRow(Icons.Filled.CalendarMonth, "El mismo dia", settings.cutSameDay) { onSettingsChanged(settings.copy(cutSameDay = it)) }
            SettingsToggleRow(Icons.Filled.CalendarMonth, "1 dia antes", settings.cut1Day) { onSettingsChanged(settings.copy(cut1Day = it)) }
            SettingsToggleRow(Icons.Filled.CalendarMonth, "2 dias antes", settings.cut2Days) { onSettingsChanged(settings.copy(cut2Days = it)) }
            SettingsToggleRow(Icons.Filled.CalendarMonth, "3 dias antes", settings.cut3Days) { onSettingsChanged(settings.copy(cut3Days = it)) }
            SettingsToggleRow(Icons.Filled.CalendarMonth, "5 dias antes", settings.cut5Days) { onSettingsChanged(settings.copy(cut5Days = it)) }
            SettingsToggleRow(Icons.Filled.CalendarMonth, "7 dias antes", settings.cut7Days) { onSettingsChanged(settings.copy(cut7Days = it)) }

            TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar")
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = Accent)
            Text(label)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> BankDropdown(
    label: String,
    expanded: Boolean,
    value: String,
    onExpandedChange: (Boolean) -> Unit,
    items: List<T>,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemLabel(item)) },
                    onClick = { onItemSelected(item) },
                )
            }
        }
    }
}

private fun Context.hasNotificationPermission(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

private fun Context.openExternalLink(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}

private fun Context.appVersionName(): String {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    return packageInfo.versionName ?: "1.0"
}

private tailrec fun Context.findActivity(): android.app.Activity? {
    return when (this) {
        is android.app.Activity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

private fun buildVisibleCards(
    cards: List<CreditCardItem>,
    filter: CardsFilter,
    sortByDate: Boolean,
): List<CreditCardItem> {
    val filtered = cards.filter { item ->
        when (filter) {
            CardsFilter.All -> true
            CardsFilter.Pending -> !item.isPaid && item.daysRemaining >= 0L
            CardsFilter.Overdue -> !item.isPaid && item.daysRemaining < 0L
            CardsFilter.Paid -> item.isPaid
        }
    }

    if (!sortByDate) {
        return filtered.sortedBy { it.manualOrder }
    }

    val today = LocalDate.now()

    fun group(item: CreditCardItem): Int {
        return when {
            item.isPaid -> 3
            item.dueDate.isBefore(today) -> 0
            item.daysRemaining <= 5L -> 1
            else -> 2
        }
    }

    return filtered.sortedWith(
        compareBy<CreditCardItem> { group(it) }
            .thenBy { it.dueDate }
            .thenBy { it.bankName }
            .thenBy { it.lastDigits },
    )
}

private fun nextManualOrder(cards: List<CreditCardItem>): Int {
    return (cards.maxOfOrNull { it.manualOrder } ?: -1) + 1
}

private fun normalizeCardsForToday(cards: List<CreditCardItem>, today: LocalDate = LocalDate.now()): List<CreditCardItem> {
    return cards.map { item ->
        if (item.isPaid && item.dueDate.isBefore(today)) {
            item.advanceToNextMonth(today)
        } else {
            item
        }
    }
}

private fun buildDebtChartEntries(cards: List<CreditCardItem>): List<DebtChartEntry> {
    val sorted = cards
        .mapNotNull { item ->
            val debt = item.totalDebt ?: 0.0
            if (debt <= 0.0) {
                null
            } else {
                DebtChartEntry(
                    label = "${item.bankName} - ${item.lastDigits}",
                    amount = debt,
                )
            }
        }
        .sortedByDescending { it.amount }

    if (sorted.size <= 6) return sorted

    val visible = sorted.take(5)
    val remaining = sorted.drop(5).sumOf { it.amount }
    return visible + DebtChartEntry(
        label = "Otras",
        amount = remaining,
    )
}

private fun buildDebtChartSlices(entries: List<DebtChartEntry>): List<DebtChartSlice> {
    val totalDebt = entries.sumOf { it.amount }
    if (totalDebt <= 0.0) return emptyList()

    val palette = listOf(
        Color(0xFF1A80DB),
        Color(0xFF15A57D),
        Color(0xFFF59A1B),
        Color(0xFFD9534F),
        Color(0xFF7D66E0),
        Color(0xFF48505C),
    )

    var runningAngle = -90f
    return entries.mapIndexed { index, entry ->
        val sweepAngle = ((entry.amount / totalDebt) * 360.0).toFloat()
        DebtChartSlice(
            label = entry.label,
            amount = entry.amount,
            color = palette[index % palette.size],
            startAngle = runningAngle,
            sweepAngle = sweepAngle,
        ).also {
            runningAngle += sweepAngle
        }
    }
}

private fun launchInAppReview(activity: android.app.Activity) {
    val reviewManager = ReviewManagerFactory.create(activity)
    reviewManager.requestReviewFlow().addOnCompleteListener { requestTask ->
        if (requestTask.isSuccessful) {
            val reviewInfo = requestTask.result
            reviewManager.launchReviewFlow(activity, reviewInfo)
        } else {
            val errorCode = (requestTask.exception as? ReviewException)?.errorCode
            android.util.Log.w("PayAlertReview", "No se pudo abrir in-app review. Codigo: $errorCode")
        }
    }
}

private fun moveManualCard(
    cards: List<CreditCardItem>,
    cardId: String,
    direction: Int,
): List<CreditCardItem> {
    val sorted = cards.sortedBy { it.manualOrder }.toMutableList()
    val currentIndex = sorted.indexOfFirst { it.id == cardId }
    if (currentIndex == -1) return cards

    val targetIndex = currentIndex + direction
    if (targetIndex !in sorted.indices) return cards

    val current = sorted[currentIndex]
    sorted[currentIndex] = sorted[targetIndex]
    sorted[targetIndex] = current

    val reassigned = sorted.mapIndexed { index, item ->
        item.copy(manualOrder = index)
    }

    val byId = reassigned.associateBy { it.id }
    return cards.map { original -> byId[original.id] ?: original }
}
