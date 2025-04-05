import Mimi_98.FileManager
import java.io.File

fun main(args: Array<String>) {

    if (args.isEmpty()) {
        println("Error: Empty parameter for main method!")
        return
    }

    val sourceDirectory = File(args[0])
    if (!sourceDirectory.exists() || !sourceDirectory.isDirectory) {
        println("Error: Provided path '${sourceDirectory.path}' is not a directory or it does not exists")
        return
    }

    val fileManager = FileManager()
    fileManager.processKotlinFiles(sourceDirectory)
}

