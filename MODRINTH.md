# CallXPEconomy for Modrinth

## Project details

- **Type:** Plugin
- **Loader:** Paper
- **Environment:** Server-side only
- **Minecraft:** 1.21.4
- **Categories:** Economy, Utility, Server
- **License:** MIT
- **Source:** https://github.com/calladot/CallXPEconomy
- **Issues:** https://github.com/calladot/CallXPEconomy/issues

## Short description

```text
Use Minecraft XP as a Vault economy balance on Paper servers.
```

## Description

```markdown
CallXPEconomy lets Vault-compatible plugins use player XP as currency on Paper servers.

- Syncs balances with player XP.
- Supports SQLite and YAML storage.
- Requires Paper 1.21.4 and Java 21.
- Vault is optional, but required for the Economy API.
```

## Release 0.1.1

- **File:** `CallXPEconomy-0.1.1.jar`
- **Version type:** Release
- **Dependency:** Vault (optional)

```markdown
Initial release: Vault economy provider backed by player XP, with SQLite and YAML storage.
```

Before publishing, upload the JAR from `build/libs/`, add an icon, and test with Paper 1.21.4 and Vault.