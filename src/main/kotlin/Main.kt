import Mimi_98.PublicDeclarationPrinter
import java.io.File
import java.io.FileNotFoundException

fun main(args: Array<String>) {

    require(args.isNotEmpty()) { "Error: Missing required target directory path parameter" }

    val sourceDirectory = File(args[0])

    require(sourceDirectory.isDirectory) { "Error: Provided path is not a directory" }

    if (!sourceDirectory.exists()) {
        throw FileNotFoundException("Error: Provided path does not exists")
    }

    PublicDeclarationPrinter().print(sourceDirectory)
}

