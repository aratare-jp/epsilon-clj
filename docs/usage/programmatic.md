# Programmatic

`epsilon-clj` also allows you to integrate programmatically with your codebase. Overall, it exposes several useful 
functions which you can call:

## `generate-all`
Given a template directory, model paths and output directory, generate all templates found in the template directory.

```clojure
(require '[epsilon.generator :as gen])

(gen/generate-all "templates" ["model.xml"] "output" false)
```

The above example will take all the templates inside `templates`, combined with the models including `model.xml` and 
generate files inside `output`. The last boolean `false` indicates we don't want watch mode.

!!! info
    This function will validate all models before generating. If failed, generation will halt.

## `validate-all`
Similar to [`generate-all`](#generate-all) but only validates models. For example:

```clojure
(require '[epsilon.generator :as gen])

(gen/validate-all "templates" ["model.xml"] false)
```

It also takes a boolean to indicate if you want watch mode.

## `generate`
Generate a single EGX file.

```clojure
(require '[epsilon.generator :as gen])

(gen/generate "templates/foo.egx" ["model.xml"] "output")
```

## `validate`
Validate a single EVL file.

```clojure
(require '[epsilon.generator :as gen])

(gen/validate "templates/foo.evl" ["model.xml"])
```

## `watch`

Watch over a directory for file changes. It takes a list of predicates which will be run against file changes to 
determine if it should be kept or not.

```clojure
(require '[epsilon.generator :as gen])

(gen/watch "templates" ["model.xml"] "output")
```