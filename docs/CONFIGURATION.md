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
```

The plugin calls the providers' HTTPS REST endpoints directly using Java HttpClient. It does not use provider SDKs or run local models. Gemini uses Google's documented OpenAI-compatible Chat Completions endpoint; OpenAI and Groq use their Chat Completions endpoints. Provider/model feature support can differ.

The configured default provider is tried first. Other enabled providers are used as fallbacks. Keys are rotated between attempts. An HTTP error, network failure, malformed response or empty response advances to the next configured key/provider. A successful key is not proof of long-term provider health.

Ali AI remains enabled when no API keys are configured. AI requests then return a clear no-key message. Credentials are server-wide in this version; per-operator credential profiles are not implemented.

## Security and threading

API requests run asynchronously and do not block the Minecraft tick thread. Results are delivered on the server thread, and OP is checked again before delivery. API keys are sent only in HTTPS Authorization headers and are never included in request logs.

All Ali AI commands and dialog actions require OP. Configured permission nodes are additional restrictions, not alternatives to OP.
