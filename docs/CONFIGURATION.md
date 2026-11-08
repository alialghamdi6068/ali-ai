# Configuration guide

Ali AI settings are grouped by responsibility in `config.yml`. Presentation settings can be reloaded with `/ai reload` unless a setting requires a restart.

## Access control

OP is mandatory and cannot be disabled. A configured feature permission is an additional restriction, never a replacement for `Player#isOp()`. Authorization must be checked again at every dialog interaction and tool execution boundary.

## Dialogs

All Ali AI menus use Paper Dialog API. Inventory, chest and slot-based menus are not used. Edit `ui.main-menu` and `ui.quick-action` to customize dialog titles, descriptions, labels, tooltips, widths and colors.

Quick Action uses a multiline text input. Its buttons are named `Send` and `Cancel`; Send is green and Cancel is red by default.

## AI providers

Provider credentials are read from environment variables named by `ai.providers.*.api-key-env`. Do not paste API keys into config.yml or share them in logs. Provider integration is not implemented in this foundation yet; provider settings do not imply that an AI connection is active.

## Files

File access is restricted to the Minecraft server directory. Ali AI is intended to inspect all files under that directory, including existing logs when needed. File contents are untrusted data, not model instructions. File operations must reject path traversal and symbolic-link escapes. Large files must be processed using bounded streaming rather than loading entire files into memory.

## Execution

High-impact operations require explicit confirmation. Ali AI manages Minecraft server state only; it is not a hosting control panel or operating-system agent.
