package org.littlegit.client.ui.app.graph

import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.Priority
import org.littlegit.client.ui.util.strokeLine
import org.littlegit.client.ui.view.BaseView
import tornadofx.*
import java.awt.Point
import java.awt.geom.Point2D
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.max


class GraphView: BaseView(), EventHandler<ScrollEvent> {

    private val gridSize = 50
    private val commitWidth = 10.0
    private var scrollY = 0.0
    private lateinit var canvasPane: CanvasPane
    private var graph: GitGraph? = null
    private var lastYPos = 1.0

    override val root = vbox {
        vgrow = Priority.ALWAYS

        canvasPane = CanvasPane(500.0, 500.0)
        canvasPane.addOnResizeListener(this@GraphView::drawGraph)
        addChildIfPossible(canvasPane)
        repoController.logObservable.addListener(ListChangeListener {
            graph = GitGraph(repoController.currentLog)
            drawGraph(canvasPane.canvas.graphicsContext2D)
        })

        canvasPane.onScroll = this@GraphView
        drawGraph(canvasPane.canvas.graphicsContext2D, canvasPane.canvas)
    }


    override fun handle(event: ScrollEvent) {
        val newScrollY = scrollY + event.deltaY

        // Prevent users scrolling higher up and out of sight of the graph or down below the graph

        val lowerBoundary = canvasPane.height - lastYPos - 2* gridSize

        scrollY = when {
            newScrollY > 0 -> 0.0
            newScrollY < lowerBoundary -> lowerBoundary
            else -> newScrollY
        }

        drawGraph(canvasPane.canvas.graphicsContext2D)
    }

    private fun drawGraph(gc: GraphicsContext, canvas: Canvas = gc.canvas) {
        val graph = this.graph ?: return

        gc.clearRect(0.0, 0.0, canvas.width, canvas.width)

        graph.connections.forEach {
            gc.strokeLine(pointToCoordinate(it.point1), pointToCoordinate(it.point2.location))
        }

        graph.commitLocations.forEach {
            val location = pointToCoordinate(it)
            lastYPos = max(lastYPos, location.y)
            gc.fillOval(location.x - commitWidth / 2, location.y - commitWidth / 2, commitWidth, commitWidth)
            gc.strokeText(it.commit.hash, 500.0, location.y)
            gc.strokeText(it.commit.commitSubject, 500.0, location.y + 20)
        }
    }

    fun pointToCoordinate(point: Point): Point2D.Double {
        val xGridPos = point.x
        val yGridPos = point.y
        return Point2D.Double((xGridPos + 0.5) * gridSize, (yGridPos + 0.5) * gridSize + scrollY)

    }
    fun pointToCoordinate(commitLocation: CommitLocation): Point2D.Double {
        return pointToCoordinate(commitLocation.location)
    }
}
