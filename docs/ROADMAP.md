# Development roadmap

## Implemented foundation

- Native Paper Dialog main menu and Quick Action dialog.
- Mandatory OP authorization checks at command and dialog entry points.
- Server-local provider credential file with bounded key slots.
- Direct asynchronous REST chat requests for Gemini, OpenAI and Groq.
- Configurable models, provider enablement, preferred provider, key rotation and provider fallback.
- Clear no-key and no-enabled-provider responses.
- Explicit `/ai search <query>` using Groq Browser Search on supported GPT-OSS models.
- No separate plugin logs or visible cache directory.

## Still to implement

- Reliable conversation history and per-operator context.
- Broader internet search support across providers and structured source-link extraction; current live search is Groq-only.
- Tool calling with a strict server-side allowlist and permission checks per tool.
- Server intelligence and diagnostics.
- File indexing and secure file operations.
- Plugin, player and world management.
- Performance and security analysis.
- Provider-specific capability handling, model discovery and usage reporting.
- Automated tests with mocked HTTP responses and Paper integration tests.

A provider's ability to return text does not mean it can safely administer the server. Administrative tools must be implemented and authorized separately.
