# Contribution
Hi there! First of all, thank you for considering contributing to this project.

This project was created mainly for my own personal use, as such I try my best to keep it in good shape. Of course, 
sometimes bugs will find their way into the code, and hair will be pulled in anger and/or confusion because things 
don't work the way you want it to. As such, this page is dedicated to show different ways you can help make this 
project better.

## Bugs
If you find a bug, please create an issue [here](https://github.com/aratare-jp/epsilon-clj/issues) with the `Bug 
report` template.

## Features
If you want a feature, please create an issue [here](https://github.com/aratare-jp/epsilon-clj/issues) with the 
`Feature rqeuest` template.

## Pull Requests (PRs)
PRs will be _always_ run against the test suite, and to merge into `main` PRs will need to:

- Have all tests passing
- Have at least the same code coverage
- Clean code with proper docstrings (with respect to best Clojure practices)

## Development Environment
`epsilon-clj` is just a Leiningen project, as such you will need to have:

- Java 11
- [Leiningen](https://leiningen.org)

To run all tests locally, use 

```bash
lein with-profile test eftest
```

To run all tests locally with code coverage, use:

```bash
lein with-profile test cloverage --runner :eftest
```