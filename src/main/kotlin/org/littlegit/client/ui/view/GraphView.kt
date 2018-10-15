package org.littlegit.client.ui.view

import javafx.scene.layout.Priority
import org.littlegit.client.ui.util.format
import org.littlegit.client.ui.util.secondarylabel
import tornadofx.*

class GraphView: BaseView() {


    override val root = vbox {
        vgrow = Priority.ALWAYS

        listview(repoController.logObservable) {
            vgrow = Priority.ALWAYS

            cellFormat {
                graphic = cache {
                    vbox {
                        label(it.commitSubject)
                        secondarylabel(it.date.format())
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        repoController.loadLog()
    }
}
