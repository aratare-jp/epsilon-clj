# 15-minutes Quick Start

Here we'll use `epsilon-clj` to build a mini website. Throughout the process I'll mention various concepts to highlight
the advantages of using a template engine with a merge engine.

Don't worry if some things I've just said don't really make sense. It will all be clear by the time we're done.

## 1. Let's set things up!

First, you would need to install `epsilon-clj`. I'd recommend checking out the [Installation](installation.md) page. For
now, you can open up a terminal and run the following commands:

```bash linenums="1"
# Where we will do this quick start guide.
mkdir awesome_epsilon
cd awesome_epsilon

# Download epsilon-clj executable.
wget <Enter URL here later>
```

## 2. Let's make a model!

The first thing you need is a model. A model is essentially a representation of a domain, concept or "thing". For
example:

- Toy car models are little representations of real-life cars.
- A flowchart is a representation of a process.
- A scientific model represents a certain phenomenon in real life.
- And so on.

A model in `epsilon-clj` is just a plain XML file. So create a file called `library.xml` and copy and paste the 
following content into it:

```xml linenums="1"
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<library>
	<book title="EMF Eclipse Modeling Framework" pages="744" public="true">
		<id>EMFBook</id>
		<author>Dave Steinberg</author>
		<author>Frank Budinsky</author>
		<author>Marcelo Paternostro</author>
		<author>Ed Merks</author>
		<published>2009</published>
	</book>
	<book
		title="Eclipse Modeling Project: A Domain-Specific Language (DSL) Toolkit"
		pages="736" public="true">
		<id>EMPBook</id>
		<author>Richard Gronback</author>
		<published>2009</published>
	</book>
	<book title="Official Eclipse 3.0 FAQs" pages="432" public="false">
		<id>Eclipse3FAQs</id>
		<author>John Arthorne</author>
		<author>Chris Laffra</author>
		<published>2004</published>
	</book>
</library>
```

!!! tip
    An XML model can have anything, but it is recommended that you should keep the names simple since you'll be using 
    them quite extensively in the templates.

## 3. Let's create some templates!

A template is like a blueprint. You build or create things based on what is described in the blueprint, just like
building a house or a car. Template allows you to create lots of things that are structurally identical to each other. 
Here templates are used to create lots of files at once.

However, just templates alone are not enough: You need specific details. Just like having a car blueprint is not really
useful without the specific numbers and measurements.

Here, templates receive these "numbers" and "measurements" from one or multiple models. To illustrate this, let's create
our very first template. But first, we need a place to store these templates. Run the following commands in your
terminal:

```bash linenums="1"
mkdir templates
cd templates
```

Now, create a new file called `book.html.egl` and copy and paste the following content into it:

``` linenums="1"
<h1>Book [%=index%]: [%=book.a_title%]</h1>
<h2>Authors</h2>
<ul>
[%	for (author in book.c_author) { %]
	<li>[%=author.text%]
[%	} %]
</ul>
```

!!! tip
    There is no rule how to name a template file. But I like naming it after what I'm expecting to produce. So here
    I'm expecting to produce `book.html`, so naming it `book.html.egl` makes it easier for tracking.

