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
    val gpsBox: GpsBoundingBox
)


// Logic 2: Associate GPS bounding boxes with each tappable region.
// IMPORTANT: These GPS coordinates are placeholders and must be replaced with the
// actual coordinates for your campus map.
private val tappableRegions = listOf(
    TappableRegion("Main Entrance", "The main point of entry to the campus. A common meeting spot.", Rect(973f, 1655f, 1106f, 1783f), GpsBoundingBox(GpsCoordinates(1.378, 103.849), GpsCoordinates(1.376, 103.847))),
    TappableRegion("Convention Centre", "A large venue for events, conferences, and exhibitions.", Rect(813f, 1061f, 941f, 1212f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
    TappableRegion("Atrium/Library", "A quiet place for study and research, with a vast collection of books.", Rect(1100f, 1396f, 1194f, 1542f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
    TappableRegion("Field", "An open green space for sports and recreational activities.", Rect(1561f, 1512f, 1856f, 1644f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
    TappableRegion("Back Entrance", "A secondary entrance providing access to the rear of the campus.", Rect(1779f, 1300f, 1842f, 1365f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
    TappableRegion("SIT", "The Singapore Institute of Technology building.", Rect(1167f, 827f, 1257f, 942f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
    TappableRegion("Makan Place", "A popular food court offering a variety of local dishes.", Rect(599f, 873f, 719f, 926f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
    TappableRegion("Food Club", "Another dining option on campus with diverse food choices.", Rect(1251f, 1125f, 1357f, 1178f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
    TappableRegion("Munch", "A cafe perfect for a quick snack or coffee break.", Rect(605f, 1455f, 723f, 1500f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
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
                    Modifier.fillMaxHeight(0.5f)
                } else {
                    Modifier.height(140.dp)
                }
            )
            .padding(16.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    onToggleExpand()
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = region.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = region.description,
                style = MaterialTheme.typography.bodyLarge
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                // Add more content here when expanded
                Text(
                    text = "More details will go here...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
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
    val zoomLevels = listOf(1.5f, 2f, 2.5f, 3f, 4f)

    // Display text (what the user sees)
    val zoomText = when (currentZoom) {
        1.5f -> "0.5x"
        2f -> "1x"
        2.5f -> "1.5x"
        3f -> "2x"
        4f -> "3x"
        else -> String.format("%.1fx", currentZoom / 2f) // Fallback: divide by 2 for display
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
                            // Find current index and move to next
                            val currentIndex = zoomLevels.indexOf(currentZoom)
                            val nextIndex = if (currentIndex == -1) {
                                0 // Default to first if not found
                            } else {
                                (currentIndex + 1) % zoomLevels.size // Cycle through
                            }
                            onZoomChange(zoomLevels[nextIndex])
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
                            // Find current index and move to previous
                            val currentIndex = zoomLevels.indexOf(currentZoom)
                            val prevIndex = when (currentIndex) {
                                -1 -> {
                                    0 // Default to first if not found
                                }
                                0 -> {
                                    zoomLevels.size - 1 // Cycle to end
                                }
                                else -> {
                                    currentIndex - 1
                                }
                            }
                            onZoomChange(zoomLevels[prevIndex])
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}
@Composable
fun SchoolMap() {
    val density = LocalDensity.current
    var scale by remember { mutableFloatStateOf(2f) }
    var targetScale by remember { mutableFloatStateOf(2f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var userInteracted by remember { mutableStateOf(false) }
    var isInteracting by remember { mutableStateOf(false) }

    var dashboardRegion by remember { mutableStateOf<TappableRegion?>(null) }
    var isDashboardExpanded by remember { mutableStateOf(false) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    val painter = painterResource(id = R.drawable.campus_map_1)

    val currentUserLocation = GpsCoordinates(1.377, 103.848) // Simulated location for "Main Entrance"

    // Function to detect region at screen center
    fun detectRegionAtCenter() {
        val imageIntrinsicSize = painter.intrinsicSize
        if (viewSize == IntSize.Zero || imageIntrinsicSize == Size.Zero) return

        // Offset adjusts with zoom - 10 pixels at scale 1.0, scales proportionally
        val pinOffset = 10f * scale

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

        // Transform screen center (adjusted for pin tip) to image coordinates
        val centerPointOnFitImage = viewCenter + (-offset) / scale

        if (imageRectInView.contains(centerPointOnFitImage)) {
            val centerOnScaledImage = centerPointOnFitImage - imageRectInView.topLeft
            val imageX = (centerOnScaledImage.x / imageRectInView.width) * imageIntrinsicSize.width
            val imageY = (centerOnScaledImage.y / imageRectInView.height) * imageIntrinsicSize.height
            val centerImagePoint = Offset(imageX, imageY)

            val detectedRegion = tappableRegions.find { it.rect.contains(centerImagePoint) }
            // Only update if a region is found, otherwise keep the previous one
            if (detectedRegion != null) {
                dashboardRegion = detectedRegion
            }
        }
    }

    // Detect region when user stops interacting
    LaunchedEffect(isInteracting) {
        if (!isInteracting && userInteracted) {
            kotlinx.coroutines.delay(300) // Small delay after stopping
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

            val viewCenter = Offset(viewSize.width / 2f, viewSize.height * 0.6f)
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
    // Animate to target zoom when preset is selected
// Animate to target zoom when preset is selected
    LaunchedEffect(targetScale) {
        if (targetScale != scale && !isInteracting) {
            scale = targetScale
            // Recalculate offset constraints for new scale
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

                // Constrain current offset to new limits
                offset = Offset(
                    offset.x.coerceIn(-maxOffsetX, maxOffsetX),
                    offset.y.coerceIn(-maxOffsetY, maxOffsetY)
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFADD8E6)) // Light blue background
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

                                        // First update the scale - CORRECTED RANGE
                                        val newScale = (scale * zoomChange).coerceIn(1.5f, 4f)

                                        // Calculate what the new offset would be
                                        val newOffsetX = offset.x + panChange.x
                                        val newOffsetY = offset.y + panChange.y

                                        // Get image intrinsic size to calculate fitted dimensions
                                        val imageIntrinsicSize = painter.intrinsicSize

                                        if (imageIntrinsicSize != Size.Zero) {
                                            val viewAspectRatio = viewSize.width.toFloat() / viewSize.height.toFloat()
                                            val imageAspectRatio = imageIntrinsicSize.width / imageIntrinsicSize.height

                                            // Calculate fitted image size (ContentScale.Fit behavior)
                                            val fittedWidth: Float
                                            val fittedHeight: Float
                                            if (imageAspectRatio > viewAspectRatio) {
                                                // Image is wider - fits to width
                                                fittedWidth = viewSize.width.toFloat()
                                                fittedHeight = fittedWidth / imageAspectRatio
                                            } else {
                                                // Image is taller - fits to height
                                                fittedHeight = viewSize.height.toFloat()
                                                fittedWidth = fittedHeight * imageAspectRatio
                                            }

                                            // Calculate scaled dimensions
                                            val scaledWidth = fittedWidth * newScale
                                            val scaledHeight = fittedHeight * newScale

                                            val borderPxX = 130.dp.toPx() // left/right - allow 10px PAST edge
                                            val borderPxY = 330.dp.toPx() // top/bottom - allow 15px PAST edge

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

                                            // Apply constraints
                                            offset = Offset(
                                                newOffsetX.coerceIn(-maxOffsetX, maxOffsetX),
                                                newOffsetY.coerceIn(-maxOffsetY, maxOffsetY)
                                            )
                                        } else {
                                            offset = Offset(newOffsetX, newOffsetY)
                                        }

                                        scale = newScale
                                        // targetScale = newScale  // REMOVED - Don't override preset zoom

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

            // Red "You are here" pin at actual user location
            if (viewSize != IntSize.Zero) {
                val mainEntranceRegion = tappableRegions.find { it.name == "Main Entrance" }
                if (mainEntranceRegion != null) {
                    val imageIntrinsicSize = painter.intrinsicSize
                    if (imageIntrinsicSize != Size.Zero) {
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
                            left = imageOffsetX, top = imageOffsetY,
                            right = imageOffsetX + scaledImageWidth, bottom = imageOffsetY + scaledImageHeight
                        )

                        val targetCenterOnImage = mainEntranceRegion.rect.center
                        val targetCenterInFitImage = Offset(
                            x = imageRectInView.left + (targetCenterOnImage.x / imageIntrinsicSize.width) * imageRectInView.width,
                            y = imageRectInView.top + (targetCenterOnImage.y / imageIntrinsicSize.height) * imageRectInView.height
                        )

                        val viewCenter = Offset(viewSize.width / 2f, viewSize.height / 2f)
                        val pinScreenPosition = viewCenter + (targetCenterInFitImage - viewCenter) * scale + offset

                        val pinAlpha = if (isInteracting) 0.5f else 1f

                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .graphicsLayer {
                                    translationX = pinScreenPosition.x - size.width / 2f
                                    translationY = pinScreenPosition.y - size.height
                                    alpha = pinAlpha
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "You are here",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "You are here",
                                tint = Color.Red,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }
            }

            // Blue pin at screen center (where user is looking) - NO TEXT
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Current view center",
                tint = Color.Blue,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .graphicsLayer {
                        alpha = if (isInteracting) 0.7f else 1f
                    }
            )
        }

        // Dashboard ALWAYS visible at the bottom - shows last detected region
        if (dashboardRegion != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                LocationDashboard(
                    region = dashboardRegion!!,
                    isExpanded = isDashboardExpanded,
                    onToggleExpand = { isDashboardExpanded = !isDashboardExpanded }
                )
            }
        }
// Zoom controls overlay at top right
        ZoomControls(
            currentZoom = scale,
            onZoomChange = { newZoom ->
                targetScale = newZoom
            },
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}