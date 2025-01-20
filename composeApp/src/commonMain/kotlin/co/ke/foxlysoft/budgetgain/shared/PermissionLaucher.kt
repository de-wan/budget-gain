package co.ke.foxlysoft.budgetgain.shared

import androidx.compose.runtime.Composable

@Composable
expect fun PermissionLaucher(onPermissionGranted: () -> Unit,onPermissionDenied: () -> Unit,)