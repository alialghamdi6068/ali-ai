# Ali AI implementation roadmap

## Initial vertical slice
- Paper plugin bootstrap and /ai OP gate.
- Operator-only in-game control menu.
- Native Paper Dialog Quick Action with free-text input, green Send and red Cancel.
- Re-check OP at the dialog submission and request-service boundary.
- Provider placeholder fails closed instead of pretending to execute AI requests.

## Next slices
1. Provider abstraction, secret-safe configuration, timeouts, retries and provider health.
2. Typed tool registry and multi-step task planner with approval gates.
3. Server status and performance inspection tools.
4. Existing-log discovery, streaming search and stack-trace analysis.
5. Server-root-confined file indexing and safe file operations with diff/backup/rollback.
6. Plugin/config/world/player administration tools.
7. Build/test workspace for Java plugin projects.
8. Persistent task history, memory, audit trail and recovery.

Ali AI manages Minecraft server state only. Starting or stopping the host JVM requires an external process supervisor and is not an in-plugin capability.
