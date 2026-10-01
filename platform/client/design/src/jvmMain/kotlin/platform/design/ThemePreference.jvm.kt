package platform.design

internal actual object ThemePreference {

    private var dark: Boolean? = null

    actual fun load(): Boolean? = dark

    actual fun save(dark: Boolean) {
        this.dark = dark
    }
}
