package com.zime.garage.ui.version

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zime.garage.common.ResourceLoader
import java.text.SimpleDateFormat
import java.util.*

/**
 * 설정 화면을 표시하는 다이얼로그 컴포넌트
 * @param onDismiss 다이얼로그를 닫을 때 호출되는 콜백 함수
 */
@Composable
fun VersionDialog(onDismiss: () -> Unit) {
    // 플랫폼 Dialog 대신 Material AlertDialog를 사용하여 ComposeScene 컨텍스트 요구사항을 피합니다.
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = ResourceLoader.getString("version_title"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.widthIn(min = 300.dp, max = 500.dp)
            ) {
                VersionInfoSection()
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(ResourceLoader.getString("version_close"))
            }
        }
    )
}

/**
 * 버전 정보를 표시하는 섹션 컴포넌트
 */
@Composable
private fun VersionInfoSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 버전 정보 제목
        Text(
            text = ResourceLoader.getString("version_info"),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colors.onSurface
        )
        
        // 앱 이름
        InfoRow(
            label = ResourceLoader.getString("version_app_name"),
            value = "19Garage"
        )
        
        // 앱 버전
        InfoRow(
            label = ResourceLoader.getString("version_app_version"),
            value = ResourceLoader.APP_VERSION
        )
        
        // 빌드 날짜 (현재 날짜로 표시)
        val buildDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        InfoRow(
            label = ResourceLoader.getString("version_build_date"),
            value = buildDate
        )
    }
}

/**
 * 정보 행을 표시하는 컴포넌트
 * @param label 라벨 텍스트
 * @param value 값 텍스트
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colors.onSurface
        )
    }
}