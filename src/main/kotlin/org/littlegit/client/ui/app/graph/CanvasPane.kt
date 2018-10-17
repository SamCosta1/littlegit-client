package org.littlegit.client.ui.app.graph


import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import tornadofx.*


class CanvasPane(width: Double, height: Double) : Pane(), ChangeListener<Number> {


    val canvas: Canvas = Canvas(width, height)
    private val onResize: MutableList<(GraphicsContext, Canvas) -> Unit> = mutableListOf()
    init {
        children.add(canvas)
        vgrow = Priority.ALWAYS
        heightProperty().addListener(this)
        widthProperty().addListener(this)
    }

    fun addOnResizeListener(drawer: (GraphicsContext, Canvas) -> Unit) {
        onResize.add(drawer)
    }

    override fun changed(observable: ObservableValue<out Number>?, oldValue: Number?, newValue: Number?) {
        canvas.width = this.width
        canvas.height = this.height
        onResize.forEach { it.invoke(canvas.graphicsContext2D, canvas) }
    }


    override fun isResizable(): Boolean {
        return true
    }
}
