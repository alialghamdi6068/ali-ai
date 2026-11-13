# Ali AI

Ali AI is a Java Paper server-administration plugin for Minecraft servers.

## Target platform
- Paper 26.2
- Java 25
- Gradle

## Current functionality
- Paper Dialog main menu and Quick Action.
- OP-only command access.
- Gemini, OpenAI and Groq REST chat providers.
- Groq built-in web search on supported GPT-OSS models.
- Live server, player, world and plugin information.
- Bounded read-only server file inspection.

## Security contract
- Every Ali AI command and action requires an operator (`Player#isOp`).
- Feature permission nodes are additional restrictions, never alternatives to OP.
- File inspection is confined to the Minecraft server directory and rejects symbolic links.
- No arbitrary host shell or hosting-panel access is provided.
- Existing server logs can be inspected as ordinary files; Ali AI does not create replacement logs.

## Build
Run `gradle clean test build` with Java 25.

See [docs/ROADMAP.md](docs/ROADMAP.md), [docs/CONFIGURATION.md](docs/CONFIGURATION.md), and [docs/SECURITY.md](docs/SECURITY.md) for implementation status and boundaries.
