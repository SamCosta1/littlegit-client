package org.littlegit.client.ui.view.modal

import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.view.BaseFragment
import tornadofx.*

class NoNetworkConnectionModal : BaseFragment() {

    override val root = borderpane {
        center {
            label("erg")
            label(localizer.observable(I18nKey.NoNetworkConnection))
        }
    }
}
