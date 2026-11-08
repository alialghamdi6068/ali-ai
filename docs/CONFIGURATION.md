# Configuration guide

Ali AI's settings are grouped by responsibility in `config.yml`. Most presentation and behavior changes can be applied with `/ai reload` without restarting the server.

## Access control and permissions

OP is a mandatory security boundary. No config value or permission plugin can grant Ali AI access to a non-OP player. Each feature also checks its configured permission node. Nodes under `permissions` are registered with OP as their default, so operators receive them by default. Keep the OP check enabled.

## Quick Action dialog

Edit `ui.quick-action` to change the title, description, input label, width, maximum length, multiline settings, Send/Cancel labels, tooltips, and button colors. Colors use hexadecimal `#RRGGBB` values. Send defaults to green and Cancel defaults to red.

## Main menu

Edit `ui.menu` to change the menu title, item names, descriptions, materials, and slots. Materials must be valid Bukkit material names. Slots are limited to the 27-slot menu.

## AI providers

Provider credentials are read from environment variables named in `ai.providers.*.api-key-env`. Do not paste API keys into config.yml or share them in logs. Provider integration is not implemented in this foundation yet; the settings are prepared for the provider layer.

## Files and logs

File access is restricted to the Minecraft server directory. Existing logs are read from the configured directories; Ali AI does not create server log files. File tools must reject path traversal and symbolic-link escapes.

## Backups and execution

Backup retention and confirmation rules are grouped under `backups` and `execution`. Destructive or high-impact operations must require explicit confirmation and create a backup where supported.
