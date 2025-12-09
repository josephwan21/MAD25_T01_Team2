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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize

// Data class to hold information about a tappable area on the map
data class TappableRegion(val name: String, val rect: Rect)

// A sample list of tappable regions.
// You need to replace these with the actual coordinates from your map image.
// You can use an image editor (like Paint or GIMP) to find the pixel coordinates (left, top) and dimensions (width, height) of your regions.
private val tappableRegions = listOf(
    // Example: TappableRegion("Block A", Rect(left=100f, top=200f, right=300f, bottom=400f))
    TappableRegion("Main Building", Rect(150f, 250f, 450f, 550f))
    // Add more regions here
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
                    scale = (scale * zoom).coerceIn(1f, 3f) // Clamp zoom level
                    offset += pan
                    // TODO: You could add logic here to prevent panning the image off-screen
                }
            }
            .pointerInput(Unit) {
                 detectTapGestures { tapOffset ->
                    val imageIntrinsicSize = painter.intrinsicSize
                    if (viewSize == IntSize.Zero) return@detectTapGestures

                    // Convert tap coordinates from screen to image space
                    val pivot = Offset(viewSize.width / 2f, viewSize.height / 2f)
                    val transformedTap = ((tapOffset - pivot - offset) / scale) + pivot

                    val imageX = (transformedTap.x / viewSize.width) * imageIntrinsicSize.width
                    val imageY = (transformedTap.y / viewSize.height) * imageIntrinsicSize.height

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
