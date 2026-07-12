package com.example.personalassistant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun RadarScreen(authToken: String) {
    var isVisible by remember { mutableStateOf(false) }
    var nearbyUsers by remember { mutableStateOf<List<RadarClient.NearbyUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("رادار افراد نزدیک", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        Card {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("قابل‌مشاهده روی رادار باش")
                    Text(
                        "وقتی روشن باشه، دیگران می‌تونن ببینن نزدیکشونی و درخواست چت بفرستن",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = isVisible,
                    onCheckedChange = { checked ->
                        isVisible = checked
                        scope.launch {
                            RadarClient.setVisibility(authToken, checked)
                            if (checked) {
                                isLoading = true
                                nearbyUsers = RadarClient.getNearbyUsers(authToken)
                                isLoading = false
                            } else {
                                nearbyUsers = emptyList()
                            }
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (!isVisible) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "برای دیدن افراد نزدیک، اول باید خودت قابل‌مشاهده بشی",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else if (isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (nearbyUsers.isEmpty()) {
            Text("فعلاً کسی نزدیکت پیدا نشد")
        } else {
            LazyColumn {
                items(nearbyUsers) { user ->
                    NearbyUserRow(user, authToken, scope) {
                        scope.launch {
                            nearbyUsers = RadarClient.getNearbyUsers(authToken)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyUserRow(
    user: RadarClient.NearbyUser,
    authToken: String,
    scope: kotlinx.coroutines.CoroutineScope,
    onListChanged: () -> Unit
) {
    var requestSent by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(user.displayName, style = MaterialTheme.typography.titleMedium)
                Text(user.approxDistance, style = MaterialTheme.typography.bodySmall)
            }

            if (!requestSent) {
                Button(onClick = {
                    scope.launch {
                        if (RadarClient.sendChatRequest(authToken, user.userId)) {
                            requestSent = true
                        }
                    }
