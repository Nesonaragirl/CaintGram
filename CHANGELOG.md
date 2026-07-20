# FluxGram Changelog

All notable changes to FluxGram are tracked here.

## 0.1.0 - dev (unreleased)

### Plugins
- Added `manifest.json` plugin manifest format (Name, Version, Author, Description, ApiVersion, Entrypoint, Permissions, Containers, Homepage, Icon, Tags, UpdateUrl)
- Added `PluginManifest` parser with validation for required fields (Name, Version, Entrypoint)
- Added `PluginManager`: scans `flux_plugins/`, reads each plugin's manifest, loads its Entrypoint script into an isolated Lua state
- Wired plugin loading into app startup via `Flux.init()`
- Added zip/folder import for plugins (`Import from File` in FluxGram Preferences)
- `PluginsActivity` now lists installed plugins with per-plugin visibility toggles
- Added retry queue for `Flux.UI` components (buttons, menu items, containers) that attach before an Activity is available, so they're no longer silently dropped

### FluxGram Preferences
- Added FluxGram Preferences screen under Settings
- Modernized layout with real Telegram-style switch toggles
- Removed the standalone Plugins on/off row
- Removed the Appearance section
- Wired up Changelog, GitHub Repository, and About FluxGram
- Renamed "Check for Updates" to "Versions", showing the current app version

### Build
- Fixed R8 release build failure caused by missing `javax.script` classes referenced by the LuaJ dependency
- Added GitHub Actions CI workflow to build FluxGram on push
