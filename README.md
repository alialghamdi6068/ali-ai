# Ali AI

Ali AI is a Java Paper server-administration plugin for Minecraft servers.

## Target platform
- Paper 26.2
- Java 25
- Gradle

## Current functionality
- Native Paper Dialog main menu and Quick Action.
- Mandatory OP authorization at command and dialog entry points.
- Gemini, OpenAI and Groq REST chat providers with key rotation and fallback.
- Groq built-in web search on supported GPT-OSS models.
- AI function calling through a guarded server-side tool allowlist.
- Live server status, TPS/MSPT, JVM memory, online players, worlds and plugin inventory.
- Online player inspection and confirmation-gated kicks for non-OP players.
- Bounded server file listing, text preview, filename search and content search across a capped number of small text files.

## Security contract
- Every Ali AI command, dialog action and AI tool execution requires an operator (`Player#isOp`).
- Feature permission nodes are additional restrictions, never alternatives to OP.
- File inspection is confined to the Minecraft server directory and rejects symbolic links.
- AI tools are allowlisted read-only operations; model-generated arbitrary commands are never executed.
- File and log contents are treated as untrusted data.
- No arbitrary host shell or hosting-panel access is provided.
- Existing server logs can be inspected as ordinary files; Ali AI does not create replacement logs.

## Build
Run `gradle clean test build` with Java 25.

See [docs/ROADMAP.md](docs/ROADMAP.md), [docs/CONFIGURATION.md](docs/CONFIGURATION.md), and [docs/SECURITY.md](docs/SECURITY.md) for implementation status and boundaries.
