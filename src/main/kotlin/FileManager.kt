package Mimi_98

import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import java.io.File

class FileManager {

    fun processKotlinFiles(sourceFile: File) {
        val environment = createEnvironment()
        val kotlinFiles = getKotlinFiles(sourceFile)

        if (kotlinFiles.isEmpty()) {
            println("Warning: No Kotlin files found in '${sourceFile.path}'.")
            return
        }

        kotlinFiles.forEach { file ->
            val psiFile = createPsiFile(file, environment)

            if (psiFile.declarations.isEmpty()) {
                println("Info: No declarations found in ${file.name}")
            } else {
                psiFile.declarations.forEach { declaration ->
                    printPublicDeclarations(declaration)
                }
            }
        }
    }

    private fun createEnvironment(): KotlinCoreEnvironment {
        setIdeaIoUseFallback()

        val configuration = CompilerConfiguration().apply {
            put(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        }

        return KotlinCoreEnvironment.createForProduction(
            Disposer.newDisposable(),
            configuration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
    }

    private fun getKotlinFiles(dir: File): List<File> =
        dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

    private fun createPsiFile(file: File, environment: KotlinCoreEnvironment): KtFile {
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
