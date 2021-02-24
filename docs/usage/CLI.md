# Command-line Interface

`epsilon-clj` put emphasis on being able to integrate generation workflow into your own. Having a library that does 
not have a CLI introduces some complexities when you want to take advantage of your CI/CD, and thus `epsilon-clj` 
should avoid being that library.

This page serves as a reference guide on different commands and options you can use.

!!! info
    For most up-to-date commands and options, use `-h` or `--help` option.

It is also assumed that you have also installed `epsilon-clj`. If not, check out 
[Installation page](../installation.md).

## Commands
### `generate`
Generate all files. Requires

- [template directory](#-d-dir)
- [model paths](#-m-model)
- [output directory](#-o-output-dir)

!!! info
    This option will run validation against the models before generation.

### `validate`
Validate all models. Requires

- [template directory](#-d-dir)
- [model paths](#-m-model)

## Options
### `-h` `--help`
Prints out help message.

### `-d` `--dir`
Points `epsilon-clj` to the directory that contains all the templates used for file generation.


### `-m` `--model`
Add an XML model to be used for generation. Can be used multiple times.

### `-o` `--output-dir`
Where to put all the generated files.

### `-w` `--watch`
Enable watch mode on the given directories. Any changes will trigger a regeneration.

## Examples
```bash
# Display help message.
$ java -jar epsilon-{{ file.version }}-standalone.jar -h

# Execute generation with a model, a template directory and an output directory
$ java -jar epsilon-{{ file.version }}-standalone.jar -m "model.xml"  -d "templates" -o "gen" generate

# Execute watch mode with a model, a template directory and an output directory
$ java -jar epsilon-{{ file.version }}-standalone.jar -m "model.xml"  -d "templates" -o "gen" -w generate

# Execute validation with a model and a template directory
$ java -jar epsilon-{{ file.version }}-standalone.jar -m "model.xml"  -d "templates" validate
```