package io.github.poeschl.pixeljet

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import xyz.poeschl.kixelflut.Area
import xyz.poeschl.kixelflut.Pixel
import xyz.poeschl.kixelflut.Pixelflut
import xyz.poeschl.kixelflut.Point
import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import kotlin.math.roundToInt
import kotlin.system.exitProcess


class Application(host: String, port: Int) {

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }

    private val textFlut = Pixelflut(host, port)
    private val backgroundFlut = Pixelflut(host, port)

    fun flut(offset: Point, text: String, selectedFontName: String, fontSize: Float) {

        val font = getFont(selectedFontName, fontSize) ?: exitProcess(1)

        LOGGER.info { "Using font '${font.fontName}'" }

        val lines = text.split("\\n")

        val textArea = getTextBox(lines, font)
        val blankPixels = createClearSpace(textArea, offset)

        val pixels = createTextPixels(lines, font, textArea, offset)
        val blankExcluded = blankPixels.filter { !pixels.contains(it) }.toSet()
        while (true) {
            textFlut.drawPixels(pixels)
            backgroundFlut.drawPixels(blankExcluded)
        }
    }

    private fun getFont(fontName: String, fontSize: Float): Font? {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val font = ge.allFonts.find { it.fontName.lowercase() == fontName.lowercase() }?.deriveFont(fontSize)
        if (font == null) {
            LOGGER.error { "Can't find font '$fontName' in system fonts" }
            LOGGER.error { "Available fonts: ${ge.allFonts.joinToString { it.fontName }}" }
        }
        return font
    }

    private fun getTextBox(lines: List<String>, font: Font): Area {
        val bounds = lines.map { font.getStringBounds(it, FontRenderContext(null, true, false)) }
        var maxWidth = 0
        var maxHeight = 0

        bounds.forEach {
            if (it.width > maxWidth) {
                maxWidth = it.width.roundToInt()
            }
            if (it.height > maxHeight) {
                maxHeight = it.height.roundToInt()
            }
        }

        return Area(Point(0, 0), Point(maxWidth, maxHeight * lines.size))
    }

    private fun createClearSpace(textArea: Area, offset: Point): Set<Pixel> {
        LOGGER.info { "Clear space" }

        val blanking = mutableSetOf<Pixel>()
        for (x: Int in 0 until textArea.width) {
            for (y: Int in 0 until textArea.height) {
                blanking.add(Pixel(Point(x, y).plus(offset), Color.BLACK))
            }
        }
        return blanking;
    }

    /**
     * Creates the Pixels of the text, with the origin at the top left of the text.
     */
    private fun createTextPixels(lines: List<String>, font: Font, textArea: Area, offset: Point): Set<Pixel> {
        LOGGER.info { "Create text pixels" }


        val bufferedImage = BufferedImage(textArea.width, textArea.height, BufferedImage.TYPE_INT_ARGB)
        val graphics = bufferedImage.createGraphics()
        graphics.background = Color.CYAN
        graphics.clearRect(0, 0, textArea.width, textArea.height)
        graphics.font = font
        graphics.color = Color.WHITE

        lines.forEachIndexed { index, string -> graphics.drawString(string, 0, (font.size * (index + 1))) }

        graphics.dispose()

        val pixels = mutableSetOf<Pixel>()
        for (x: Int in 0 until bufferedImage.width) {
            for (y: Int in 0 until bufferedImage.height) {
                val color = Color(bufferedImage.getRGB(x, y))
                if (color != Color.CYAN) {
                    pixels.add(Pixel(Point(x, y).plus(offset), color))
                }
            }
        }
        return pixels
    }
}

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::Args).run {
        val rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
        if (debug) {
            rootLogger.level = Level.DEBUG
        } else {
            rootLogger.level = Level.INFO
        }

        val logger = KotlinLogging.logger {}
        logger.info { "Connecting to $host:$port" }

        Application(host, port).flut(Point(x, y), text, font, fontSize)
    }
}

class Args(parser: ArgParser) {
    val host by parser.storing("--host", help = "The host of the pixelflut server").default("localhost")
    val port by parser.storing("-p", "--port", help = "The port of the server. (Default: 1234)") { toInt() }.default(1234)
    val debug by parser.flagging("-d", "--debug", help = "Enable debug output. (also time measurements)")
    val x by parser.storing("-x", help = "The x offset") { toInt() }.default(0)
    val y by parser.storing("-y", help = "The y offset") { toInt() }.default(0)
    val font by parser.storing("--font", help = "The name of a font installed on your system").default("Arial")
    val fontSize by parser.storing("--size", help = "The font size in pt") { toFloat() }.default(16.0f)
    val text by parser.positional("TEXT", help = "The text to display.")
}
