# `epsilon-clj`

![Clojure CI](https://github.com/aratare-jp/epsilon-clj/workflows/Clojure%20CI/badge.svg?branch=master)
[![codecov](https://codecov.io/gh/aratare-jp/epsilon-clj/branch/master/graph/badge.svg?token=RJCEPYBF3I)](https://codecov.io/gh/aratare-jp/epsilon-clj)

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

Vanilla Epsilon, however, has much left to be desired. It lacks various features
such as hot-reload and CLI-friendly interface that truly enhance the development
and deployment process, which is something of a must in today's standard.

Enters `epsilon-clj`.

Sprinkled with goodness, `epsilon-clj` aims to be the bridge between Epsilon and
Clojure to bring Model-driven Software Development closer to clojurists while
making sure they still have their favourite workflow.

# Features

- [x] XML model
- [ ] EDN model
- [ ] JSON model
- [ ] Hot-reload on file changes
- [ ] REPL

# Documentation

For Epsilon's specifics, check out [here](https://www.eclipse.org/epsilon/).

## CLI

Running `epsilon-clj` in a CLI is as simple as executing a Jar file:

```bash
# Display help message.
$ java -jar epsilon-clj.jar -h

# Execute generation with a model, a template directory and an output directory
$ java -jar epsilon-clj.jar -m "model.xml"  -d "templates" -o "gen" generate

# Execute watch mode with a model, a template directory and an output directory
$ java -jar epsilon-clj.jar -m "model.xml"  -d "templates" -o "gen" -w generate

# Execute validation with a model and a template directory
$ java -jar epsilon-clj.jar -m "model.xml"  -d "templates" validate
```

## Programmatic

To use `epsilon-clj` in code, simply require the namespace and
use `generate-all`:

```clojure
(require '[altio.generator :as gen])

;; Takes in the template directory, the watch mode flag, the models and the output directory.
(gen/generate-all "templates" false ["model.xml"] "gen")

;; Using watch mode will return a watcher handler, which can be used to stop the watcher.
(let [handler (gen/generate-all "templates" true ["model.xml"] "gen")]
  (handler))
```

## Contributing

Currently, any features listed in this project are subjective to what I need on
a daily basis. If there is a feature you want to add, feel free to let me know
via issues, or if you feel generous, perhaps even a PR.

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
