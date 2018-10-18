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
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.system.measureTimeMillis


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
            runAsync {
                GitGraph(repoController.currentLog)
            } ui {
                graph = it
                lastYPos = pointToCoordinate(graph?.commitLocations?.lastOrNull()?.location ?: Point()).y
                drawGraph(canvasPane.canvas.graphicsContext2D)
            }
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
            drawConnection(gc, it)
        }

        drawCommitBlobs(graph, gc)
    }

    private fun drawCommitBlobs(graph: GitGraph, gc: GraphicsContext) {
        val minVisibleRow = floor(abs(scrollY) / gridSize)
        var count = 0

        for (commitLocation in graph.commitLocations) {
            if (commitLocation.location.y < minVisibleRow) {
                continue
            }

            val location = pointToCoordinate(commitLocation)

            if (isInView(location)) {
                drawCommitBlob(gc, location, commitLocation)
            } else {
                break
            }
        }
    }

    // For now ignoring the x coordinate
    private fun isInView(location: Point2D.Double): Boolean {
        return location.y <= canvasPane.height + commitWidth
    }

    private fun drawConnection(gc: GraphicsContext, connection: Connection) {
        val start = pointToCoordinate(connection.point1)
        val end = pointToCoordinate(connection.point2)

        // Same column => Simple vertical line
        when {
            start.x == end.x -> gc.strokeLine(start, end)
            start.x < end.x -> {
                gc.strokeLine(start.x, start.y, end.x, start.y)
                gc.strokeLine(end.x, start.y, end.x, end.y)
            }
            else -> {
                gc.strokeLine(start.x, start.y, start.x, end.y)
                gc.strokeLine(start.x, end.y, end.x, end.y)
            }
        }
    }

    private fun drawCommitBlob(gc: GraphicsContext, location: Point2D.Double, commitLocation: CommitLocation) {
        gc.fillOval(location.x - commitWidth / 2, location.y - commitWidth / 2, commitWidth, commitWidth)
        gc.strokeText(commitLocation.commit.hash, 500.0, location.y)
        gc.strokeText(commitLocation.commit.commitSubject, 500.0, location.y + 20)

    }

    private fun pointToCoordinate(point: Point): Point2D.Double {
        val xGridPos = point.x
        val yGridPos = point.y
        return Point2D.Double((xGridPos + 0.5) * gridSize, (yGridPos + 0.5) * gridSize + scrollY)

    }

    private fun pointToCoordinate(commitLocation: CommitLocation): Point2D.Double {
        return pointToCoordinate(commitLocation.location)
    }

    fun drawCoordsText(gc: GraphicsContext, point: Point2D) {
        gc.strokeText("(${point.x}, ${point.y}", point.x, point.y)
    }
}
