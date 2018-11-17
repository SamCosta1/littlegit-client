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
        val primaryPadding by cssclass()
    }

    init {
        primaryBackground {
            backgroundColor += ThemeColors.DarkPrimary2
            padding = box(10.px)
        }

        primaryPadding {
            padding = box(10.px)
        }

        label {
            textFill = ThemeColors.PrimaryText
        }

        label and heading {
            fontSize = 25.px
            fontWeight = FontWeight.BOLD
            textFill = ThemeColors.Accent
        }

        label and subheading {
            fontSize = 15.px
            fontWeight = FontWeight.BOLD
            textFill = ThemeColors.PrimaryText
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
            textFill = ThemeColors.PrimaryText
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
            borderColor += box(ThemeColors.DarkPrimary1)
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
    val LightPrimary = c("#3D4C5A")
    val Primary = c("#31628b")
    val DarkPrimary1 = c("#455A64")
    val DarkPrimary2 = c("#333D49")
    val DarkPrimary3 = c("#232C35")
    val Accent = c("#b86414")
    val DarkAccent = c("#f25423")
    val DarkestAccent = c("#bb4722")
    val PrimaryText = c("#e2e2e2")
    val SecondaryText = c("#757575")
    val Error = c("#771422")
}