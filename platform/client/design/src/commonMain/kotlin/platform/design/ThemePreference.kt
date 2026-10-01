package platform.design

/**
 * This device's choice between the palettes, kept across visits. `null` until the device makes
 * one, which leaves it following the system setting.
 */
internal expect object ThemePreference {
    fun load(): Boolean?

    fun save(dark: Boolean)
}
