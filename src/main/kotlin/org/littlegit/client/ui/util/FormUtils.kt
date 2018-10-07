package org.littlegit.client.ui.util

import javafx.beans.property.ObjectProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TextInputControl
import javafx.util.StringConverter
import org.littlegit.client.ui.app.Styles
import tornadofx.*

fun TextInputControl.blurValidator(validator: ValidationContext.(String?) -> ValidationMessage?)
        = tornadofx.validator(this, textProperty(), tornadofx.ValidationTrigger.OnBlur, validator)

inline fun <reified T> EventTarget.secondarylabel(
        observable: ObservableValue<T>,
        graphicProperty: ObjectProperty<Node>? = null,
        converter: StringConverter<in T>? = null,
        noinline op: Label.() -> Unit = {}
): Label {
    return label(observable, graphicProperty, converter, op).addClass(Styles.secondaryLabel)

}