package org.littlegit.client.ui.app.graph

import javafx.collections.ListChangeListener
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.Priority
import org.littlegit.client.ui.util.strokeLine
import org.littlegit.client.ui.view.BaseView
import tornadofx.*
import java.awt.Point
import java.awt.geom.Point2D


class GraphView: BaseView() {

    private val gridSize = 50
    private val commitWidth = 10.0
    override val root = vbox {
        vgrow = Priority.ALWAYS

        val canvasPane = CanvasPane(500.0, 500.0)
        canvasPane.addOnResizeListener(this@GraphView::drawGraph)
        addChildIfPossible(canvasPane)
        repoController.logObservable.addListener(ListChangeListener {
            drawGraph(canvasPane.canvas.graphicsContext2D)
        })

        drawGraph(canvasPane.canvas.graphicsContext2D, canvasPane.canvas)
    }

    private fun drawGraph(gc: GraphicsContext, canvas: Canvas = gc.canvas) {

        gc.clearRect(0.0, 0.0, canvas.width, canvas.width)
        val graph = GitGraph(repoController.currentLog)

        graph.connections.forEach {
            gc.strokeLine(pointToCoordinate(it.point1), pointToCoordinate(it.point2.location))
        }

        graph.commitLocations.forEach {
            val location = pointToCoordinate(it)
            gc.fillOval(location.x - commitWidth / 2, location.y - commitWidth / 2, commitWidth, commitWidth)
            gc.strokeText(it.commit.hash, 500.0, location.y)
        }
    }

    fun pointToCoordinate(point: Point): Point2D.Double {
        val xGridPos = point.x
        val yGridPos = point.y
        return Point2D.Double((xGridPos + 0.5) * gridSize, (yGridPos + 0.5) * gridSize)

    }
    fun pointToCoordinate(commitLocation: CommitLocation): Point2D.Double {
        return pointToCoordinate(commitLocation.location)
    }
}
