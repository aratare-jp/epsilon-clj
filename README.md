# Altio
![Clojure CI](https://github.com/aratare-tech/altio-core/workflows/Clojure%20CI/badge.svg?branch=master)
[![codecov](https://codecov.io/gh/aratare-tech/altio-core/branch/master/graph/badge.svg?token=RJCEPYBF3I)](https://codecov.io/gh/aratare-tech/altio-core)

Altio is an awesome Model-Driven code generator that aims to be a complete replacement of JET, [Acceleo](https://www.eclipse.org/acceleo/) and [Epsilon](https://www.eclipse.org/epsilon/).

For more details on Model-Driven Software Development (MDSD), check out [here](https://en.wikipedia.org/wiki/Model-driven_engineering).

# Installation
TBC

# Rationale
So why would there be another code generator?

Current Model-Driven code generators are plagued with these issues:
- **Slow**. Due to AOT compilation, in larger projects, code generation can take up to 10~15 minutes. A single typo means another 15 minutes.
- **Complicated**. Current MDSD mainly revolves around the ECore ecosystem. However, it feels overly and needlessly cumbersome. Eclipse is also a must to use these frameworks.
- **Inflexibility**. Any non-standard workflow requires an extreme amount of time and efforts with no easy way to extend or integrate into your own tooling.

This is where Altio comes in. Altio is designed to elevate these problems and provide a much smoother experience to the developers.

# Goals
Altio has four, and only four goals:
- **Fast:** Altio needs to be fast, _really_ fast. As time is gold, the less time developers have to wait, the ~~less time to have sword fights~~ better.
- **Light:** No flowers or unnecessary things. Altio needs to be lightweight with a minimum amount of dependencies.
- **Interactive:** Want to test out what this code is doing? Chuck it into the REPL and find out.
- **Easy to use:** No tied-in ecosystem, no cumbersome operations and processes. Feed it a model and a template and press the button. Done.

# Features
Altio provides a convenient way to generate code using the ***Altio language***, which is a small templating language designed to be expressive and easy to use.

User has two ways to interact with Altio: CLI, or programmatic.
## CLI
TBC

## Programmatic
TBC

## Contributing
Currently any features listed in this project are subjective to what I need on a daily basis. If there is a feature you want to add, feel free to let me know via issues, or if you feel generous, perhaps even a PR.

## License
Copyright 2020 Rex Truong

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
