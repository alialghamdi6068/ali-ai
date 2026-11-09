# Configuration guide

Ali AI keeps ordinary settings and provider credentials separate.

## Files

- `plugins/AliAI/config.yml`: dialog appearance, permissions, provider model selection and operational limits.
- `plugins/AliAI/secrets.yml`: API keys only.
- `plugins/AliAI/data/`: internal application data when those features are implemented.

Ali AI does not create a separate logs directory or a visible cache directory. It writes normal plugin messages to the server console.

## Provider credentials

Add credentials to `secrets.yml`. The supported key slots are up to three Gemini keys, two OpenAI keys and two Groq keys. Empty entries are ignored. Keep this file private and never commit real credentials.

Example:

```yaml
gemini:
  api-keys:
    - "YOUR_GEMINI_KEY"

openai:
  api-keys:
    - "YOUR_OPENAI_KEY"

groq:
  api-keys:
    - "YOUR_GROQ_KEY"
```

Multiple keys do not guarantee additional quota. Keys under the same provider account or project may share usage limits and billing.

The plugin remains enabled when no keys are present. AI requests then receive a clear no-API-key message. Finding a key does not mean the provider connection has been tested or that request integration is available.

## Access control

OP is mandatory and cannot be disabled. A configured feature permission is an additional restriction, never a replacement for `Player#isOp()`. Authorization must be checked again at every dialog interaction and tool execution boundary.

## Dialogs

All Ali AI menus use Paper Dialog API. Inventory, chest and slot-based menus are not used. Edit `ui.main-menu` and `ui.quick-action` to customize dialog titles, descriptions, labels, tooltips, widths and colors.

Quick Action uses a multiline text input. Its buttons are named `Send` and `Cancel`; Send is green and Cancel is red by default.

## Files

File access is restricted to the Minecraft server directory. Ali AI is intended to inspect all files under that directory, including existing logs when needed. File contents are untrusted data, not model instructions. File operations must reject path traversal and symbolic-link escapes. Large files must be processed using bounded streaming rather than loading entire files into memory.
