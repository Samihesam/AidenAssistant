package com.example.personalassistant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TermsOfServiceScreen(onAccepted: () -> Unit) {
    var hasScrolledToEnd by remember { mutableStateOf(false) }
    var isChecked by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState.value) {
        if (scrollState.value >= scrollState.maxValue - 50 && scrollState.maxValue > 0) {
            hasScrolledToEnd = true
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("شرایط استفاده از خدمات", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Text(TERMS_OF_SERVICE_TEXT, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(12.dp))

        if (!hasScrolledToEnd) {
            Text(
                "لطفاً برای فعال‌شدن تیک تایید، متن رو تا انتها اسکرول کن",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                enabled = hasScrolledToEnd
            )
            Text("شرایط استفاده از خدمات رو خوندم و قبول دارم")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onAccepted,
            enabled = isChecked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تایید و ادامه")
        }
    }
}

private const val TERMS_OF_SERVICE_TEXT = """
با نصب یا استفاده از اپلیکیشن «دستیار شخصی ایدن»، شما با شرایط زیر موافقت می‌کنید:

۱. مالکیت و مجوز استفاده
این اپلیکیشن و کلیه‌ی کدها متعلق به [اسم خودت] است. نصب و استفاده از این اپلیکیشن صرفاً یک مجوز محدود و شخصی برای استفاده به شما می‌دهد. کپی، توزیع مجدد، یا مهندسی معکوس این اپلیکیشن بدون مجوز کتبی ممنوع است.

۲. دسترسی‌ها و حریم خصوصی
این اپلیکیشن برای قابلیت‌هایش (پیامک/تماس، تشخیص گفتار، رادار) نیازمند دسترسی‌های خاصیه. هر دسترسی جداگانه و با رضایت صریح شما درخواست می‌شه و در هر زمان قابل لغوه.

۳. مسئولیت کاربر
شما مسئول استفاده‌ی قانونی از اپ، صحت اطلاعات ثبت‌شده (از جمله تاریخ تولد)، و رفتار مناسب در قابلیت‌های اجتماعی هستید.

۴. محدودیت سنی
قابلیت رادار و چت صرفاً برای کاربران بالای ۱۸ سال است.

۵. سلب مسئولیت
این اپ «همان‌طور که هست» ارائه می‌شه، بدون ضمانت عملکرد بی‌نقص یا در دسترس بودن مداوم.

۶. محدودیت مسئولیت
توسعه‌دهنده مسئولیتی در قبال خسارات ناشی از استفاده از این اپ، از جمله سوءاستفاده‌ی احتمالی از طریق قابلیت رادار، نخواهد داشت.

۷. رفتار ممنوعه
آزار و مزاحمت سایر کاربران، انتشار محتوای غیرقانونی، و سوءاستفاده از قابلیت دسترسی‌پذیری (Accessibility) ممنوعه.

۸. تعلیق دسترسی
در صورت نقض این شرایط، دسترسی شما ممکنه بدون اطلاع قبلی محدود بشه.

۹. تغییرات
این شرایط ممکنه به‌روزرسانی بشه. ادامه‌ی استفاده به‌معنای پذیرش تغییراته.

۱۰. قانون حاکم
این شرایط طبق قوانین [کشور خودت] تفسیر می‌شه.

۱۱. تماس
برای سوالات: [ایمیل خودت]
"""
