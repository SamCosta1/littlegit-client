package org.littlegit.client.ui.app

import javafx.scene.Cursor
import javafx.scene.layout.Border
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
        val bulletText by cssclass()
        val transparentTitle by cssclass()
        val handOnHover by cssclass()
        val fileview by cssclass()
    }

    init {
        primaryBackground {
            backgroundColor += ThemeColors.DarkPrimary2
        }

        primaryPadding {
            padding = box(15.px)
        }

        handOnHover {
            cursor = Cursor.HAND
        }

        label {
            textFill = ThemeColors.PrimaryText
        }

        label and heading {
            fontSize = 25.px
            fontWeight = FontWeight.BOLD
            textFill = ThemeColors.Accent
        }

        bulletText {
            textFill = ThemeColors.TertiaryText
            fontSize = 15.px
        }

        fileview {
            padding = box(10.px)
            borderColor += box(ThemeColors.Accent)
            borderStyle += BorderStrokeStyle.SOLID
            borderWidth += box(2.px)
            borderRadius += box(3.px)
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
            backgroundColor += Color.TRANSPARENT
            textFill = ThemeColors.Accent
            borderStyle += BorderStrokeStyle.SOLID
            borderWidth += box(5.px)
            borderColor += box(ThemeColors.Accent)
            borderRadius += box(14.px)
            fontSize = 17.px
            fontWeight = FontWeight.BOLD
            padding = box(10.px)
            cursor = Cursor.HAND
        }

        button and pressed {
            borderColor += box(ThemeColors.DarkestAccent)
            textFill = ThemeColors.DarkestAccent
        }

        button and hover {
            borderColor += box(ThemeColors.DarkestAccent)
            textFill = ThemeColors.DarkestAccent
        }

        secondaryLabel {
            textFill = ThemeColors.SecondaryText
        }

        textArea {
            textFill = ThemeColors.PrimaryText
            content {
                backgroundColor += ThemeColors.LightPrimary
                borderStyle += BorderStrokeStyle.SOLID
                borderColor += box(ThemeColors.DarkPrimary1)
                borderWidth += box(1.px)
            }

        }

        transparentTitle {
            fontSize = 20.px
            textFill = ThemeColors.TransparentText
        }

        cardView {
            padding = box(10.px)
            borderStyle += BorderStrokeStyle.SOLID
            borderColor += box(ThemeColors.Primary)
            backgroundRadius += box(15.px)
            borderRadius += box(15.px)
        }

        selectableCardView and hover {
            cursor = Cursor.HAND
            borderColor += box(ThemeColors.Accent)
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
    val DarkPrimary3 = c("#1e252c")
    val Accent = c("#b86414")
    val DarkAccent = c("#f25423")
    val DarkestAccent = c("#bb4722")
    val PrimaryText = c("#e2e2e2")
    val SecondaryText = c("#757575")
    val TertiaryText = c(1.0,1.0,1.0,0.46)
    val Error = c("#771422")
    val TransparentText = c(255, 255, 255, 0.26)
}