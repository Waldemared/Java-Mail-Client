package com.wald.apps.emailclient

import java.awt.Frame
import javax.swing.BorderFactory
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel

/* This class displays a simple dialog instructing the user
   that messages are being downloaded. */
class DownloadingDialog(parent: Frame?) : JDialog(parent, true) {
    // Constructor for dialog.
    init {
        title = "E-mail Client"
        defaultCloseOperation = DO_NOTHING_ON_CLOSE

        val contentPane = JPanel()
        contentPane.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        contentPane.add(JLabel("Downloading messages..."))
        setContentPane(contentPane)

        pack()

        setLocationRelativeTo(parent)
    }
}