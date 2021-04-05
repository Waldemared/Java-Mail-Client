package com.wald.apps.emailclient

import com.wald.apps.emailclient.mime.ParsedMessage
import jakarta.mail.Message


/**
 * @author vkosolapov
 */
data class MessageContext(val message: Message, val content: ParsedMessage)