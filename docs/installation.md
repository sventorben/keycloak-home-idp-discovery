---
layout: default
title: Installation
nav_order: 1
---

# Installation
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

Download a release (*.jar file) that works with your Keycloak version from the [list of releases](https://github.com/sventorben/keycloak-home-idp-discovery/releases).

## Server
Copy the jar to the `providers` folder and execute the following command:

```shell
${kc.home.dir}/bin/kc.sh build
```

## Container image (Docker)
For Docker-based setups mount or copy the jar to `/opt/keycloak/providers`.

You may want to check [docker-compose.yml](https://github.com/sventorben/keycloak-home-idp-discovery/blob/main/docker-compose.yml) as an example.

## Maven/Gradle

Packages are being released to GitHub Packages. You find the coordinates [here](https://github.com/sventorben/keycloak-home-idp-discovery/packages/1112199/versions)!

It may happen that I remove older packages without prior notice, because the storage is limited on the free tier.
