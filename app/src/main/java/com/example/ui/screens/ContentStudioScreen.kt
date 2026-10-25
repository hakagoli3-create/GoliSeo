package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.TableChart
import com.example.data.model.ArticleTopicSuggestion
import com.example.data.model.BrandKnowledgeBase
import com.example.data.model.CompetitorAnalysisReport
import com.example.data.model.ContentModuleProcessor
import com.example.data.model.ElementorPageDesign
import com.example.data.model.FreeformCommandPlan
import com.example.data.model.GeneratedArticle
import com.example.data.model.ImageMetadata
import com.example.data.model.PersianNormalizer
import com.example.data.model.ProductOptimizationPackage
import com.example.data.model.SiteItem
import com.example.data.model.SiteItemType
import com.example.data.model.TaskType
import com.example.data.model.ValidationCheckResult
import com.example.data.remote.SeoAiResult
import com.example.ui.components.ArticleStudioDialog
import com.example.ui.components.BrandKnowledgeBaseDialog
import com.example.ui.components.CompetitorAnalysisDialog
import com.example.ui.components.ElementorStudioDialog
import com.example.ui.components.FreeformCommandDialog
import com.example.ui.components.JalaliSchedulerDialog
import com.example.ui.components.ProductStudioDialog
import com.example.ui.components.ValidationDialog
import com.example.ui.theme.GliseoError
import com.example.ui.theme.GliseoHardLock
import com.example.ui.theme.GliseoPrimary
import com.example.ui.theme.GliseoSecondary
import com.example.ui.theme.GliseoSuccess
import com.example.ui.theme.GliseoTertiary

