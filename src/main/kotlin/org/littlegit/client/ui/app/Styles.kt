package org.littlegit.client.ui.app

import javafx.scene.text.FontWeight
import tornadofx.Stylesheet
import tornadofx.cssclass
import tornadofx.px

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
    }

    init {
        label and heading {
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
        }
    }
}