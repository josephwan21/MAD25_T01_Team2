package np.ict.mad.mad25_t01_team2_npal2

import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun SchoolMap() {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var userInteracted by remember { mutableStateOf(false) }
    var isInteracting by remember { mutableStateOf(false) }

    var selectedRegion by remember { mutableStateOf<TappableRegion?>(null) }
    var dashboardRegion by remember { mutableStateOf<TappableRegion?>(null) }
    var isDashboardExpanded by remember { mutableStateOf(false) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    val painter = painterResource(id = R.drawable.campus_map_1)

    val currentUserLocation = GpsCoordinates(1.377, 103.848) // Simulated location for "Main Entrance"

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

            val viewCenter = Offset(viewSize.width / 2f, viewSize.height * 0.6f) // Adjusted to be slightly higher
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

    if (selectedRegion != null) {
        AlertDialog(
            onDismissRequest = { selectedRegion = null },
            title = { Text(text = selectedRegion!!.name) },
            text = { Text("You tapped on ${selectedRegion!!.name}.") },
            confirmButton = {
                TextButton(onClick = { selectedRegion = null }) {
                    Text("OK")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { viewSize = it }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        var zoom = 1f
                        var pan = Offset.Zero
                        var pastTouchSlop = false
                        val touchSlop = viewConfiguration.touchSlop
                        var lockedToPanZoom = false

                        val down = awaitFirstDown()
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

                                        // Update scale first
                                        val newScale = (scale * zoomChange).coerceIn(1f, 5f)

                                        // Calculate new offset with pan
                                        val newOffset = offset + panChange

                                        // Calculate pan constraints based on the NEW scale
                                        val maxOffsetX = ((viewSize.width * newScale - viewSize.width) / 2f).coerceAtLeast(0f)
                                        val maxOffsetY = ((viewSize.height * newScale - viewSize.height) / 2f).coerceAtLeast(0f)

                                        // Apply constraints
                                        offset = Offset(
                                            newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                                            newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                                        )

                                        scale = newScale

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

                        if (!pastTouchSlop) {
                            // Tap logic
                            val tapOffset = down.position
                            val imageIntrinsicSize = painter.intrinsicSize
                            if (viewSize != IntSize.Zero && imageIntrinsicSize != Size.Zero) {
                                val viewCenter = Offset(viewSize.width / 2f, viewSize.height / 2f)
                                val tapOnFitImage =
                                    viewCenter + (tapOffset - viewCenter - offset) / scale

                                val viewAspectRatio =
                                    viewSize.width.toFloat() / viewSize.height.toFloat()
                                val imageAspectRatio =
                                    imageIntrinsicSize.width / imageIntrinsicSize.height
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

                                if (imageRectInView.contains(tapOnFitImage)) {
                                    val tapOnScaledImage = tapOnFitImage - imageRectInView.topLeft
                                    val imageX =
                                        (tapOnScaledImage.x / imageRectInView.width) * imageIntrinsicSize.width
                                    val imageY =
                                        (tapOnScaledImage.y / imageRectInView.height) * imageIntrinsicSize.height
                                    val tappedImagePoint = Offset(imageX, imageY)
                                    val tappedRegion = tappableRegions.find { it.rect.contains(tappedImagePoint) }
                                    if (tappedRegion != null) {
                                        selectedRegion = tappedRegion
                                        dashboardRegion = tappedRegion
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            Image(
                painter = painter,
                contentDescription = "Campus Map",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )
            if (dashboardRegion != null && viewSize != IntSize.Zero) {
                val mainEntranceRegion = tappableRegions.find { it.name == "Main Entrance" }
                if (mainEntranceRegion != null) {
                    val imageIntrinsicSize = painter.intrinsicSize
                    if (imageIntrinsicSize == Size.Zero) return@Box

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
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "You are here",
                            tint = Color.Red,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }

        // Dashboard overlaid at the bottom
        dashboardRegion?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                LocationDashboard(
                    region = it,
                    isExpanded = isDashboardExpanded,
                    onToggleExpand = { isDashboardExpanded = !isDashboardExpanded }
                )
            }
        }
    }
}

