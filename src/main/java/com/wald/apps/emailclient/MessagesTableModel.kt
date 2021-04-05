package com.wald.apps.emailclient

import javax.swing.table.AbstractTableModel
import com.wald.apps.emailclient.MessagesTableModel
import jakarta.mail.Message
import jakarta.mail.internet.InternetAddress
import java.lang.Exception
import java.util.ArrayList

// This class manages the e-mail table's data.
class MessagesTableModel : AbstractTableModel() {
    private val messageList = ArrayList<Message>()

    fun addMessages(newMessages: Iterable<Message>) {
        for (newMessage in newMessages) {
            messageList.add(0, newMessage)
        }
        fireTableDataChanged()
    }

    fun removeMessages(removedMessages: Iterable<Message>) {
        for (removedMessage in removedMessages) {
            messageList.remove(removedMessage)
        }
        fireTableDataChanged()
    }

    fun setMessages(messages: Array<Message>) {
        for (i in messages.indices.reversed()) {
            messageList.add(messages[i])
        }
        fireTableDataChanged()
    }

    fun getMessage(row: Int) = messageList[row]

    fun deleteMessage(row: Int) {
        messageList.removeAt(row)
        fireTableRowsDeleted(row, row)
    }

    override fun getColumnCount() = columnNames.size

    override fun getColumnName(col: Int) = columnNames[col]

    override fun getRowCount() = messageList.size

    override fun getValueAt(row: Int, col: Int): Any {
        try {
            val message = messageList[row]
            when (col) {
                0 -> {
                    val senders = message.from
                    return if (senders != null && senders.size > 0) {
                        val senderAddress = senders[0]
                        if (senderAddress is InternetAddress) {
                            senderAddress.toUnicodeString()
                        } else senderAddress.toString()
                    } else {
                        "[none]"
                    }
                }
                1 -> {
                    val subject = message.subject
                    return if (subject != null && subject.length > 0) {
                        subject
                    } else {
                        "[none]"
                    }
                }
                2 -> {
                    val date = message.sentDate
                    return date?.toString() ?: "[none]"
                }
            }
        } catch (e: Exception) {
            return ""
        }
        return ""
    }

    companion object {
        private val columnNames = arrayOf("Sender", "Subject", "Date")
    }
}