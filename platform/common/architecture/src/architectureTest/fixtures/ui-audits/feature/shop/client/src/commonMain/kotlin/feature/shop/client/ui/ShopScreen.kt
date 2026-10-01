package feature.shop.client.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import platform.design.BackAgainPreviewFrame
import platform.design.BackAgainSpacing
import platform.design.BackAgainTheme

@Composable
internal fun ShopScreenContent(state: SaveState) {
    Column(modifier = Modifier.padding(BackAgainSpacing.md)) {
        Text(text = "Shop", color = BackAgainTheme.colors.onSurface)
    }
}

@Preview
@Composable
internal fun ShopScreenCompactWidthPreview() {
    BackAgainPreviewFrame(width = 360.dp) {
        ShopScreenContent(state = SaveState())
    }
}
