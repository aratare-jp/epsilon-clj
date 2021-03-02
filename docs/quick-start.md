# Quick Start

Hi there! Glad you decided to give this a try.

Here we will use `epsilon-clj` to build a mini website for a library to demonstrate what codebase generation can 
give you.

Some new concepts will be introduced along the way with examples to follow, so you can see how things come together.

Ready? Let's get started!

## 1. Let's set things up!

First, you need to install `epsilon-clj`. Open a new terminal window and enter the following commands:

```sh linenums="1"
# Our new project directory.
mkdir awesome_epsilon

# Move to the newly created directory.
cd awesome_epsilon
```

For this tutorial, we will do things in a terminal, so follow the CLI section in the 
[Installation guide](installation.md) to set `epsilon-clj` up.

For now, our project directory will look like this:

```
awesome_epsilon
└── epsilon-{{ file.version }}-standalone.jar
```

## 2. Let's make a model!

The first thing you need is a model.

A model is essentially a representation of a domain, concept or "thing". For example:

- Toy car models are little representations of real-life cars.
- A flowchart is a representation of a process.
- A scientific model represents a certain phenomenon in real life.

A model captures the essence of a domain or problem that you are working on. Since we are building a 
library website, our model will represent our library.

A model in `epsilon-clj` is just a plain XML file. So create a file called `library.xml` in our newly created directory 
and insert following content into it:

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

You can see we have a root `library` element which contains multiple `book` elements, each has its own attributes like 
`title` and `page`, as well as `author`.

Your directory should now look like this:

```
awesome_epsilon
├── epsilon-{{ file.version }}-standalone.jar
└── library.xml
```

A model alone is not enough. The reason is simple: It just stores data in a certain shape or form. However, to 
_display_ these data, e.g. which data to show, which data to ignore, simple strings manipulation, etc. you will need 
some _templates_.

## 3. Let's create some templates!

A template is like a blueprint. You build or create things based on what is described in the blueprint, just like
building a house or assembling a car. Each template will generate a file based on whatever content you have in them 
(but not every kind of content as we will see later).

However, templates alone are not enough: We need specific details. Having only a house blueprint is not really 
useful without the details and measurements.

Templates receive these details and measurements from models. To illustrate this, let's create our very first 
template. First, we need a place to store these templates. Create a directory called `templates` and a template file 
called `book.html.egl`. Now, insert the following content into `book.html.egl`:

``` linenums="1"
<h1>Book [%=index%]: [%=book.a_title%]</h1>
<h2>Authors</h2>
<ul>
[%	for (author in book.c_author) { %]
	<li>[%=author.text%]
[%	} %]
</ul>
```

Let's go through some interesting things we just did:

- At line 1, we create an `h1` HTML tag based on a book's `index` and `title`.
- From line 4 to 6, we create `li` tags based on a book's author.

!!! info
    Some hawkeyes out there may be wondering where `index` came from. After all, there was no `index` in the model. 
    It actually came from somewhere else, as we'll see shortly.

Your directory should now look like this:

```
awesome_epsilon
├── epsilon-{{ file.version }}-standalone.jar
├── library.xml
└── templates
    └── book.html.egl
```

I won't go too deep into EGL syntax, but a more interesting question is: _**How 
does this template understand which book to use?**_

The answer is: _**Template coordinator**_, as we will see in the very next section shortly.

## 4. Let's create a template coordinator!

A template requires a coordinator, sort of like a controller telling what the template should do. It is also used to
expose certain data, handle validations, tell the template where to output, etc.

In the same directory as our template, let's create a file called `home.html.egx` with the following content:

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

- At line 1, you are declaring that you want to output the template below for every book.
- At line 2, you are declaring that you only want to output for books that are public.
- At line 3 to 7, you are exposing certain data to the template. Remember that `index` at line 1 in the template? 
  This is where it comes from. The passed-in data always comes as a map.
- At line 8, you are declaring that this controller will use the `book.html.egl` template, which you just created 
  earlier. This works as a relative path.
- At line 9, you are declaring you want to output an HTML file whose name comes from the ID of the book. This is 
  relative to the output directory you're about to give `epsilon-clj`.

!!! tip
    You can have multiple rules in a coordinator file. However, it can be tricky to manage since it's hard to know 
    which template generates which files when you're working backward. Having, say, `book.html.egl` and `EMPBook.html` 
    can make things easier when you want to know where `EMPBook.html` came from.