I'm sure you just squinted a little to make sure you didn't just misread those `[% %]` things. This weird syntax is 
from [Eclipse Epsilon](https://www.eclipse.org/epsilon/), based on which `epsilon-clj` was built. You can visit the 
site to learn more about the syntax, but for now, this block

```
[%	for (author in book.c_author) { %]
	<li>[%=author.text%]
[%	} %]
```

means "Go through all the authors of the book and then list out their names".

## 4. Let's create a template coordinator!

A template requires a coordinator, sort of like a controller telling what the template should do. It is also used to
expose certain data, handle validations, tell the template where to output, etc. You can learn more about it
[here](https://www.eclipse.org/epsilon/doc/egx/).

Let's create a file called `home.html.egx` with the following content:

``` linenums="1"
rule Book2PageHTML transform book : t_book {
	guard : book.b_public
	parameters {
		var params : new Map;
		params.put("index", t_book.all.indexOf(book) + 1);
		return params;
	}
	template : "book.html.egl"
	target : book.e_id.text + ".html"
}
```

Wow, that's a lot. Let's dissect what is going on here:

- At line 1, you are declaring that you want to output the template below for every book. Of course, you can also 
  declare that a coordinator should only run once.
- At line 2, you are declaring that you only want to output for books that are public.
- At line 3 to 7, you are exposing certain data to the template. Remember that `[%=index%]` at line 1 in the 
  template? This is where it comes from. The passed in data is always a map.
- At line 8, you are declaring that this controller will use the `book.html.egl` template, which you just created 
  earlier. This works as a relative path.
- And finally at line 9, you are declaring you want to output an HTML file whose name comes from the ID of the book. 
  This works as a relative path inside the output directory.

!!! tip
    You are not limited to just a single rule in a coordinator file. However, having multiple rules in one place has 
    proven to be quite hard to maintain in my experience. That is why I recommend having separate coordinators for 
    each template. Doing so would also allow you to unlock some nice `epsilon-clj`'s features such as hot-reloading.

!!! tip
    Speaking of nice features, the name of the coordinator file can be anything. But, again, to unlock some features 
    that `epsilon-clj` supports, you need to name it after the template it's controlling. E.g. `book.html.egl` and 
    `book.html.egx`.

## 5. Let's start making some files!
Eh, not so fast, tiger! There is one last thing you need: where `epsilon-clj` should put your files. So let's make one:

```bash linenums="1"
cd ..
mkdir output
```

Your directory should now look like this:

```
epsilon-clj.jar

library.xml

templates
  |_ book.html.egl
  |_ book.html.egx

output
```

Let's start making some files! As mentioned earlier, you need three things when running `epsilon-clj`: templates, 
models and where to put the generated files. So it's no surprise when the CLI command looks like this:

```bash linenums="1"
java -jar epsilon-clj.jar -d templates -m library.xml -o output -w generate
```

Here, we can see `epsilon-clj` takes various arguments. Let's break them down:

- `-d templates` indicates all the templates are in the `templates` directory.
- `-m library.xml` indicates you want to use `library.xml` model. You can have as many `-m`s as you wish.
- `-o output` indicates you want to output files at `output`.
- `-w` indicates you want to use template hot-reload. 
- `generate` tells you want to generate files using the provided arguments. There's also `validate`.

It should take a few seconds for `epsilon-clj` to parse everything and start doing its work. Once it's done, your 
directory should look like this:

```
epsilon-clj.jar

library.xml

templates
  |_ book.html.egl
  |_ book.html.egx

output
  |_ EMPBook.html
  |_ EMFBook.html
```

Notice the two new generated files inside `output`: `EMPBook.html` and `EMFBook.html`

## Let's see some magic!
Recall earlier I specifically used the phrase **_template engine with a merge engine_**. Hopefully by now the 
_**template engine**_ part has made sense to you. But what about the _**merge engine**_ part?

Let's first have some observation: Everything we've done until now is one-way. Everything starts from the model, 
goes through the templates, and finishes at the generated files. But sometimes there are things you want to 
specifically have in the generated files, but makes no sense to have them in the templates.

Let's imagine our library example website is so good we start selling them to people. And let's say one client wants 
to add a new piece of information at the bottom of each book, like a slogan. This is tricky because how do you do this?
You can't make the change in the generated files because it will get rewritten the next time you run `epsilon-clj`, 
but it also makes no sense to make it in the template because other clients may not want it. 

This is where **_protected region_** comes in.

Protected regions are special places inside the generated files where you can safely insert your custom code without 
the fear of them being rewritten. So in this case it is a perfect place for our custom code.

Let's make some changes to our `book.html.egl` template:

``` linenums="1"
<h1>Book [%=index%]: [%=book.a_title%]</h1>
<h2>Authors</h2>
<ul>
[%	for (author in book.c_author) { %]
	<li>[%=author.text%]
[%	} %]
</ul>
[% protected("<!--", "Enter your custom code here", "-->") %]
```

Notice the last line: this is where we declare a protected region so we can add things later on.

`epsilon-clj` will detect the file change and rerun the generation. After which `EMPBook.html` and `EMFBook.html` 
will have something like this at the end of the file:

```html
<!-- Omit for brevity -->
...

<!-- protected region: Enter your custom code here off -->
<!-- end protected region -->
```

Anything between those two lines will be reserved throughout future generations. So let's insert some cool code:

```html
<!-- Omit for brevity -->
...

<!-- protected region: Enter your custom code here off -->
<h1>Epsilon is awesome!</h1>
<!-- end protected region -->
```

Nice! Let's trigger some regeneration by adding a newline in the `book.html.egl` template. Check the content of 
`EMPBook.html` and `EMFBook.html` again and you'll see:

```html
<!-- Omit for brevity -->
...

<!-- protected region: Enter your custom code here off -->
<!-- end protected region -->
```

Hang on! That's not right! Where's the custom code?

It turns out that protected regions are by default disabled: notice that little word `off` at the end. Turn it on by 
simply replace `off` with `on`, and then add the custom code again. Now if you rerun the generation, the custom code 
will stay there.

!!! question
    So why is it disabled by default if we're just going to use it anyway? It turns out that protected regions can 
    also have default content, i.e. things you want to put there by default. For example, let's say we have:

    ```
    [% startProtected("<!--", "Enter your custom code here", "-->") %]
    <h1>Hello world!</h1>
    [% endProtected %]
    ```

    it will generate something like this:

    ```html
    <!-- protected region: Enter your custom code here off -->
    <h1>Epsilon is awesome!</h1>
    <!-- end protected region -->
    ```

    It is suitable for places where you want to have default content but also allow users to change the content 
    later on.

## Wrap Up
Andddddddd that's it! I know it's not the quickest guide _ever_, but it aims to give you a good idea of what 
`epsilon-clj` can do for you. So let's recap:

- You've learnt what models and templates are and how they can work together to create files.
- You've learnt how just creating files won't be enough for customisation, and how protected regions can solve this.
- You've learnt how `epsilon-clj` can provide some nice features like hot-reloading so you can work much faster.

In the end, Eclipse Epsilon is just another template engine. But by having a merge engine to allow us to include 
special places, we can truly take advantage of text generation without the fear of customisation.

Here are some stuff you can look at to have a better understanding of concepts we've talk about here:

- Model-driven Software Development
- Eclipse Epsilon and its big family of languages
- In-depth `epsilon-clj` usage, both CLI and Programmatic

Happy coding!