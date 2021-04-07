package com.wald.apps.emailclient

import jakarta.activation.DataSource
import jakarta.mail.Message
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.*

// This class displays the dialog used for creating messages.
class MessageDialog(parent: Frame, direction: MessageDirection, priorMessage: MessageContext?) : JDialog(parent, true) {
    private val fromTextField: JTextField
    private val toTextField: JTextField
    private val subjectTextField: JTextField
    private val contentTextArea: JTextArea

    var attachedFiles: List<File> = emptyList()
    var resentMessage: Message? = null

    private var cancelled = false

    // Validate message fields and close dialog.
    private fun actionSend() {
        if (fromTextField.text.isBlank()
            || toTextField.text.isBlank()
            || subjectTextField.text.isBlank()
            || contentTextArea.text.isBlank()) {
            JOptionPane.showMessageDialog(
                this,
                "One or more fields is missing.",
                "Missing Field(s)", JOptionPane.ERROR_MESSAGE
            )
            return
        }

        // Close dialog.
        dispose()
    }

    // Cancel creating this message and close dialog.
    private fun actionCancel() {
        cancelled = true

        // Close dialog.
        dispose()
    }

    // Show dialog.
    fun display(): Boolean {
        show()

        // Return whether or not display was successful.
        return !cancelled
    }

    // Get message's "From" field value.
    val from: String
        get() = fromTextField.text

    // Get message's "To" field value.
    val to: String
        get() = toTextField.text

    // Get message's "Subject" field value.
    val subject: String
        get() = subjectTextField.text

    // Get message's "content" field value.
    val content: String
        get() = contentTextArea.text

    companion object;

    // Constructor for dialog.
    init {

        /* Set dialog title and get message's "to", "subject"
       and "content" values based on message type. */
        var to: String = ""
        var subject: String? = ""
        var content: String = ""
        when (direction) {
            MessageDirection.REPLY -> {
                title = "Reply To Message"

                // Get message "to" value
                val senders = priorMessage!!.message.from
                if (senders != null && senders.isNotEmpty()) {
                    to = senders[0].toString()
                }
                to = priorMessage.message.from[0].toString()

                // Get message subject.
                subject = priorMessage.message.subject
                if (subject != null && subject.isNotEmpty()) {
                    subject = "RE: $subject"
                } else {
                    subject = "RE:"
                }

                // Get message content and add "REPLIED TO" notation.
                content = ("\n----------------- " +
                        "REPLIED TO MESSAGE" +
                        " -----------------\n" +
                        priorMessage.content.prioritisedPlainText())

                resentMessage = priorMessage.message
            }
            MessageDirection.FORWARD -> {
                title = "Forward Message"

                // Get message subject.
                subject = priorMessage!!.message.subject
                if (subject != null && subject.isNotEmpty()) {
                    subject = "FWD: $subject"
                } else {
                    subject = "FWD:"
                }

                // Get message content and add "FORWARDED" notation.
                content = ("\n----------------- " +
                        "FORWARDED MESSAGE" +
                        " -----------------\n" +
                        priorMessage.content.prioritisedPlainText())

                resentMessage = priorMessage.message
            }
            MessageDirection.NEW -> {
                title = "New Message"
            }
        }

        // Handle closing events.
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                actionCancel()
            }
        })

        // Setup fields panel.
        val fieldsPanel = JPanel()
        var constraints: GridBagConstraints
        val layout = GridBagLayout()
        fieldsPanel.layout = layout
        val fromLabel = JLabel("From:")
        constraints = GridBagConstraints()
        constraints.anchor = GridBagConstraints.EAST
        constraints.insets = Insets(5, 5, 0, 0)
        layout.setConstraints(fromLabel, constraints)
        fieldsPanel.add(fromLabel)
        fromTextField = JTextField()
        constraints = GridBagConstraints()
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridwidth = GridBagConstraints.REMAINDER
        constraints.insets = Insets(5, 5, 0, 0)
        layout.setConstraints(fromTextField, constraints)
        fieldsPanel.add(fromTextField)
        val toLabel = JLabel("To:")
        constraints = GridBagConstraints()
        constraints.anchor = GridBagConstraints.EAST
        constraints.insets = Insets(5, 5, 0, 0)
        layout.setConstraints(toLabel, constraints)
        fieldsPanel.add(toLabel)
        toTextField = JTextField(to)
        constraints = GridBagConstraints()
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridwidth = GridBagConstraints.REMAINDER
        constraints.insets = Insets(5, 5, 0, 0)
        constraints.weightx = 1.0
        layout.setConstraints(toTextField, constraints)
        fieldsPanel.add(toTextField)
        val subjectLabel = JLabel("Subject:")
        constraints = GridBagConstraints()
        constraints.insets = Insets(5, 5, 5, 0)
        layout.setConstraints(subjectLabel, constraints)
        fieldsPanel.add(subjectLabel)
        subjectTextField = JTextField(subject)
        constraints = GridBagConstraints()
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridwidth = GridBagConstraints.REMAINDER
        constraints.insets = Insets(5, 5, 5, 0)
        layout.setConstraints(subjectTextField, constraints)
        fieldsPanel.add(subjectTextField)

        // Setup content panel.
        val contentPanel = JScrollPane()
        contentTextArea = JTextArea(content, 10, 50)
        contentPanel.setViewportView(contentTextArea)

        // Setup buttons panel.
        val buttonsPanel = JPanel()
        val sendButton = JButton("Send")
        sendButton.addActionListener { actionSend() }
        buttonsPanel.add(sendButton)
        val cancelButton = JButton("Cancel")
        cancelButton.addActionListener { actionCancel() }
        buttonsPanel.add(cancelButton)
        val fileDialog = JFileChooser()
        val showFileDialogButton = JButton("Open File")
        showFileDialogButton.addActionListener {
            val returnVal = fileDialog.showOpenDialog(buttonsPanel)
            if (returnVal == JFileChooser.APPROVE_OPTION) {
            }
        }

        val chooseAttachmentsButton = JButton("Attach")
        chooseAttachmentsButton.addActionListener {
            val attachmentDialog = ManageAttachmentDialog(this, attachedFiles)
            attachmentDialog.show()
            attachedFiles = attachmentDialog.filesToAttach
        }
        buttonsPanel.add(chooseAttachmentsButton)

        // Add panels to display.
        contentPane.layout = BorderLayout()
        contentPane.add(fieldsPanel, BorderLayout.NORTH)
        contentPane.add(contentPanel, BorderLayout.CENTER)
        contentPane.add(buttonsPanel, BorderLayout.SOUTH)

        // Size dialog to components.
        pack()

        // Center dialog over application.
        setLocationRelativeTo(parent)
    }
}