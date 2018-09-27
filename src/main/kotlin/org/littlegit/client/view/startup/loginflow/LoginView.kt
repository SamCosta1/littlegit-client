package org.littlegit.client.view.startup.loginflow

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.view.BaseView
import tornadofx.*

class LoginView : BaseView() {

    private val authController: AuthController by inject()

    private val model = ViewModel()
    private val email = model.bind { SimpleStringProperty() }
    private val password = model.bind { SimpleStringProperty() }
    private val message = model.bind { SimpleStringProperty() }


    override val root = vbox {
        form {
            fieldset {
                label(localizer[I18nKey.Login])
                field(localizer[I18nKey.Email]) {
                    textfield(email).required()
                }
                field(localizer[I18nKey.Password]) {
                    textfield(password).required()
                }
                button(localizer[I18nKey.Login]) {
                    enableWhen(model.valid)
                    action {
                        authController.login(email.value, password.value) {
                            message.value = it.isSuccess.toString()
                        }
                    }
                }
                label(message)
            }
        }

    }
}

