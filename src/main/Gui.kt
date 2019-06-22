package main

import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File

class Gui : Application() {
    private var readSettings = ReadSettings()
    private lateinit var sourceFolder: CheckBox
    private lateinit var sourceExtension: TextField
    private lateinit var fileCount: Label
    private lateinit var fileLabel: Label
    private lateinit var targetLabel: Label
    private lateinit var offsetInput: TextField
    private lateinit var widthInput: TextField
    private lateinit var heightInput: TextField
    private lateinit var bytesPerPixel: TextField
    private lateinit var autoSubDir: CheckBox
    private lateinit var postProcess: CheckBox
    private lateinit var postProcessCommand: TextField
    private lateinit var progressBar : ProgressBar

    var autoMode = true

    fun startGui(readSettings: ReadSettings) {
        this.readSettings = readSettings
        Application.launch()
    }

    override fun start(stage: Stage) {

        val box = VBox()
        box.padding = Insets(5.0, 5.0, 5.0, 5.0)

        sourceFolder = CheckBox()
        sourceFolder.text = "SourceFolder"
        sourceFolder.isSelected = readSettings.sourceIsFolder
        sourceFolder.selectedProperty().addListener { observableValue, old, new ->
            readSettings.sourceIsFolder = new
            sourceExtension.isDisable = !new
        }

        sourceExtension = TextField()
        sourceExtension.text = this.readSettings.sourceFileExtension
        sourceExtension.isDisable = !readSettings.sourceIsFolder
        sourceExtension.textProperty().addListener { observableValue, old, newValue ->
            sourceExtension.text = newValue
            readSettings.sourceFileExtension = newValue
        }

        var hBox = HBox()
        hBox.padding = Insets(5.0, 5.0, 5.0, 5.0)
        hBox.spacing = 10.0
        hBox.children.addAll(sourceFolder, sourceExtension)
        box.children.add(hBox)

        val fileChooser = FileChooser()
        fileChooser.title = "Open Resource File"

        val sourceChosser = DirectoryChooser()
        sourceChosser.title = "Open Source Dir"

        val fileBtn = Button("Select File")
        fileBtn.setOnAction {
            val file =
                if (!readSettings.sourceIsFolder) fileChooser.showOpenDialog(stage) else sourceChosser.showDialog(stage)
            if (file != null) {
                fileLabel.text = if (file != null) file.absolutePath else null

                val targetFolder : File
                if (!readSettings.sourceIsFolder) {
                    readSettings.sourceFile = file
                    targetFolder = file.parentFile
                    fileCount.text = ""
                } else {
                    readSettings.sourceFolder = file
                    targetFolder = file
                   val count = file.listFiles{ e ->
                        e.name.toLowerCase().endsWith(readSettings.sourceFileExtension);
                    }.size

                    fileCount.text = "File count $count"
                }

                if (targetFolder.isDirectory && readSettings.targetFolder.path == "") {
                    targetLabel.text = targetFolder.absolutePath
                    readSettings.targetFolder = targetFolder
                }
            }
        }

        val fileName = Label("File:")

        fileLabel = Label("")
        fileCount = Label("")

        hBox = HBox()
        hBox.padding = Insets(5.0, 5.0, 5.0, 5.0)
        hBox.spacing = 10.0
        hBox.children.addAll(fileBtn, fileName, fileLabel,fileCount)
        box.children.add(hBox)

        val chooser = DirectoryChooser()
        chooser.title = "Open Target Dir"

        val targetBtn = Button("Select Target")
        targetBtn.setOnAction {
            val target = chooser.showDialog(stage)
            if (target != null) {
                targetLabel.text = if (target != null) target.absolutePath else null
                readSettings.targetFolder = target
            }
        }

        val targetName = Label("Export Dir:")

        targetLabel = Label("")

        hBox = HBox()
        hBox.padding = Insets(5.0, 5.0, 5.0, 5.0)
        hBox.spacing = 10.0
        hBox.children.addAll(targetBtn, targetName, targetLabel)
        box.children.add(hBox)

        autoSubDir = CheckBox()
        autoSubDir.text = "Subfolder erstellen"
        autoSubDir.isSelected = this.readSettings.autoSubDir
        autoSubDir.selectedProperty().addListener { observableValue, old, new ->
            readSettings.autoSubDir = new
        }

        hBox = HBox()
        hBox.padding = Insets(5.0, 5.0, 5.0, 5.0)
        hBox.spacing = 10.0
        hBox.children.addAll(autoSubDir)
        box.children.add(hBox)

        val cb1 = CheckBox()
        cb1.text = "Auto mode"
        cb1.isSelected = this.autoMode
        cb1.selectedProperty().addListener { observableValue, old, new ->
            autoMode = new
            offsetInput.isDisable = autoMode
            widthInput.isDisable = autoMode
            heightInput.isDisable = autoMode
            bytesPerPixel.isDisable = autoMode
            readSettings.autoMode = autoMode
        }

        hBox = HBox()
        hBox.padding = Insets(5.0, 5.0, 5.0, 5.0)
        hBox.spacing = 10.0
        hBox.children.addAll(cb1)
        box.children.add(hBox)

        val offsetLAbel = Label("Offset (Byte)")
        offsetLAbel.prefWidth = 100.0;

        offsetInput = TextField()
        offsetInput.text = readSettings.manualOffset.toString()
        offsetInput.isDisable = autoMode
        offsetInput.textProperty().addListener { observableValue, old, newValue ->
            if (newValue.matches(Regex("\\d"))) {
                offsetInput.text = newValue.replace("[^\\d]", "")
                readSettings.manualOffset = offsetInput.text.toLong()
            }
        }

        hBox = HBox()
        hBox.padding = Insets(5.0, 5.0, 5.0, 5.0)
        hBox.spacing = 10.0
        hBox.children.addAll(offsetLAbel, offsetInput)
        box.children.add(hBox)

        val widthRender = Label("Width")
        widthRender.prefWidth = 100.0;

        widthInput = TextField()
        widthInput.text = this.readSettings.x.toString()
        widthInput.isDisable = this.autoMode
        widthInput.textProperty().addListener { observableValue, old, newValue ->
            if (newValue.matches(Regex("\\d"))) {
                widthInput.text = newValue.replace("[^\\d]", "")
                readSettings.x = widthInput.text.toInt()
            }
        }

        hBox = HBox()
        hBox.padding = Insets(5.0, 5.0, 5.0, 5.0)
        hBox.spacing = 10.0
        hBox.children.addAll(widthRender, widthInput)
        box.children.add(hBox)

        val heightLabel = Label("Height")
        heightLabel.prefWidth = 100.0;

        heightInput = TextField()
        heightInput.text = this.readSettings.y.toString()
        heightInput.isDisable = this.autoMode
        heightInput.textProperty().addListener { observableValue, old, newValue ->
            if (newValue.matches(Regex("\\d"))) {
                heightInput.text = newValue.replace("[^\\d]", "")
                readSettings.y = heightInput.text.toInt()
            }
        }

        hBox = HBox()
        hBox.padding = Insets(5.0, 5.0, 5.0, 5.0)
        hBox.spacing = 10.0
        hBox.children.addAll(heightLabel, heightInput)
        box.children.add(hBox)

        val bytesPerPixLAbel = Label("Bytes Per Pixel")
        bytesPerPixLAbel.prefWidth = 100.0;

        bytesPerPixel = TextField()
        bytesPerPixel.text = this.readSettings.bytesPerPixel.toString()
        bytesPerPixel.isDisable = this.autoMode
        bytesPerPixel.textProperty().addListener { observableValue, old, newValue ->
            if (newValue.matches(Regex("\\d"))) {
                bytesPerPixel.text = newValue.replace("[^\\d]", "")
                readSettings.bytesPerPixel = bytesPerPixel.text.toInt()
            }
        }

        hBox = HBox()
        hBox.padding = Insets(5.0, 5.0, 5.0, 5.0)
        hBox.spacing = 10.0
        hBox.children.addAll(bytesPerPixLAbel,bytesPerPixel)
        box.children.add(hBox)

        postProcess = CheckBox()
        postProcess.text = "Post Process"
        postProcess.isSelected = readSettings.postProcess
        postProcess.selectedProperty().addListener { observableValue, old, new ->
            readSettings.postProcess = new
            postProcessCommand.isDisable = !new
        }

        postProcessCommand = TextField()
        postProcessCommand.minWidth = 300.0
        postProcessCommand.text = this.readSettings.postProcessCommand
        postProcessCommand.isDisable = !readSettings.postProcess
        postProcessCommand.textProperty().addListener { observableValue, old, newValue ->
            postProcessCommand.text = newValue
            readSettings.postProcessCommand = newValue
        }

        hBox = HBox()
        hBox.padding = Insets(5.0, 5.0, 5.0, 5.0)
        hBox.spacing = 10.0
        hBox.children.addAll(postProcess, postProcessCommand)
        box.children.add(hBox)

        val drawBtn = Button("Export")
        drawBtn.setOnAction { e ->
            val bitReader = BitReader()
            if (bitReader.validate(readSettings)) {
                bitReader.readTo(readSettings)
            }
        }

        hBox = HBox()
        hBox.padding = Insets(5.0, 5.0, 5.0, 5.0)
        hBox.spacing = 10.0
        hBox.children.addAll(drawBtn)
        box.children.add(hBox)

        val scene = Scene(box, 500.0, 390.0)
        stage.scene = scene
        stage.title = "Tomey Reader"
        stage.show()
    }
}
