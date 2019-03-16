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
    IcClose("ic_close.png"),
    ICTick("ic_tick.png"),
    IcOpenRepo("ic_open_repo.png");


    val path; get() = "images/$raw"
}

fun EventTarget.imageView(image: Image, lazyload: Boolean = true, op: ImageView.() -> Unit = {}) = this.imageview(image.path, lazyload, op)

fun GraphicsContext.strokeLine(p1: Point2D.Double, p2: Point2D.Double) {
    strokeLine(p1.x, p1.y, p2.x, p2.y)
}

private var arrowSize = 5
fun GraphicsContext.setArrowSize(size: Int) {
    arrowSize = size
}

fun GraphicsContext.strokeUpArrowHead(point: Point2D.Double) {
    strokeLine(point.x, point.y, point.x - arrowSize, point.y + arrowSize)
    strokeLine(point.x, point.y, point.x + arrowSize, point.y + arrowSize)
}

fun GraphicsContext.strokeLeftArrowHead(point: Point2D.Double) {
    strokeLine(point.x, point.y, point.x + arrowSize, point.y - arrowSize)
    strokeLine(point.x, point.y, point.x + arrowSize, point.y + arrowSize)
}

fun GraphicsContext.strokeRightArrowHead(point: Point2D.Double) {
    strokeLine(point.x, point.y, point.x - arrowSize, point.y + arrowSize)
    strokeLine(point.x, point.y, point.x - arrowSize, point.y - arrowSize)
}

