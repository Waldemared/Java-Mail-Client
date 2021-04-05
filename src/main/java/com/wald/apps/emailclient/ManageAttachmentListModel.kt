package com.wald.apps.emailclient

import java.io.File
import java.nio.file.Files
import javax.swing.DefaultListModel


/**
 * @author vkosolapov
 */
class ManageAttachmentListModel(initialFiles: List<File>) : DefaultListModel<OutgoingAttachment>() {
    val files: List<File>
        get() {
            return toArray()
                .map { it as OutgoingAttachment }
                .map { it.file }
        }

    init {
        val fileDataList = initialFiles.map { OutgoingAttachment(it) }
        addAll(fileDataList)
    }
}

class OutgoingAttachment(val file: File) {
    override fun toString(): String {
        val size = Files.size(file.toPath())
        val sizeString = sizeLabel(size)
        return "${file.name}.${file.extension}: $sizeString"
    }
}