package platform.design

import kotlinx.browser.localStorage

internal actual object ThemePreference {

    // index.html reads the same key, so the boot screen already matches the palette the app draws.
    private const val KEY = "backagain.theme"

    actual fun load(): Boolean? = when (localStorage.getItem(KEY)) {
        "dark" -> true
        "light" -> false
        else -> null
    }

    actual fun save(dark: Boolean) {
        localStorage.setItem(KEY, if (dark) "dark" else "light")
    }
}
