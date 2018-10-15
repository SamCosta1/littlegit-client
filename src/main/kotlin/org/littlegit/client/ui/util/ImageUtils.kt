package org.littlegit.client.ui.util

import javafx.event.EventTarget
import javafx.scene.image.ImageView
import tornadofx.*

enum class Image(private val raw: String) {
    WelshFlag("lang_cy.png"),
    EnglishFlag("lang_en-gb.png"),
    Logo("logo.png");

    val path; get() = "images/$raw"
}

fun EventTarget.imageView(image: Image, lazyload: Boolean = true, op: ImageView.() -> Unit = {}) = this.imageview(image.path, lazyload, op)
