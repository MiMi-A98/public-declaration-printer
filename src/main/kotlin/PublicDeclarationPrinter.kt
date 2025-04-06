package Mimi_98

import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import java.io.File

class PublicDeclarationPrinter {

    fun print(sourceFile: File) {

        val environment = createKotlinEnvironment()
        val kotlinFiles = getKotlinFiles(sourceFile)

        if (kotlinFiles.isEmpty()) {
            println("Warning: No Kotlin files found in '${sourceFile.path}'.")
            return
        }

        kotlinFiles.forEach { file ->
            val psiFile = convertPsiFile(file, environment)

            if (psiFile.declarations.isEmpty()) {
                println("Info: No declarations found in ${file.name}")
            } else {
                psiFile.declarations.forEach { declaration ->
                    printPublicDeclarations(declaration)
                }
            }
            println() // Empty line to separate files
        }
    }

    /**
     * Creates a Kotlin environment for compiling and analyzing Kotlin code.
     *
     * This environment is required to parse Kotlin files into PSI structures. It sets up
     * a fallback I/O mechanism for compatibility outside the IntelliJ IDE and disables
     * message collection to suppress compiler output.
     * @return A configured KotlinCoreEnvironment
     */
    private fun createKotlinEnvironment(): KotlinCoreEnvironment {

        // Ensures the Kotlin compiler can safely access files when running outside of IntelliJ
        setIdeaIoUseFallback()

        val configuration = CompilerConfiguration().apply {
            put(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        }

        // Create a production environment configured for JVM projects
        return KotlinCoreEnvironment.createForProduction(
            Disposer.newDisposable(),
            configuration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
    }

    /**
     *  Collects all Kotlin source files from the given directory
     *
     * @param dir The root directory from where to get the files.
     * @return A list of File objects representing Kotlin source files.
     */
    private fun getKotlinFiles(dir: File): List<File> =
        dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

    /**
     * Read file content and converts it into a PSI file for further processing
     *
     * @param file The Kotlin source file to be parsed.
     * @param environment The Kotlin compiler environment, which provides the project context.
     * @return A KtFile representing the parsed structure of the Kotlin source file.
     */
    private fun convertPsiFile(file: File, environment: KotlinCoreEnvironment): KtFile {
        val fileContent = file.readText()
        return KtPsiFactory(environment.project).createFile(file.name, fileContent)
    }

    private fun printPublicDeclarations(declaration: KtDeclaration, indent: String = "") {

        when (declaration) {
            is KtClassOrObject -> {
                if (declaration.isPublic) {
                    println(indent + "class ${declaration.name} {")
                }
                declaration.declarations.forEach {
                    printPublicDeclarations(it, "$indent    ")
                }
                println("$indent}")
            }

            is KtNamedFunction -> {
                if (declaration.isPublic) {
                    println(indent + "fun ${declaration.name}()")
                }
            }

            is KtProperty -> {
                if (declaration.isPublic) {
                    println(indent + "${declaration.valOrVarKeyword.text} ${declaration.name}")
                }
            }
        }
    }
}
