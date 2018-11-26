package org.littlegit.client.ui.view.startup.loginflow

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import org.littlegit.client.engine.api.CallFailure
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.controller.SShController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.app.Styles
import org.littlegit.client.ui.util.*
import org.littlegit.client.ui.view.BaseView
import org.littlegit.client.ui.view.modal.NoNetworkConnectionModal
import tornadofx.*

class LoginView : BaseView() {

    private val authController: AuthController by inject()
    private val sshController: SShController by inject()

    private val model = ViewModel()
    private val email = model.bind { SimpleStringProperty() }
    private val password = model.bind { SimpleStringProperty() }
    private val message = model.bind { SimpleStringProperty() }

    private var emailError: I18nKey? = null
    private var passwordError: I18nKey? = null

    override val root = borderpane {
        addClass(Styles.primaryBackground)
        addClass(Styles.primaryPadding)
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
            //imageView(Image.Logo)
            label("Logo goes here")
        }
        bottom {
            form {
                spacing = 15.0
                fieldset {
                    field(orientation = Orientation.VERTICAL) {
                        secondarylabel(localizer.observable(I18nKey.Email))
                        textfield(email) {
                            promptText = "frodo.baggins@gmail.com"
                            validator {
                                if (it.isNullOrBlank() || !ValidationUtils.validateEmail(it!!)) {
                                    error(localizer[I18nKey.InvalidEmail])
                                } else {
                                    emailError?.let {
                                        val err = error(localizer[it])
                                        emailError = null
                                        err
                                    }
                                }
                            }
                        }
                    }

                    field(orientation = Orientation.VERTICAL) {
                        secondarylabel(localizer.observable(I18nKey.Password))
                        passwordfield(password) {
                            promptText = "*****"
                            validator {
                                if (it.isNullOrBlank() || !ValidationUtils.validatePassword(it!!)) {
                                    error(localizer[I18nKey.InvalidPassword])
                                } else {
                                    passwordError?.let {
                                        val err = error(localizer[it])
                                        passwordError = null
                                        err
                                    }
                                }
                            }
                        }
                    }

                    label(message) {
                        addClass(Styles.error)
                        visibleWhen { message.isNotBlank() }
                    }

                    button(localizer.observable(I18nKey.Login)) {
                        enableWhen(model.valid)
                        useMaxWidth = true
                        action {
                            authController.login(email.value, password.value) {
                                when {
                                    it.isSuccess -> {
                                        sshController.generateAndAddSshKey {
                                            NavigationUtils.navigateFromLoginFlow(this@LoginView, repoController)

                                            if (!it.isSuccess) {
                                                // TODO: Warn the user something went wrong
                                            }
                                        }
                                    }
                                    it.errorBody is CallFailure.ApiError -> {
                                        if (it.errorBody.errorCode == 401) {
                                            message.value = localizer[I18nKey.IncorrectLoginDetails]
                                        } else {
                                            emailError = it.errorBody.localisedMessage.findOneOf(I18nKey.EmailInUse, I18nKey.InvalidEmail)
                                            passwordError = it.errorBody.localisedMessage.findOneOf(I18nKey.InvalidPassword)

                                            if (emailError == null && passwordError == null) {
                                                message.value = localizer[I18nKey.UnknownError]
                                            }
                                            model.validationContext.validate()
                                        }

                                    }
                                    else -> find<NoNetworkConnectionModal>().openModal()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

