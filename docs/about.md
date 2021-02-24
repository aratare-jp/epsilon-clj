# About

## What is it?
To put it simply: 

> `epsilon-clj` is a Clojure wrapper around [Eclipse Epsilon](https://www.eclipse.org/epsilon/).

[Eclipse Epsilon](https://www.eclipse.org/epsilon/) is a code generator that complies to Model-Driven Software 
Development (MDSD) philosophy. 

A quick search on Google will give you a lot of places to look at, but in a nutshell, 
MDSD is not that different from what you might have seen before. Namely: **Code generation**.

!!! info
    Note that I call this **code** generation instead of **text** generation. The difference between them is, well, 
    dependent on how you classify "code" and "text". For consistency, any time I say code generation, I'm referring 
    to both of them.

## What is code generation?

Code generation involves two main parts: **templates**, and, if applicable, **models**.

Generally speaking, code generators read in **templates**, which have various "slots" where they require additional 
information. They also require **models**, which provides these additional information to fill those "slots".

For example, let's say that we have a template like this:

```
Hello, {name}!
```

This template contains a single "slot" that requires an additional piece of information: `name`. So if we combine 
this template with a model like this:

```json
{
  "name": "Bob"
}
```

This model provides that additional piece of information we are looking for. Thus, when you run code generation on 
these two pieces, you will have

```
Hello, Bob!
```

Almost all code generators work like this. But, here therein lies the problem with conventional code generator.

## Where it falls short

Conventional code generation works in top-down fashion: Information flows from models to templates. As you can see 
from the example above, once you've reached the final stage (i.e. `Hello, Bob!`), information cannot flow back to 
the previous stages.

This is what we want!

Having one-way information flow means that you don't have inconsistent information in various places. All 
information flows from one place: the top, which is the single source of truth.

However, there will be times when you need to be able to place custom code. But, this introduces a challenge: 
_Where_ do you place this custom code?

An obvious answer to the question above is **templates**, but that means it will affect all other generated files, 
and that may not be what you want.

Another answer is the **generated files**, but this means it will be overwritten the next time you regenerate.

Some may say that we can put them inside custom templates, which are imported whenever we need them. This is a good 
way to solve this, but it also means you're littering your templates with not only shared templates, but also custom 
templates. Once your templates reach maturity, maintenance will be much harder.

So, then, how do we solve this problem? The answer is: **protected regions**.

## The return of the templates

Protected regions are special "slots" inside your templates that effectively tell the code generator "I want this 
region right here preserved".

This means that anything you write inside these "slots" will not be overwritten no matter how many times you 
regenerate.

Why would you want something like this? The answer is when you don't have access to the templates themselves.

Imagine you use these templates to generate a small project, may be a small HTML static website, and give it to your 
friend to use. One day, your friend tells you she wants to change the logo. Since she doesn't have your templates, 
anything she changes will be gone the next time either of you regenerates. You want to keep the templates, but you 
also want to allow her to change the logo. And thus, you put the logo inside a protected region. This means that 
any changes she makes to the logo will be guaranteed to stay there.

In a nutshell, it ensures freedom to customise while keeping uniform changes throughout the entire codebase.

## Model-Driven Software Development

As stated before, MDSD can be described as the combination between conventional code generation and protected 
regions, and more.

MDSD puts emphasis on models with inheritance and meta-inheritance. This means you can make models that generate 
other models, which in turn are used to generate code via templates. There is no limit on how far up you can go, so 
theoretically you can have a fairly complex model inheritance tree.  In reality, however, usually just 2 or 3 levels 
are enough to describe an entire domain of problems you're working on.

Here I won't go too deep into MDSD since it is quite complex and may not contribute much to the value `epsilon-clj` 
brings. If you're interested, feel free to have a read [here](https://en.wikipedia.org/wiki/Model-driven_engineering).

## Why this library

[Eclipse Epsilon](https://www.eclipse.org/epsilon/) is good and modern, but it also contains lots of problems that 
are results of either oversight or by design. For example:

- APIs feel outdated and neglected. 
- No javadoc and documentation for lots of things.
- Everything is single-threaded, so scaling up requires investing a lot of time to mitigate this.
- Since there are lots of shared states and singletons, even moving to multi-threading takes a lot of time.
- Lots of functionality requires Eclipse's exclusive packages, which are not the easiest to find if you want to 
  build your own wrapper.
- Confusing and sometimes outright infuriating errors that are inconsistent and hard to debug.

The above list are just some of the problems I've personally encountered when using Epsilon. Some of which have been 
resolved since then, but a lot still remains.

And this is where `epsilon-clj` enters the scene.

`epsilon-clj` brings a much more streamlined experience where you can use what you need, and it will take care of 
the rest. Generating a file is as simple as calling a function with the template, the models and where you want to 
put the file, instead of having importing and configuring lots of things.

It also aims to either solve or work around the aforementioned problems so you don't have to waste time trying to 
figure out things yourself.

Lastly, it brings great features like template hot-reload so you can work faster without interruption.

So, head over to the next page to try `epsilon-clj` out!.

Happy coding!