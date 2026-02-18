# ── Builder: Node.js tools ──
FROM node:22-slim AS node-tools
ENV NPM_CONFIG_PREFIX=/opt/node-tools
RUN npm install -g \
    @google/gemini-cli \
    @modelcontextprotocol/server-filesystem \
    @modelcontextprotocol/server-memory

# Pre-install Zed runtime dependencies so first launch is instant.
# Claude agent ACP adapter
RUN mkdir -p /opt/zed-data/external_agents/claude-agent-acp/0.17.1 \
    && cd /opt/zed-data/external_agents/claude-agent-acp/0.17.1 \
    && echo '{"dependencies":{"@zed-industries/claude-agent-acp":"0.17.1"}}' > package.json \
    && npm install --no-fund --no-audit
# Language servers (JSON, YAML) and Prettier
RUN mkdir -p /opt/zed-data/languages/json-language-server \
    && cd /opt/zed-data/languages/json-language-server \
    && echo '{"dependencies":{"vscode-langservers-extracted":"4.10.0"}}' > package.json \
    && npm install --no-fund --no-audit \
    && mkdir -p /opt/zed-data/languages/yaml-language-server \
    && cd /opt/zed-data/languages/yaml-language-server \
    && echo '{"dependencies":{"yaml-language-server":"1.20.0"}}' > package.json \
    && npm install --no-fund --no-audit \
    && mkdir -p /opt/zed-data/prettier \
    && cd /opt/zed-data/prettier \
    && echo '{"dependencies":{"prettier":"3.8.1"}}' > package.json \
    && npm install --no-fund --no-audit

# ── Builder: Python tools ──
FROM python:3.12-slim AS python-tools
RUN pip install --prefix /opt/python-tools --no-cache-dir \
    mcp-server-git \
    mcp-server-fetch

# ── Stage 1: System dependencies ──
FROM eclipse-temurin:25-jdk AS system

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
    git curl ca-certificates sudo jq \
    iptables ipset iproute2 dnsutils aggregate \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# ── Stage 2: Developer environment ──
FROM system AS developer

ARG USERNAME=developer
ARG USER_UID=1000
ARG USER_GID=${USER_UID}

RUN groupmod --new-name ${USERNAME} ubuntu \
    && usermod --login ${USERNAME} --home /home/${USERNAME} --move-home ubuntu \
    && echo "${USERNAME} ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers.d/${USERNAME} \
    && chmod 0440 /etc/sudoers.d/${USERNAME}

RUN mkdir -p /workspace /commandhistory \
    && touch /commandhistory/.bash_history \
    && chown -R ${USERNAME}:${USERNAME} /workspace /commandhistory

COPY --from=node-tools /opt/node-tools /opt/node-tools
# Pre-install Zed runtime deps (agent adapter, language servers, prettier).
# Docker populates empty named volumes with image contents, so this seeds the
# shared-zed-data volume on first use. Zed skips downloads when versions match.
COPY --from=node-tools /opt/zed-data /home/${USERNAME}/.local/share/zed
RUN chown -R ${USERNAME}:${USERNAME} /home/${USERNAME}/.local
COPY --from=python-tools /opt/python-tools /opt/python-tools
ENV PATH="/opt/node-tools/bin:/opt/python-tools/bin:${PATH}" \
    PYTHONPATH="/opt/python-tools/lib/python3.12/site-packages"

# Fix shebangs: pip entry-points from the build stage reference /usr/local/bin/python3.12
# which doesn't exist in the runtime image. The devcontainer Python feature installs
# python3 at /usr/bin/env-discoverable path, so use portable shebangs instead.
RUN sed -i '1s|^#!.*python.*|#!/usr/bin/env python3|' /opt/python-tools/bin/mcp-server-*

# ── Stage 3: Security hardening ──
FROM developer AS devcontainer

COPY init-firewall.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/init-firewall.sh \
    && echo "developer ALL=(root) NOPASSWD: /usr/local/bin/init-firewall.sh" > /etc/sudoers.d/developer-firewall \
    && chmod 0440 /etc/sudoers.d/developer-firewall

ENV DEVCONTAINER=true
USER developer
WORKDIR /workspace
