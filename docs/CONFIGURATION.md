# Configuration guide

Ali AI keeps operator access mandatory. The `security.require-op` value is informational and reserved for compatibility; setting it to false does not disable the hard OP check.

## Quick Action dialog

Edit `ui.quick-action` to change the dialog title, description, input label and size, multiline behavior, button labels, tooltips and button colors. Colors use hexadecimal `#RRGGBB` values. Use `/ai reload` to apply changes.

## Main menu

Edit `ui.menu` to change the menu title, item names, descriptions, materials and slots. Materials must be valid Bukkit material names.

## Permissions

Every command and action first checks `Player#isOp()`. Permission nodes are an additional layer and never grant access to a non-OP player. The permission names are declared in `plugin.yml` and can be integrated with a permissions plugin while OP remains mandatory.

## AI providers

Provider credentials are read from environment variables named in `ai.providers.*.api-key-env`. Do not paste API keys into config.yml or share them in logs. The provider integration is not implemented in this foundation yet.

## Logs and files

Log discovery reads existing files only. Ali AI does not create server log files. File access must remain inside the Minecraft server directory; symbolic-link escapes and path traversal must be rejected by the file service.
