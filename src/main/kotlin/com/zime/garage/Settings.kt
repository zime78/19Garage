package com.zime.garage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zime.garage.common.ResourceLoader
import java.text.SimpleDateFormat
import java.util.*

/**
 * 설정 화면을 표시하는 다이얼로그 컴포넌트
 * @param onDismiss 다이얼로그를 닫을 때 호출되는 콜백 함수
 */
@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .width(400.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colors.surface,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 설정 제목
                Text(
                    text = ResourceLoader.getString("settings_title"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )
                
                Divider(color = Color.Gray.copy(alpha = 0.3f))
                
                // 버전 정보 섹션
                VersionInfoSection()
                
                Divider(color = Color.Gray.copy(alpha = 0.3f))
                
                // 닫기 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Text(
                            text = ResourceLoader.getString("settings_close"),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
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
            text = ResourceLoader.getString("settings_version_info"),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colors.onSurface
        )
        
        // 앱 이름
        InfoRow(
            label = ResourceLoader.getString("settings_app_name"),
            value = "19Garage"
        )
        
        // 앱 버전
        InfoRow(
            label = ResourceLoader.getString("settings_app_version"),
            value = ResourceLoader.APP_VERSION
        )
        
        // 빌드 날짜 (현재 날짜로 표시)
        val buildDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        InfoRow(
            label = ResourceLoader.getString("settings_build_date"),
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