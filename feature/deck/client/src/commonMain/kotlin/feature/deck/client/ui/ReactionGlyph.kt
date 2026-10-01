package feature.deck.client.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import feature.live.Reaction
import platform.design.BackAgainTheme

/**
 * A reaction, drawn as an icon. Compose for Web renders its own text and ships no emoji font, so an
 * emoji would draw as an empty box.
 */
@Composable
internal fun ReactionGlyph(
    reaction: Reaction,
    modifier: Modifier = Modifier,
) {
    val palette = BackAgainTheme.codeColors
    val (icon, tint) = when (reaction) {
        Reaction.Applause -> Icons.Filled.ThumbUp to palette.function
        Reaction.Love -> Icons.Filled.Favorite to BackAgainTheme.colors.error
        Reaction.Party -> Icons.Filled.Celebration to palette.annotation
        Reaction.Rocket -> Icons.Filled.RocketLaunch to BackAgainTheme.colors.accent
        Reaction.Idea -> Icons.Filled.Lightbulb to palette.number
    }
    Icon(
        imageVector = icon,
        contentDescription = reaction.name,
        tint = tint,
        modifier = modifier,
    )
}
