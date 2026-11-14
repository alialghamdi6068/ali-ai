# Development roadmap

## Implemented foundation
- Native Paper Dialog main menu and Quick Action dialog.
- Mandatory OP authorization at command, dialog and AI tool execution boundaries.
- Server-local provider credential file with bounded key slots.
- Direct asynchronous REST chat requests for Gemini, OpenAI and Groq.
- Configurable models, provider enablement, preferred provider, key rotation and provider fallback.
- Explicit `/ai search <query>` using Groq Browser Search on supported GPT-OSS models.
- Guarded read-only AI tools for live server status, performance, online players, loaded worlds and installed plugins.
- Guarded file tools for directory listing, bounded text preview, filename search and bounded text-content search.
- Online player inspection and confirmation-gated kick actions for non-OP players; each confirmation re-checks OP, feature permission and target eligibility.
- Loaded-world inspection and confirmation-gated world-time and weather changes; confirmations re-check OP, feature permission and world availability.
- File inspection is confined to the server root, rejects symbolic links and caps read/scan sizes.
- In-memory task executor foundation; no task history is persisted.
- Basic OP/plugin-count security snapshot with its limitations stated.
- Regression tests for file path confinement, preview size limits, text search and symbolic-link rejection.

## Still to implement
- Reliable conversation history and per-operator context.
- Multi-round agent planning and tool execution with bounded steps.
- Natural-language execution of administrative changes with per-tool authorization and confirmation flows.
- File creation, modification, deletion, moving, copying, archive handling and durable content indexing.
- Remaining player actions (ban/unban/whitelist/permissions/inventory) with confirmation flows.
- Remaining world actions (load/unload/create/delete/gamerules) with confirmation flows.
- Plugin installation/update/removal with trusted-source and compatibility verification.
- MSPT and plugin-level performance attribution.
- Deep security checks for plugin permissions, configuration risks and suspicious files.
- Complete configurable Dialog navigation for all ten administration sections.
- Provider-specific capability discovery, usage reporting and structured source-link extraction.
- Paper integration tests and mocked-provider contract tests.

A successful provider text response is not an administrative action. Administrative changes must be backed by trusted Java tools, explicit authorization and confirmation where required; the model must never execute arbitrary server commands or host shell commands.
