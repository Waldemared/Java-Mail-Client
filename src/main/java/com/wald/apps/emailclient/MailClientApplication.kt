package com.wald.apps.emailclient

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.wald.apps.emailclient.configuration.*
import java.nio.file.Files
import javax.swing.JOptionPane
import kotlin.system.exitProcess


/**
 * @author vkosolapov
 */
fun main(args: Array<String>) {
    val client = EmailClient()
    client.show()

    val mapper = ObjectMapper()

    val servicesSource = java.nio.file.Path.of("services.json").toAbsolutePath()
    if (!Files.isRegularFile(servicesSource)) {
        errorAndTerminate("Failed to find description of available mail services.")
    }
    if (!Files.isReadable(servicesSource)) {
        errorAndTerminate("Cannot read description of available mail services.")
    }

    val services = runCatching {
        mapper.readValue(servicesSource.toFile(), object : TypeReference<List<MailConnectionConfig>>() {})
    }.onFailure { errorAndTerminate("Failed to scan description of available services.") }.getOrThrow()

    client.connect(services)
}

fun errorAndTerminate(message: String): Nothing {
    JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
    exitProcess(0)
}