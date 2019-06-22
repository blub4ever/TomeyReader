package main

import java.io.File
import kotlin.jvm.internal.Intrinsics

class ReadSettings {
    var autoMode = true
    var sourceIsFolder = true;
    var sourceFileExtension =".exam"
    var sourceFolder = File("")
    var sourceFile = File("")
    var targetFolder = File("")
    var x = 512
    var y = 900
    var bytesPerPixel = 2
    var manualOffset: Long = 0
    var autoSubDir = true
    val autoReadTags = AutoReadTags()


    //    %programmDir%
    //   %targetDir%"
    var postProcess = true
    var postProcessCommand =
        "%programmDir%/helper/mogrify.exe -path %targetDir%/ -resize 1300x650! -median 2x2 -contrast-stretch 10%x0% *.png"
    var postProcessTarget = File("")


    val imageSize: Int
        get() = this.x * this.y * this.bytesPerPixel
}
