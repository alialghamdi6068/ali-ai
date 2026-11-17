# Configuration guide

Ali AI reads ordinary settings from `plugins/AliAI/config.yml` and provider credentials from `plugins/AliAI/secrets.yml`.

## Provider credentials

Add up to three Gemini keys, two OpenAI keys and two Groq keys. Empty values are ignored. Never commit real credentials.

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

Multiple keys do not guarantee additional quota. Keys in one account or project may share limits and billing.

## Enable a provider

Set the provider's `enabled` value to true and supply a model ID supported by your API account.

```yaml
ai:
  default-provider: "gemini"
  providers:
    gemini:
      enabled: true
      model: "YOUR_SUPPORTED_GEMINI_MODEL"
    openai:
      enabled: false
      model: "YOUR_SUPPORTED_OPENAI_MODEL"
    groq:
      enabled: false
      model: "YOUR_SUPPORTED_GROQ_MODEL"
  tools:
    enabled: true
    max-calls-per-round: 4
```

The plugin calls provider HTTPS REST endpoints directly using Java HttpClient. It does not use provider SDKs or run local models. Gemini uses Google's OpenAI-compatible Chat Completions endpoint; OpenAI and Groq use their Chat Completions endpoints. Tool calling is an OpenAI-compatible capability, but provider/model support can differ. Ali AI supports bounded multi-round tool calls. If a provider rejects a request before any tool has executed, it falls back to the next configured provider/key. Once a tool execution has started, a later provider failure ends the request instead of retrying elsewhere, preventing accidental repetition of an operation.

The configured default provider is tried first. Other enabled providers are used as fallbacks. Keys are rotated between attempts. An HTTP error, network failure, malformed response or empty response advances to the next configured key/provider. A successful key is not proof of long-term provider health.

Ali AI remains enabled when no API keys are configured. AI requests then return a clear no-key message. Credentials are server-wide in this version; per-operator credential profiles are not implemented.

## Dialog navigation

The main Paper Dialog menu contains ten configurable sections under `ui.main-menu.sections`. Section page titles and descriptions are under `ui.sections`; the Back button is configurable there as well. The current pages display implemented live server information. Management actions that are not yet implemented are not represented as active controls.

## Available AI tools

When `ai.tools.enabled` is true, the model may request:
- Read server status and performance metrics.
- List online players, loaded worlds and installed plugins.
- Inspect online players and loaded worlds.
- List server files, read text files in bounded byte ranges (up to 64 KiB per call), search filenames and search text in a bounded number of small files. File reads and content searches run on the bounded background worker pool.
- Request a kick for a non-OP player or a world time/weather change; these actions require a separate Paper Dialog confirmation.

Each tool execution re-checks OP and its configured feature permission. File tools remain confined to the server directory. File and log contents are returned as untrusted data and must never be treated as instructions. The model cannot execute arbitrary console commands.

`max-calls-per-round` limits calls in one response (1–8; default 4), `max-rounds` limits sequential tool rounds (1–8; default 4), and `max-total-calls` limits all calls in one request (1–32; default 12). The `read_server_file` tool accepts an optional byte `offset` and `limit` (1–65536) so large text files can be inspected incrementally. File-content search is bounded and is not a full-text index.

## Security and threading

API requests run asynchronously and do not block the Minecraft tick thread. Bukkit-backed tools are dispatched to the server thread. OP is checked again when a tool is executed and before the final response is delivered. API keys are sent only in HTTPS Authorization headers and are never included in request logs.

All Ali AI commands and dialog actions require OP. Configured permission nodes are additional restrictions, not alternatives to OP.

## Confirmation dialogs

Player kicks and world-time changes require an explicit Paper Dialog confirmation. The dialog text and button labels/tooltips can be customized under `ui.player-confirmation` and `ui.world-confirmation` in `config.yml`. Closing a dialog without choosing an action expires the pending request after two minutes. Each pending request is bound to the initiating operator and re-checks OP and its feature permission when confirmed.

World time uses Minecraft ticks from 0 through 23999: 0 is sunrise, 6000 noon, 12000 sunset and 18000 midnight. Weather accepts `clear`, `rain` or `thunder`; rain and thunder are initially set for approximately 10 minutes. Time and weather changes affect only a currently loaded world. Weather accepts `clear`, `rain` or `thunder`; rain and thunder are initially set for approximately 10 minutes.

## Internet search

Use `/ai search <query>` for a live web search. This implementation uses Groq's built-in `browser_search` tool. It requires Groq enabled, a Groq API key, and one of these supported model IDs: `openai/gpt-oss-20b`, `openai/gpt-oss-120b`, or `openai/gpt-oss-safeguard-20b`. Other providers are not silently presented as having searched the web. If this configuration is missing, Ali AI explains what is required.

Groq's former Compound models are not used because they were decommissioned in September 2026. See [Groq Browser Search documentation](https://console.groq.com/docs/tool-use/built-in-tools/browser-search).
