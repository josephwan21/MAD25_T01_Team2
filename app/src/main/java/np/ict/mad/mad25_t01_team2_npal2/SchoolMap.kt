package np.ict.mad.mad25_t01_team2_npal2

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize

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

// Updated data class to include GPS information for each region
data class TappableRegion(val name: String, val rect: Rect, val gpsBox: GpsBoundingBox)


// Logic 2: Associate GPS bounding boxes with each tappable region.
// IMPORTANT: These GPS coordinates are placeholders and must be replaced with the
// actual coordinates for your campus map.
private val tappableRegions = listOf(
    TappableRegion("Main Entrance", Rect(973f, 1655f, 1106f, 1783f), GpsBoundingBox(GpsCoordinates(1.378, 103.849), GpsCoordinates(1.376, 103.847))),
    TappableRegion("Convention Centre", Rect(813f, 1061f, 941f, 1212f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
    TappableRegion("Atrium/Library", Rect(1100f, 1396f, 1194f, 1542f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
    TappableRegion("Field", Rect(1561f, 1512f, 1856f, 1644f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
    TappableRegion("Back Entrance", Rect(1779f, 1300f, 1842f, 1365f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
    TappableRegion("SIT", Rect(1167f, 827f, 1257f, 942f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
    TappableRegion("Makan Place", Rect(599f, 873f, 719f, 926f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
    TappableRegion("Food Club", Rect(1251f, 1125f, 1357f, 1178f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
    TappableRegion("Munch", Rect(605f, 1455f, 723f, 1500f), GpsBoundingBox(GpsCoordinates(0.0, 0.0), GpsCoordinates(0.0, 0.0))),
)

@Composable
fun SchoolMap() {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var userInteracted by remember { mutableStateOf(false) }

    var selectedRegion by remember { mutableStateOf<TappableRegion?>(null) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    val painter = painterResource(id = R.drawable.campus_map_1)

    // Logic 3: Simulate fetching the user's current GPS location.
    // In a real app, you would replace this with a call to the device's location services.
    val currentUserLocation = GpsCoordinates(1.377, 103.848) // Simulated location for "Main Entrance"


    LaunchedEffect(viewSize) {
        if (viewSize == IntSize.Zero || userInteracted) return@LaunchedEffect

        // Logic 4: Determine the target region based on the simulated GPS location.
        // If the user's location is within one of the defined GPS bounding boxes, that region becomes the target.
        // If not, targetRegion will be null, and the map will show the default, full view.
        val targetRegion = tappableRegions.find { it.gpsBox.contains(currentUserLocation) }

        if (targetRegion != null) {
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
            val newOffset = (viewCenter - targetCenterInFitImage) * newScale

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { viewSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    userInteracted = true

                    scale = (scale * zoom).coerceIn(0.5f, 5f)

                    val newOffset = offset + pan

                    // Calculate bounds for panning
                    val maxOffsetX = (viewSize.width * scale - viewSize.width) / 2
                    val maxOffsetY = (viewSize.height * scale - viewSize.height) / 2

                    // Clamp the offset to prevent panning off-screen
                    offset = Offset(
                        newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                        newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                    )
                }
                detectTapGestures { tapOffset ->
                    val imageIntrinsicSize = painter.intrinsicSize
                    if (viewSize == IntSize.Zero || imageIntrinsicSize == Size.Zero) return@detectTapGestures

                    val viewCenter = Offset(viewSize.width / 2f, viewSize.height / 2f)
                    val tapOnFitImage = viewCenter + (tapOffset - viewCenter - offset) / scale

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

                    if (!imageRectInView.contains(tapOnFitImage)) {
                        return@detectTapGestures
                    }

                    val tapOnScaledImage = tapOnFitImage - imageRectInView.topLeft
                    val imageX = (tapOnScaledImage.x / imageRectInView.width) * imageIntrinsicSize.width
                    val imageY = (tapOnScaledImage.y / imageRectInView.height) * imageIntrinsicSize.height
                    val tappedImagePoint = Offset(imageX, imageY)

                    selectedRegion = tappableRegions.find { it.rect.contains(tappedImagePoint) }
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
    }
}
