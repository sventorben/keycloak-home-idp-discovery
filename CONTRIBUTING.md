# Contributing

I kindly ask anyone who wants to contribute to this project to follow some basic guidelines:

1. Open a discussion around the change before contributing any code

   https://github.com/sventorben/keycloak-home-idp-discovery/discussions/categories/ideas)
2. Create a GitHub Issue with a good description associated with the PR
4. One feature/change per PR
5. One commit per PR 
6. PR rebased on main (`git rebase`, not `git pull`) 
7. Good descriptive [conventional commit message](https://www.conventionalcommits.org/en/v1.0.0/), with link to issue
8. No changes to code not directly related to your PR
9. Includes basic tests

   Please do not add additional test frameworks without prior consultation
10. Include some documentation/extend the README
11. PR must pass DCO check

## Sign off Your Work

The Developer Certificate of Origin (DCO) is a lightweight way for contributors to certify that they wrote or otherwise have the right to submit the code they are contributing to the project. Here is the full text of the [DCO](http://developercertificate.org/). Contributors must sign-off that they adhere to these requirements by adding a `Signed-off-by` line to commit messages.

```text
This is my commit message

Signed-off-by: Random J Developer <random@developer.example.org>
```

See `git help commit`:

```text
-s, --signoff
    Add Signed-off-by line by the committer at the end of the commit log
    message. The meaning of a signoff depends on the project, but it typically
    certifies that committer has the rights to submit this work under the same
    license and agrees to a Developer Certificate of Origin (see
    http://developercertificate.org/ for more information).
```
