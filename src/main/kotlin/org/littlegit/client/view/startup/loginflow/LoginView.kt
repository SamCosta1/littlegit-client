package org.littlegit.client.view.startup.loginflow

import javafx.beans.property.SimpleStringProperty
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.engine.model.Language
import org.littlegit.client.view.BaseView
import org.littlegit.client.view.MainView
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
                label(localizer.observable(I18nKey.Login))
                label(localizer.observable(I18nKey.Email))
                field {
                    textfield(email).required()
                }
                label(localizer.observable(I18nKey.Password))
                field {
                    textfield(password).required()
                }
                button(localizer.observable(I18nKey.Login)) {
                    enableWhen(model.valid)
                    action {
                        authController.login(email.value, password.value) {
                            replaceWith(MainView::class)
                        }
                    }
                }
                button("english").action {
                    localizer.updateLanguage(Language.English)
                }
                button("welsh").action {
                    localizer.updateLanguage(Language.Welsh)
                }
                label(message)
            }
        }
    }
}

