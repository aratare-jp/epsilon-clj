# Altio
Altio is a code generator that aims to be:
- Fast
- Light
- Interactive
- Easy to use

![Clojure CI](https://github.com/aratare-tech/altio-core/workflows/Clojure%20CI/badge.svg?branch=master)

# Installation
TBC

# Rationale
So why would there be another templating framework?

Existing templating frameworks most often do one thing and one thing only: Use templates to write files. The same can be said for Altio. And that's enough. After all, that's what the frameworks were designed to do. But...

Altio takes a slightly different course compared to them. In fact, Altio is designed to be more involved in the development process, rather than being used whenever there is a need for mass code generation.

Altio embraces the **Model-driven Software Development (MDSD)** methodology, which promises less disconnection between the model, which represents the current domain, and the software. In layman's terms, we've all seen it: UML.

UML represents the current domains via classes and attributes, and it aims to be readable and understandable for less tech-savvy folks. A typical project may start with designing the software via UML. However, as the development process goes on, most often UML is only considered after-the-fact, which means that UML is often derived from the software, not vice-versa.

MDSD tries to solve this problem by promoting the model to first-class citizen, and that the software is derived from the model via code generation. If the model changes, the software changes accordingly. No longer would the model be an afterthought.

With the risk of oversimplying, here's the entirety of MDSD:
> **Model** + **Templates** + **Custom persistent code** = **_New code_**

Some eagle eyes out there may have noticed that this is quite similar to React's states and views. And it is, but on a much higher level.

There are some libraries which are widely used in this space, namely Acceleo, JET, Epsilon. Setting aside the syntactical and grammatical differences between them, they were all designed to generate code from a common model. However, from my own experience working with these tools, they are:
- Slow. I mean, very, very, very slow. Except Epsilon, Acceleo and JET both compile the templates before generation. In larger projects, this can take up to 10~15 minutes of pure compilation. There is no incremental compilation, so a single typo and there goes another 15 minutes. Epsilon, on the other hand, is quite fast because it parses the templates directly, but the average speed still has much to be desired since everything is parsed serially.
- Overly cumbersome. The entire ECore ecosystem feels overly and needlessly cumbersome. All these frameworks work with ECore, but each has its own "way" of working, and thus has a big learning curve which deters new learners. There are also some questionable design decision of ECore that often left me having to find workarounds for problems that could be solved in a much more elegant way.
- Inflexibility. Eclipse is absolutely needed to use these frameworks, since they were designed to be plugins rather than libraries that can be integrated into other projects. Epsilon, while can be used programmatically, does not provide a smooth experience either. Often dependent libraries are either missing, or scattered across different repos.
- Extremely limited functionality. Most of the functionality come from Eclipse, and thus anything outside it is extremely limited. Life enhancement does not exist and often require mannual process for lots of operations, even code generation. This means no automation.

This is where Altio comes in. Learnt from the experience of the developer, Altio is designed to solve these problems and provide a smooth experience with the hope of popularising MDSD.

# Goals
As stated in the beginning, Altio has four, and only four goals:
- **Fast:** Altio needs to be fast, _very_ fast. As time is gold, the less time developers have to wait, the ~~less time to have sword fights~~ smoother the feedback loop is going to be.
- **Light:** No flowers or unnecessary things. Altio needs to be lightweight for ease of integration with various backends.
- **Interactive:** Taken lesson learnt from Clojure and various languages and frameworks, Altio needs to offer an interactive experience. Want to test out what this code is doing? Chuck it into the REPL and find out.
- **Easy to use:** Last but not least, Altio needs to be easy to use, both in terms of what commands you need to remember, and the ideology of Altio. No more Eclipse, no more cumbersome operations and processes. Feed it a model and a template and press the button. Done.

# Features
Altio provides a convenient way to generate code using the ***Altio language***, which is a small templating language designed to be expressive and easy to use.

User has two ways to interact with Altio: CLI, or programmatic.
## CLI
TBC

## Programmatic

