---
layout: default
title: Notice for Developers
nav_order: 4
---

# Notice for Developers
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Using Public APIs of this Extension

This extension provides various SPIs/APIs marked with the `@PublicAPI` annotation, designed to facilitate extension and integration by developers. These APIs are officially supported and maintained according to project's deprecation and compatibility policies. However, please observe the following guidelines and warnings:

### Stable vs. Unstable APIs
- **Stable APIs** (`@PublicAPI` without `unstable = true`) are reliable for use in production environments. They adhere to strict semantic versioning and aim to maintain backward compatibility wherever possible.
- **Unstable APIs** (`@PublicAPI(unstable = true)`) are provided for experimental purposes and to solicit feedback from the developer community. These APIs are subject to change and may undergo significant modifications, including backward-incompatible changes. Their use should be limited to non-production environments or experimental projects.

## Caution When Using Unstable APIs
If you choose to implement any unstable APIs, please be aware that:
- Unstable APIs might change in ways that can break your application.
- I may modify, deprecate, or remove unstable APIs in any future release without adherence to typical deprecation processes.
- Using unstable APIs in critical or production systems is not recommended, and you should proceed with caution.

## Responsibility of Developers
By using any APIs marked with the `@PublicAPI` annotation, you agree to the following:
- **To Monitor Updates**: You are responsible for keeping abreast of changes to the APIs, especially the unstable ones, by following project updates, changelogs, and documentation.
- **To Prepare for Changes**: If you use unstable APIs, be prepared to make necessary adjustments or refactorings to your codebase as the APIs evolve.
- **To Provide Feedback**: Your feedback on unstable APIs is invaluable and can significantly shape the evolution of the API. I encourage active communication and feedback through the project channels.

## Conclusion
I am committed to providing robust and useful APIs and appreciate your involvement in using and improving these interfaces. Please consider the stability tags as an integral part of your development planning when using the APIs.
