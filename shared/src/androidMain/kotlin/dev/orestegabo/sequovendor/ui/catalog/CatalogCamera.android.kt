package dev.orestegabo.sequovendor.ui.catalog

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import java.io.ByteArrayOutputStream

@Composable
internal actual fun rememberCatalogCamera(onResult: (ByteArray?) -> Unit): () -> Unit {
    val result = rememberUpdatedState(onResult)
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        val jpeg = bitmap?.let {
            ByteArrayOutputStream().use { output ->
                if (it.compress(Bitmap.CompressFormat.JPEG, 90, output)) output.toByteArray() else null
            }
        }
        result.value(jpeg)
    }
    return {
        try {
            camera.launch(null)
        } catch (_: ActivityNotFoundException) {
            result.value(null)
        } catch (_: SecurityException) {
            result.value(null)
        }
    }
}
