# PixelJet

A [PixelFlut](https://github.com/defnull/pixelflut) tool which paints the given text to the canvas.

It connects to a running Pixelflut server, renders the text with a given font and sends it to the Canvas.

## Commandline parameters

Example:

```shell
java -jar /app/pixeljet.jar --host 123.456.789.123 Text

docker run ghcr.io/poeschl/pixeljet --host 123.456.789.123 Text
```

To get a programmatic argument help use `--help` as parameter.

### `TEXT`

The text to display, multiple lines are possible with `\n`.

### `--host <host>`

Specify the host of the targeted Pixelflut server. (Default: `localhost`)

### `-p`, `--port <port>`

Specify the port of the targeted Pixelflut server. (Default: `1234`)

### `-d`, `--debug`

Enables the debugging log output

### `-x`

The X offset of the text (Default: `0`)

### `-y`

The Y offset of the text (Default: `0`)

### `--font`

The name of a font installed on your system. If not available all available fonts are outputted. (Default: `Arial`)

### `--size`

The font size in pt (Default: `16`)

## Docker

The application can also be run as docker container.

To execute the docker container run `docker run ghcr.io/poeschl/pixeljet <commandline parameters>`. For own font files, the files must be
availabe in the container.
