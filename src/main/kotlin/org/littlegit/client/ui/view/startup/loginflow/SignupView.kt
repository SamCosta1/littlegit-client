package org.littlegit.client.ui.view.startup.loginflow

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import org.littlegit.client.engine.api.CallFailure
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.app.Styles
import org.littlegit.client.ui.util.*
import org.littlegit.client.ui.view.BaseView
import org.littlegit.client.ui.view.MainView
import org.littlegit.client.ui.view.modal.NoNetworkConnectionModal
import tornadofx.*

class SignupView: BaseView() {

    private val authController: AuthController by inject()

    private val model = ViewModel()
    private val email = model.bind { SimpleStringProperty() }
    private val password = model.bind { SimpleStringProperty() }
    private val name = model.bind { SimpleStringProperty() }

    private var emailError: I18nKey? = null
    private var nameError: I18nKey? = null
    private var passwordError: I18nKey? = null

    override val root = borderpane {
        addClass(Styles.loginFlow)
        top {
            borderpane().center {

                imageView(Image.WelshFlag)
            }
        }
        bottom {
            form {
                fieldset {
                    spacing = 15.0
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

                    field(orientation = Orientation.VERTICAL) {
                        secondarylabel(localizer.observable(I18nKey.Name))
                        textfield(name) {
                            promptText = "Frodo"
                            validator {
                                when {
                                    it.isNullOrBlank() -> error(localizer[I18nKey.FirstNameBlank])
                                    it!!.length > 15 -> error(localizer[I18nKey.FirstNameTooLong])
                                    else ->  {
                                    nameError?.let {
                                        val err = error(localizer[it])
                                        nameError = null
                                        err
                                    }
                                }
                                }
                            }
                        }
                    }

                    button(localizer.observable(I18nKey.Signup)) {

                        useMaxWidth = true
                        action {
                            authController.signup(email.value, password.value, name.value) {
                                when {
                                    it.isSuccess -> replaceWith(MainView::class)
                                    it.errorBody is CallFailure.ApiError -> {
                                        emailError = it.errorBody.localisedMessage.findOneOf(I18nKey.EmailInUse, I18nKey.InvalidEmail)
                                        passwordError = it.errorBody.localisedMessage.findOneOf(I18nKey.InvalidPassword)
                                        nameError = it.errorBody.localisedMessage.findOneOf(I18nKey.FirstNameTooLong, I18nKey.FirstNameBlank)

                                        model.validationContext.validate()
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