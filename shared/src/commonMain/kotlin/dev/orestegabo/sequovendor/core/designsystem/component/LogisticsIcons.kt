package dev.orestegabo.sequovendor.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Package2: ImageVector
    get() {
        if (_package2 != null) {
            return _package2!!
        }
        _package2 = ImageVector.Builder(
            name = "Filled.Package2",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(12f, 3f)
                lineTo(20.5f, 7.6f)
                lineTo(20.5f, 16.4f)
                lineTo(12f, 21f)
                lineTo(3.5f, 16.4f)
                lineTo(3.5f, 7.6f)
                close()
                moveTo(3.5f, 7.6f)
                lineTo(12f, 12.2f)
                lineTo(20.5f, 7.6f)
                moveTo(12f, 12.2f)
                verticalLineTo(21f)
                moveTo(7.8f, 5.3f)
                lineTo(16.4f, 9.9f)
            }
        }.build()
        return _package2!!
    }

private var _package2: ImageVector? = null
