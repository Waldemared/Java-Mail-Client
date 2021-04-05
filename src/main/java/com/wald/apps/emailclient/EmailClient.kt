package com.wald.apps.emailclient

import com.wald.apps.emailclient.configuration.AuthConfig
import com.wald.apps.emailclient.configuration.MailConnectionConfig
import com.wald.apps.emailclient.util.TEXT_HTML
import com.wald.apps.emailclient.util.parse
import jakarta.activation.DataHandler
import jakarta.activation.FileDataSource
import jakarta.mail.*
import jakarta.mail.event.MessageCountEvent
import jakarta.mail.event.MessageCountListener
import jakarta.mail.internet.*
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.*
import javax.swing.text.JTextComponent
import kotlin.system.exitProcess

//import java.net.*;
// The E-mail Client.
internal class EmailClient : JFrame() {
    // Message table's data model.
    private val tableModel: MessagesTableModel

    // Table listing messages.
    private val table: JTable

    // This the text area for displaying messages.
    private var messageTextArea: JTextComponent

    /* This is the split panel that holds the messages
     table and the message view panel. */
    private val splitPane: JSplitPane

    // These are the buttons for managing the selected message.
    private val attachmentsButton: JButton
    private val replyButton: JButton
    private val forwardButton: JButton
    private val deleteButton: JButton

    // Currently selected message in table.
    private var selectedMessage: MessageContext? = null

    // Flag for whether or not a message is being deleted.
    private var deleting = false

    protected lateinit var scheduleService: ScheduledExecutorService

    lateinit var mailConfig: AuthConfig

    lateinit var senderProperties: Properties

    private var resourceReleasers = mutableListOf<() -> Unit>()