!!! Info
    Speaking of nice features, `epsilon-clj` requires its name to be the same as the template it's controlling, e.g. 
    `book.html.egl` and `book.html.egx`, for hot-reloading.

Your directory should now look like this:
```
awesome_epsilon
├── epsilon-{{ file.version }}-standalone.jar
├── library.xml
└── templates
    ├── book.html.egl
    └── book.html.egx
```

## 5. Let's start generating some files!
Eh, not so fast, tiger! There is one last thing you need: where your generated files should go! So let's make a new 
directory called `output` in our root directory.

Your directory should now look like this:

```
awesome_epsilon
├── epsilon-{{ file.version }}-standalone.jar
├── library.xml
├── templates
│   ├── book.html.egl
│   ├── book.html.egx
└── output
```

Now we can generate some files. `epsilon-clj` requires three things: _**template**_, _**model**_ and _**output 
**_ directories. Run the following command in your terminal:

```bash linenums="1"
java -jar epsilon-{{ file.version }}-standalone.jar -d templates -m library.xml -o output generate
```

Let's break this down:

- `-d templates` indicates all the templates are in the `templates` directory.
- `-m library.xml` indicates you want to use `library.xml` model.
- `-o output` indicates you want to output files at `output`.
- `generate` tells you want to generate files using the provided arguments.

It should take a few seconds for `epsilon-clj` to parse everything and start doing its work. Once it's done, your 
directory should now look like this:

```
awesome_epsilon
├── epsilon-{{ file.version }}-standalone.jar
├── library.xml
├── templates
│   ├── book.html.egl
│   ├── book.html.egx
└── output
    ├── EMPBook.html
    └── EMFBook.html
```

Notice the two new generated files inside `output`: `EMPBook.html` and `EMFBook.html`. Let's have a look at their 
content:

EMFBook.html
```html
<h1>Book 1: EMF Eclipse Modeling Framework</h1>
<h2>Authors</h2>
<ul>
    <li>Dave Steinberg</li>
    <li>Frank Budinsky</li>
    <li>Marcelo Paternostro</li>
    <li>Ed Merks</li>
</ul>
```

EMPBook.html
```html
<h1>Book 2: Eclipse Modeling Project: A Domain-Specific Language (DSL) Toolkit</h1>
<h2>Authors</h2>
<ul>
    <li>Richard Gronback</li>
</ul>
```

Congratulations! You have just created the library website. You can open these files in a browser to check out their 
beauty.

## 6. Let's see some magic!
If you've worked with other generators before, this may seem to be similar to, well, all of them except the syntax. So 
what's so special about `epsilon-clj` and [Eclipse Epsilon](https://www.eclipse.org/epsilon/)?

Let's first have some observation: Everything we've done until now is one-way: 
```
models -> templates -> generated files.
```
This means that if you want to include other things in the generated files, they will be rewritten the next time you 
regenerate! The only way forward is to include them in the templates. However, sometimes there are things you want to 
specifically have in the generated files, but makes no sense to have them in the templates.

Let's imagine our library example website is so good we start selling them to people. Let's say one client wants 
to add a new piece of information at the bottom of each book. How do you approach this? You can't include
it in the generated files for the stated reason, but it also makes no sense to make it in the template because other 
clients may not want it. 

This is where **_protected region_** comes in.

Protected regions are special places inside the generated files where you can safely insert your custom stuff without 
the fear of them being rewritten. In this case it is a perfect place for our custom code.

Let's make some changes to our `book.html.egl` template:

``` linenums="1"
<h1>Book [%=index%]: [%=book.a_title%]</h1>
<h2>Authors</h2>
<ul>
[%	for (author in book.c_author) { %]
	<li>[%=author.text%]
[%	} %]
</ul>
[%= protected(out, "<!--", "Enter your custom code here", false, "-->") %]
```

Notice the last line: this is where we declare a protected region. Run this again:

```bash linenums="1"
java -jar epsilon-{{ file.version }}-standalone.jar -d templates -m library.xml -o output generate
```

After which `EMPBook.html` and `EMFBook.html` will have something like this at the end of the file:

```html
<!-- Omit for brevity -->
<!-- protected region Enter your custom code here off begin -->
<!-- protected region Enter your custom code here end -->
```

Anything between those two lines will be reserved throughout future regenerations. So let's insert some cool code into 
`EMPBook.html`:

```html
<!-- Omit for brevity -->
<!-- protected region Enter your custom code here off begin -->
<h1>Epsilon is awesome!</h1>
<!-- protected region Enter your custom code here end -->
```

