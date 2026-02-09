package np.ict.mad.mad25_t01_team2_npal2

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

@Composable
fun HomeScreenContent(
    user: UserData,
    onTaskClick: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenStudentCard: () -> Unit
) {

    // User's Data + Fallback
    val displayName = user.username.ifBlank { "User" }
    val studentId = user.studentId.ifBlank { "No Student ID" }
    val uid = user.uid.ifBlank { "No UID" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            HeaderRow(
                name = displayName,
                profileImageRes = R.drawable.login_image
            )
        }

        item { TodayRow() }

        item { ScheduleCard() }

        item {
            Text(
                text = "Student Card",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            StudentCard(
                name = displayName,
                course = "ICT",
                studentId = studentId,
                validFrom = "04/2021",
                themeIndex = user.cardThemeIndex,
                uid = uid,
                onClick = onOpenStudentCard
            )
        }
    }
}

@Composable
private fun HeaderRow(
    name: String,
    profileImageRes: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Welcome Back,",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
        }

        Image(
            painter = painterResource(profileImageRes),
            contentDescription = "Profile picture",
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .border(1.dp, Color(0x22000000), CircleShape)
        )
    }
}

@Composable
private fun TodayRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Previous day"
        )
        Text(
            text = "Today",
            modifier = Modifier.padding(horizontal = 10.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Next day"
        )

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = "Calendar"
        )
    }
}

@Composable
private fun ScheduleCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 320.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F6FA)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        )
    }
}

@Composable
private fun StudentCard(
    name: String,
    course: String,
    studentId: String,
    validFrom: String,
    themeIndex: Int,
    onClick: () -> Unit,
    uid: String
) {
    val gradientOptions = listOf(
        listOf(Color(0xFF2E6CF6), Color(0xFF63B4FF)), // blue
        listOf(Color(0xFFFF6CAB), Color(0xFFFFC3A0)), // pink
        listOf(Color(0xFF11998E), Color(0xFF38EF7D)), // green
        listOf(Color(0xFF000000), Color(0xFF434343)), // black
        listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)), // red
        listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))  // purple
    )

    val safeIndex = themeIndex.coerceIn(0, gradientOptions.lastIndex)
    val selectedGradient = gradientOptions[safeIndex]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Brush.linearGradient(colors = selectedGradient))
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.TopStart)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Course: $course", color = Color.White)
                    Text(text = "Student ID: $studentId", color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Valid From: $validFrom", color = Color.White)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x22FFFFFF))
                )
            }

            // Emily: Bar Code Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                BarcodeFromUid(
                    uid = uid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
            }

            Text(
                text = "Tap to Show Details",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0x88000000)
            )
        }
    }
}

// Emily: Generate Bar Code
private fun generateCode128BarcodeBitmap(data: String, width: Int, height: Int): Bitmap? {
    return try {
        val encoder = BarcodeEncoder()
        encoder.encodeBitmap(data, BarcodeFormat.CODE_128, width, height)
    } catch (e: Exception) {
        null
    }
}

// Emily: Renders User's UID into BarCOde
@Composable
private fun BarcodeFromUid(
    uid: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(uid) {
        val safe = uid.trim()
        if (safe.isBlank() || safe == "No UID") null
        else generateCode128BarcodeBitmap(safe, width = 1200, height = 300)
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "UID Barcode",
            modifier = modifier,
            contentScale = ContentScale.FillBounds
        )
    } else {
        Box(
            modifier = modifier.background(Color(0x11000000))
        )
    }
}