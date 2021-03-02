# About

## What is it?
`epsilon-clj` is a codebase generator based on [Eclipse Epsilon](https://www.eclipse.org/epsilon/) and written in 
Clojure.

Codebase generation is similar to text or code generation, but instead works on a much larger picture, i.e. the 
codebase itself. In a nutshell, instead of generating a single file, you'll generate an entire codebase. Sounds cool,
right?

!!! info
    You may be wondering: a code generator can also generate an entire codebase. And you'd be right. Here I'm using 
    the term _codebase generator_ to put emphasis on the fact that this library is mainly for generating codebase 
    rather than individual pieces, which you can also do by the way 😉

## How does codebase generation work?

Codebase generation involves two main parts: **templates**, and, if applicable, **models**.

Generally speaking, codebase generators read in **templates**, which have various "slots" where they require additional 
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

This model provides that additional piece of information we are looking for. Thus, when you run codebase generation on 
these two pieces, you will have

```
Hello, Bob!
```

Almost all generators work like this. But, here therein lies the problem with conventional generation.

## Where it falls short

Conventional generation works in top-down fashion: Information flows from models to templates. As you can see 
from the example above, once you've reached the final stage (i.e. `Hello, Bob!`), and this is what we want.

However, there will be times when you need to be able to place custom code. But, this introduces a challenge: 
_Where_ do you place this custom code?

An obvious answer is the **templates**, but that means it will affect all other generated files, and that may not be 
what you want.

Another answer is the **generated files**, but this means it will be overwritten the next time you regenerate.

Some may say that we can put them inside custom templates, which are imported whenever we need them. This approach will 
still affect other generated files, unless you have conditional import, which is something Eclipse Epsilon itself 
does not support.

So, then, how do we solve this problem? The answer is: **protected regions**.

## The return of the templates

Protected regions are special "slots" inside your templates that effectively tell the codebase generator "I want this 
region right here preserved".

This means that anything you write inside these "slots" will not be overwritten no matter how many times you 
regenerate.

Imagine you use these templates to generate a small project, may be a small HTML static website, for your friend. One 
day, she wants to change the logo. Since she doesn't have your templates, anything she changes will be gone the next 
time either of you regenerates. You want to keep the templates, but you also want to allow her to change the logo. 
The solution: putting the logo inside a protected region. This means that any changes she makes to the logo will be 
guaranteed to stay there no matter how many times either of you regenerates the project.

In a nutshell, it ensures freedom to customise while still giving you the benefits of a codebase generator.

## Why this library

[Eclipse Epsilon](https://www.eclipse.org/epsilon/) is good and modern, but it also contains lots of problems when 
you start using it a bit more extensively:

- APIs feel outdated and neglected. 
- No javadoc and documentation for lots of things.
- Everything is single-threaded, so scaling up requires investing a lot of time to mitigate this.
- Since there are lots of shared states and singletons, even moving to multi-threading takes a lot of time.
- Lots of functionality requires Eclipse's exclusive packages, which are not the easiest to find if you want to 
  build your own wrapper.
- Confusing and sometimes outright infuriating errors that are inconsistent and hard to debug.

The above list are just some of the problems I've personally encountered when using Epsilon. Some of which have been 
resolved since then, but a lot still remains.

This is where `epsilon-clj` enters the scene.

`epsilon-clj` was created to bring a much more streamlined experience where you can use what you need, and it will 
take care of the rest. Generating a file is as simple as calling a function with the template, the models and where 
you want to put the file, and leave the back-scene configurations to `epsilon-clj` to take care.

It also aims to either solve or work around the aforementioned problems and bring great features like template 
hot-reload so you can work faster and happier(!) without interruption.

So, head over to the next page to try `epsilon-clj` out!.

Happy coding!