package org.littlegit.client.ui.util

import javafx.scene.control.TextInputControl
import tornadofx.*

fun TextInputControl.blurValidator(validator: ValidationContext.(String?) -> ValidationMessage?)
        = tornadofx.validator(this, textProperty(), tornadofx.ValidationTrigger.OnBlur, validator)