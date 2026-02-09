package np.ict.mad.mad25_t01_team2_npal2

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

private fun generateBarcode(text: String, width: Int = 800, height: Int = 200): Bitmap {
    val bitMatrix: BitMatrix =
        MultiFormatWriter().encode(text, BarcodeFormat.CODE_128, width, height)

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(
                x,
                y,
                if (bitMatrix[x, y]) android.graphics.Color.BLACK
                else android.graphics.Color.WHITE
            )
        }
    }
    return bitmap
}

@Composable
fun StudentCardScreen(
    user: UserData,
    onUserUpdated: (UserData) -> Unit,
    onBack: () -> Unit
) {
    val firestore = remember { FirebaseFirestore.getInstance() }

    val barcodeBitmap = remember(user.studentId) {
        generateBarcode(user.studentId.ifBlank { "00000000" })
    }

    val gradientOptions = listOf(
        listOf(Color(0xFF2E6CF6), Color(0xFF63B4FF)),
        listOf(Color(0xFFFF6CAB), Color(0xFFFFC3A0)),
        listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
        listOf(Color(0xFF000000), Color(0xFF434343)),
        listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)),
        listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
    )

    // Emily: Save User Data To
    var selectedGradientIndex by rememberSaveable { mutableIntStateOf(user.cardThemeIndex.coerceIn(0, 5)) }

    // Emily: To sync the option after they launch
    LaunchedEffect(user.cardThemeIndex) {
        selectedGradientIndex = user.cardThemeIndex.coerceIn(0, 5)
    }

    val selectedGradient = gradientOptions[selectedGradientIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Barcode Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    bitmap = barcodeBitmap.asImageBitmap(),
                    contentDescription = "Student Barcode",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Gradient Student Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(brush = Brush.linearGradient(colors = selectedGradient))
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(70.dp),
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = user.username.ifBlank { "No Name" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Student ID: ${user.studentId}", color = Color.White)
                        Text("Email: ${user.email}", color = Color.White)
                        Text("UID: ${user.uid}", color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Card Colors",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Gradient Picker
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            for (row in 0 until 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        val gradient = gradientOptions[index]
                        val isSelected = index == selectedGradientIndex

                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(gradient))
                                .border(
                                    width = if (isSelected) 4.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFFFC107) else Color(0x22000000),
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedGradientIndex = index

                                    // Emily: Update local user state in BottomNavbar immediately
                                    onUserUpdated(user.copy(cardThemeIndex = index))

                                    // Emily: Save in Firestore
                                    if (user.uid.isNotBlank()) {
                                        firestore.collection("users")
                                            .document(user.uid)
                                            .update("cardThemeIndex", index)
                                    }
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text("Close")
        }
    }
}