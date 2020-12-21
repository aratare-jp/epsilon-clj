# Altio
![Clojure CI](https://github.com/aratare-tech/altio-core/workflows/Clojure%20CI/badge.svg?branch=master)
[![codecov](https://codecov.io/gh/aratare-tech/altio-core/branch/master/graph/badge.svg?token=RJCEPYBF3I)](https://codecov.io/gh/aratare-tech/altio-core)

Altio is an awesome Model-Driven code generator that doesn't suck. Period.

For more details on Model-Driven Software Development (MDSD), check out [here](https://en.wikipedia.org/wiki/Model-driven_engineering).

# Installation
TBC

# Rationale
So why would there be another code generator?

On one hand, we have well-known "ordinary" code generators like [Selmer](https://github.com/yogthos/Selmer) in Clojure land; [StringTemplate](https://www.stringtemplate.org/) or [ThymeLeaf](https://www.thymeleaf.org/) in Java land; [Django](https://docs.djangoproject.com/en/dev/ref/templates/builtins/) in Python; and so on. These generators are fast, well-tested, and will meet every requirement you may have for a code generator. However, they are not model-driven. There are no protected regions, nor anything remotely resemble such feature.

On the other hand, we have a few code generators that are model-driven, such as [JET](https://projects.eclipse.org/projects/modeling.m2t.jet), [Acceleo](https://www.eclipse.org/acceleo/) and [Epsilon](https://www.eclipse.org/epsilon/). However, they are plagued with major issues such as:
- **Slow**. Due to AOT compilation, in larger projects, code generation can take up to 10~15 minutes. A single typo means another 15 minutes.
- **Complicated**. All current model-driven generators exist within the [Ecore](https://wiki.eclipse.org/Ecore) ecosystem, which is overly complex and raises high learning curve for new learners.
- **Inflexible**. The Ecore system is not only complicated, but also inflexible such that there is no easy way to tailor its functionality to your workflow. As Eclipse is always required for any kind of operations, CLI-only environments such as CI/CD is either not possible or extremely hard to achieve.

# Goals & Non-goals
Based on the previous section, this is where Altio comes in.

It is designed to be model-driven from the ground up, while offering to be a "good enough" code generator. As such, it aims to be:
- **Fast:** As time is gold, the less time developers have to wait, the ~~less time to have sword fights~~ better.
- **Light:** No flowers or unnecessary things. Altio needs to be lightweight so you can easily use it as a tool or a library.
- **Informative:** With the REPL and tooling, you can get the right information about your model or templates.
- **Easy to use:** No tied-in ecosystem, no steep learning curve, no complicated setup.

Of course, on the other hand, Altio does NOT aim to be mutually exclusive with other generators. Rather, it provides an alternative way of applying MDSD to your project.

# Features
- [ ] EDN/JSON for models.
- [ ] Simple Altio language for templating.
- [ ] Hot-reload.
- [ ] REPL for template inspection.
- [ ] Can be used either as CLI or a library.
- [ ] Plugin for IntelliJ.

## Contributing
Currently any features listed in this project are subjective to what I need on a daily basis. If there is a feature you want to add, feel free to let me know via issues, or if you feel generous, perhaps even a PR.

## [License](https://github.com/aratare-tech/altio/blob/master/LICENSE)
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
