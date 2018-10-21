package org.littlegit.client.ui.app.graph

import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.GestureEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.input.SwipeEvent
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.littlegit.client.ui.util.strokeLine
import org.littlegit.client.ui.view.BaseView
import tornadofx.*
import java.awt.Point
import java.awt.geom.Point2D
import java.util.*
import kotlin.math.abs
import kotlin.math.floor


class GraphView: BaseView(), EventHandler<ScrollEvent> {

    private val branchColours = with(this) {
        val rand = Random()
        val initial = mutableListOf(Color.RED, Color.GREEN, Color.BROWN, Color.TEAL, Color.CRIMSON)
        for (i in 0 until 100) {
            initial.add(Color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble(), 1.0))
        }

        initial
    }

    private val gridSize = 40
    private val commitWidth = 20.0
    private var scrollY = 0.0
    private lateinit var canvasPane: CanvasPane
    private var graph: GitGraph? = null
    private var lastYPos = 1.0

    override val root = stackpane {
        vgrow = Priority.ALWAYS

        canvasPane = CanvasPane(500.0, 500.0)
        canvasPane.addOnResizeListener(this@GraphView::drawGraph)
        addChildIfPossible(canvasPane)
        repoController.logObservable.addListener(ListChangeListener {
            runAsync {
                GitGraph(repoController.currentLog)
            } ui {
                graph = it
                lastYPos = gridCenterPoint(graph?.commitLocations?.lastOrNull()?.location ?: Point()).y
                drawGraph(canvasPane.canvas.graphicsContext2D)
            }
        })


        canvasPane.addEventFilter(ScrollEvent.ANY, {
            handle(it)
        })


        drawGraph(canvasPane.canvas.graphicsContext2D, canvasPane.canvas)

    }

    // Pretty much only used for debugging
    private fun drawGrid(gc: GraphicsContext) {
        gc.lineWidth = 0.4
        var x = 0.0
        var y = 0.0 + scrollY % gridSize

        while (x < canvasPane.width) {
            x += gridSize
            gc.strokeLine(x, 0.0, x, canvasPane.height)
        }

        while (y < canvasPane.height) {
            y += gridSize
            gc.strokeLine(0.0, y, canvasPane.width, y)
        }
    }


    override fun handle(event: ScrollEvent) {
        val newScrollY = scrollY + event.deltaY
        println()
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

        gc.lineWidth = 2.5
        graph.connections.forEach {
            drawConnection(gc, it)
        }

        drawCommitBlobs(graph, gc)
    }

    private fun drawCommitBlobs(graph: GitGraph, gc: GraphicsContext) {
        val minVisibleRow = floor(abs(scrollY) / gridSize)

        for (commitLocation in graph.commitLocations) {
            if (commitLocation.location.y < minVisibleRow) {
                continue
            }

            val location = gridCenterPoint(commitLocation)

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
        val start = gridCenterPoint(connection.point1)
        val end = gridCenterPoint(connection.point2)

        val oldFill = gc.stroke
        when {
            // Same column => Simple vertical line
            start.x == end.x -> {
                gc.stroke = branchColours[connection.point1.x % branchColours.size]
                gc.strokeLine(start, end)
            }
            start.x < end.x -> {
                gc.stroke = branchColours[connection.point2.x % branchColours.size]

                val line1EndX = end.x - gridSize / 2
                val line2StartY = start.y + gridSize / 2

                gc.strokeLine(start.x, start.y, line1EndX, start.y)
                gc.curve(line1EndX, start.y, end.x, start.y, end.x, line2StartY)
                gc.strokeLine(end.x, line2StartY, end.x, end.y)

            }
            else -> {
                gc.stroke = branchColours[connection.point1.x % branchColours.size]

                val line1EndY = end.y - gridSize / 2
                val line2StartX = start.x - gridSize / 2

                gc.strokeLine(start.x, start.y, start.x, line1EndY)
                gc.curve(start.x, line1EndY, start.x, end.y, line2StartX, end.y)
                gc.strokeLine(line2StartX, end.y, end.x, end.y)

            }
        }

        gc.stroke = oldFill
    }

    private fun GraphicsContext.curve(startX: Double, startY: Double, controlX: Double, controlY: Double, endX: Double, endY: Double) {
        beginPath()
        moveTo(startX, startY)
        quadraticCurveTo(controlX, controlY, endX, endY)
        stroke()

    }

    private fun drawCommitBlob(gc: GraphicsContext, location: Point2D.Double, commitLocation: CommitLocation) {
        gc.fillOval(location.x - commitWidth / 2, location.y - commitWidth / 2, commitWidth, commitWidth)

        val oldStroke = gc.stroke
        gc.stroke = branchColours[commitLocation.location.x % branchColours.size]
        gc.strokeOval(location.x - commitWidth / 2, location.y - commitWidth / 2, commitWidth, commitWidth)
        gc.stroke = oldStroke

        val oldLineWidth = gc.lineWidth
        gc.lineWidth = 1.0
        gc.fillText(commitLocation.commit.hash, 500.0, location.y)
        gc.fillText(commitLocation.commit.commitSubject, 500.0, location.y + 20)
        gc.lineWidth = oldLineWidth

    }

    private fun gridCenterPoint(point: Point): Point2D.Double {
        val xGridPos = point.x
        val yGridPos = point.y
        return Point2D.Double((xGridPos + 0.5) * gridSize, (yGridPos + 0.5) * gridSize + scrollY)

    }

    private fun gridCenterPoint(commitLocation: CommitLocation): Point2D.Double {
        return gridCenterPoint(commitLocation.location)
    }

    fun drawCoordsText(gc: GraphicsContext, point: Point2D) {
        gc.strokeText("(${point.x}, ${point.y}", point.x, point.y)
    }
}
