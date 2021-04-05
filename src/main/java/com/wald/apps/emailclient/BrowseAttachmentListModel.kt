package com.wald.apps.emailclient

import jakarta.activation.DataSource
import javax.swing.DefaultListModel


/**
 * @author vkosolapov
 * @since
 */
class BrowseAttachmentListModel(attachmentSources: List<DataSource>) : DefaultListModel<IncomingAttachment>() {
    init {
        attachmentSources.forEach {
            addElement(IncomingAttachment(it))
        }
    }
}

class IncomingAttachment(val datasource: DataSource) {
    override fun toString() = datasource.name
}