package dev.orestegabo.sequovendor.ui.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun rememberCatalogCamera(onResult: (ByteArray?) -> Unit): () -> Unit {
    val result = rememberUpdatedState(onResult)
    val delegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>,
            ) {
                val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                val data = image?.let { UIImageJPEGRepresentation(it, 0.75) }
                val bytes = data?.takeIf { it.length in 1uL..(8uL * 1024uL * 1024uL) }
                    ?.let { it.bytes?.readBytes(it.length.toInt()) }
                picker.dismissViewControllerAnimated(true, completion = null)
                result.value(bytes)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, completion = null)
                result.value(null)
            }
        }
    }
    return {
        val source = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        var presenter = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (presenter?.presentedViewController != null) presenter = presenter.presentedViewController
        if (presenter == null || !UIImagePickerController.isSourceTypeAvailable(source)) {
            result.value(null)
        } else {
            val camera = UIImagePickerController()
            camera.sourceType = source
            camera.delegate = delegate
            presenter.presentViewController(camera, animated = true, completion = null)
        }
    }
}
