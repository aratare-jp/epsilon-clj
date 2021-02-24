# Installation

## Prerequisites
- Java 11
- A terminal console

## Setting up

Installing `epsilon-clj` is as easy as downloading a JAR file and executing it:

1. Open your terminal
2. Go to your root project directory
3. Download `epsilon-clj` executable:

=== "Linux/MacOS"
    ```
    curl https://github.com/aratare-jp/epsilon-clj/releases/download/v1.0.0/epsilon-v1.0.0-standalone.jar
    ```

=== "Windows"

    1. Head to `https://github.com/aratare-jp/epsilon-clj/releases/tag/{{ file.version }}`
    2. Download `epsilon-{{ file.version }}-standalone.jar`
    3. Move the downloaded file to your root project directory.


You can also download the executable and move it to any directory on `PATH`. That way you can set an alias to invoke 
`epsilon-clj` directly. 

For example, let's say that you've moved `epsilon-clj` to `/usr/local/bin`. Add this to your `.bashrc`:

```bash
alias epsilon="java -jar /usr/local/bin/epsilon-{{ file.version }}-standalone.jar"
```

This allows you to simply invoke `epsilon` anywhere you want.