Rerun the previous command and check the content of `EMPBook.html` again, and you'll see:

```html
<!-- Omit for brevity -->
<!-- protected region Enter your custom code here off begin -->
<!-- protected region Enter your custom code here end -->
```

Hang on! That's not right! Where's the custom code?

It turns out that protected regions are by default disabled: notice the word `off` at the end. Turn it on by simply 
replace `off` with `on`, and then add the custom code again and rerun the command. You will now see:

```html
<!-- Omit for brevity -->
<!-- protected region Enter your custom code here on begin -->
<h1>Epsilon is awesome!</h1>
<!-- protected region Enter your custom code here end -->
```

Magic!

!!! question
    So why are protected regions disabled by default if we're just going to use them anyway? It turns out that 
    protected regions can also have default content, i.e. things you want to put there by default. For example, let's 
    say we have:

    ```
    [%= startProtected(out, "<!--", "Enter your custom code here", false, "-->") %]
    <h1>Hello world!</h1>
    [%= endProtected(out) %]
    ```

    it will generate something like this:

    ```html
    <!-- protected region Enter your custom code here off begin -->
    <h1>Hello world!</h1>
    <!-- protected region Enter your custom code here end -->
    ```

    It is suitable for places where you want to have default content but also allow users to change the content 
    later on.

!!! info
    You can actually turn protected regions on automatically by replacing `false` with `true` when using it. For 
    example:

    ```
    [%= startProtected(out, "<!--", "Enter your custom code here", true, "-->") %]
    [%= endProtected(out) %]
    ```

    will produce:

    ```html
    <!-- protected region Enter your custom code here on begin -->
    <!-- protected region Enter your custom code here end -->
    ```

## 7. Let's do some validation! (Optional)

As your model gets bigger, you may want to do some validation on your model to make sure your templates will not 
cause issues because it can't find an element in your model. So in this section, we'll make a quick validation file 
to handle such case.

Let's create a file called `book.evl` inside your template directory. Your project should now look like this:

```
awesome_epsilon
├── epsilon-{{ file.version }}-standalone.jar
├── library.xml
├── templates
│   ├── book.evl
│   ├── book.html.egl
│   ├── book.html.egx
└── output
    ├── EMPBook.html
    └── EMFBook.html
```

Insert the following code into `book.evl`:

``` linenums="1"
context t_book {
  constraint ValidAuthors {
    check : self.c_author.size() > 0
    message: "Book " + self.e_id.text + " needs to have more than one author"
  }
}
```

Let's break it down:

- At line 1, we want to run this validation against all `book` elements in the model.
- At line 2, we name this constraint/rule "ValidAuthors".
- At line 3, we want to validate that every book has at least 1 author.
- At line 4, if the previous validation failed, display a custom message to the console.

Much like template coordinators, you can have as many constraints inside a context, and as many contexts inside a 
validation file as you want.

When you use `generate` command, it will first validate the model by using all the validations inside your template 
directory before generating anything. If you only want to only validate, consider using the `validate` command:

```bash linenums="1"
java -jar epsilon-{{ file.version }}-standalone.jar -d templates -m library.xml -o output validate
```

## 8. One more thing

By now, I'm sure you have gotten quite tired of having to run this command

```bash linenums="1"
java -jar epsilon-{{ file.version }}-standalone.jar -d templates -m library.xml -o output generate
```

every time you want to regenerate. Fortunately, `epsilon-clj` got you covered!

By simply enabling hot-reload with the `-w` flag, `epsilon-clj` will automatically regenerate whenever it sees a 
file change inside your templates. For example, this command:

```bash linenums="1"
java -jar epsilon-{{ file.version }}-standalone.jar -d templates -m library.xml -o output -w generate
```

will regenerate whenever there's a file change inside `templates`.

!!! info
    Not just EGL files, EVL files will also trigger a validation run.

    For more options, check out [this page](usage/CLI.md).

## 9. Wrapping things up
That's it! Let's recap:

- You've learnt what models and templates are and how they can work together to create a codebase.
- You've learnt how just creating files won't be enough for customisation, and how protected regions can solve this.
- You've learnt how `epsilon-clj` can provide some nice features like hot-reload so you can work much faster.

In the end, Eclipse Epsilon is just another generator. By having a merge engine to allow us to include special 
places, we can truly take advantage of codebase generation without the fear of customisation.

As a parting gift, you can learn more about Epsilon's syntax [here](https://www.eclipse.org/epsilon/).

Happy coding!