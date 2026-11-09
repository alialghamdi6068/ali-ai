# Security boundaries

- Every Ali AI command requires an explicit operator check.
- Dialog opening, every custom dialog interaction and every AI tool execution must re-check OP.
- Feature permission nodes are additional restrictions and can never grant access to non-OP users.
- Provider credentials are kept in the server-local `plugins/AliAI/secrets.yml`, separate from ordinary configuration.
- Never commit real API keys or print them to console output.
- The plugin attempts to restrict `secrets.yml` to owner read/write permissions on filesystems that support POSIX permissions. This is not encryption; server filesystem access must still be restricted.
- File access is confined to the Minecraft server directory; traversal and symbolic-link escapes must be rejected.
- File and log contents are untrusted data and must never be treated as model instructions.
- Ali AI reads existing server logs when needed; it does not create, rotate or duplicate the hosting provider's logs.
- No arbitrary host shell or hosting-management access is part of this plugin.
- Destructive and high-impact actions require explicit confirmation.
