package main

import java.awt.Point
import java.awt.image.*
import java.io.File
import java.io.IOException
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.experimental.or

class BitReader {
    private val nullChar = '\u0000'
    private val dleChar = 16.toChar()
    private val soChar = 14.toChar()
    private val newLine = 10.toChar()
    private val returnChar = 13.toChar()
    private val lineBreak: String = String(charArrayOf(this.returnChar, this.newLine))
    private var imgArray: ShortArray = shortArrayOf()
    private var copyArray: ByteArray = byteArrayOf()
    private lateinit var img: BufferedImage

    fun validate(readSettings: ReadSettings): Boolean {

        if (!readSettings.sourceIsFolder) {
            if (!Files.exists(readSettings.sourceFile.toPath())) {
                println("SourceFile -f does not exist")
                return false
            }
        } else {
            if (!Files.isDirectory(readSettings.sourceFolder.toPath())) {
                println("Source directory -t does not exist")
                return false
            }

            val fileList = readSettings.sourceFolder.listFiles { e ->
                e.name.toLowerCase().endsWith(readSettings.sourceFileExtension);
            }

            if (fileList.isEmpty()) {
                println("Source directory does not contain \"${readSettings.sourceFileExtension}\" files")
                return false
            }
        }


        if (!Files.isDirectory(readSettings.targetFolder.toPath())) {
            println("Target directory -t does not exist")
            return false
        }

        if (readSettings.x == 0 || readSettings.y == 0) {
            println("Width -x or height -y is not set")
            return false
        }
        if (readSettings.bytesPerPixel == 0) {
            println("Bytes per pixel -b not set")
            return false
        }
        return true
    }

    fun readTo(readSettings: ReadSettings) {
        if(readSettings.sourceIsFolder) {
            val sourceFiles = readSettings.sourceFolder.listFiles { e ->
                e.name.toLowerCase().endsWith(readSettings.sourceFileExtension);
            }

            for (i in sourceFiles) {
                readSettings.sourceFile = i
                readFile(readSettings)
            }
        }else
            readFile(readSettings)
    }

    private fun readFile(readSettings: ReadSettings) {
        var offset: Long = 0
        this.copyArray = ByteArray(readSettings.imageSize)
        this.imgArray = ShortArray(copyArray.size / readSettings.bytesPerPixel)
        this.img = BufferedImage(readSettings.x, readSettings.y, 11)

        val fileContent = read(readSettings.sourceFile)
        offset = if (readSettings.autoMode) {
            findAutoOffset(readSettings, fileContent).toLong()
        } else {
            readSettings.manualOffset
        }

        var imageCount = 0
        var imageOffset = offset + 1
        while (imageOffset.toInt() + readSettings.imageSize < fileContent.size) {
            val image = getNextImage(imageOffset.toInt(), readSettings.imageSize, fileContent, readSettings)
            if (image != null) {
                val targetFile = getTargetFile(
                    readSettings
                )

                val str = "$imageCount.png"
                write(targetFile, str, image)
                imageOffset += readSettings.imageSize
                imageCount++
            } else {
                return
            }
        }

        println("File read successful")

        if (readSettings.postProcess) {
            val f = File(getTargetFile(readSettings).absolutePath.toString(), "post")
            f.mkdir()
            postProcess(readSettings, f)
        }
    }

    private fun getTargetFile(readSettings: ReadSettings): File {
        if (readSettings.autoSubDir) {
            val f =
                File(readSettings.targetFolder, readSettings.sourceFile.nameWithoutExtension)
            f.mkdirs()
            return f
        }
        return readSettings.targetFolder
    }

    private fun getNextImage(
        from: Int, length: Int, content: ByteArray, readSettings: ReadSettings
    ): BufferedImage? {
        System.arraycopy(content, from, copyArray, 0, length)
        return this.byteArrToImage(copyArray, readSettings.bytesPerPixel)
    }

    private fun read(file: File): ByteArray {
        return Files.readAllBytes(file.toPath())
    }

    private fun write(targetDir: File, file: String, image: BufferedImage) {
        try {
            ImageIO.write(image, "png", File(targetDir, file))
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun byteArrToImage(data: ByteArray, bytesPerPixel: Int): BufferedImage {

        var origCounter = 0
        var clearEnd = false

        val length = imgArray.size

        for (i in 0 until length) {

            imgArray[i] = 0

            if (!clearEnd) {
                for (y in 0 until bytesPerPixel) {
                    if (origCounter >= data.size) {
                        clearEnd = true
                        break
                    }
                    imgArray[i] = imgArray[i] or ((data[origCounter].toUByte().toInt() shl y * 7).toShort())
                    origCounter++
                }
            }
        }

        img.data =
            Raster.createRaster(img.sampleModel, DataBufferUShort(imgArray, imgArray.size) as DataBuffer, Point())

        return img
    }

    private fun findAutoOffset(readSettings: ReadSettings, content: ByteArray): Int {
        val fileName = findTag(readSettings.autoReadTags.fileName, lineBreak, content, 0)
        return findOffset(
            String(charArrayOf(this.dleChar, this.soChar)), content,
            findOffset(
                fileName.second + this.nullChar + this.nullChar, content, fileName.first
            )
        )
    }

    private fun findTag(prefix: String, suffix: String, content: ByteArray, offset: Int): Pair<Int, String> {
        val index = findOffset(prefix, content, offset)
        if (index == -1) {
            throw IllegalArgumentException("Prefix not found")
        }

        val name = StringBuffer()
        var offsetCount = 0
        val length = content.size

        for (i in index + 1 until length) {
            when {
                content[i].toChar() == suffix[offsetCount] -> {
                    if (offsetCount == suffix.length - 1) {
                        return Pair(i, name.toString())
                    }
                    offsetCount++
                }
                offsetCount != 0 -> offsetCount = 0
                else -> name.append(content[i].toChar())
            }
        }

        return Pair(index, name.toString())
    }

    private fun findOffset(search: String, arr: ByteArray, offset: Int): Int {
        var offsetCount = 0
        val length = arr.size
        for (i in offset until length) {
            if (arr[i].toChar() == search[offsetCount]) {
                if (offsetCount == search.length - 1) {
                    return i
                }
                offsetCount++
            } else if (offsetCount != 0) {
                offsetCount = 0
            }
        }
        return -1
    }


    private fun postProcess(readSettings: ReadSettings, postTargetDir: File) {
        var command = readSettings.postProcessCommand
        command = command.replace("%programmDir%", System.getProperty("user.dir"))
        command = command.replace("%targetDir%", postTargetDir.absolutePath.toString())

        println("Running post process command: $command")

        val process2 = Runtime.getRuntime().exec(
            command, null, getTargetFile(readSettings)
        )
        println("Waiting for batch file ...");
        process2.waitFor();
        println("Batch file done.");
    }
}