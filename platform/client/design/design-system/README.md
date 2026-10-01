# Design system

Tokens and primitives for the deck, in `:platform:client:design` (`platform.design`). Feature code
reads tokens through `BackAgainTheme` and never restates a colour, size or typeface.

| Token | Where | Notes |
| --- | --- | --- |
| Colours | `BackAgainColors` | Semantic roles. The deck runs in `Dark`, because it is projected. |
| UI type | `BackAgainTypography` | Five roles for chrome: display, title, body, label, caption. |
| Slide type | `SlideTypography` | Hero, title, body, caption and code, sized for a 1280 × 720 dp slide; `compactFrom` for portrait phones. |
| Code | `CodeColors` | Syntax colours for code on slides. |
| Typefaces | `BackAgainFonts` | JetBrains Mono for everything, bundled as a variable font under `composeResources/font`. |
| Spacing, shapes | `BackAgainSpacing`, `BackAgainShapes` | Plain objects: one density, three corner radii. |
| Viewport | `BackAgainViewport` | One question, `isCompact`, for choosing between layouts. |

Both typefaces are licensed under the SIL Open Font License; the licences are in [fonts](fonts).

The module has no navigation or DI, so any feature can use it
(`DesignSystemRules.noNavigationOrDi`).
