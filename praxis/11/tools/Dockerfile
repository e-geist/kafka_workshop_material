# Werkzeuge für Kubernetes in Docker: Docker CLI, kind und kubectl
FROM docker:29-cli

ARG TARGETARCH
ARG KIND_VERSION=v0.33.0
ARG KUBECTL_VERSION=v1.37.1

RUN apk add --no-cache bash curl bash-completion \
    && curl -fsSLo /usr/local/bin/kind https://github.com/kubernetes-sigs/kind/releases/download/${KIND_VERSION}/kind-linux-${TARGETARCH} \
    && curl -fsSLo /usr/local/bin/kubectl https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/${TARGETARCH}/kubectl \
    && chmod +x /usr/local/bin/kind /usr/local/bin/kubectl \
    && echo 'source <(kubectl completion bash)' >> /root/.bashrc

WORKDIR /workspace
CMD ["sleep", "infinity"]
