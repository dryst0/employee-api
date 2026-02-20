#!/usr/bin/env bash
# Runs once after the devcontainer is created.
# Installs project dependencies and generates editor configuration.
set -euo pipefail

# Fix volume mount ownership — Docker named volumes may be root-owned
# from prior sessions or devcontainer feature installs (which run as root).
# Excludes /var/lib/docker (managed by docker-in-docker feature).
mkdir -p /home/developer/.local/share/zed /home/developer/.config/zed /home/developer/.claude
sudo chown -R developer:developer \
    /home/developer/.m2 \
    /home/developer/.npm \
    /home/developer/.claude \
    /home/developer/.gemini \
    /home/developer/.copilot \
    /home/developer/.local/share/zed \
    /home/developer/.mcp-memory \
    /home/developer/.config/zed \
    /commandhistory

# Java dependencies
./mvnw dependency:go-offline -B

# Claude Code default settings — full access with credential deny rules,
# plus MCP servers so the ACP adapter (Zed agent panel) discovers them.
if [ ! -f /home/developer/.claude/settings.json ]; then
  cat > /home/developer/.claude/settings.json << 'CLAUDEEOF'
{
  "permissions": {
    "defaultMode": "plan",
    "allow": [
      "Bash",
      "Edit",
      "MultiEdit",
      "Write",
      "Read",
      "Glob",
      "Grep",
      "WebFetch",
      "WebSearch",
      "Task",
      "TodoWrite",
      "NotebookEdit",
      "mcp__*"
    ],
    "deny": [
      "Read(./.env)",
      "Read(./.env.*)",
      "Read(./.envrc)",
      "Edit(./.env)",
      "Edit(./.env.*)",
      "Edit(./.envrc)",
      "Write(./.env)",
      "Write(./.env.*)",
      "Write(./.envrc)",
      "Read(~/.claude.json)",
      "Read(~/.ssh/**)",
      "Read(~/.gitconfig)"
    ]
  },
  "mcpServers": {
    "filesystem": {
      "command": "mcp-server-filesystem",
      "args": ["/"]
    },
    "memory": {
      "command": "mcp-server-memory",
      "args": [],
      "env": {
        "MEMORY_FILE_PATH": "/home/developer/.mcp-memory/mcp_memory.json"
      }
    },
    "git": {
      "command": "mcp-server-git",
      "args": ["--repository", "/workspace"]
    },
    "fetch": {
      "command": "mcp-server-fetch",
      "args": []
    }
  }
}
CLAUDEEOF
fi

# Zed configuration (project-level, gitignored)
cat > /home/developer/.config/zed/settings.json << 'ZEDEOF'
{
  "auto_install_extensions": {
    "java": true,
    "html": true
  },
  "agent_servers": {
    "gemini": {
      "ignore_system_version": false
    }
  }
}
ZEDEOF
