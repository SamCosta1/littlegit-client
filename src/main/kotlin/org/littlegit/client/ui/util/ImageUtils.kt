package org.littlegit.client.ui.util

import javafx.event.EventTarget
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.ImageView
import tornadofx.*
import java.awt.geom.Point2D

enum class Image(private val raw: String) {
    WelshFlag("lang_cy.png"),
    EnglishFlag("lang_en-gb.png"),
    Logo("logo.png"),
    IcLogout("ic_logout.png"),
    IcOpenRepo("ic_open_repo.png");

    val path; get() = "images/$raw"
}

fun EventTarget.imageView(image: Image, lazyload: Boolean = true, op: ImageView.() -> Unit = {}) = this.imageview(image.path, lazyload, op)

fun GraphicsContext.strokeLine(p1: Point2D.Double, p2: Point2D.Double) {
    strokeLine(p1.x, p1.y, p2.x, p2.y)
}