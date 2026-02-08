package np.ict.mad.mad25_t01_team2_npal2

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

// Logic 1: Define data structures for handling GPS coordinates.
// Data class for GPS coordinates
data class GpsCoordinates(val latitude: Double, val longitude: Double)

// Data class for a bounding box using GPS coordinates to define a region.
data class GpsBoundingBox(val northEast: GpsCoordinates, val southWest: GpsCoordinates) {
    fun contains(coords: GpsCoordinates): Boolean {
        return coords.latitude < northEast.latitude && coords.latitude > southWest.latitude &&
                coords.longitude < northEast.longitude && coords.longitude > southWest.longitude
    }
}

// Updated data class to include GPS information and a description for each region
data class TappableRegion(
    val name: String,
    val description: String,
    val rect: Rect,
    val gpsBox: GpsBoundingBox,
    val backgroundColor: Color
)

// Logic 2: Associate GPS bounding boxes with each tappable region.
// IMPORTANT: These GPS coordinates are placeholders and must be replaced with the
// actual coordinates for your campus map.
private val tappableRegions = listOf(
    TappableRegion("Main Entrance", "The main point of entry to the campus. A common meeting spot.", Rect(973f, 1655f, 1106f, 1783f), GpsBoundingBox(GpsCoordinates(1.378, 103.849), GpsCoordinates(1.376, 103.847)),Color.Gray),
    TappableRegion("Convention Centre", "A large venue for events, conferences, and exhibitions.", Rect(813f, 1061f, 941f, 1212f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),Color.Cyan),
    TappableRegion("Atrium/Library", "A quiet place for study and research, with a vast collection of books.", Rect(1100f, 1396f, 1194f, 1542f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),Color(0xFF2196F3)),
    TappableRegion("Field", "An open green space for sports and recreational activities.", Rect(1561f, 1512f, 1856f, 1644f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),Color(0xFF4CAF50)),
    TappableRegion("Back Entrance", "A secondary entrance providing access to the rear of the campus.", Rect(1779f, 1300f, 1842f, 1365f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),Color.Gray),
    TappableRegion("SIT", "The Singapore Institute of Technology building.", Rect(1167f, 827f, 1257f, 942f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),Color(0xFFF44336)),
    TappableRegion("Makan Place", "A popular food court offering a variety of local dishes.", Rect(599f, 873f, 719f, 926f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),Color(0xFFFFA500)),
    TappableRegion("Food Club", "Another dining option on campus with diverse food choices.", Rect(1251f, 1125f, 1357f, 1178f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),Color(0xFFFFA500)),
    TappableRegion("Munch", "A cafe perfect for a quick snack or coffee break.", Rect(605f, 1455f, 723f, 1500f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0)),Color(0xFFFFA500))
)

@Composable
fun LocationDashboard(
    region: TappableRegion,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            .padding(16.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    onToggleExpand()
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = region.backgroundColor // Apply the region's color
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = region.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black // Ensure text is readable
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = region.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black // Ensure text is readable
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "More details will go here...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )

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

@SuppressLint("DefaultLocale")
@Composable
fun ZoomControls(
    currentZoom: Float,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Actual zoom levels (what's applied to the image)
    val zoomLevels = listOf(1.5f, 2f, 2.5f, 3f, 3.5f, 4f)

    // Display text (what the user sees)
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
            // Zoom In button
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            // Find the next higher zoom level
                            val nextZoom = zoomLevels.firstOrNull { it > currentZoom } ?: zoomLevels.first()
                            onZoomChange(nextZoom)
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Current zoom level
            Text(
                text = zoomText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Zoom Out button
            Text(
                text = "−",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            // Find the next lower zoom level
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
@Composable
fun SchoolMap() {
    val density = LocalDensity.current
    var scale by remember { mutableFloatStateOf(2.5f) } // Start at 2.5f = "1.5x" display
    var offset by remember { mutableStateOf(Offset.Zero) }
    var userInteracted by remember { mutableStateOf(false) }
    var isInteracting by remember { mutableStateOf(false) }

    var dashboardRegion by remember { mutableStateOf<TappableRegion?>(null) }
    var isDashboardExpanded by remember { mutableStateOf(false) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var hasMovedFromStart by remember { mutableStateOf(false) }
    val painter = painterResource(id = R.drawable.campus_map_1)

    val currentUserLocation = GpsCoordinates(1.377, 103.848)

    // Function to detect region at screen center
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
    // Function to recenter to user's location
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
            hasMovedFromStart = false // Reset the flag
        }
    }

    // Detect region when user stops interacting
    LaunchedEffect(isInteracting) {
        if (!isInteracting && userInteracted) {
            kotlinx.coroutines.delay(300)
            detectRegionAtCenter()
        }
    }

    // Initial setup
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

            val viewCenter = Offset(viewSize.width / 2f, viewSize.height / 2f) // Changed from 0.6f to 0.5f (true center)
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

    // Constrain offset when scale changes (from zoom buttons)
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

                                        // Update scale with pinch gesture
                                        scale = (scale * zoomChange).coerceIn(1.5f, 4f)

                                        // Update offset with pan gesture
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

// Red "You are here" pin at screen center (follows map position)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        // Offset the entire column UP by half the icon height
                        // so the bottom tip of the pin is at center
                        translationY = -40.dp.toPx() // Half of 80dp pin size
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Text only visible when NOT interacting AND user hasn't moved
                if (!isInteracting && !hasMovedFromStart) {
                    Text(
                        text = "You are here",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                // Pin always visible, slightly transparent while panning
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

        // Dashboard
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

        // Zoom controls
        ZoomControls(
            currentZoom = scale,
            onZoomChange = { newZoom ->
                scale = newZoom
            },
            modifier = Modifier.align(Alignment.TopEnd)
        )
// Recenter button (just above dashboard on right side)
        if (hasMovedFromStart) {
            RecenterButton(
                onClick = { recenterToUserLocation() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 0.dp,
                        bottom = if (isDashboardExpanded)
                            (viewSize.height * 0.5f + 216f).dp  // 200dp dashboard + 16dp padding
                        else
                            216.dp  // 200dp dashboard + 16dp padding
                    )
            )
        }
    }
}