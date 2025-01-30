---
layout: default
title: Introduction
nav_order: 1
---

# Home IdP Discovery - Introduction

This is a simple Keycloak authenticator to redirect users to their home identity provider during login.

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/sventorben/keycloak-home-idp-discovery?sort=semver)
![Keycloak Dependency Version](https://img.shields.io/badge/Keycloak-26.0.2-blue)
![GitHub Release Date](https://img.shields.io/github/release-date-pre/sventorben/keycloak-home-idp-discovery)
![Github Last Commit](https://img.shields.io/github/last-commit/sventorben/keycloak-home-idp-discovery)

![CI build](https://github.com/sventorben/keycloak-home-idp-discovery/actions/workflows/buildAndTest.yml/badge.svg)
![open issues](https://img.shields.io/github/issues/sventorben/keycloak-home-idp-discovery)
[![CodeScene Code Health](https://codescene.io/projects/53524/status-badges/code-health)](https://codescene.io/projects/53524)

## What is it good for?

When a federated user wants to log in via Keycloak, Keycloak will present a username/password form and a list of configured identity providers to the user. The user needs to choose an identity provider to get redirected.
This authenticator allows to skip the step of selecting an identity provider.

## How does it work?

If this authenticator gets configured as part of a browser based login flow, Keycloak will present a username form (without password form and without list of configured identity providers).
A user can then enter an email address. Keycloak will then choose an identity provider based on the domain part of the provided email address and forward the user to the chosen provider.

The identity provider will be chosen by the following preference:
1. If the `forwardToLinkedIdp` config option is enabled
    1. Use the first linked identity provider with matching domain
    2. Use the first linked identity provider
    3. Use non-linked identity provider with matching domain
2. If the `forwadToLinkedIdp` config option is disabled
    1. Use the first identity provider with matching domain

Only enabled and not link-only identity providers will be considered.
