package com.wald.apps.emailclient

import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.dnd.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.IOException
import javax.swing.*
import kotlin.system.exitProcess


/**
 * @author vkosolapov
 */
class ManageAttachmentDialog(parent: Dialog?, val initialFiles: List<File>) : JDialog(parent, true), DropTargetListener {

    protected var filesList: JList<OutgoingAttachment>

    private val fileListModel: ManageAttachmentListModel

    protected var removeFileButton: JButton

    var filesToAttach: List<File> = emptyList()

    init {
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                cancel()
                exitProcess(0)
            }
        })

        // Setup fields panel.
        val fieldsPanel = JPanel().apply {
            layout = GridBagLayout()

            val helper = GridBagWrapper()

            fileListModel = ManageAttachmentListModel(initialFiles)
            filesList = JList(fileListModel)
            filesList.dragEnabled = true
            val dropTarget = DropTarget(filesList, this@ManageAttachmentDialog)
            filesList.fixedCellWidth = 460
            helper.nextCell().fillHorizontally().setInsets(20, 10, 20, 10).fillBoth()
            add(JScrollPane(filesList), helper.get())

            helper.nextCell().gap(20)
            val fileListButtons = JPanel()
            fileListButtons.layout = BorderLayout(10, 10)

            val addFileButton = JButton("Add")
            val fileDialog = JFileChooser()
            fileDialog.isMultiSelectionEnabled = true
            addFileButton.addActionListener {
                val returnVal = fileDialog.showOpenDialog(this)
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    val files = fileDialog.selectedFiles.filter { it.canRead() && it.isFile }
                    val selectedFiles = files.map { OutgoingAttachment(it) }
                    fileListModel.addAll(selectedFiles)
                }
            }
            fileListButtons.add(addFileButton, BorderLayout.NORTH)

            removeFileButton = JButton("Remove")
            removeFileButton.isEnabled = false
            removeFileButton.addActionListener {
                val selectedFileIndexes = filesList.selectedIndices
                fileListModel.removeRange(selectedFileIndexes.first(), selectedFileIndexes.last())
                isEnabled = false
            }
            filesList.addListSelectionListener {
                removeFileButton.isEnabled = true
            }

            fileListButtons.add(removeFileButton, BorderLayout.SOUTH)


            add(fileListButtons, helper.get())
        }

        val actionButtons = JPanel().apply {
            val acceptButton = JButton("OK")
            acceptButton.addActionListener { confirm() }
            add(acceptButton)

            val cancelButton = JButton("Cancel")

            cancelButton.addActionListener(object : ActionListener {
                override fun actionPerformed(e: ActionEvent) {
                    cancel()
                }
            })

            add(cancelButton)
        }

        // Add panels to display.
        contentPane.layout = BorderLayout()
        contentPane.add(fieldsPanel, BorderLayout.CENTER)
        contentPane.add(actionButtons, BorderLayout.SOUTH)

        // Size dialog to components.
        pack()
    }

    override fun dragEnter(dtde: DropTargetDragEvent) = Unit

    override fun dragOver(dtde: DropTargetDragEvent) = Unit

    override fun dropActionChanged(dtde: DropTargetDragEvent) = Unit

    override fun dragExit(dte: DropTargetEvent) = Unit

    override fun drop(evt: DropTargetDropEvent) {
        val action: Int = evt.dropAction
        evt.acceptDrop(action)
        print("g")
        try {
            val data: Transferable = evt.transferable
            if (data.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                val files = data.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                for (file in files) {
                    fileListModel.addElement(OutgoingAttachment(file))
                }
            }
        } catch (e: UnsupportedFlavorException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            evt.dropComplete(true)
        }
    }

    private fun confirm() {
        filesToAttach = fileListModel.files
        dispose()
    }

    private fun cancel() {
        filesToAttach = initialFiles
        dispose()
    }
}