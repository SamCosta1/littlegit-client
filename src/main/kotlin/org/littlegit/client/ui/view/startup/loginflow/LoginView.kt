package org.littlegit.client.ui.view.startup.loginflow

import javafx.beans.property.SimpleStringProperty
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.engine.model.Language
import org.littlegit.client.ui.app.Styles
import org.littlegit.client.ui.util.Image
import org.littlegit.client.ui.util.NavigationUtils
import org.littlegit.client.ui.util.imageView
import org.littlegit.client.ui.view.BaseView
import org.littlegit.client.ui.view.MainView
import tornadofx.*

class LoginView : BaseView() {

    private val authController: AuthController by inject()

    private val model = ViewModel()
    private val email = model.bind { SimpleStringProperty() }
    private val password = model.bind { SimpleStringProperty() }
    private val message = model.bind { SimpleStringProperty() }


    override val root = borderpane {
        addClass(Styles.loginFlow)
        top {
            borderpane {
                left {
                    label(localizer.observable(I18nKey.Login)) {
                        addClass(Styles.heading)
                    }
                }
                right {
                    button(localizer.observable(I18nKey.Signup)).action {
                        replaceWith(SignupView::class)
                    }
                }
            }
        }
        center {
            imageView(Image.WelshFlag)
        }
        bottom {
            form {
                fieldset {
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
                        useMaxWidth = true
                        action {
                            authController.login(email.value, password.value) {
                                NavigationUtils.navigateFromLoginFlow(this@LoginView, repoController)
                            }
                        }
                    }
                    label(message)
                }
            }
        }
    }
}

