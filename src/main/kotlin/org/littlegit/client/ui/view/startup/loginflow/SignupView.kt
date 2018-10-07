package org.littlegit.client.ui.view.startup.loginflow

import javafx.beans.property.SimpleStringProperty
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.util.ValidationUtils
import org.littlegit.client.ui.util.blurValidator
import org.littlegit.client.ui.view.BaseView
import org.littlegit.client.ui.view.LoginFlowView
import org.littlegit.client.ui.view.MainView
import tornadofx.*

class SignupView: BaseView() {

    private val authController: AuthController by inject()

    private val model = ViewModel()
    private val email = model.bind { SimpleStringProperty() }
    private val password = model.bind { SimpleStringProperty() }
    private val name = model.bind { SimpleStringProperty() }

    override val root = vbox {
        form {
            fieldset {
                label(localizer.observable(I18nKey.Signup))
                label(localizer.observable(I18nKey.Email))
                field {
                    textfield(email).blurValidator {
                        if (it.isNullOrBlank() || !ValidationUtils.validateEmail(it!!)) {
                            error(localizer[I18nKey.InvalidEmail])
                        } else null
                    }
                }
                label(localizer.observable(I18nKey.Password))
                field {
                    textfield(password).blurValidator {
                        if (it.isNullOrBlank() || !ValidationUtils.validatePassword(it!!)) {
                            error(localizer[I18nKey.InvalidPassword])
                        } else null
                    }
                }
                label(localizer.observable(I18nKey.Name))
                field {
                    textfield(name).blurValidator {
                        when {
                            it.isNullOrBlank() -> error(localizer[I18nKey.FirstNameBlank])
                            it!!.length > 15 -> error(localizer[I18nKey.FirstNameTooLong])
                            else -> null
                        }
                    }
                }

                button(localizer.observable(I18nKey.Signup)) {
                    enableWhen(model.valid)
                    action {
                        authController.signup(email.value, password.value, name.value) {
                            if (it.isSuccess) {
                                replaceWith(MainView::class)
                            } else {

                            }
                        }
                    }
                }
            }
        }
    }
}