@Composable
fun ContentStudioScreen(
    items: List<SiteItem>,
    selectedItem: SiteItem?,
    aiOptimizing: Boolean,
    aiResult: SeoAiResult?,
    dryRunPreviewItem: SiteItem?,
    brandKnowledgeBase: BrandKnowledgeBase,
    freeformPlan: FreeformCommandPlan?,
    validationResult: ValidationCheckResult?,
    productPackage: ProductOptimizationPackage?,
    topicSuggestions: List<ArticleTopicSuggestion>,
    articlePreview: GeneratedArticle?,
    onSelectItem: (SiteItem?) -> Unit,
    onGenerateAiSeo: () -> Unit,
    onRunDryRun: (Map<String, Any?>) -> Unit,
    onDismissDryRun: () -> Unit,
    onCommitUpdate: (Map<String, Any?>, String) -> Unit,
    onTestHardLock: () -> Unit,
    onEnqueueTask: (Long, TaskType) -> Unit,
    onExecuteFreeform: (String, Boolean) -> Unit,
    onDismissFreeform: () -> Unit,
    onPrepareProductPackage: (Long) -> Unit,
    onApplyProductPackage: (Long) -> Unit,
    onDismissProductPackage: () -> Unit,
    onBatchOptimizeProducts: () -> Unit,
    onSelectArticleTopic: (ArticleTopicSuggestion) -> Unit,
    onPublishArticle: (GeneratedArticle) -> Unit,
    onDismissArticlePreview: () -> Unit,
    onScheduleFuture: (Long, Int, String?) -> Unit,
    onValidatePrePublish: (SiteItem) -> Unit,
    onDismissValidation: () -> Unit,
    onLearnUserEdit: (String, String, String) -> Unit,
    onSyncSite: () -> Unit = {},
    isSyncing: Boolean = false,
    syncStatusMessage: String = "",
    onGenerateCustomArticle: (String, String, String?) -> Unit = { _, _, _ -> },
    onAddNewContent: (String, String, SiteItemType, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    onScheduleArticle: (GeneratedArticle, Int, String?) -> Unit = { _, _, _ -> },
    onApplyElementorDesign: (SiteItem, ElementorPageDesign) -> Unit = { _, _ -> },
    onCreateElementorPage: (ElementorPageDesign) -> Unit = {},
    onAutoOptimizeFullItem: (SiteItem) -> Unit = {},
    onBatchOptimizeTab: (SiteItemType?) -> Unit = {},
    competitorReport: CompetitorAnalysisReport? = null,
    analyzingCompetitors: Boolean = false,
    onAnalyzeCompetitors: (SiteItem) -> Unit = {},
    onDismissCompetitorReport: () -> Unit = {},
    onRewriteContentAdvanced: (SiteItem) -> Unit = {},
    onApplyWinningStrategy: (SiteItem, CompetitorAnalysisReport) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {

    var showFreeformDialog by remember { mutableStateOf(false) }
    var showArticleStudioDialog by remember { mutableStateOf(false) }
    var showBrandKnowledgeDialog by remember { mutableStateOf(false) }
    var showSchedulerDialogForItem by remember { mutableStateOf<SiteItem?>(null) }
    var showImageStudioForItem by remember { mutableStateOf<SiteItem?>(null) }
    var showCreateContentDialog by remember { mutableStateOf(false) }
    var showElementorStudioForItem by remember { mutableStateOf<SiteItem?>(null) }
    var showNewElementorPageDialog by remember { mutableStateOf(false) }

    if (selectedItem != null) {
        ItemEditorView(
            item = selectedItem,
            aiOptimizing = aiOptimizing,
            aiResult = aiResult,
            dryRunPreview = dryRunPreviewItem,
            brandKnowledgeBase = brandKnowledgeBase,
            onClose = { onSelectItem(null) },
            onGenerateAiSeo = onGenerateAiSeo,
            onAutoOptimizeFull = { onAutoOptimizeFullItem(selectedItem) },
            onAnalyzeCompetitors = { onAnalyzeCompetitors(selectedItem) },
            onRewriteContentAdvanced = { onRewriteContentAdvanced(selectedItem) },
            onRunDryRun = onRunDryRun,
            onDismissDryRun = onDismissDryRun,
            onCommit = onCommitUpdate,
            onTestHardLock = onTestHardLock,
            onEnqueue = { taskType -> onEnqueueTask(selectedItem.id, taskType) },
            onOpenProductStudio = { onPrepareProductPackage(selectedItem.id) },
            onOpenElementorStudio = { showElementorStudioForItem = selectedItem },
            onOpenScheduler = { showSchedulerDialogForItem = selectedItem },
            onOpenImageStudio = { showImageStudioForItem = selectedItem },
            onValidate = { onValidatePrePublish(selectedItem) },
            onLearnEdit = onLearnUserEdit,
            modifier = modifier
        )
    } else {
        ItemListView(
            items = items,
            onSelectItem = onSelectItem,
            onAutoOptimizeItem = onAutoOptimizeFullItem,
            onAnalyzeCompetitors = onAnalyzeCompetitors,
            onOpenFreeform = { showFreeformDialog = true },
            onOpenArticleStudio = { showArticleStudioDialog = true },
            onOpenBrandKnowledge = { showBrandKnowledgeDialog = true },
            onOpenNewElementorPage = { showNewElementorPageDialog = true },
            onBatchOptimizeAllProducts = onBatchOptimizeProducts,
            onBatchOptimizeTab = onBatchOptimizeTab,
            onOpenCreateContent = { showCreateContentDialog = true },
            onSyncSite = onSyncSite,
            isSyncing = isSyncing,
            syncStatusMessage = syncStatusMessage,
            modifier = modifier
        )
    }


    // Modal Dialogs
    if (showFreeformDialog || freeformPlan != null) {
        FreeformCommandDialog(
            plan = freeformPlan,
            onExecute = onExecuteFreeform,
            onDismiss = {
                showFreeformDialog = false
                onDismissFreeform()
            }
        )
    }

    if (showArticleStudioDialog || articlePreview != null) {
        ArticleStudioDialog(
            topics = topicSuggestions,
            previewArticle = articlePreview,
            onSelectTopicToGenerate = onSelectArticleTopic,
            onGenerateCustomTopic = onGenerateCustomArticle,
            onPublishArticle = onPublishArticle,
            onScheduleArticle = onScheduleArticle,
            onDismiss = {
                showArticleStudioDialog = false
                onDismissArticlePreview()
            }
        )
    }

    if (showCreateContentDialog) {
        CreateContentDialog(
            onCreate = { title, content, type, keyword, excerpt, price ->
                onAddNewContent(title, content, type, keyword, excerpt, price)
                showCreateContentDialog = false
            },
            onDismiss = { showCreateContentDialog = false }
        )
    }

    if (showBrandKnowledgeDialog) {
        BrandKnowledgeBaseDialog(
            knowledgeBase = brandKnowledgeBase,
            onDismiss = { showBrandKnowledgeDialog = false }
        )
    }

    if (showElementorStudioForItem != null) {
        val target = showElementorStudioForItem!!
        ElementorStudioDialog(
            item = target,
            brandKnowledge = brandKnowledgeBase,
            onApplyToItem = { itm, design ->
                onApplyElementorDesign(itm, design)
                showElementorStudioForItem = null
            },
            onCreateNewPage = { design ->
                onCreateElementorPage(design)
                showElementorStudioForItem = null
            },
            onDismiss = { showElementorStudioForItem = null }
        )
    }

    if (showNewElementorPageDialog) {
        ElementorStudioDialog(
            item = null,
            brandKnowledge = brandKnowledgeBase,
            onApplyToItem = { _, _ -> },
            onCreateNewPage = { design ->
                onCreateElementorPage(design)
                showNewElementorPageDialog = false
            },
            onDismiss = { showNewElementorPageDialog = false }
        )
    }

    if (productPackage != null && selectedItem != null) {
        ProductStudioDialog(
            pkg = productPackage,
            onApplySingle = { onApplyProductPackage(selectedItem.id) },
            onBatchAllProducts = onBatchOptimizeProducts,
            onDismiss = onDismissProductPackage
        )
    }

    if (validationResult != null && selectedItem != null) {
        ValidationDialog(
            result = validationResult,
            targetTitle = selectedItem.title,
            onDismiss = onDismissValidation
        )
    }

    if (showSchedulerDialogForItem != null) {
        val target = showSchedulerDialogForItem!!
        JalaliSchedulerDialog(
            targetTitle = target.title,
            onSchedule = { days, occasion ->
                onScheduleFuture(target.id, days, occasion)
                showSchedulerDialogForItem = null
            },
            onDismiss = { showSchedulerDialogForItem = null }
        )
    }

    if (showImageStudioForItem != null) {
        val target = showImageStudioForItem!!
        val meta = ContentModuleProcessor.generateImageMetadata(target)
        ImageStudioDialog(
            item = target,
            imageMeta = meta,
            onDismiss = { showImageStudioForItem = null }
        )
    }

    if (competitorReport != null && selectedItem != null) {
        CompetitorAnalysisDialog(
            report = competitorReport,
            onApplyWinningStrategy = {
                onApplyWinningStrategy(selectedItem, competitorReport)
            },
            onDismiss = onDismissCompetitorReport
        )
    }
}

@Composable
fun ItemListView(
    items: List<SiteItem>,
    onSelectItem: (SiteItem) -> Unit,
    onAutoOptimizeItem: (SiteItem) -> Unit,
    onAnalyzeCompetitors: (SiteItem) -> Unit = {},
    onOpenFreeform: () -> Unit,
    onOpenArticleStudio: () -> Unit,
    onOpenBrandKnowledge: () -> Unit,
    onOpenNewElementorPage: () -> Unit,
    onBatchOptimizeAllProducts: () -> Unit,
    onBatchOptimizeTab: (SiteItemType?) -> Unit,
    onOpenCreateContent: () -> Unit = {},
    onSyncSite: () -> Unit = {},
    isSyncing: Boolean = false,
    syncStatusMessage: String = "",
    modifier: Modifier = Modifier
) {

    // 0: All, 1: Products, 2: Articles, 3: Pages
    var selectedCategoryTab by remember { mutableIntStateOf(1) } // Default to Products
    var selectedSubFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val products = items.filter { it.isProduct }
    val posts = items.filter { it.isPost }
    val pages = items.filter { it.isPage }

    val categoryFiltered = when (selectedCategoryTab) {
        1 -> products
        2 -> posts
        3 -> pages
        else -> items
    }

    val subFiltered = categoryFiltered.filter { item ->
        when (selectedSubFilter) {
            "PUBLISHED" -> item.status == "publish"
            "DRAFT" -> item.status == "draft"
            "LOW_SEO" -> item.seoScore < 75
            "HIGH_SEO" -> item.seoScore >= 85
            "ELEMENTOR" -> item.isElementor
            "IN_STOCK" -> item.stockStatus == "instock"
            else -> true
        }
    }

    val finalItems = subFiltered.filter { item ->
        searchQuery.isBlank() ||
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.focusKeyword.contains(searchQuery, ignoreCase = true) ||
            item.slug.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "استودیو جامع مدیریت محتوا و سئو",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "همگام‌سازی کامل تمام محصولات، مقالات و صفحات با هوش مصنوعی و المنتور",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onSyncSite) {
                Icon(
                    imageVector = if (isSyncing) Icons.Default.Refresh else Icons.Default.Sync,
                    contentDescription = "همگام‌سازی",
                    tint = GliseoPrimary
                )
            }
        }

        // Live Syncing Progress Banner
        AnimatedVisibility(visible = isSyncing) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GliseoPrimary.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = GliseoPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = syncStatusMessage.ifBlank { "در حال استخراج و صفحه‌بندی کامل داده‌های سایت..." },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GliseoPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = GliseoPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Tab Bar
        TabRow(
            selectedTabIndex = selectedCategoryTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GliseoPrimary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedCategoryTab == 1,
                onClick = { selectedCategoryTab = 1; selectedSubFilter = "ALL" },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("محصولات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        CategoryBadge(count = products.size, color = GliseoSecondary)
                    }
                },
                icon = { Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedCategoryTab == 2,
                onClick = { selectedCategoryTab = 2; selectedSubFilter = "ALL" },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("مقالات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        CategoryBadge(count = posts.size, color = GliseoPrimary)
                    }
                },
                icon = { Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedCategoryTab == 3,
                onClick = { selectedCategoryTab = 3; selectedSubFilter = "ALL" },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("برگه‌ها", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        CategoryBadge(count = pages.size, color = GliseoSuccess)
                    }
                },
                icon = { Icon(Icons.Default.ViewQuilt, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedCategoryTab == 0,
                onClick = { selectedCategoryTab = 0; selectedSubFilter = "ALL" },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("همه", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        CategoryBadge(count = items.size, color = GliseoTertiary)
                    }
                },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab-Specific Action Bar
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when (selectedCategoryTab) {
                1 -> {
                    // Products Tab Actions
                    item {
                        ModuleQuickButton(
                            title = "⚡ سئو خودکار تمام محصولات",
                            icon = Icons.Default.AutoAwesome,
                            color = GliseoPrimary,
                            onClick = onBatchOptimizeAllProducts
                        )
                    }
                    item {
                        ModuleQuickButton(
                            title = "+ افزودن محصول جدید",
                            icon = Icons.Default.Add,
                            color = GliseoSecondary,
                            onClick = onOpenCreateContent
                        )
                    }
                    item {
                        ModuleQuickButton(
                            title = "همگام‌سازی کامل سایت",
                            icon = Icons.Default.Sync,
                            color = GliseoTertiary,
                            onClick = onSyncSite
                        )
                    }
                }
                2 -> {
                    // Articles Tab Actions
                    item {
                        ModuleQuickButton(
                            title = "✍️ نگارش مقاله E-E-A-T با جمینای",
                            icon = Icons.Default.AutoAwesome,
                            color = GliseoSecondary,
                            onClick = onOpenArticleStudio
                        )
                    }
                    item {
                        ModuleQuickButton(
                            title = "+ پست جدید",
                            icon = Icons.Default.Add,
                            color = GliseoPrimary,
                            onClick = onOpenCreateContent
                        )
                    }
                    item {
                        ModuleQuickButton(
                            title = "⚡ سئو خودکار همه مقالات",
                            icon = Icons.AutoMirrored.Filled.Article,
                            color = GliseoSuccess,
                            onClick = { onBatchOptimizeTab(SiteItemType.POST) }
                        )
                    }
                }
                3 -> {
                    // Pages & Elementor Tab Actions
                    item {
                        ModuleQuickButton(
                            title = "🎨 طراحی برگه جدید با المنتور",
                            icon = Icons.Default.ViewQuilt,
                            color = GliseoSuccess,
                            onClick = onOpenNewElementorPage
                        )
                    }
                    item {
                        ModuleQuickButton(
                            title = "+ ایجاد برگه جدید",
                            icon = Icons.Default.Add,
                            color = GliseoPrimary,
                            onClick = onOpenCreateContent
                        )
                    }
                    item {
                        ModuleQuickButton(
                            title = "⚡ سئو خودکار تمام برگه‌ها",
                            icon = Icons.Default.AutoAwesome,
                            color = GliseoSecondary,
                            onClick = { onBatchOptimizeTab(SiteItemType.PAGE) }
                        )
                    }
                }
                else -> {
                    item {
                        ModuleQuickButton(
                            title = "دستور آزاد هوشمند",
                            icon = Icons.Default.AutoAwesome,
                            color = GliseoPrimary,
                            onClick = onOpenFreeform
                        )
                    }
                    item {
                        ModuleQuickButton(
                            title = "حافظه و برندینگ",
                            icon = Icons.Default.Psychology,
                            color = GliseoTertiary,
                            onClick = onOpenBrandKnowledge
                        )
                    }
                    item {
                        ModuleQuickButton(
                            title = "همگام‌سازی سایت",
                            icon = Icons.Default.Sync,
                            color = GliseoSecondary,
                            onClick = onSyncSite
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar & Sub-filters
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("جستجو در عنوان، کلیدواژه، اسلاگ...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sub Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = selectedSubFilter == "ALL",
                    onClick = { selectedSubFilter = "ALL" },
                    label = { Text("همه (${categoryFiltered.size})", fontSize = 11.sp) }
                )
            }
            item {
                FilterChip(
                    selected = selectedSubFilter == "LOW_SEO",
                    onClick = { selectedSubFilter = "LOW_SEO" },
                    label = { Text("نیازمند سئو (<۷۵)", fontSize = 11.sp) }
                )
            }
            item {
                FilterChip(
                    selected = selectedSubFilter == "HIGH_SEO",
                    onClick = { selectedSubFilter = "HIGH_SEO" },
                    label = { Text("سئو عالی (>۸۵)", fontSize = 11.sp) }
                )
            }
            if (selectedCategoryTab == 1) {
                item {
                    FilterChip(
                        selected = selectedSubFilter == "IN_STOCK",
                        onClick = { selectedSubFilter = "IN_STOCK" },
                        label = { Text("موجود در انبار", fontSize = 11.sp) }
                    )
                }
            }
            if (selectedCategoryTab == 3 || selectedCategoryTab == 0) {
                item {
                    FilterChip(
                        selected = selectedSubFilter == "ELEMENTOR",
                        onClick = { selectedSubFilter = "ELEMENTOR" },
                        label = { Text("طراحی المنتور 🎨", fontSize = 11.sp) }
                    )
                }
            }
            item {
                FilterChip(
                    selected = selectedSubFilter == "DRAFT",
                    onClick = { selectedSubFilter = "DRAFT" },
                    label = { Text("پیش‌نویس‌ها", fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Items List
        if (finalItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "هیچ آیتمی در این دسته‌بندی یافت نشد.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onSyncSite,
                        colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("همگام‌سازی کامل با سایت")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(finalItems) { item ->
                    SiteItemCard(
                        item = item,
                        onClick = { onSelectItem(item) },
                        onQuickAutoOptimize = { onAutoOptimizeItem(item) },
                        onQuickAnalyzeCompetitors = { onAnalyzeCompetitors(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(count: Int, color: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 1.dp)
    ) {
        Text(text = count.toString(), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SiteItemCard(
    item: SiteItem,
    onClick: () -> Unit,
    onQuickAutoOptimize: () -> Unit,
    onQuickAnalyzeCompetitors: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    val (typeColor, typeBg) = when (item.type) {
                        SiteItemType.PRODUCT -> GliseoSecondary to GliseoSecondary.copy(alpha = 0.12f)
                        SiteItemType.POST -> GliseoPrimary to GliseoPrimary.copy(alpha = 0.12f)
                        SiteItemType.PAGE -> GliseoSuccess to GliseoSuccess.copy(alpha = 0.12f)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(typeBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.type.persianLabel,
                            color = typeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (item.isElementor) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF9333EA).copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🎨 طراحی المنتور",
                                color = Color(0xFF9333EA),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (item.focusKeyword.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GliseoTertiary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⭐ Rank Math",
                                color = GliseoTertiary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // SEO Score Indicator
                val scoreColor = if (item.seoScore >= 80) GliseoSuccess else if (item.seoScore >= 65) GliseoTertiary else GliseoError
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "سئو: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${item.seoScore}٪", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = scoreColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.excerpt.ifBlank { item.content.take(90) + "..." },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom details and quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isProduct) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GliseoHardLock, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = item.regularPrice.ifBlank { "قیمت ثبت نشده" }, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        if (item.salePrice.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "حراج: ${item.salePrice}", fontSize = 10.sp, color = GliseoSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = "کلیدواژه: ${item.focusKeyword.ifEmpty { "تعیین نشده" }}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GliseoSecondary.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onQuickAnalyzeCompetitors() }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Insights, contentDescription = null, tint = GliseoSecondary, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("تحلیل رقبا", fontSize = 9.sp, color = GliseoSecondary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = GliseoPrimary.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onQuickAutoOptimize() }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = GliseoPrimary, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("سئو هوشمند", fontSize = 9.sp, color = GliseoPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "جزئیات ❯",
                        color = GliseoPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun ItemEditorView(
    item: SiteItem,
    aiOptimizing: Boolean,
    aiResult: SeoAiResult?,
    dryRunPreview: SiteItem?,
    brandKnowledgeBase: BrandKnowledgeBase,
    onClose: () -> Unit,
    onGenerateAiSeo: () -> Unit,
    onAutoOptimizeFull: () -> Unit,
    onAnalyzeCompetitors: () -> Unit = {},
    onRewriteContentAdvanced: () -> Unit = {},
    onRunDryRun: (Map<String, Any?>) -> Unit,
    onDismissDryRun: () -> Unit,
    onCommit: (Map<String, Any?>, String) -> Unit,
    onTestHardLock: () -> Unit,
    onEnqueue: (TaskType) -> Unit,
    onOpenProductStudio: () -> Unit,
    onOpenElementorStudio: () -> Unit,
    onOpenScheduler: () -> Unit,
    onOpenImageStudio: () -> Unit,
    onValidate: () -> Unit,
    onLearnEdit: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {

    var editorTab by remember { mutableIntStateOf(0) } // 0: Text & Content, 1: Rank Math & SEO, 2: Media, 3: Schema, 4: Elementor

    var title by remember(item) { mutableStateOf(item.title) }
    var content by remember(item) { mutableStateOf(item.content) }
    var excerpt by remember(item) { mutableStateOf(item.excerpt) }
    var metaTitle by remember(item) { mutableStateOf(item.metaTitle) }
    var metaDescription by remember(item) { mutableStateOf(item.metaDescription) }
    var focusKeyword by remember(item) { mutableStateOf(item.focusKeyword) }
    var canonicalUrl by remember(item) { mutableStateOf(item.canonicalUrl) }
    var metaRobots by remember(item) { mutableStateOf(item.metaRobots) }
    var schemaJson by remember(item) { mutableStateOf(item.schemaJson) }
    var imageAlt by remember(item) { mutableStateOf(item.imageAlt) }
    var featuredImageUrl by remember(item) { mutableStateOf(item.featuredImageUrl) }
    var elementorDataJson by remember(item) { mutableStateOf(item.elementorDataJson) }
    var isElementor by remember(item) { mutableStateOf(item.isElementor) }
    var elementorTemplate by remember(item) { mutableStateOf(item.elementorTemplate) }

    fun normalizeAllFields() {
        title = PersianNormalizer.normalize(title)
        content = PersianNormalizer.normalize(content)
        excerpt = PersianNormalizer.normalize(excerpt)
        metaTitle = PersianNormalizer.normalize(metaTitle)
        metaDescription = PersianNormalizer.normalize(metaDescription)
        focusKeyword = PersianNormalizer.normalize(focusKeyword)
        imageAlt = PersianNormalizer.normalize(imageAlt)
    }

    fun applyAiResultFields(res: SeoAiResult) {
        if (res.suggestedTitle.isNotBlank()) title = PersianNormalizer.normalize(res.suggestedTitle)
        if (res.suggestedShortDescription.isNotBlank()) excerpt = PersianNormalizer.normalize(res.suggestedShortDescription)
        if (res.suggestedStructuredContent.isNotBlank()) content = res.suggestedStructuredContent
        metaTitle = PersianNormalizer.normalize(res.suggestedTitle)
        metaDescription = PersianNormalizer.normalize(res.suggestedMetaDescription)
        focusKeyword = PersianNormalizer.normalize(res.suggestedFocusKeyword)
        schemaJson = res.suggestedSchemaJson
        imageAlt = PersianNormalizer.normalize(res.suggestedImageAlt)
    }

    fun buildCurrentChanges(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "content" to content,
            "excerpt" to excerpt,
            "metaTitle" to metaTitle,
            "metaDescription" to metaDescription,
            "focusKeyword" to focusKeyword,
            "canonicalUrl" to canonicalUrl,
            "metaRobots" to metaRobots,
            "schemaJson" to schemaJson,
            "imageAlt" to imageAlt,
            "featuredImageUrl" to featuredImageUrl,
            "elementorDataJson" to elementorDataJson,
            "isElementor" to isElementor,
            "elementorTemplate" to elementorTemplate
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ویرایش و بهینه‌سازی پیشرفته محتوا",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${item.type.persianLabel} • شناسه: ${item.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GliseoPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "${item.seoScore}٪ سئو", color = GliseoPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Hard Lock Notice
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GliseoHardLock.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GliseoHardLock, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "قفل سخت امنیتی گلیسئو (فیلدهای محافظت‌شده):",
                            color = GliseoHardLock,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "پیوند یکتا (اسلاگ): ${item.slug} | دسته‌ها: ${item.categories.joinToString("، ")}${if (item.isProduct) " | قیمت: ${item.regularPrice}" else ""}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // AI 1-Tap Full Auto-Optimization Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GliseoPrimary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = GliseoPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "سئو خودکار هوشمند با خلبان جمینای (Gemini AI)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GliseoPrimary)
                                Text(text = "بهینه‌سازی عنوان، متادیسکریپشن، اسکیما و Alt تصویر با الگوهای زنده گوگل", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (aiOptimizing) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = GliseoPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "در حال تحلیل و بهینه‌سازی دقیق با جمینای...", fontSize = 11.sp, color = GliseoPrimary)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onGenerateAiSeo,
                                colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تولید پیشنهادات سئو", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = onAutoOptimizeFull,
                                colors = ButtonDefaults.buttonColors(containerColor = GliseoSuccess),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اعمال ۱-کلیک سئو کامل", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    if (aiResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "پیشنهادات استخراج‌شده توسط جمینای:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GliseoSuccess)
                                    Button(
                                        onClick = { applyAiResultFields(aiResult) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GliseoSuccess),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("اعمال همه در فرم", fontSize = 10.sp)
                                    }
                                }
                                if (aiResult.isOfficialAgencyBrand) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(GliseoSuccess.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "✦ گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد.",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GliseoSuccess
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                if (aiResult.detectedBrand.isNotBlank()) {
                                    Text(text = "• برند سازنده: ${aiResult.detectedBrand} (کشور: ${aiResult.detectedCountry})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(text = "• عنوان سئو (H1): ${aiResult.suggestedTitle}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "• کلمه کلیدی: ${aiResult.suggestedFocusKeyword}", fontSize = 10.sp, color = GliseoPrimary)
                                Text(text = "• توضیحات متا: ${aiResult.suggestedMetaDescription}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (aiResult.suggestedShortDescription.isNotBlank()) {
                                    Text(text = "• متن کوتاه جذاب: ${aiResult.suggestedShortDescription.take(90)}...", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(text = "امتیاز جدید: +${aiResult.seoScoreImprovement}٪ (${aiResult.groundingSummary})", fontSize = 10.sp, color = GliseoSuccess, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Sub-tabs for granular inspection and editing
        item {
            ScrollableTabRow(
                selectedTabIndex = editorTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = GliseoPrimary,
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = editorTab == 0,
                    onClick = { editorTab = 0 },
                    text = { Text("متن و محتوا", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = editorTab == 1,
                    onClick = { editorTab = 1 },
                    text = { Text("سئو Rank Math", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = editorTab == 2,
                    onClick = { editorTab = 2 },
                    text = { Text("تصویر و رسانه", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = editorTab == 3,
                    onClick = { editorTab = 3 },
                    text = { Text("اسکیما JSON-LD", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                if (item.isPage) {
                    Tab(
                        selected = editorTab == 4,
                        onClick = { editorTab = 4 },
                        text = { Text("المنتور 🎨", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        // Tab Content
        when (editorTab) {
            0 -> {
                // Text & Content Tab
                item {
                    val hasH2orH3 = content.contains("<h2") || content.contains("<h3")
                    val hasTable = content.contains("<table")
                    val hasBullets = content.contains("<ul") || content.contains("<li") || content.contains("•") || content.contains("✔") || content.contains("✦")
                    val hasInternalLinks = content.contains("gliseo-internal-links") || content.contains("محصولات مرتبط") || content.contains("پیشنهاد خرید") || content.contains("پیشنهاد مطالعه") || content.contains("<a href")
                    val isWallOfText = content.length > 350 && !hasH2orH3 && !hasTable

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "مشخصات محتوای متنی و سئو", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            OutlinedButton(
                                onClick = { normalizeAllFields() },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Spellcheck, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ویراستار و نیم‌فاصله فارسی", fontSize = 10.sp)
                            }
                        }

                        // Advanced Content Optimization & SEO Structure Banner
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GliseoSecondary.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = GliseoSecondary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ارتقای پیشرفته ساختار محتوا (ضد دیوار متنی)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GliseoSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "شکستن متن‌های طولانی به سرفصل‌های منطقی H2/H3، جدول مشخصات، بولت‌پوینت‌های جذاب و لینک‌های داخلی در انتهای متن",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Real-time Structural Health Badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val badgeH2Color = if (hasH2orH3) GliseoSuccess else GliseoError
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(badgeH2Color.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = if (hasH2orH3) "✓ سرفصل H2/H3" else "✗ فاقد H2/H3", fontSize = 9.sp, color = badgeH2Color, fontWeight = FontWeight.Bold)
                                    }

                                    val badgeTableColor = if (hasTable) GliseoSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(badgeTableColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = if (hasTable) "✓ جدول مقایسه/مشخصات" else "○ بدون جدول", fontSize = 9.sp, color = badgeTableColor, fontWeight = FontWeight.Bold)
                                    }

                                    val badgeBulletsColor = if (hasBullets) GliseoSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(badgeBulletsColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = if (hasBullets) "✓ لیست نقطه‌ای" else "○ بدون بولت", fontSize = 9.sp, color = badgeBulletsColor, fontWeight = FontWeight.Bold)
                                    }

                                    val badgeLinksColor = if (hasInternalLinks) GliseoSuccess else GliseoError
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(badgeLinksColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = if (hasInternalLinks) "✓ لینک داخلی پایان متن" else "✗ بدون لینک داخلی", fontSize = 9.sp, color = badgeLinksColor, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (isWallOfText) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "⚠️ متن فعلی حالت پیوسته (دیوار متنی) دارد و تجربه کاربری را کاهش می‌دهد. روی دکمه بازنویسی پیشرفته زیر کلیک کنید.",
                                        fontSize = 10.sp,
                                        color = GliseoError,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val pkg = ContentModuleProcessor.generateProductPackage(
                                                item.copy(title = title, content = content, excerpt = excerpt, focusKeyword = focusKeyword),
                                                brandKnowledgeBase
                                            )
                                            title = pkg.seoTitle
                                            excerpt = pkg.shortDescription
                                            content = pkg.structuredHtmlContent
                                            metaTitle = pkg.seoTitle
                                            metaDescription = pkg.rankMathMetaDesc
                                            focusKeyword = pkg.focusKeyword
                                            onRewriteContentAdvanced()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GliseoSecondary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("بازنویسی پیشرفته سئو", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { onAnalyzeCompetitors() },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(14.dp), tint = GliseoPrimary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تحلیل رقبا در گوگل", fontSize = 10.sp, color = GliseoPrimary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val specs = ContentModuleProcessor.generateProductPackage(item.copy(title = title, focusKeyword = focusKeyword), brandKnowledgeBase).specificationsTable
                                            val tableHtml = buildString {
                                                append("\n\n<!-- جدول مشخصات و مقایسه کالا -->\n")
                                                append("<div class=\"wp-block-table is-style-stripes\" style=\"margin:16px 0; overflow-x:auto;\">\n")
                                                append("<table style=\"width:100%; border-collapse:collapse; font-size:13px;\">\n")
                                                append("<thead><tr style=\"background:#6366F1; color:#fff;\"><th style=\"padding:8px 12px; text-align:right;\">مشخصه کالا</th><th style=\"padding:8px 12px; text-align:right;\">توضیحات و مقادیر</th></tr></thead>\n<tbody>\n")
                                                specs.forEach { (k, v) ->
                                                    append("<tr><td style=\"padding:8px 12px; font-weight:bold; border-bottom:1px solid #E2E8F0; width:35%;\">$k</td><td style=\"padding:8px 12px; border-bottom:1px solid #E2E8F0;\">$v</td></tr>\n")
                                                }
                                                append("</tbody>\n</table>\n</div>\n")
                                            }
                                            content = content + tableHtml
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ درج جدول مشخصات", fontSize = 10.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val linksHtml = ContentModuleProcessor.buildRealGolikalaInternalLinksHtml(title)
                                            content = content + linksHtml
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ لینک داخلی انتهای متن", fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("عنوان اصلی (تنها تگ H1 صفحه)") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = excerpt,
                            onValueChange = { excerpt = it },
                            label = { Text("چکیده / توضیح کوتاه (Short Description)") },
                            shape = RoundedCornerShape(10.dp),
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("توضیحات کامل و ساختاریافته (Content - شامل جدول و H2/H3)") },
                            shape = RoundedCornerShape(10.dp),
                            minLines = 8,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "طول متن: ${content.length} کاراکتر • کلمات: ${content.split("\\s+".toRegex()).size}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            1 -> {
                // Rank Math & SEO Tab
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "پیکربندی افزونه‌های سئو (Rank Math و Yoast)", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        // Google SERP live preview snippet
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "پیش‌نمایش زنده در نتایج گوگل (Snippet Preview):", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = metaTitle.ifBlank { title }, fontSize = 13.sp, color = GliseoPrimary, fontWeight = FontWeight.Bold)
                                Text(text = "https://your-site.com/${item.slug}", fontSize = 10.sp, color = GliseoSuccess)
                                Text(text = metaDescription.ifBlank { excerpt.ifBlank { "توضیحات پیش‌فرض متا در گوگل..." } }, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        OutlinedTextField(
                            value = focusKeyword,
                            onValueChange = { focusKeyword = it },
                            label = { Text("کلمه کلیدی کانونی (Focus Keyword)") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Column {
                            OutlinedTextField(
                                value = metaTitle,
                                onValueChange = { metaTitle = it },
                                label = { Text("عنوان سئو (Rank Math Meta Title)") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp, start = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "طول مناسب: زیر ۶۰ کاراکتر", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${metaTitle.length}/60",
                                    fontSize = 10.sp,
                                    color = if (metaTitle.length > 60) GliseoError else GliseoSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column {
                            OutlinedTextField(
                                value = metaDescription,
                                onValueChange = { metaDescription = it },
                                label = { Text("توضیحات متا سئو (Meta Description)") },
                                shape = RoundedCornerShape(10.dp),
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp, start = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "طول استاندارد: بین ۱۲۰ تا ۱۶۰ کاراکتر", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${metaDescription.length}/160",
                                    fontSize = 10.sp,
                                    color = if (metaDescription.length in 120..160) GliseoSuccess else GliseoTertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedTextField(
                            value = canonicalUrl,
                            onValueChange = { canonicalUrl = it },
                            label = { Text("پیوند کانونیکال (Canonical URL)") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = metaRobots,
                            onValueChange = { metaRobots = it },
                            label = { Text("دستورات متا ربات‌ها (Robots Meta)") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            2 -> {
                // Media & Image Tab
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "مشخصات تصویر شاخص و متادیتای تصویری سئو", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = featuredImageUrl,
                            onValueChange = { featuredImageUrl = it },
                            label = { Text("نشانی تصویر شاخص (Featured Image URL)") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = imageAlt,
                            onValueChange = { imageAlt = it },
                            label = { Text("متن جایگزین تصویر (Alt Text)") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = onOpenImageStudio,
                            colors = ButtonDefaults.buttonColors(containerColor = GliseoTertiary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استودیو پیشرفته تصویر و تبدیل فرمت WebP", fontSize = 11.sp)
                        }
                    }
                }
            }

            3 -> {
                // Schema JSON-LD Tab
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "ساختار داده‌ای اسکیما (JSON-LD Schema)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = {
                                    schemaJson = if (item.isProduct) {
                                        """{"@context":"https://schema.org","@type":"Product","name":"$title","description":"$excerpt","offers":{"@type":"Offer","price":"${item.regularPrice.filter { it.isDigit() }}","priceCurrency":"IRR","availability":"https://schema.org/InStock"}}"""
                                    } else {
                                        """{"@context":"https://schema.org","@type":"Article","headline":"$title","description":"$excerpt","mainEntityOfPage":"https://example.com/${item.slug}"}"""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("تولید خودکار اسکیما", fontSize = 10.sp)
                            }
                        }

                        OutlinedTextField(
                            value = schemaJson,
                            onValueChange = { schemaJson = it },
                            label = { Text("کد JSON-LD معتبر گوگل") },
                            shape = RoundedCornerShape(10.dp),
                            minLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            4 -> {
                // Elementor Tab for Pages
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "پیکربندی افزونه المنتور و المنتور پرو", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF9333EA).copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.ViewQuilt, contentDescription = null, tint = Color(0xFF9333EA))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "طراحی و ساختار ویجت‌های المنتور", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = onOpenElementorStudio,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("بازطراحی با المنتور و هوش مصنوعی", fontSize = 10.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "این برگه قابلیت بازطراحی کامل با هوش مصنوعی و افزونه المنتور را داراست تا بدون دستکاری ساختار دیتابیس، مستقیماً لندینگ‌های خیره‌کننده تولید کنید.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedTextField(
                            value = elementorTemplate,
                            onValueChange = { elementorTemplate = it },
                            label = { Text("قالب برگه المنتور (elementor_header_footer / canvas)") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = elementorDataJson,
                            onValueChange = {
                                elementorDataJson = it
                                isElementor = it.isNotBlank()
                            },
                            label = { Text("ساختار دیتای خام المنتور (_elementor_data JSON)") },
                            shape = RoundedCornerShape(10.dp),
                            minLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Primary Save & Push Button
                Button(
                    onClick = {
                        onCommit(buildCurrentChanges(), "بهینه‌سازی کامل محتوا، سئو و المنتور")
                        onLearnEdit(item.title, title, item.type.persianLabel)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "ذخیره تغییرات و ارسال مستقیم به سایت", fontWeight = FontWeight.Bold)
                }

                // Dry Run Button
                OutlinedButton(
                    onClick = { onRunDryRun(buildCurrentChanges()) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.Preview, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "اجرای آزمایشی و شبیه‌سازی (Dry Run)", fontWeight = FontWeight.SemiBold)
                }

                // Pre-Publish Quality Audit Button
                Button(
                    onClick = onValidate,
                    colors = ButtonDefaults.buttonColors(containerColor = GliseoSecondary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "اعتبارسنجی کیفی سئو پیش از انتشار", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Test Hard Lock Button
                Button(
                    onClick = onTestHardLock,
                    colors = ButtonDefaults.buttonColors(containerColor = GliseoHardLock.copy(alpha = 0.12f), contentColor = GliseoHardLock),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "آزمایش نفوذ قفل سخت (تضمین حفاظت قیمت و اسلاگ)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ModuleQuickButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun CreateContentDialog(
    onCreate: (title: String, content: String, type: SiteItemType, focusKeyword: String, excerpt: String, regularPrice: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var excerpt by remember { mutableStateOf("") }
    var focusKeyword by remember { mutableStateOf("") }
    var regularPrice by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(SiteItemType.PRODUCT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "ایجاد محتوای جدید در سایت", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "نوع محتوا را انتخاب کنید:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedType == SiteItemType.PRODUCT,
                        onClick = { selectedType = SiteItemType.PRODUCT },
                        label = { Text("محصول ووکامرس", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedType == SiteItemType.POST,
                        onClick = { selectedType = SiteItemType.POST },
                        label = { Text("نوشته وبلاگ", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedType == SiteItemType.PAGE,
                        onClick = { selectedType = SiteItemType.PAGE },
                        label = { Text("برگه سایت", fontSize = 11.sp) }
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = focusKeyword,
                    onValueChange = { focusKeyword = it },
                    label = { Text("کلمه کلیدی کانونی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedType == SiteItemType.PRODUCT) {
                    OutlinedTextField(
                        value = regularPrice,
                        onValueChange = { regularPrice = it },
                        label = { Text("قیمت عادی (تومان)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = excerpt,
                    onValueChange = { excerpt = it },
                    label = { Text("چکیده / توضیح کوتاه") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("توضیحات و متن اصلی") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title, content, selectedType, focusKeyword, excerpt, regularPrice)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ایجاد و انتشار")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun ImageStudioDialog(
    item: SiteItem,
    imageMeta: ImageMetadata,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = GliseoTertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "استودیو بهینه‌سازی تصویر و WebP", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "نام فایل بهینه‌شده سئو (انگلیسی پیوسته):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = imageMeta.suggestedFileName, fontSize = 11.sp, color = GliseoPrimary, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "فرمت فشرده پیشنهادی:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${imageMeta.format} (کاهش حجم تا ۷۰٪ بدون افت کیفیت)", fontSize = 10.sp, color = GliseoSuccess)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "متن جایگزین (Alt Text):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = imageMeta.persianAltText, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "نام فایل بهینه (WebP):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = imageMeta.suggestedFileName, fontSize = 10.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("تایید و بستن")
            }
        }
    )
}
