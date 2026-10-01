package platform.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * The corner language.
 *
 * Like [BackAgainSpacing], a bare object rather than a theme-scoped holder: shape is part of the
 * identity's constant vocabulary, not something a palette swap should change.
 *
 * Three steps, chosen by the size of the thing being shaped rather than by taste — a chip and a
 * dialog should not share a radius, because the same absolute radius reads as much rounder on a
 * small element.
 */
object BackAgainShapes {
    /** Chips, badges, inputs, buttons. */
    val small: Shape = RoundedCornerShape(4.dp)
    /** Cards and list rows. */
    val medium: Shape = RoundedCornerShape(8.dp)
    /** Sheets, dialogs, and other full surfaces. */
    val large: Shape = RoundedCornerShape(12.dp)
}
