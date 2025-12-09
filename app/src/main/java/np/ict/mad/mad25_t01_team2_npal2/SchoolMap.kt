package np.ict.mad.mad25_t01_team2_npal2

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

// Data class to hold information about a tappable area on the map
data class TappableRegion(val name: String, val rect: Rect)


private val tappableRegions = listOf(
    TappableRegion("Main Entrance", Rect(980f, 1666f, 1110f, 1777f)),
    TappableRegion("Convention Centre", Rect(820f, 1080f, 930f, 1200f))
)

@Composable
fun SchoolMap() {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var selectedRegion by remember { mutableStateOf<TappableRegion?>(null) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    val painter = painterResource(id = R.drawable.campus_map_1)

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
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 4f) // Clamp zoom level
                    offset += pan

                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val imageIntrinsicSize = painter.intrinsicSize
                    if (viewSize == IntSize.Zero || imageIntrinsicSize == Size.Zero) return@detectTapGestures

                    // Reverse the zoom/pan transformations to find the tap position on the un-transformed Box
                    val pivot = Offset(viewSize.width / 2f, viewSize.height / 2f)
                    val transformedTap = ((tapOffset - pivot - offset) / scale) + pivot

                    // Calculate the actual bounds of the image within the Box, accounting for ContentScale.Fit
                    val viewAspectRatio = viewSize.width.toFloat() / viewSize.height.toFloat()
                    val imageAspectRatio = imageIntrinsicSize.width / imageIntrinsicSize.height

                    val scaledImageWidth: Float
                    val scaledImageHeight: Float
                    if (imageAspectRatio > viewAspectRatio) {
                        // Image is wider than the view, so it's scaled to fit the width.
                        scaledImageWidth = viewSize.width.toFloat()
                        scaledImageHeight = scaledImageWidth / imageAspectRatio
                    } else {
                        // Image is taller than or same aspect ratio as the view, so it's scaled to fit the height.
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

                    // Check if the tap was inside the image bounds
                    if (!imageRectInView.contains(transformedTap)) {
                        // Tap was on the letterbox area, do nothing
                        return@detectTapGestures
                    }

                    // Convert the tap position from Box coordinates to original image coordinates
                    val tapOnScaledImage = transformedTap - imageRectInView.topLeft

                    val imageX = (tapOnScaledImage.x / imageRectInView.width) * imageIntrinsicSize.width
                    val imageY = (tapOnScaledImage.y / imageRectInView.height) * imageIntrinsicSize.height

                    val tappedImagePoint = Offset(imageX, imageY)

                    // Find which region was tapped
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
