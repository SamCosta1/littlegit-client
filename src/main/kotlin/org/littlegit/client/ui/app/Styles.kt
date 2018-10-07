package org.littlegit.client.ui.app

import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
        val loginFlow by cssclass()
    }

    init {
        loginFlow {
            backgroundColor += ThemeColors.LightPrimary
        }
        label and heading {
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
        }
        textField {
            borderStyle += BorderStrokeStyle.NONE
            borderWidth += box(10.px,10.px,0.px,0.px)
            backgroundColor += Color.ALICEBLUE
            textFill = ThemeColors.PrimaryText
        }
        button {
            backgroundColor += ThemeColors.Accent
        }
    }
}

object ThemeColors {
    val Primary = c("#607D8B")
    val DarkPrimary = c("#455A64")
    val LightPrimary = c("#CFD8DC")
    val Accent = c("#FF5722")
    val PrimaryText = c("#212121")
    val SecondaryText = c("#757575")
}