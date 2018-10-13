package org.littlegit.client.ui.app

import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
        val subheading by cssclass()
        val primaryBackground by cssclass()
        val secondaryLabel by cssclass()
        val cardView by cssclass()
        val selectableCardView by cssclass()
        val error by cssclass()
    }

    init {
        primaryBackground {
            backgroundColor += ThemeColors.LightPrimary
            padding = box(10.px)

        }
        label and heading {
            fontSize = 25.px
            fontWeight = FontWeight.BOLD
            textFill = ThemeColors.Accent
        }

        label and subheading {
            fontSize = 15.px
            fontWeight = FontWeight.BOLD
            textFill = ThemeColors.Accent
        }

        textField {
            borderStyle += BorderStrokeStyle.SOLID
            borderWidth += box(0.px,0.px,1.px,0.px)
            borderColor += box(ThemeColors.Accent)
            textFill = ThemeColors.PrimaryText
            backgroundColor += Color.TRANSPARENT
            padding = box(5.px, 5.px, 5.px, 0.px)

        }

        listView {
            backgroundColor += Color.TRANSPARENT
        }

        listCell {
            backgroundColor += Color.TRANSPARENT
        }

        button {
            backgroundColor += ThemeColors.Accent
            backgroundRadius += box(50.px)
            padding = box(10.px)
        }

        button and pressed {
            backgroundColor += ThemeColors.DarkestAccent
        }

        button and hover {
            backgroundColor += ThemeColors.DarkAccent
        }

        secondaryLabel {
            textFill = ThemeColors.SecondaryText
        }

        cardView {
            padding = box(10.px)
            borderStyle += BorderStrokeStyle.SOLID
            borderColor += box(ThemeColors.Primary)
            backgroundRadius += box(15.px)
            borderRadius += box(15.px)
        }

        cardView and selectableCardView and hover {
            borderWidth += box(2.px)
            borderColor += box(ThemeColors.DarkPrimary)
        }

        error {
            textFill = ThemeColors.Error
        }

        separator {
            backgroundColor += ThemeColors.Primary
            backgroundRadius += box(1.px)
        }
    }
}

object ThemeColors {
    val Primary = c("#607D8B")
    val DarkPrimary = c("#455A64")
    val LightPrimary = c("#CFD8DC")
    val Accent = c("#FF5722")
    val DarkAccent = c("#f25423")
    val DarkestAccent = c("#bb4722")
    val PrimaryText = c("#212121")
    val SecondaryText = c("#757575")
    val Error = c("#771422")
}