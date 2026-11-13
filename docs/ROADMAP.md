# Development roadmap

## Implemented foundation
- Native Paper Dialog main menu and Quick Action dialog.
- Mandatory OP authorization at command and dialog entry points.
- Server-local provider credential file with bounded key slots.
- Direct asynchronous REST chat requests for Gemini, OpenAI and Groq.
- Configurable models, provider enablement, preferred provider, key rotation and provider fallback.
- Explicit `/ai search <query>` using Groq Browser Search on supported GPT-OSS models.
- Live server status, online player details, loaded world summaries and installed plugin inventory.
- Bounded read-only file listing, text preview and filename search, confined to the server root with symbolic-link rejection.
- In-memory task executor foundation; no task history is persisted.
- Basic OP/plugin-count security snapshot with its limitations stated.

## Still to implement
- Reliable conversation history and per-operator context.
- Provider-specific tool calling and a strict server-side tool allowlist.
- Natural-language execution of administrative tools with per-tool authorization and confirmations.
- File creation, modification, deletion, moving, copying, archive handling and content indexing.
- Player actions (kick/ban/whitelist/permissions/inventory) with confirmation flows.
- World actions (load/unload/create/delete/time/weather/gamerules) with confirmation flows.
- Plugin installation/update/removal with trusted-source and compatibility verification.
- MSPT and plugin-level performance attribution.
- Deep security checks for plugin permissions, configuration risks and suspicious files.
- Complete configurable Dialog navigation for all ten administration sections.
- Provider-specific capability discovery, usage reporting and structured source-link extraction.
- Automated unit, Paper integration and mocked-provider tests.

A successful provider text response is not an administrative action. The remaining action systems must be backed by trusted Java tools and authorization checks; the model must never execute arbitrary server commands or host shell commands.
