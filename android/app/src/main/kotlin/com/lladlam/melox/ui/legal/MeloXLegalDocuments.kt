package com.lladlam.melox.ui.legal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.lladlam.melox.core.lyrics.MeloXLyricScript
import com.lladlam.melox.core.lyrics.MeloXLyricScriptConverter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lladlam.melox.R
import com.lladlam.melox.ui.glass.MeloXGlassButton
import com.lladlam.melox.ui.glass.MeloXGlassButtonStyle
import com.lladlam.melox.ui.glass.MeloXGlassDialog
import com.lladlam.melox.ui.glass.MeloXSymbol
import com.lladlam.melox.ui.glass.MeloXSymbolIcon
import com.lladlam.melox.ui.glass.MeloXSystemColors

const val MELOX_LEGAL_VERSION = "1.2-2026-08-27"

enum class MeloXLegalDocument(
    val titleRes: Int,
    internal val assetPath: String,
) {
    PrivacyPolicy(R.string.legal_privacy, "legal/privacy-policy-zh-CN.md"),
    Disclaimer(R.string.settings_legal_disclaimer, "legal/disclaimer-zh-CN.md"),
    CloudControlPrivacy(R.string.legal_cloud, "legal/cloud-control-privacy-zh-CN.md"),
    ThirdPartyMusicSources(R.string.settings_legal_third_party, "legal/third-party-music-sources-zh-CN.md"),
    ;

    fun localizedTitle(context: android.content.Context): String = context.getString(titleRes)
}

private enum class LegalBlockKind { Heading, Subheading, Paragraph, Bullet }

private data class LegalBlock(
    val kind: LegalBlockKind,
    val text: String,
)

@Composable
fun MeloXLegalLinks(
    modifier: Modifier = Modifier,
    tint: Color = MeloXSystemColors.Blue,
) {
    var selectedDocument by remember { mutableStateOf<MeloXLegalDocument?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegalLink(stringResource(R.string.legal_privacy), tint) { selectedDocument = MeloXLegalDocument.PrivacyPolicy }
            Text(
                text = stringResource(R.string.legal_and),
                modifier = Modifier.padding(horizontal = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
            LegalLink(stringResource(R.string.legal_disclaimer), tint) { selectedDocument = MeloXLegalDocument.Disclaimer }
        }
        LegalLink(stringResource(R.string.legal_cloud), tint) { selectedDocument = MeloXLegalDocument.CloudControlPrivacy }
    }

    selectedDocument?.let { document ->
        MeloXLegalDocumentDialog(
            document = document,
            onDismiss = { selectedDocument = null },
        )
    }
}

@Composable
fun MeloXThirdPartyMusicSourceConsentDialog(
    onReject: () -> Unit,
    onAccept: () -> Unit,
) {
    var showPolicy by remember { mutableStateOf(false) }
    MeloXGlassDialog(visible = true, onDismiss = {}) {
        Text(stringResource(R.string.legal_third_party_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.legal_third_party_body),
            modifier = Modifier.padding(top = 9.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f),
            fontSize = 14.sp,
            lineHeight = 21.sp,
        )
        Text(
            stringResource(R.string.legal_view_third_party),
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable(role = Role.Button) { showPolicy = true }
                .padding(horizontal = 6.dp, vertical = 7.dp),
            color = MeloXSystemColors.Blue,
            fontWeight = FontWeight.Medium,
        )
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MeloXGlassButton(
                onClick = onReject,
                modifier = Modifier.weight(1f),
                style = MeloXGlassButtonStyle.Plain,
            ) { Text(stringResource(R.string.legal_disagree)) }
            MeloXGlassButton(
                onClick = onAccept,
                modifier = Modifier.weight(1f),
                style = MeloXGlassButtonStyle.BorderedProminent,
            ) { Text(stringResource(R.string.legal_agree_enable)) }
        }
    }
    if (showPolicy) {
        MeloXLegalDocumentDialog(
            document = MeloXLegalDocument.ThirdPartyMusicSources,
            onDismiss = { showPolicy = false },
        )
    }
}

@Composable
private fun LegalLink(text: String, tint: Color, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 5.dp),
        color = tint,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
