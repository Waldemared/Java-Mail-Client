package com.wald.apps.emailclient

import com.wald.apps.emailclient.configuration.MailService
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import kotlin.system.exitProcess


/* This class displays a dialog for entering e-mail
  server connection settings. */
class ConnectDialog(parent: Frame, serviceProviders: List<MailService>) : JDialog(parent, true) {
    // -- UI Items --
    // Combo box for e-mail server types.

    private val serviceComboBox: JComboBox<MailService>

    // Server, username and SMTP server text fields.
    private val usernameTextField: JTextField

    // Password text field.
    private val passwordField: JPasswordField
    
    val service: MailService
        get() = serviceComboBox.selectedItem as MailService

    // Get e-mail username.
    val username: String
        get() = usernameTextField.text

    // Get e-mail password.
    val password: String
        get() = String(passwordField.password)

    // Validate connection settings and close dialog.
    private fun actionConnect() {
        if (username.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(
                this,
                "One or more settings is missing.",
                "Missing Setting(s)", JOptionPane.ERROR_MESSAGE
            )
            return
        }

        // Close dialog.
        dispose()
    }

    // Cancel connecting and exit program.
    private fun actionCancel() {
        exitProcess(0)
    }
    // Constructor for dialog.
    init {
        // Set dialog title.
        title = "Connect"

        // Handle closing events.
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                actionCancel()
            }
        })

        // Setup settings panel.
        val settingsPanel = JPanel()
        settingsPanel.border = BorderFactory.createTitledBorder("Connection Settings")
        val layout = GridBagLayout()
        settingsPanel.layout = layout
        val configLabel = JLabel("Connect to:")
        layout.constraintsOn(configLabel) {
            anchor = GridBagConstraints.EAST
            insets = Insets(5, 5, 0, 0)
        }
        settingsPanel.add(configLabel)
        serviceComboBox = JComboBox<MailService>(serviceProviders.toTypedArray())
        layout.constraintsOn(serviceComboBox) {
            anchor = GridBagConstraints.WEST
            gridwidth = GridBagConstraints.REMAINDER
            insets = Insets(5, 5, 0, 5)
            weightx = 1.0
        }
        settingsPanel.add(serviceComboBox)

        val usernameLabel = JLabel("Username:")
        layout.constraintsOn(usernameLabel) {
            anchor = GridBagConstraints.EAST
            insets = Insets(5, 5, 0, 0)
        }
        settingsPanel.add(usernameLabel)
        usernameTextField = JTextField()
        layout.constraintsOn(usernameTextField) {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            gridwidth = GridBagConstraints.REMAINDER
            insets = Insets(5, 5, 0, 5)
            weightx = 1.0
        }
        settingsPanel.add(usernameTextField)
        val passwordLabel = JLabel("Password:")
        layout.constraintsOn(passwordLabel) {
            anchor = GridBagConstraints.EAST
            insets = Insets(5, 5, 5, 0)
        }
        settingsPanel.add(passwordLabel)
        passwordField = JPasswordField()
        layout.constraintsOn(passwordField) {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            gridwidth = GridBagConstraints.REMAINDER
            insets = Insets(5, 5, 5, 5)
            weightx = 1.0
        }
        settingsPanel.add(passwordField)

        // Setup buttons panel.
        val buttonsPanel = JPanel()
        val connectButton = JButton("Connect")
        connectButton.addActionListener { actionConnect() }
        buttonsPanel.add(connectButton)
        val cancelButton = JButton("Cancel")
        cancelButton.addActionListener { actionCancel() }
        buttonsPanel.add(cancelButton)

        // Add panels to display.
        contentPane.layout = BorderLayout()
        contentPane.add(settingsPanel, BorderLayout.CENTER)
        contentPane.add(buttonsPanel, BorderLayout.SOUTH)

        // Size dialog to components.
        pack()

        // Center dialog over application.
        setLocationRelativeTo(parent)
    }

    private fun GridBagLayout.constraintsOn(component: Component, constraintsConstructor: GridBagConstraints.() -> Unit) {
        val constraints = GridBagConstraints().apply(constraintsConstructor)
        this.setConstraints(component, constraints)
    }
}