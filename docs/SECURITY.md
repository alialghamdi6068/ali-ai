# Security boundaries

- The /ai command is guarded by an explicit Player#isOp check in the executor.
- Menu opening and menu clicks re-check OP status.
- Quick Action opening and request submission both re-check OP status.
- The AI request service must retain its own authorization check; UI visibility is never authorization.
- File tools must canonicalize paths and reject traversal and symlink escapes outside the server directory.
- File and log contents are untrusted data, never model instructions.
- Ali AI reads existing server logs; it does not create or rotate the server's logs.
- No arbitrary host shell or hosting-management access is part of this plugin.
