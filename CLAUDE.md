# CLAUDE.md

Claude Code-specific instructions for this repository. Agent-agnostic project knowledge is in @AGENTS.md.

## Workflow

- **Plan before executing**: Always explore the codebase and present a plan for approval before making any code changes. Never edit files immediately. Understand the scope, identify affected files, and agree on the approach first.

## Tool Selection Strategy

### ACP Tools (Default for Code Changes)
Use `mcp__acp__Read`, `mcp__acp__Write`, `mcp__acp__Edit`, `mcp__acp__Bash` for:
- All code changes (diff preview + approval workflow)
- Git operations, build/test/run commands
- Any operation requiring user review

### Filesystem MCP Tools (Read-Only Exploration)
Use `mcp__filesystem__*` tools for:
- Bulk file reads (`read_multiple_files` for 5+ files)
- Directory exploration (`directory_tree`, `list_directory`)
- File metadata queries (`get_file_info`)

### Decision Rule
Will this operation change files or require approval?
- **Yes** → ACP tools
- **No** (read-only) → Filesystem MCP tools
