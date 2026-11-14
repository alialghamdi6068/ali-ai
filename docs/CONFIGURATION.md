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

The plugin calls provider HTTPS REST endpoints directly using Java HttpClient. It does not use provider SDKs or run local models. Gemini uses Google's OpenAI-compatible Chat Completions endpoint; OpenAI and Groq use their Chat Completions endpoints. Tool calling is an OpenAI-compatible capability, but provider/model support can differ. If a provider rejects a tool-enabled request, Ali AI falls back to the next configured provider/key.

The configured default provider is tried first. Other enabled providers are used as fallbacks. Keys are rotated between attempts. An HTTP error, network failure, malformed response or empty response advances to the next configured key/provider. A successful key is not proof of long-term provider health.

Ali AI remains enabled when no API keys are configured. AI requests then return a clear no-key message. Credentials are server-wide in this version; per-operator credential profiles are not implemented.

## Available AI tools

When `ai.tools.enabled` is true, the model may request these read-only operations:
- Read server status and performance metrics.
- List online players, loaded worlds and installed plugins.
- List server files, read small text files, search filenames and search text in a bounded number of small files.

Each tool execution re-checks OP and its configured feature permission. File tools remain confined to the server directory. File and log contents are returned as untrusted data and must never be treated as instructions. The current implementation does not allow the model to execute arbitrary console commands or make destructive changes.

`max-calls-per-round` limits how many function calls can be accepted from one provider response (1–8; default 4). File-content search is bounded and is not a full-text index.

## Security and threading

API requests run asynchronously and do not block the Minecraft tick thread. Bukkit-backed tools are dispatched to the server thread. OP is checked again when a tool is executed and before the final response is delivered. API keys are sent only in HTTPS Authorization headers and are never included in request logs.

All Ali AI commands and dialog actions require OP. Configured permission nodes are additional restrictions, not alternatives to OP.

## Confirmation dialogs

Player kicks and world-time changes require an explicit Paper Dialog confirmation. The dialog text and button labels/tooltips can be customized under `ui.player-confirmation` and `ui.world-confirmation` in `config.yml`. Closing a dialog without choosing an action expires the pending request after two minutes. Each pending request is bound to the initiating operator and re-checks OP and its feature permission when confirmed.

World time uses Minecraft ticks from 0 through 23999: 0 is sunrise, 6000 noon, 12000 sunset and 18000 midnight. Time changes affect only a currently loaded world.

## Internet search

Use `/ai search <query>` for a live web search. This implementation uses Groq's built-in `browser_search` tool. It requires Groq enabled, a Groq API key, and one of these supported model IDs: `openai/gpt-oss-20b`, `openai/gpt-oss-120b`, or `openai/gpt-oss-safeguard-20b`. Other providers are not silently presented as having searched the web. If this configuration is missing, Ali AI explains what is required.

Groq's former Compound models are not used because they were decommissioned in September 2026. See [Groq Browser Search documentation](https://console.groq.com/docs/tool-use/built-in-tools/browser-search).
