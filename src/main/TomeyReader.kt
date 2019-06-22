package main

import java.io.File
import java.util.Arrays

class TomeyReader {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.out.println("current dir = " + System.getProperty("user.dir"));
            val readSettings = ReadSettings()
            var startGui = false
            var postProcess = false

            println("Starting TomeyReader... with ${Arrays.toString(args)}")

            var c = 0
            while (c < args.size) {
                when (args[c]) {
                    "-sf" -> {
                        if (c++ >= args.size) {
                            println("Error, -sf (source folder) needs a filename")
                            return
                        }
                        readSettings.sourceFolder = File(args[c])
                        readSettings.sourceIsFolder = true
                    }
                    "-s" -> {
                        if (c++ >= args.size) {
                            println("Error, -s (file) needs a filename")
                            return
                        }
                        readSettings.sourceFile = File(args[c])
                        readSettings.sourceIsFolder = false
                    }
                    "-se" -> {
                        if (c++ >= args.size) {
                            println("Error, -se (source file extension) needs a value")
                            return
                        }
                        readSettings.sourceFileExtension = args[c]
                    }
                    "-t" -> {
                        if (c++ >= args.size) {
                            println("Error, -t (target dir) needs a dirname")
                            return
                        }

                        readSettings.targetFolder = File(args[c])
                    }
                    "-g" -> {
                        startGui = true
                    }
                    "-o" -> {
                        if (c++ >= args.size) {
                            println("Error, -o (manual offset) is not set")
                            return
                        }
                        try {
                            readSettings.manualOffset = args[c].toLong()
                        } catch (var13: NumberFormatException) {
                            println("Error, -o (manula offset) is no integer")
                            return
                        }
                    }
                    "-x" -> {
                        if (c++ >= args.size) {
                            println("Error, -x (image width) is not set")
                            return
                        }
                        try {
                            readSettings.x = args[c].toInt()
                        } catch (var13: NumberFormatException) {
                            println("Error, -x (image width) is not set")
                            return
                        }
                    }
                    "-y" -> {
                        if (c++ >= args.size) {
                            println("Error, -y (image height) is not set")
                            return
                        }
                        try {
                            readSettings.y = args[c].toInt()
                        } catch (var13: NumberFormatException) {
                            println("Error, -y (image height) is not set")
                            return
                        }
                    }
                    "-b" -> {
                        if (c++ >= args.size) {
                            println("Error, -b (bytes per pixel) is not set")
                            return
                        }
                        try {
                            readSettings.bytesPerPixel = args[c].toInt()
                        } catch (var13: NumberFormatException) {
                            println("Error, -b (bytes per pixel) is no integer")
                            return
                        }
                    }
                    "-m" -> {
                        readSettings.autoMode = false
                    }
                    "-nsub" -> {
                        readSettings.autoSubDir = false
                    }
                    "-post" -> {
                        if (c++ >= args.size) {
                            println("Error, -post (post process command) need command")
                            return
                        }
                        readSettings.postProcessCommand = args[c]
                        postProcess = true
                    }
                    "-h" -> {
                        println("-s sourceFile")
                        println("-sf sourcefolder")
                        println("-se extension of source files e.g. .exam")
                        println("-t targetdir")
                        println("-g start gui")
                        println("-o manual offset")
                        println("-x width")
                        println("-y height")
                        println("-b bytes per pixel")
                        println("-m auto mode off")
                        println("-nsub auto subdir mode off")
                    }

                }
                c++
            }

            readSettings.postProcess = postProcess

            if (startGui) {
                Gui().startGui(readSettings)
            } else {
                val bitReader = BitReader()
                if (bitReader.validate(readSettings)) {
                    bitReader.readTo(readSettings)
                }
            }

        }
    }
}
