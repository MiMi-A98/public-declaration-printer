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

fun main(args: Array<String>) {

    if (args.isEmpty()) {
        println("Empty parameter for main method!")
        return
    }
    val sourceDir = File(args[0])
    if (!sourceDir.exists() || !sourceDir.isDirectory) {
        println("Provided path is not a directory")
        return
    }
    val environment = createEnvironment()
    val kotlinFiles = getKotlinFiles(sourceDir)

    kotlinFiles.forEach { file ->
        val ktFile = parseFile(file, environment)
        ktFile.declarations.filterIsInstance<KtNamedDeclaration>()
            .filter(::isPublic)
            .forEach(::printPublicDeclarations)
    }
}

fun getKotlinFiles(dir: File): List<File> =
    dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

fun createEnvironment(): KotlinCoreEnvironment {
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

fun parseFile(file: File, enviroment: KotlinCoreEnvironment): KtFile {
    val fileContent = file.readText()
    return KtPsiFactory(enviroment.project).createFile(file.name, fileContent)
}

fun isPublic(declaration: KtNamedDeclaration): Boolean {
    val modifierList = (declaration as? KtModifierListOwner)?.modifierList
    return modifierList == null || modifierList.hasModifier(KtTokens.PUBLIC_KEYWORD)
}

fun printPublicDeclarations(declaration: KtDeclaration) {
    println(declaration.text.substringBefore("{").trim())
}
