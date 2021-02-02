# `epsilon-clj`

![Clojure CI](https://github.com/aratare-tech/altio-core/workflows/Clojure%20CI/badge.svg?branch=master)
[![codecov](https://codecov.io/gh/aratare-tech/altio-core/branch/master/graph/badge.svg?token=RJCEPYBF3I)](https://codecov.io/gh/aratare-tech/altio-core)

A neat wrapper around [Eclipse Epsilon](https://www.eclipse.org/epsilon/) to add
various goodness.

For more details on Model-Driven Software Development (MDSD), check
out [here](https://en.wikipedia.org/wiki/Model-driven_engineering).

# Installation

TBC

# What is it?

[Eclipse Epsilon](https://www.eclipse.org/epsilon/) is a code generator just
like other various frameworks out
there: [Selmer](https://github.com/yogthos/Selmer)
, [StringTemplate](https://www.stringtemplate.org/)
, [ThymeLeaf](https://www.thymeleaf.org/)
, [Django](https://docs.djangoproject.com/en/dev/ref/templates/builtins/), etc.

What differentiates Epsilon is that it has a merge engine so that your
hand-written code inside the generated files is kept throughout other generation
cycles via **protected regions**.

Vanilla Epsilon, however, has much to be desired. It lacks various features that
truly enhance the development process, which is something of a must in today's
standard.

Enters `epsilon-clj`.

Sprinkled with goodness, `epsilon-clj` aims to be the bridge between Epsilon and
Clojure to bring MDSD closer to clojurists while making sure they still have
their favourite workflow.

# Features

- [ ] XML model
- [ ] EDN model
- [ ] JSON model
- [ ] Hot-reload on file changes
- [ ] REPL

## Contributing

Currently any features listed in this project are subjective to what I need on a
daily basis. If there is a feature you want to add, feel free to let me know via
issues, or if you feel generous, perhaps even a PR.

## [License](https://github.com/aratare-tech/altio/blob/master/LICENSE)

Copyright 2020 Rex Truong

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
