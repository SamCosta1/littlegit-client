package org.littlegit.client.ui.app

import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
        val loginFlow by cssclass()
        val secondaryLabel by cssclass()
    }

    init {
        loginFlow {
            backgroundColor += ThemeColors.LightPrimary
            padding = box(10.px)

        }
        label and heading {
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
            textFill = ThemeColors.Accent;
        }
        textField {
            borderStyle += BorderStrokeStyle.SOLID
            borderWidth += box(0.px,0.px,1.px,0.px)
            borderColor += box(ThemeColors.Accent)
            textFill = ThemeColors.PrimaryText
            backgroundColor += Color.TRANSPARENT
            padding = box(5.px, 5.px, 5.px, 0.px)

        }

        button {
            backgroundColor += ThemeColors.Accent
            backgroundRadius += box(100.px)
            padding = box(10.px)

        }

        secondaryLabel {
            textFill = ThemeColors.SecondaryText
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