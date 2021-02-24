# Troubleshooting

This document serves as a place where you can find some of the most annoying and obscure bugs in Epsilon. Some of them
are results of oversight and others are by design. Hopefully this page will give you the help you need to debug your
templates.

## No file generated

### Symptoms

Output directory does not contain any files, or all generated files remain unchanged.

### Reason

This problem mostly occurs when you have used an unknown variable or types, and hence mainly a semantic error. However,
note that this is caused by something like this:

```
[%= foo %]
```

or

```
[% foo; %]
```

which is quite different from the [blank generated files bug](#blank-generated-files).

### Remedy

Check what `epsilon-clj` is printing out on the CLI, or returns from function calls. Check what variables you are
referencing incorrectly and use the correct ones.

## Partially missing content

### Symptoms

Your generated files are missing **_some_** content but not all.

### Reason

One of the most annoying bugs in existence. This is mainly caused by a mismatch tag within your template. For example,

``` linenums="1"
[%
[%= "blah" %]
```

The annoyance of this bug is the fact this is not classified as a grammatical error by Epsilon. Thus Epsilon will
happily parse everything and move on, even though the syntax is wrong. Unfortunately, this also means `epsilon-clj`
cannot catch such problem.

### Remedy

Check where the content is missing in your generated file and cross-reference with the template to see which tag is
missing its counterpart.

## Blank generated files

### Symptoms

Your generated files are completely blank.

### Reason

Yet another annoying bugs from Epsilon. Unlike [this bug](#no-file-generated), this is only caused by statement blocks
that miss semi-colons. For example:

```
[% foo %]
```

However, Epsilon does not recognise that this is a grammatical error and thus should report immediately. Instead it will
continue parsing and generating everything. Unfortunately, there is no such way for `epsilon-clj` to intercept or
recognise this either.

### Remedy

Unfortunately, it is quite hard to track down because, well, you have nothing in the generated files to cross-reference.
Usually this is quite easy for other languages to pick up when you have a linter, but the Epsilon ecosystem is quite
limited in terms of tooling.

My recommendation for this bug is to do a manual scan through your templates and check where you're missing the
semi-colons.

To make matter worse, if your templates are referencing a buggy template, you will need to check them as well.

This is, by far, **_the worst bug of Epsilon_** due to its obnoxious symptoms and cause, compounded by difficulty to 
debug and remedy.