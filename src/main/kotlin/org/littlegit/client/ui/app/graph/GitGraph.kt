package org.littlegit.client.ui.app.graph

import org.littlegit.core.commandrunner.CommitHash
import org.littlegit.core.model.RawCommit
import java.awt.Point
import java.util.*
import kotlin.math.min


data class Connection(val point1: Point, val point2: Point)
data class CommitLocation(val commit: RawCommit, val location: Point)

class GitGraph(commits: List<RawCommit>) {
    val connections: List<Connection>
    val commitLocations: List<CommitLocation>



    init {
        val assignedColumns = mutableMapOf<CommitHash, Int>() // The reserved column number for each commit
        val commitLocationsMap = mutableMapOf<CommitHash, CommitLocation>()
        commitLocations = mutableListOf()
        connections = mutableListOf()

        val availableColumnsQueue = PriorityQueue<Int>()
        var nextRow = 1
        var nextColumn = 1
        commits.forEach { commit ->

            // If this commit has been assigned a column then put it there otherwise get a new one
            val column = assignedColumns[commit.hash] ?: nextColumn++

            val location = CommitLocation(commit, Point(column, nextRow++))
            commitLocations.add(location)
            commitLocationsMap[commit.hash] = location

            // Now deal with it's parents
            if (commit.parentHashes.isNotEmpty()) {
                // We keept track of whether any of this commit's parent maintain the same column as it, because if not, that column gets freed up
                var thisColumnStillInUse = false
                val parentHash = commit.parentHashes[0]

                // Assign the first parent to be the same column as this commit if possible
                var firstParentPos = assignedColumns[parentHash] ?: column
                firstParentPos = min(column, firstParentPos)

                assignedColumns[parentHash] = firstParentPos

                if (column == firstParentPos) {
                    thisColumnStillInUse = true
                }

                for (i in 1 until commit.parentHashes.size) {
                    val parentiHash = commit.parentHashes[i]
                    val nextFreeColumn = if (availableColumnsQueue.isNotEmpty()) availableColumnsQueue.remove() else ++nextColumn
                    var parentiPos = assignedColumns[parentiHash] ?: nextFreeColumn
                    parentiPos = min(parentiPos, nextFreeColumn)
                    assignedColumns[parentiHash] = parentiPos

                    if (parentiPos == column) {
                        thisColumnStillInUse = true
                    }
                }

                if (!thisColumnStillInUse) {
                    availableColumnsQueue.add(column)
                }
            }
        }

        commitLocations.forEach { commitLocation ->
            commitLocation.commit.parentHashes.forEach { parentHash ->
                commitLocationsMap[parentHash]?.location?.let {
                    connections.add(Connection(commitLocation.location, it))
                }
            }
        }
    }
}

