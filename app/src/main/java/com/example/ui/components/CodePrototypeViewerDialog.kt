package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

enum class CodeViewTab {
    TAILWIND_HTML,
    FASTAPI_PYTHON,
    ARCHITECTURE_SPEC
}

@Composable
fun CodePrototypeViewerDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(CodeViewTab.TAILWIND_HTML) }

    val tailwindCode = remember {
        """
<!-- MacroShift Single-File Tailwind Web Prototype -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MacroShift - Adaptive Capital Survival Engine</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        brandDark: '#090D16', cardDark: '#111726',
                        cardElevated: '#182238', cyanNeon: '#00E5FF',
                        emeraldNeon: '#00E676', amberNeon: '#FFD600',
                        crimsonNeon: '#FF1744'
                    }
                }
            }
        }
    </script>
</head>
<body class="bg-[#090D16] text-slate-100 min-h-screen">
    <!-- PERSISTENT PROMINENT TRACKING BAR -->
    <header class="sticky top-0 z-50 bg-[#111726]/95 backdrop-blur border-b border-slate-800 p-4">
        <div class="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-3">
            <div class="bg-[#182238] rounded-xl p-3 border border-slate-700">
                <span class="text-xs uppercase text-slate-400">Total Net Worth</span>
                <div class="text-2xl font-bold mono text-white" id="nw-display">${'$'}115,000</div>
            </div>
            <div class="bg-[#182238] rounded-xl p-3 border border-slate-700">
                <span class="text-xs uppercase text-slate-400">Liquid Cash</span>
                <div class="text-2xl font-bold mono text-emerald-400" id="cash-display">${'$'}35,000</div>
            </div>
            <div class="bg-[#182238] rounded-xl p-3 border border-slate-700">
                <span class="text-xs uppercase text-slate-400">Debt Liabilities</span>
                <div class="text-2xl font-bold mono text-amber-400" id="debt-display">${'$'}918 / cycle</div>
            </div>
        </div>
    </header>
    <main class="max-w-6xl mx-auto p-4">
        <!-- Interactive Asset Buckets & Fast-Paced Capital Shifting Loop -->
    </main>
</body>
</html>
        """.trimIndent()
    }

    val fastApiCode = remember {
        """
# High-Performance FastAPI State Manager Script
from fastapi import FastAPI, WebSocket
from pydantic import BaseModel
from typing import Dict, List
from enum import Enum

app = FastAPI(title="MacroShift Engine")

class AssetType(str, Enum):
    CASH = "CASH"
    BONDS = "BONDS"
    TECH_EQUITIES = "TECH_EQUITIES"
    COMMODITIES = "COMMODITIES"
    REAL_ESTATE = "REAL_ESTATE"
    CRYPTO = "CRYPTO"

class MacroEngine:
    def __init__(self, timeline="1970s_stagflation"):
        self.repo_rate = 0.085
        self.inflation = 0.125
        self.debt_principal = 90000.0
        self.balances = {AssetType.CASH: 35000.0, AssetType.BONDS: 30000.0}

    def shift_capital(self, from_asset: AssetType, to_asset: AssetType, fraction: float):
        amt = self.balances[from_asset] * fraction
        self.balances[from_asset] -= amt
        self.balances[to_asset] += amt
        return True

    def inject_governance_directive(self):
        # Automated Directive Injector for sudden repo rate & geopolitical shocks
        pass
        """.trimIndent()
    }

    val specDoc = remember {
        """
MACROSHIFT ARCHITECTURE SPECIFICATION
=====================================
1. Dynamic Macro-Economic Engine:
   - Penalizes static financial structures.
   - Evaluates compound tick yields every second.
   - Cash devalued by real inflation drag if unallocated.
   - Sovereign Bonds penalized by duration: ΔP = -Duration × ΔRepoRate.
   - Debt Liabilities: Required service = Debt × (Repo + Spread + CPI/3) / 12.

2. Automated Random "Governance Directive Injector":
   - Fires sudden geopolitical and central bank rate decrees.
   - Immediate principal shocks and duration stress tests.

3. Persistent Tracking Bar:
   - Real-time Net Worth, Liquid Cash Runway, Inflation-Adjusted Debt Liabilities.
        """.trimIndent()
    }

    val activeContent = when (selectedTab) {
        CodeViewTab.TAILWIND_HTML -> tailwindCode
        CodeViewTab.FASTAPI_PYTHON -> fastApiCode
        CodeViewTab.ARCHITECTURE_SPEC -> specDoc
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("code_prototype_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            border = BorderStroke(1.dp, BorderHighlight),
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Code",
                            tint = CyanNeon
                        )
                        Column {
                            Text(
                                text = "CODE & SPEC EXPORT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Zero-lag copy-pasteable web prototype & FastAPI engine",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TabChip(
                        label = "Tailwind Web (Single-File)",
                        isSelected = selectedTab == CodeViewTab.TAILWIND_HTML,
                        onClick = { selectedTab = CodeViewTab.TAILWIND_HTML },
                        modifier = Modifier.weight(1f)
                    )
                    TabChip(
                        label = "FastAPI Backend (Python)",
                        isSelected = selectedTab == CodeViewTab.FASTAPI_PYTHON,
                        onClick = { selectedTab = CodeViewTab.FASTAPI_PYTHON },
                        modifier = Modifier.weight(1f)
                    )
                    TabChip(
                        label = "Engine Spec",
                        isSelected = selectedTab == CodeViewTab.ARCHITECTURE_SPEC,
                        onClick = { selectedTab = CodeViewTab.ARCHITECTURE_SPEC },
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Bar (Copy to Clipboard)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (selectedTab) {
                            CodeViewTab.TAILWIND_HTML -> "web_prototype_tailwind.html"
                            CodeViewTab.FASTAPI_PYTHON -> "fastapi_macro_engine.py"
                            CodeViewTab.ARCHITECTURE_SPEC -> "MACRO_ENGINE_SPEC.md"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = CyanNeon
                    )

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("MacroShift Code", activeContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied code to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = BackgroundDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("copy_code_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "COPY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Code Display Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    color = BackgroundDark,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = activeContent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) CyanNeon.copy(alpha = 0.2f) else SurfaceVariantDark,
        border = BorderStroke(1.dp, if (isSelected) CyanNeon else BorderSubtle)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) CyanNeon else TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}
