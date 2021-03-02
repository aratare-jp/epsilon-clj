# Command-line Interface

`epsilon-clj` put emphasis on being able to integrate generation workflow into your own. Having a library that does 
not have a CLI introduces some complexities when you want to take advantage of your CI/CD, and thus `epsilon-clj` 
should avoid being that library.

This page serves as a reference guide on different commands and options you can use.

!!! info
    For most up-to-date commands and options, use `-h` or `--help` option.

It is also assumed that you have installed `epsilon-clj`. If not, check out [Installation page](../installation.md).

The general structure of a typical `epsilon-clj` command is like this:

```bash linenums="1"
java -jar epsilon-{{ file.version }}-standalone.jar [options] [command]
```

## Options

| Short form | Long form | Description |
| ---- | ---- | ---- |
| `-h` | `--help` | Display help message. |
| `-d` | `--dir` | Template directory. Can be relative or absolute. |
| `-m` | `--model` | Path to XML model to use. Can be relative or absolute. Can be used multiple times. |
| `-o` | `--output` | Where to output the templates. Can be relative or absolute. |
| `-v` | | Verbosity level; may be specified up to 2 times. Levels: INFO -> DEBUG -> TRACE |
| `-w` | `--watch` | Watch the given template directory. |

## Commands
### `generate`
Generate all files. Requires

- template directory
- model paths
- output directory

!!! warning
This option will run validation against the models before generation.

### `validate`
Validate all models. Requires

- template directory
- model paths

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