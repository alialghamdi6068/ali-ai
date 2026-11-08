# Ali AI

Ali AI is a Paper server-administration plugin for Minecraft servers.

## Target platform
- Paper 26.2
- Java 25
- Gradle

## Security contract
- Every `/ai` command and every Ali AI action requires an operator (`Player#isOp`).
- Permission checks are enforced at command entry points and again at action execution boundaries.
- Quick Action is available only to operators. Its dialog uses a green **Send** action and a red **Cancel** action.
- Server files are confined to the configured Minecraft server directory. Logs are read from existing files; Ali AI does not create server log files.

## Scope
Ali AI manages the Minecraft server only. It is not a hosting control panel or operating-system agent.

## Status
Initial repository bootstrap. Features are implemented incrementally with verification evidence; see the source tree and project documentation.
