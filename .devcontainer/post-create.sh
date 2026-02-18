#!/usr/bin/env bash
# Runs once after the devcontainer is created.
# Installs project dependencies and generates editor configuration.
set -euo pipefail

# Fix volume mount ownership — Docker named volumes may be root-owned
# from prior sessions or devcontainer feature installs (which run as root).
# Excludes /var/lib/docker (managed by docker-in-docker feature).
mkdir -p /home/developer/.local/share/zed
sudo chown -R developer:developer \
    /home/developer/.m2 \
    /home/developer/.npm \
    /home/developer/.claude \
    /home/developer/.gemini \
    /home/developer/.copilot \
    /home/developer/.local/share/zed \
    /home/developer/.mcp-memory \
    /commandhistory

# Java dependencies
./mvnw dependency:go-offline -B

# Claude Code default settings — plan mode start + all tools pre-approved
if [ ! -f ~/.claude/settings.json ]; then
  cat > ~/.claude/settings.json << 'CLAUDEEOF'
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
    ]
  }
}
CLAUDEEOF
fi

# Zed configuration (project-level, gitignored)
mkdir -p /workspace/.zed
cat > /workspace/.zed/settings.json << 'ZEDEOF'
{
  "auto_install_extensions": {
    "java": true,
    "toml": true,
    "dockerfile": true,
    "sql": true
  },
  "agent_servers": {
    "claude": {
      "env": {
        "CLAUDE_CODE_EXECUTABLE": "claude"
      }
    },
    "gemini": {
      "ignore_system_version": false
    }
  },
  "context_servers": {
    "filesystem": {
      "command": "mcp-server-filesystem",
      "args": ["/workspace"]
    },
    "memory": {
      "command": "mcp-server-memory",
      "args": [],
      "env": { "MEMORY_FILE_PATH": "/home/developer/.mcp-memory/memory.json" }
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
ZEDEOF
