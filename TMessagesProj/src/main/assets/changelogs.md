# Changelog

## Plugins
- Added manifest.json plugin manifest format
- Added PluginManifest parser with validation
- Added PluginManager: scans caint_plugins/, loads each plugin's Entrypoint script
- Wired plugin loading into app startup
- Added zip/folder import for plugins
- My Plugins now lists installed plugins with visibility toggles
- Added retry queue so UI components attach reliably

## Caint Preferences
- Added Caint Preferences screen under Settings
- Modernized layout with real switch toggles
- Removed the standalone Plugins on/off row
- Removed the Appearance section
- Wired up Changelog, GitHub Repository, and About Caint
- Renamed "Check for Updates" to "Versions"

## Build
- Fixed R8 release build failure from the LuaJ dependency
- Added GitHub Actions CI workflow
