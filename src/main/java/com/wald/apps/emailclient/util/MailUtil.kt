package com.wald.apps.emailclient.util

import com.wald.apps.emailclient.mime.ParsedMessage
import jakarta.activation.DataSource
import jakarta.mail.Multipart
import jakarta.mail.Part
import jakarta.mail.internet.MimePart


fun parse(part: MimePart): ParsedMessage {
    return runCatching {
        val content = part.content
        when {
            content is Multipart -> parseMultipart(part, content)
            Part.ATTACHMENT == part.disposition -> parseAttachment(part)
            else -> parseText(part, content)
        }
    }.onFailure {
        it.printStackTrace()
        throw it
    }.getOrNull()!!
}

fun parseText(part: Part, content: Any): ParsedMessage {
    return when {
        part.isMimeType(TEXT_HTML) -> ParsedMessage(html = content.toString())
        else -> ParsedMessage(plain = content.toString())
    }
}

fun parseMultipart(part: MimePart, content: Multipart): ParsedMessage {
    return when {
        part.isMimeType(MULTIPART_ALTERNATIVE) -> parseAlternative(content)
        part.isMimeType(MULTIPART_MIXED) -> parseMixed(content)
        else -> parseMixed(content)
    }
}

fun parseAttachment(part: MimePart): ParsedMessage {
    val dataSource = part.dataHandler.dataSource
    return ParsedMessage(attachments = listOf(dataSource))
}

fun parseAlternative(content: Multipart): ParsedMessage {
    val plain = StringBuilder()
    val html = StringBuilder()

    for (i in 0 until content.count) {
        val part = content.getBodyPart(i)
        if (part.isMimeType(TEXT_HTML)) {
            html.append(part.content)
        } else {
            plain.append(part.content)
        }
    }

    return ParsedMessage(
        plain = plain.toString().takeIf { it.isNotEmpty() },
        html = html.toString().takeIf { it.isNotEmpty() }
    )
}

fun parseMixed(content: Multipart): ParsedMessage {
    val plain = StringBuilder()
    val html = StringBuilder()
    val attachments = mutableListOf<DataSource>()

    for (i in 0 until content.count) {
        val part = content.getBodyPart(i)
        val parsedPart = parse(part as MimePart)
        parsedPart.plain?.let { plain.append(it) }
        parsedPart.html?.let { html.append(it) }
        parsedPart.attachments?.let { attachments.addAll(it) }
    }

    return ParsedMessage(
        plain = plain.toString().takeIf { it.isNotEmpty() },
        html = html.toString().takeIf { it.isNotEmpty() },
        attachments = attachments
    )
}

const val TEXT_PLAIN = "text/plain"
const val TEXT_HTML = "text/html"
const val MULTIPART_ALTERNATIVE = "multipart/alternative"
const val MULTIPART_MIXED = "multipart/mixed"