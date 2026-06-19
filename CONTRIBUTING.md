# Contributing to DoorVaani

First off, thank you for considering contributing to DoorVaani! It's people like you that make DoorVaani such a great project.

## Code Style

This project is written in Kotlin and uses Jetpack Compose for the UI. We follow standard Kotlin coding conventions.

- Use 4 spaces for indentation.
- Follow standard camelCase for variables and functions, and PascalCase for classes and objects.
- Try to keep functions small and focused on a single task.
- Follow the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- For Jetpack Compose, follow the [Compose API guidelines](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md).

## Branch Naming

Please use descriptive branch names. We recommend the following format:

`type/issue-number-short-description`

Examples:
- `feature/123-add-dharma-mode`
- `bugfix/456-fix-dialpad-crash`
- `docs/789-update-readme`

## Commit Messages

We follow [Conventional Commits](https://www.conventionalcommits.org/). This leads to more readable messages that are easy to follow when looking through the project history.

A commit message should be structured as follows:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

Types include:
- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Changes that do not affect the meaning of the code (white-space, formatting, etc.)
- `refactor`: A code change that neither fixes a bug nor adds a feature
- `perf`: A code change that improves performance
- `test`: Adding missing tests or correcting existing tests
- `chore`: Changes to the build process or auxiliary tools and libraries

Example: `feat(dialpad): add sound effect on button press`

## Pull Request Process

1. Fork the repo and create your branch from `main`.
2. If you've added code that should be tested, add tests.
3. If you've changed APIs, update the documentation.
4. Ensure the test suite passes (`./gradlew test` and `./gradlew assembleDebug`).
5. Create a Pull Request using the provided template, linking any relevant issues.
6. A project maintainer will review your code. You may need to make some changes before it is merged.
7. Once approved, a maintainer will merge your PR.

Thank you for your contributions!