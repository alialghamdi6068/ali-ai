# Ali AI implementation roadmap

## Foundation
- Paper plugin bootstrap and mandatory OP authorization.
- Native Paper Dialog main menu and Quick Action.
- Re-check OP when opening dialogs, handling dialog actions and executing tools.
- Provider placeholder fails closed rather than pretending to execute AI requests.

## Product implementation
1. Real AI provider integration, secret-safe configuration, timeouts, retries and provider health.
2. Typed tool registry, multi-step planner, per-operator conversation context and approval gates.
3. Server Intelligence: live server state, player counts, versions and available performance metrics.
4. File Intelligence: server-root-confined discovery, indexing, streaming search, inspection and safe file operations.
5. Plugin Intelligence: compatibility/dependency inspection and safe install/update/remove workflows.
6. Player Management and World Management tools.
7. Performance Analyzer based on measured server/JVM data.
8. Security Center for permission, configuration and file-risk inspection.
9. Task Manager for currently running tasks only; no permanent task history by default.
10. Settings and configurable Arabic/English Dialog presentation.

Ali AI manages Minecraft server state only. Starting or stopping the host JVM requires an external process supervisor and is not an in-plugin capability.