fun MeloXLegalDocumentDialog(
    document: MeloXLegalDocument,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val blocks = remember(document) {
        val source = context.assets.open(document.assetPath).bufferedReader().use { it.readText() }
        val localized = if (MeloXLyricScript.fromSystem() == MeloXLyricScript.Traditional) {
            MeloXLyricScriptConverter.convertText(source, MeloXLyricScript.Traditional)
        } else {
            source
        }
        parseLegalDocument(localized)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        MeloXSymbolIcon(
                            symbol = MeloXSymbol.Xmark,
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            contentDescription = document.localizedTitle(context),
                            iconSize = 15.sp,
                        )
                    }
                    Text(
                        text = document.localizedTitle(context),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.size(36.dp))
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 22.dp,
                        top = 8.dp,
                        end = 22.dp,
                        bottom = 32.dp,
                    ),
                ) {
                    itemsIndexed(blocks) { index, block ->
                        LegalBlockText(
                            block = block,
                            first = index == 0,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MeloXFirstLaunchLegalConsent(
    onAgree: () -> Unit,
    onDecline: () -> Unit,
    onOpenProject: () -> Unit,
) {
    MeloXGlassDialog(visible = true, onDismiss = {}) {
        Text(stringResource(R.string.legal_welcome), style = MaterialTheme.typography.titleLarge)
        Text(
            text = stringResource(R.string.legal_welcome_body),
            modifier = Modifier.padding(top = 9.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
        MeloXLegalLinks(modifier = Modifier.padding(top = 10.dp))
        Text(
            text = stringResource(R.string.legal_agree_note),
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f),
            fontSize = 12.sp,
            lineHeight = 17.sp,
        )
        Text(
            text = stringResource(R.string.legal_project),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(MaterialTheme.shapes.small)
                .clickable(role = Role.Button, onClick = onOpenProject)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            color = MeloXSystemColors.Blue,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MeloXGlassButton(
                onClick = onDecline,
                modifier = Modifier.weight(1f),
                style = MeloXGlassButtonStyle.Plain,
            ) { Text(stringResource(R.string.legal_decline_exit)) }
            MeloXGlassButton(
                onClick = onAgree,
                modifier = Modifier.weight(1f),
                style = MeloXGlassButtonStyle.BorderedProminent,
            ) { Text(stringResource(R.string.legal_agree_continue)) }
        }
    }
}

@Composable
private fun LegalBlockText(block: LegalBlock, first: Boolean) {
    when (block.kind) {
        LegalBlockKind.Heading -> Text(
            text = block.text,
            modifier = Modifier.padding(top = if (first) 0.dp else 24.dp, bottom = 4.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 25.sp,
            lineHeight = 31.sp,
            fontWeight = FontWeight.Bold,
        )
        LegalBlockKind.Subheading -> Text(
            text = block.text,
            modifier = Modifier.padding(top = 20.dp, bottom = 2.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
        )
        LegalBlockKind.Paragraph -> Text(
            text = block.text,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f),
            fontSize = 14.sp,
            lineHeight = 21.sp,
        )
        LegalBlockKind.Bullet -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "•",
                modifier = Modifier.padding(end = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                fontSize = 14.sp,
                lineHeight = 21.sp,
            )
            Text(
                text = block.text,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f),
                fontSize = 14.sp,
                lineHeight = 21.sp,
            )
        }
    }
}

private fun parseLegalDocument(markdown: String): List<LegalBlock> = markdown
    .lineSequence()
    .map(String::trim)
    .filter(String::isNotEmpty)
    .mapNotNull { line ->
        when {
            line.startsWith("# ") -> LegalBlock(LegalBlockKind.Heading, line.removePrefix("# "))
            line.startsWith("## ") -> LegalBlock(LegalBlockKind.Subheading, line.removePrefix("## "))
            line.startsWith("### ") -> LegalBlock(LegalBlockKind.Subheading, line.removePrefix("### "))
            line.startsWith("- ") -> LegalBlock(LegalBlockKind.Bullet, line.removePrefix("- "))
            line == "---" -> null
            else -> LegalBlock(LegalBlockKind.Paragraph, line)
        }
    }
    .toList()
