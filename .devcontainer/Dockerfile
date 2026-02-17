# Stage 1: System dependencies
FROM eclipse-temurin:25-jdk AS system

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        git curl ca-certificates sudo \
        iptables ipset iproute2 dnsutils aggregate \
        postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# Stage 2: Developer environment
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

# Stage 3: Security hardening
FROM developer AS devcontainer

COPY init-firewall.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/init-firewall.sh \
    && echo "developer ALL=(root) NOPASSWD: /usr/local/bin/init-firewall.sh" > /etc/sudoers.d/developer-firewall \
    && chmod 0440 /etc/sudoers.d/developer-firewall

ENV DEVCONTAINER=true
USER developer
WORKDIR /workspace
