package com.wald.apps.emailclient

import jakarta.activation.DataSource
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridBagLayout
import java.io.File
import java.nio.file.Files
import javax.swing.*


/**
 * @author vkosolapov
 */
class BrowseAttachmentDialog(parent: JFrame?, attachments: List<DataSource>) : JDialog(parent) {

    private val attachmentList: JList<IncomingAttachment>

    private val attachmentListModel: BrowseAttachmentListModel

    init {
        val fieldsPanel = JPanel().apply {
            setLocationRelativeTo(parent)

            layout = GridBagLayout()

            val helper = GridBagWrapper()

            attachmentListModel = BrowseAttachmentListModel(attachments)
            attachmentList = JList(attachmentListModel)

            attachmentList.fixedCellWidth = 460
            attachmentList.border = BorderFactory.createTitledBorder("Attachment list")
            helper.nextCell().fillHorizontally().setInsets(20, 10, 20, 10).fillBoth()
            add(JScrollPane(attachmentList), helper.get())

            helper.nextRow().setInsets(80, 5, 80, 5)
            val fileListButtons = JPanel()
            fileListButtons.border = BorderFactory.createLineBorder(Color.YELLOW)
            fileListButtons.layout = BorderLayout(10, 10)

            val saveAsFileButton = JButton("Save")
            saveAsFileButton.addActionListener {
                val fileDialog = JFileChooser(System.getenv("user.home"))
                val attachment = attachmentList.selectedValue
                val name = attachment.datasource.name

                fileDialog.dialogTitle = "Save attached file"
                fileDialog.selectedFile = File(System.getenv("user.home") + "/$name")

                val returnVal = fileDialog.showSaveDialog(this)
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    val file = fileDialog.selectedFile

                    kotlin.runCatching {
                        Files.deleteIfExists(file.toPath())
                        Files.write(file.toPath(), attachment.datasource.inputStream.readAllBytes())
                    }.onFailure { it.printStackTrace() }
                }
            }

            add(saveAsFileButton, helper.get())
        }

        // Add panels to display.
        contentPane.layout = BorderLayout()
        contentPane.add(fieldsPanel, BorderLayout.CENTER)

        // Size dialog to components.
        pack()
    }
}