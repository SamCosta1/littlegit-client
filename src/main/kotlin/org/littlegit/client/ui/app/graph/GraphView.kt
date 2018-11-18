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
import org.littlegit.client.ui.app.ThemeColors
import org.littlegit.client.ui.util.strokeLine
import org.littlegit.client.ui.view.BaseView
import tornadofx.*
import java.awt.Point
import java.awt.geom.Point2D
import java.util.*
import kotlin.math.abs
import kotlin.math.floor


class GraphView: BaseView(), EventHandler<ScrollEvent> {

    companion object {
        private val ScrollYKey = "${GraphView::class.simpleName}_scroll_y"
        private val HighlightColor = c(216, 216, 216, 0.41)
    }
    private val branchColours = with(this) {
        val rand = Random()
        val initial = mutableListOf(Color.RED, Color.GREEN, Color.BROWN, Color.TEAL, Color.CRIMSON)
        for (i in 0 until 100) {
            initial.add(Color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble(), 1.0))
        }

        initial
    }

    private val gridSize = 60
    private val commitWidth = 40.0
    private val leftBarWidth = 10.0
    private var scrollY = 0.0; set(value) {
        field = value
        stateStore.add(ScrollYKey, value)
    }
    private lateinit var canvasPane: CanvasPane
    private var graph: GitGraph? = null
    private var lastYPos = 1.0

    override val root = stackpane {
        vgrow = Priority.ALWAYS

        canvasPane = CanvasPane(500.0, 500.0)
        canvasPane.addOnResizeListener(this@GraphView::drawGraph)
        addChildIfPossible(canvasPane)

        restoreState()
        repoController.logObservable.addListener(ListChangeListener {
            scrollY = 0.0
            reGenerateGraph()
        })


        canvasPane.onScroll = this@GraphView

        reGenerateGraph()
    }

    private fun reGenerateGraph() {
        runAsync {
            GitGraph(repoController.currentLog)
        } ui {
            graph = it
            lastYPos = gridCenterPoint(graph?.commitLocations?.lastOrNull()?.location ?: Point()).y
            drawGraph(canvasPane.canvas.graphicsContext2D)
        }
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
        // Prevent users scrolling higher up and out of sight of the graph or down below the graph

        val lowerBoundary = canvasPane.height - lastYPos - 2* gridSize

        scrollY = when {
            newScrollY > 0 || lastYPos < canvasPane.height -> 0.0
            newScrollY < lowerBoundary -> lowerBoundary
            else -> newScrollY
        }

        drawGraph(canvasPane.canvas.graphicsContext2D)
    }

    private fun drawGraph(gc: GraphicsContext, canvas: Canvas = gc.canvas) {
        val graph = this.graph ?: return
        gc.clearRect(0.0, 0.0, canvas.width, canvas.width)

        gc.lineWidth = 2.5

        highlightHeadCommitRow(graph, gc)
        graph.connections.forEach {
            drawConnection(gc, it)
        }

        drawCommitBlobs(graph, gc)
//        drawGrid(gc)
    }

    private fun highlightHeadCommitRow(graph: GitGraph, gc: GraphicsContext) {
        val headCommit = graph.commitLocations.firstOrNull { it.commit.isHead } ?: return
        val position = gridCenterPoint(headCommit.location)
        if (isInView(position)) {

            val oldFill = gc.fill
            gc.fill = HighlightColor
            gc.fillRect(0.0, position.y - gridSize / 2, canvasPane.width, gridSize.toDouble())
            gc.fill = oldFill

        }
    }

    private fun drawCommitBlobs(graph: GitGraph, gc: GraphicsContext) {
        val minVisibleRow = floor(abs(scrollY) / gridSize)

        for (commitLocation in graph.commitLocations) {
            if (commitLocation.location.y < minVisibleRow) {
                continue
            }

            val location = gridCenterPoint(commitLocation)
            val color = branchColours[commitLocation.location.x % branchColours.size]

            if (isInView(location)) {
                drawCommitBlob(gc, color, location, commitLocation)
                drawLeftBar(gc, if (commitLocation.commit.isHead) HighlightColor else color, gridTopPoint(commitLocation))
            } else {
                break
            }
        }
    }

    private fun drawLeftBar(gc: GraphicsContext, color: Color, location: Point2D.Double) {
        val height = gridSize * 0.8
        val offset = (gridSize - height) / 2
        val oldFill = gc.fill

        gc.fill = color
        gc.fillRoundRect(- leftBarWidth / 2, location.y + offset, leftBarWidth, height, 5.0, 5.0)
        gc.fill = oldFill
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

    private fun drawCommitBlob(gc: GraphicsContext, color: Color, location: Point2D.Double, commitLocation: CommitLocation) {

        val oldStroke = gc.stroke
        gc.fillOval(location.x - commitWidth / 2, location.y - commitWidth / 2, commitWidth, commitWidth)

        gc.stroke = color
        gc.strokeOval(location.x - commitWidth / 2, location.y - commitWidth / 2, commitWidth, commitWidth)
        gc.stroke = oldStroke
    }

    private fun gridCenterPoint(point: Point): Point2D.Double {
        val xGridPos = point.x
        val yGridPos = point.y
        return Point2D.Double((xGridPos + 0.5) * gridSize, (yGridPos + 0.5) * gridSize + scrollY)
    }

    private fun gridTopPoint(point: Point): Point2D.Double {
        val xGridPos = point.x
        val yGridPos = point.y
        return Point2D.Double(xGridPos * gridSize.toDouble(), yGridPos * gridSize + scrollY)
    }

    private fun gridCenterPoint(commitLocation: CommitLocation): Point2D.Double {
        return gridCenterPoint(commitLocation.location)
    }

    private fun gridTopPoint(commitLocation: CommitLocation): Point2D.Double {
        return gridTopPoint(commitLocation.location)
    }

    fun drawCoordsText(gc: GraphicsContext, point: Point2D) {
        gc.strokeText("(${point.x}, ${point.y}", point.x, point.y)
    }

    fun restoreState() {
        scrollY = stateStore.get(ScrollYKey) ?: 0.0
    }
}