    // Constructor for E-mail Client.
    init {
        title = "E-mail Client"
        setSize(640, 480)
        setLocationRelativeTo(null)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                actionExit()
            }
        })

        // Setup file menu.
        val menuBar = JMenuBar()
        val fileMenu = JMenu("File")
        fileMenu.mnemonic = KeyEvent.VK_F
        val fileExitMenuItem = JMenuItem(
            "Exit",
            KeyEvent.VK_X
        )
        fileExitMenuItem.addActionListener { actionExit() }
        fileMenu.add(fileExitMenuItem)
        menuBar.add(fileMenu)
        jMenuBar = menuBar

        // Setup buttons panel.
        val buttonPanel = JPanel()
        val newButton = JButton("Compose Mail")
        newButton.addActionListener {
            try {
                actionNew()
            } catch (ex: MessagingException) {
                Logger.getLogger(EmailClient::class.java.name).log(Level.SEVERE, null, ex)
            }
        }
        buttonPanel.add(newButton)

        // Setup messages table.
        tableModel = MessagesTableModel()
        table = JTable(tableModel)
        table.selectionModel.addListSelectionListener { tableSelectionChanged() }
        // Allow only one row at a time to be selected.
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        // Setup E-mails panel.
        val emailsPanel = JPanel()
        emailsPanel.border = BorderFactory.createTitledBorder("E-mails")
        messageTextArea = JEditorPane("text/html", "")
        messageTextArea.isEditable = false
        splitPane = JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            JScrollPane(table), JScrollPane(messageTextArea),
        )
        emailsPanel.layout = BorderLayout()
        emailsPanel.add(splitPane, BorderLayout.CENTER)

        // Setup buttons panel 2.
        val buttonPanel2 = JPanel()

        attachmentsButton = JButton("Attachments")
        attachmentsButton.addActionListener {
            try {
                val browseAttachmentDialog = BrowseAttachmentDialog(this, selectedMessage!!.content.attachments!!)
                browseAttachmentDialog.isVisible = true
            } catch (ex: MessagingException) {
                Logger.getLogger(EmailClient::class.java.name).log(Level.SEVERE, null, ex)
            }
        }
        attachmentsButton.isEnabled = false
        buttonPanel2.add(attachmentsButton)

        replyButton = JButton("Reply")
        replyButton.addActionListener {
            try {
                actionReply()
            } catch (ex: MessagingException) {
                Logger.getLogger(EmailClient::class.java.name).log(Level.SEVERE, null, ex)
            }
        }
        replyButton.isEnabled = false
        buttonPanel2.add(replyButton)

        forwardButton = JButton("Forward")
        forwardButton.addActionListener {
            try {
                actionForward()
            } catch (ex: MessagingException) {
                Logger.getLogger(EmailClient::class.java.name).log(Level.SEVERE, null, ex)
            }
        }
        forwardButton.isEnabled = false
        buttonPanel2.add(forwardButton)

        deleteButton = JButton("Delete")
        deleteButton.addActionListener { actionDelete() }
        deleteButton.isEnabled = false
        buttonPanel2.add(deleteButton)

        // Add panels to display.
        contentPane.layout = BorderLayout()
        contentPane.add(buttonPanel, BorderLayout.NORTH)
        contentPane.add(emailsPanel, BorderLayout.CENTER)
        contentPane.add(buttonPanel2, BorderLayout.SOUTH)
    }

    // Connect to e-mail server.
    fun connect(services: List<MailConnectionConfig>) {
        val connectDialog = ConnectDialog(this, services)
        var mailConfig = requestAuthData(connectDialog)
        var store = connectToStore(mailConfig)
        while (store == null) {
            error("Failed to connect to the mail store")
            mailConfig = requestAuthData(connectDialog)
            store = connectToStore(mailConfig)
        }
        resourceReleasers.add { if (store.isConnected) store.close() }

        val downloadingDialog = DownloadingDialog(this)
        SwingUtilities.invokeLater { downloadingDialog.isVisible = true }

        try {
            val folder = store.getFolder("INBOX")
            folder.open(Folder.READ_WRITE)
            folder.addMessageCountListener(object : MessageCountListener {
                override fun messagesAdded(e: MessageCountEvent) {
                    tableModel.addMessages(e.messages.asIterable())
                }

                override fun messagesRemoved(e: MessageCountEvent) {
                    tableModel.removeMessages(e.messages.asIterable())
                }
            })

            // Get folder's list of messages.
            val messages = folder.messages

            // Retrieve message headers for each message in folder.
            val profile = FetchProfile()
            profile.add(FetchProfile.Item.ENVELOPE)
            folder.fetch(messages, profile)

            scheduleService = Executors.newScheduledThreadPool(4)
            val task = Runnable {
                kotlin.runCatching {
                    folder.newMessageCount
                }.onFailure { it.printStackTrace() }
            }
            scheduleService.scheduleAtFixedRate(task, 5, 10, TimeUnit.SECONDS)
            resourceReleasers.add { scheduleService.shutdown() }

            // Put messages in table.
            tableModel.setMessages(messages)

            this.mailConfig = mailConfig

            resourceReleasers.add { if (folder.isOpen) folder.close() }
        } catch (e: Exception) {
            e.printStackTrace()
            error("Failed to download messages.")
        }

        // Close the downloading dialog.
        downloadingDialog.dispose()
    }

    private fun requestAuthData(connectDialog: ConnectDialog)  : AuthConfig {
        connectDialog.isVisible = true

        return AuthConfig(
            connectionConfig = connectDialog.config,
            username = connectDialog.username,
            password = connectDialog.password
        )
    }

    private fun connectToStore(authConfig: AuthConfig): Store? {
        senderProperties = authConfig.connectionConfig.javaMailSenderProperties(
            user = authConfig.username,
            password = authConfig.password
        )

        val storageProperties = authConfig.connectionConfig.javaMailStorageProperties()
        val emailSession = Session.getDefaultInstance(storageProperties)

        try {
            val store = emailSession.getStore(authConfig.connectionConfig.javaMailStoreProtocol())
            store.connect(authConfig.username, authConfig.password)
            return store
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Exit this program.
    private fun actionExit() {
        resourceReleasers.forEach { it() }
        exitProcess(0)
    }

    // Create a new message.
    @Throws(MessagingException::class)
    private fun actionNew() {
        sendMessage(MessageDirection.NEW, null)
    }

    // Reply to a message.
    @Throws(MessagingException::class)
    private fun actionReply() {
        sendMessage(MessageDirection.REPLY, selectedMessage!!)
    }

    // Forward a message.
    @Throws(MessagingException::class)
    private fun actionForward() {
        sendMessage(MessageDirection.FORWARD, selectedMessage!!)
    }

    // Send the specified message.
    @Throws(MessagingException::class)
    private fun sendMessage(direction: MessageDirection, priorMessage: MessageContext?) {
        // Display message dialog to get message values.
        val dialog: MessageDialog
        try {
            dialog = MessageDialog(this, direction, priorMessage)
            if (!dialog.display()) {
                // Return if dialog was cancelled.
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
            error("Unable to send message.")
            return
        }

        // Get the default Session object.
        val session = Session.getInstance(senderProperties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(mailConfig.username, mailConfig.password)
            }
        })

        try {
            val newMessage = constructMessage(dialog, session)

            // Send message
            val transport = session.getTransport(mailConfig.connectionConfig.javaMailSenderProtocol())
            transport.connect(
                mailConfig.connectionConfig.senderProperties.host,
                mailConfig.connectionConfig.senderProperties.port,
                mailConfig.username,
                mailConfig.password
            )
            transport.sendMessage(newMessage, newMessage.getRecipients(Message.RecipientType.TO))
            transport.close()
            notifyOnEvent("Message Sent Successfully......")
        } catch (mex: MessagingException) {
            mex.printStackTrace()
            error("Unable to send message.")
        }
    }

    // Delete the selected message.
    private fun actionDelete() {
        deleting = true
        try {
            // Delete message from server.
            selectedMessage!!.message.setFlag(Flags.Flag.DELETED, true)
            selectedMessage!!.message.folder.close(true)
            selectedMessage!!.message.folder.open(Folder.READ_WRITE)
        } catch (e: Exception) {
            e.printStackTrace()
            error("Unable to delete message.")
        }

        // Delete message from table.
        tableModel.deleteMessage(table.selectedRow)

        // Update GUI.
        messageTextArea.text = ""
        deleting = false
        selectedMessage = null
        attachmentsButton.isEnabled = false
        updateButtons()
    }

    // table row selection changes.
    private fun tableSelectionChanged() {
        /* If not in the middle of deleting a message, set
       the selected message and display it. */
        if (!deleting) {
            val message = tableModel.getMessage(table.selectedRow)
            val parsed = parse(message as MimePart)
            selectedMessage = MessageContext(message, parsed)
            showSelectedMessage()
            updateButtons()
        }
    }

    // Show the selected message in the content panel.
    private fun showSelectedMessage() {
        // Show hour glass cursor while message is loaded.
        cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        try {
            val message = selectedMessage!!.content
            if (message.html != null) {
                messageTextArea = JEditorPane(TEXT_HTML, message.clearHtml())
                messageTextArea.text = message.clearHtml()
            } else {
                messageTextArea = JTextPane()
                messageTextArea.text = message.plain ?: ""
            }

            attachmentsButton.isEnabled = !message.attachments.isNullOrEmpty()

            splitPane.rightComponent = JScrollPane(messageTextArea)
            splitPane.setDividerLocation(0.5)
            splitPane.resetToPreferredSizes()

            messageTextArea.isEditable = false
            messageTextArea.caretPosition = 0

        } catch (e: Exception) {
            e.printStackTrace()
            error("Unabled to load message.")
        } finally {
            // Return to default cursor.
            cursor = Cursor.getDefaultCursor()
        }
    }

    /* Update each button's state based off of whether or not
     there is a message currently selected in the table. */
    private fun updateButtons() {
        if (selectedMessage != null) {
            replyButton.isEnabled = true
            forwardButton.isEnabled = true
            deleteButton.isEnabled = true
        } else {
            replyButton.isEnabled = false
            forwardButton.isEnabled = false
            deleteButton.isEnabled = false
        }
    }

    // Show the application window on the screen.
    override fun show() {
        super.show()

        // Update the split panel to be divided 50/50.
        splitPane.setDividerLocation(.5)
    }

    private fun constructMessage(dialog: MessageDialog, session: Session): MimeMessage {
        val newMessage = MimeMessage(session)
        newMessage.setFrom(InternetAddress(dialog.from))
        newMessage.setRecipient(Message.RecipientType.TO, InternetAddress(dialog.to))
        newMessage.subject = dialog.subject
        newMessage.sentDate = Date()

        val attachments = dialog.attachments
        val attachmentsToResend = dialog.resentAttachments
        if (attachments.isEmpty() && attachmentsToResend.isEmpty()) {
            newMessage.setText(dialog.content, Charsets.UTF_8.displayName().toLowerCase())
        } else {
            val contentPart = MimeBodyPart()
            contentPart.setText(dialog.content, Charsets.UTF_8.displayName().toLowerCase())

            val attachedParts = mutableListOf<MimeBodyPart>()

            attachments.forEach {
                val part = MimeBodyPart()
                val fileSource = FileDataSource(it)
                part.dataHandler = DataHandler(fileSource)
                part.fileName = "${it.name}.${it.extension}"
                attachedParts.add(part)
            }

            attachmentsToResend.forEach {
                val part = MimeBodyPart()
                part.dataHandler = DataHandler(it)
                part.fileName = it.name
                attachedParts.add(part)
            }

            val multipart = MimeMultipart()
            multipart.addBodyPart(contentPart)
            attachedParts.forEach { multipart.addBodyPart(it) }

            newMessage.setContent(multipart)
        }

        return newMessage
    }

    // Show error dialog and exit afterwards if necessary.
    private fun error(message: String) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE)
    }

    @Suppress("SameParameterValue")
    private fun notifyOnEvent(message: String) {
        JOptionPane.showMessageDialog(this, message, "Notify", JOptionPane.INFORMATION_MESSAGE)
    }
}