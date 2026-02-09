package np.ict.mad.mad25_t01_team2_npal2

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.rememberCoroutineScope

data class GpsCoordinates(val latitude: Double, val longitude: Double)

data class GpsBoundingBox(val northEast: GpsCoordinates, val southWest: GpsCoordinates) {
    fun contains(coords: GpsCoordinates): Boolean {
        return coords.latitude < northEast.latitude && coords.latitude > southWest.latitude &&
                coords.longitude < northEast.longitude && coords.longitude > southWest.longitude
    }
}

data class TappableRegion(
    val name: String,
    val description: String,
    val rect: Rect,
    val gpsBox: GpsBoundingBox,
    val icon: ImageVector,
    val backgroundColor: Color
)


fun Color.darken(factor: Float = 0.7f): Color {
    return Color(
        red = this.red * factor,
        green = this.green * factor,
        blue = this.blue * factor,
        alpha = this.alpha
    )
}

private val tappableRegions = listOf(
    TappableRegion(
        "Main Entrance",
        "The main point of entry to the campus. A common meeting spot.",
        Rect(973f, 1655f, 1106f, 1783f),
        GpsBoundingBox(GpsCoordinates(1.378, 103.849), GpsCoordinates(1.376, 103.847)),
        Icons.Filled.Home,
        Color(0xFF2196F3)
    ),
    TappableRegion(
        "Convention Centre",
        "A large venue for events, conferences, and exhibitions.",
        Rect(813f, 1061f, 941f, 1212f),
        GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),
        Icons.Filled.Event,
        Color(0xFF9C27B0)
    ),
    TappableRegion(
        "Atrium/Library",
        "A quiet place for study and research, with a vast collection of books.",
        Rect(1100f, 1396f, 1194f, 1542f),
        GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),
        Icons.Filled.LocalLibrary,
        Color(0xFF3F51B5)
    ),
    TappableRegion(
        "Field",
        "An open green space for sports and recreational activities.",
        Rect(1561f, 1512f, 1856f, 1644f),
        GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),
        Icons.Filled.SportsScore,
        Color(0xFF4CAF50)
    ),
    TappableRegion(
        "Back Entrance",
        "A secondary entrance providing access to the rear of the campus.",
        Rect(1779f, 1300f, 1842f, 1365f),
        GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),
        Icons.Filled.DoorSliding,
        Color(0xFF607D8B)
    ),
    TappableRegion(
        "SIT",
        "The Singapore Institute of Technology building.",
        Rect(1167f, 827f, 1257f, 942f),
        GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),
        Icons.Filled.School,
        Color(0xFFF44336)
    ),
    TappableRegion(
        "Makan Place",
        "A popular food court offering a variety of local dishes.",
        Rect(599f, 873f, 719f, 926f),
        GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),
        Icons.Filled.Restaurant,
        Color(0xFFFF9800)
    ),
    TappableRegion(
        "Food Club",
        "Another dining option on campus with diverse food choices.",
        Rect(1251f, 1125f, 1357f, 1178f),
        GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),
        Icons.Filled.Restaurant,
        Color(0xFFFF5722)
    ),
    TappableRegion(
        "Munch",
        "A cafe perfect for a quick snack or coffee break.",
        Rect(605f, 1455f, 723f, 1500f),
        GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),
        Icons.Filled.Fastfood,
        Color(0xFF795548)
    )
)

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        for (i in 1..5) {
            IconButton(
                onClick = { onRatingChanged(i) }
            ) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star $i",
                    tint = if (i <= rating) Color(0xFFFFD700) else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun LocationDashboard(
    region: TappableRegion,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firebaseHelper = remember { FirebaseHelper() }
    val currentUserId = firebaseHelper.getCurrentUserId()
    val scope = rememberCoroutineScope() //resets contents when User navigates away

    var userRating by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var existingFeedback by remember { mutableStateOf<LocationFeedback?>(null) }
    var allFeedback by remember { mutableStateOf<List<LocationFeedback>>(emptyList()) }
    var averageRating by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(region.name) {
        // Reset all states
        userRating = 0
        feedbackText = ""
        showSuccessMessage = false
        showErrorMessage = false
        isSubmitting = false
        existingFeedback = null
        allFeedback = emptyList()
        averageRating = 0f

        if (currentUserId != null) {
            existingFeedback = firebaseHelper.getUserFeedbackForLocation(currentUserId, region.name)
            existingFeedback?.let {
                userRating = it.rating
                feedbackText = it.comment
            }
        }

        allFeedback = firebaseHelper.getLocationFeedback(region.name)
        averageRating = if (allFeedback.isEmpty()) {
            0f
        } else {
            allFeedback.map { it.rating }.average().toFloat()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isExpanded) {
                    Modifier.fillMaxHeight(0.7f)
                } else {
                    Modifier.height(225.dp)
                }
            )
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(3.dp, region.backgroundColor.darken())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            onToggleExpand()
                        }
                    }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.size(60.dp),
                    colors = CardDefaults.cardColors(containerColor = region.backgroundColor),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = region.icon,
                            contentDescription = region.name,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = region.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Show average rating if available
                    if (allFeedback.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", averageRating) + " (${allFeedback.size} reviews)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF546E7A)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = region.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF546E7A)
                    )
                }
            }

            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Your Rating Section
                    Text(
                        text = if (existingFeedback != null) "Update Your Rating:" else "Rate This Location:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StarRatingBar(
                        rating = userRating,
                        onRatingChanged = {
                            userRating = it
                            showSuccessMessage = false
                            showErrorMessage = false
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (existingFeedback != null) "Update Your Comment:" else "Leave a Comment:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = {
                            feedbackText = it
                            showSuccessMessage = false
                            showErrorMessage = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = {
                            Text("Share your experience...", color = Color(0xFF90A4AE))
                        },
                        maxLines = 5,
                        enabled = !isSubmitting,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFDD835),
                            unfocusedBorderColor = Color(0xFFBDBDBD),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (currentUserId != null && userRating > 0) {
                                isSubmitting = true
                                scope.launch {
                                    val success = if (existingFeedback != null) {
                                        firebaseHelper.updateLocationFeedback(
                                            existingFeedback!!.id,
                                            region.name,
                                            userRating,
                                            feedbackText
                                        )
                                    } else {
                                        firebaseHelper.saveLocationFeedback(
                                            currentUserId,
                                            region.name,
                                            userRating,
                                            feedbackText
                                        )
                                    }

                                    isSubmitting = false
                                    if (success) {
                                        showSuccessMessage = true
                                        showErrorMessage = false
                                        // Reload feedback
                                        existingFeedback = firebaseHelper.getUserFeedbackForLocation(
                                            currentUserId,
                                            region.name
                                        )
                                        allFeedback = firebaseHelper.getLocationFeedback(region.name)
                                        averageRating = if (allFeedback.isEmpty()) {
                                            0f
                                        } else {
                                            allFeedback.map { it.rating }.average().toFloat()
                                        }
                                    } else {
                                        showErrorMessage = true
                                        showSuccessMessage = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        enabled = userRating > 0 && !isSubmitting && currentUserId != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFDD835),
                            contentColor = Color(0xFF37474F)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = when {
                                isSubmitting -> "Submitting..."
                                existingFeedback != null -> "Update Feedback"
                                else -> "Submit Feedback"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (showSuccessMessage) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ " + if (existingFeedback != null)
                                "Feedback updated successfully!" else "Thank you for your feedback!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (showErrorMessage) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✗ Failed to submit feedback. Please try again.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (currentUserId == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please log in to submit feedback.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Operating Hours:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Monday - Friday: 8:00 AM - 10:00 PM\nSaturday - Sunday: 9:00 AM - 8:00 PM",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Facilities:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "• WiFi Available\n• Seating Area\n• Restrooms\n• Accessibility Features",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ZoomControls(
    currentZoom: Float,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoomLevels = listOf(1.5f, 2f, 2.5f, 3f, 3.5f, 4f)

    val zoomText = when {
        currentZoom <= 1.5f -> "0.5x"
        currentZoom <= 2f -> "1x"
        currentZoom <= 2.5f -> "1.5x"
        currentZoom <= 3f -> "2x"
        currentZoom <= 3.5f -> "2.5x"
        else -> "3x"
    }

    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            val nextZoom = zoomLevels.firstOrNull { it > currentZoom } ?: zoomLevels.first()
                            onZoomChange(nextZoom)
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Text(
                text = zoomText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(
                text = "−",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            val prevZoom = zoomLevels.lastOrNull { it < currentZoom } ?: zoomLevels.last()
                            onZoomChange(prevZoom)
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun RecenterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Recenter to current location",
            modifier = Modifier
                .padding(12.dp)
                .size(28.dp)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        onClick()
                    }
                },
            tint = Color.Red
        )
    }
}

@Composable
fun SchoolMap() {
    val density = LocalDensity.current
    var scale by remember { mutableFloatStateOf(2.5f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var userInteracted by remember { mutableStateOf(false) }
    var isInteracting by remember { mutableStateOf(false) }

    var dashboardRegion by remember { mutableStateOf<TappableRegion?>(null) }
    var isDashboardExpanded by remember { mutableStateOf(false) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var hasMovedFromStart by remember { mutableStateOf(false) }
    val painter = painterResource(id = R.drawable.campus_map_1)

    val currentUserLocation = GpsCoordinates(1.377, 103.848)

    fun detectRegionAtCenter() {
        val imageIntrinsicSize = painter.intrinsicSize
        if (viewSize == IntSize.Zero || imageIntrinsicSize == Size.Zero) return

        val pinOffset = 3.5f * scale
        val viewCenter = Offset(viewSize.width / 2f, viewSize.height / 2f + pinOffset)

        val viewAspectRatio = viewSize.width.toFloat() / viewSize.height.toFloat()
        val imageAspectRatio = imageIntrinsicSize.width / imageIntrinsicSize.height
        val scaledImageWidth: Float
        val scaledImageHeight: Float
        if (imageAspectRatio > viewAspectRatio) {
            scaledImageWidth = viewSize.width.toFloat()
            scaledImageHeight = scaledImageWidth / imageAspectRatio
        } else {
            scaledImageHeight = viewSize.height.toFloat()
            scaledImageWidth = scaledImageHeight * imageAspectRatio
        }
        val imageOffsetX = (viewSize.width - scaledImageWidth) / 2f
        val imageOffsetY = (viewSize.height - scaledImageHeight) / 2f
        val imageRectInView = Rect(
            left = imageOffsetX,
            top = imageOffsetY,
            right = imageOffsetX + scaledImageWidth,
            bottom = imageOffsetY + scaledImageHeight
        )

        val centerPointOnFitImage = viewCenter + (-offset) / scale

        if (imageRectInView.contains(centerPointOnFitImage)) {
            val centerOnScaledImage = centerPointOnFitImage - imageRectInView.topLeft
            val imageX = (centerOnScaledImage.x / imageRectInView.width) * imageIntrinsicSize.width
            val imageY = (centerOnScaledImage.y / imageRectInView.height) * imageIntrinsicSize.height
            val centerImagePoint = Offset(imageX, imageY)

            val detectedRegion = tappableRegions.find { it.rect.contains(centerImagePoint) }
            if (detectedRegion != null) {
                dashboardRegion = detectedRegion
            }
        }
    }

    fun recenterToUserLocation() {
        val targetRegion = tappableRegions.find { it.gpsBox.contains(currentUserLocation) }

        if (targetRegion != null) {
            dashboardRegion = targetRegion
            val newScale = 2.5f

            val imageIntrinsicSize = painter.intrinsicSize
            val viewAspectRatio = viewSize.width.toFloat() / viewSize.height.toFloat()
            val imageAspectRatio = imageIntrinsicSize.width / imageIntrinsicSize.height
            val scaledImageWidth: Float
            val scaledImageHeight: Float
            if (imageAspectRatio > viewAspectRatio) {
                scaledImageWidth = viewSize.width.toFloat()
                scaledImageHeight = scaledImageWidth / imageAspectRatio
            } else {
                scaledImageHeight = viewSize.height.toFloat()
                scaledImageWidth = scaledImageHeight * imageAspectRatio
            }
            val imageOffsetX = (viewSize.width - scaledImageWidth) / 2f
            val imageOffsetY = (viewSize.height - scaledImageHeight) / 2f
            val imageRectInView = Rect(
                left = imageOffsetX,
                top = imageOffsetY,
                right = imageOffsetX + scaledImageWidth,
                bottom = imageOffsetY + scaledImageHeight
            )

            val targetCenterOnImage = targetRegion.rect.center
            val targetCenterInFitImage = Offset(
                x = imageRectInView.left + (targetCenterOnImage.x / imageIntrinsicSize.width) * imageRectInView.width,
                y = imageRectInView.top + (targetCenterOnImage.y / imageIntrinsicSize.height) * imageRectInView.height
            )

            val viewCenter = Offset(viewSize.width / 2f, viewSize.height / 2f)
            val newOffsetUnconstrained = (viewCenter - targetCenterInFitImage) * newScale
            val maxOffsetX = (viewSize.width * newScale - viewSize.width) / 2f
            val maxOffsetY = (viewSize.height * newScale - viewSize.height) / 2f
            val newOffset = Offset(
                newOffsetUnconstrained.x.coerceIn(-maxOffsetX, maxOffsetX),
                newOffsetUnconstrained.y.coerceIn(-maxOffsetY, maxOffsetY)
            )

            scale = newScale
            offset = newOffset
            hasMovedFromStart = false
        }
    }

    LaunchedEffect(isInteracting) {
        if (!isInteracting && userInteracted) {
            kotlinx.coroutines.delay(300)
            detectRegionAtCenter()
        }
    }

    LaunchedEffect(viewSize) {
        if (viewSize == IntSize.Zero || userInteracted) return@LaunchedEffect

        val targetRegion = tappableRegions.find { it.gpsBox.contains(currentUserLocation) }

        if (targetRegion != null) {
            dashboardRegion = targetRegion
            val newScale = 2.5f

            val imageIntrinsicSize = painter.intrinsicSize
            val viewAspectRatio = viewSize.width.toFloat() / viewSize.height.toFloat()
            val imageAspectRatio = imageIntrinsicSize.width / imageIntrinsicSize.height
            val scaledImageWidth: Float
            val scaledImageHeight: Float
            if (imageAspectRatio > viewAspectRatio) {
                scaledImageWidth = viewSize.width.toFloat()
                scaledImageHeight = scaledImageWidth / imageAspectRatio
            } else {
                scaledImageHeight = viewSize.height.toFloat()
                scaledImageWidth = scaledImageHeight * imageAspectRatio
            }
            val imageOffsetX = (viewSize.width - scaledImageWidth) / 2f
            val imageOffsetY = (viewSize.height - scaledImageHeight) / 2f
            val imageRectInView = Rect(
                left = imageOffsetX,
                top = imageOffsetY,
                right = imageOffsetX + scaledImageWidth,
                bottom = imageOffsetY + scaledImageHeight
            )

            val targetCenterOnImage = targetRegion.rect.center
            val targetCenterInFitImage = Offset(
                x = imageRectInView.left + (targetCenterOnImage.x / imageIntrinsicSize.width) * imageRectInView.width,
                y = imageRectInView.top + (targetCenterOnImage.y / imageIntrinsicSize.height) * imageRectInView.height
            )

            val viewCenter = Offset(viewSize.width / 2f, viewSize.height / 2f)
            val newOffsetUnconstrained = (viewCenter - targetCenterInFitImage) * newScale
            val maxOffsetX = (viewSize.width * newScale - viewSize.width) / 2f
            val maxOffsetY = (viewSize.height * newScale - viewSize.height) / 2f
            val newOffset = Offset(
                newOffsetUnconstrained.x.coerceIn(-maxOffsetX, maxOffsetX),
                newOffsetUnconstrained.y.coerceIn(-maxOffsetY, maxOffsetY)
            )

            scale = newScale
            offset = newOffset
        }
    }

    LaunchedEffect(scale) {
        val imageIntrinsicSize = painter.intrinsicSize
        if (imageIntrinsicSize != Size.Zero && viewSize != IntSize.Zero) {
            val viewAspectRatio = viewSize.width.toFloat() / viewSize.height.toFloat()
            val imageAspectRatio = imageIntrinsicSize.width / imageIntrinsicSize.height

            val fittedWidth: Float
            val fittedHeight: Float
            if (imageAspectRatio > viewAspectRatio) {
                fittedWidth = viewSize.width.toFloat()
                fittedHeight = fittedWidth / imageAspectRatio
            } else {
                fittedHeight = viewSize.height.toFloat()
                fittedWidth = fittedHeight * imageAspectRatio
            }

            val scaledWidth = fittedWidth * scale
            val scaledHeight = fittedHeight * scale

            val borderPxX = with(density) { 10.dp.toPx() }
            val borderPxY = with(density) { 15.dp.toPx() }

            val maxOffsetX = if (scaledWidth > viewSize.width) {
                ((scaledWidth - viewSize.width) / 2f + borderPxX)
            } else if (fittedWidth < viewSize.width) {
                0f
            } else {
                0f
            }.coerceAtLeast(0f)

            val maxOffsetY = if (scaledHeight > viewSize.height) {
                ((scaledHeight - viewSize.height) / 2f + borderPxY)
            } else if (fittedHeight < viewSize.height) {
                0f
            } else {
                0f
            }.coerceAtLeast(0f)

            offset = Offset(
                offset.x.coerceIn(-maxOffsetX, maxOffsetX),
                offset.y.coerceIn(-maxOffsetY, maxOffsetY)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFADD8E6))
                .onSizeChanged { viewSize = it }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        var zoom = 1f
                        var pan = Offset.Zero
                        var pastTouchSlop = false
                        val touchSlop = viewConfiguration.touchSlop
                        var lockedToPanZoom = false

                        awaitFirstDown()
                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.isConsumed }
                            if (!canceled) {
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()

                                if (!pastTouchSlop) {
                                    zoom *= zoomChange
                                    pan += panChange

                                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                                    val zoomMotion = abs(1 - zoom) * centroidSize
                                    val panMotion = pan.getDistance()

                                    if (zoomMotion > touchSlop || panMotion > touchSlop) {
                                        pastTouchSlop = true
                                        lockedToPanZoom = zoomChange != 1f || panChange != Offset.Zero
                                    }
                                }

                                if (pastTouchSlop) {
                                    if (lockedToPanZoom || zoomChange != 1f || panChange != Offset.Zero) {
                                        isInteracting = true
                                        userInteracted = true
                                        hasMovedFromStart = true

                                        scale = (scale * zoomChange).coerceIn(1.5f, 4f)

                                        val newOffsetX = offset.x + panChange.x
                                        val newOffsetY = offset.y + panChange.y

                                        val imageIntrinsicSize = painter.intrinsicSize

                                        if (imageIntrinsicSize != Size.Zero) {
                                            val viewAspectRatio = viewSize.width.toFloat() / viewSize.height.toFloat()
                                            val imageAspectRatio = imageIntrinsicSize.width / imageIntrinsicSize.height

                                            val fittedWidth: Float
                                            val fittedHeight: Float
                                            if (imageAspectRatio > viewAspectRatio) {
                                                fittedWidth = viewSize.width.toFloat()
                                                fittedHeight = fittedWidth / imageAspectRatio
                                            } else {
                                                fittedHeight = viewSize.height.toFloat()
                                                fittedWidth = fittedHeight * imageAspectRatio
                                            }

                                            val scaledWidth = fittedWidth * scale
                                            val scaledHeight = fittedHeight * scale

                                            val borderPxX = 130.dp.toPx()
                                            val borderPxY = 330.dp.toPx()

                                            val maxOffsetX = if (scaledWidth > viewSize.width) {
                                                ((scaledWidth - viewSize.width) / 2f + borderPxX)
                                            } else if (fittedWidth < viewSize.width) {
                                                0f
                                            } else {
                                                0f
                                            }.coerceAtLeast(0f)

                                            val maxOffsetY = if (scaledHeight > viewSize.height) {
                                                ((scaledHeight - viewSize.height) / 2f + borderPxY)
                                            } else if (fittedHeight < viewSize.height) {
                                                0f
                                            } else {
                                                0f
                                            }.coerceAtLeast(0f)

                                            offset = Offset(
                                                newOffsetX.coerceIn(-maxOffsetX, maxOffsetX),
                                                newOffsetY.coerceIn(-maxOffsetY, maxOffsetY)
                                            )
                                        } else {
                                            offset = Offset(newOffsetX, newOffsetY)
                                        }

                                        event.changes.forEach {
                                            if (it.positionChanged()) {
                                                it.consume()
                                            }
                                        }
                                    }
                                }
                            }
                        } while (!canceled && event.changes.any { it.pressed })

                        isInteracting = false
                    }
                }
        ) {
            Image(
                painter = painter,
                contentDescription = "Campus Map",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        translationY = -40.dp.toPx()
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isInteracting && !hasMovedFromStart) {
                    Text(
                        text = "You are here",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "You are here",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer {
                            alpha = if (isInteracting) 0.7f else 1f
                        }
                )
            }
        }

        if (dashboardRegion != null) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                LocationDashboard(
                    region = dashboardRegion!!,
                    isExpanded = isDashboardExpanded,
                    onToggleExpand = { isDashboardExpanded = !isDashboardExpanded }
                )
            }
        }

        ZoomControls(
            currentZoom = scale,
            onZoomChange = { newZoom ->
                scale = newZoom
            },
            modifier = Modifier.align(Alignment.TopEnd)
        )

        if (hasMovedFromStart) {
            RecenterButton(
                onClick = { recenterToUserLocation() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 0.dp,
                        bottom = if (isDashboardExpanded)
                            (viewSize.height * 0.7f + 241f).dp
                        else
                            241.dp
                    )
            )
        }
    }
}