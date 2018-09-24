package org.littlegit.client.engine.controller

interface InitableController {
    fun onStart(onReady: (InitableController) -> Unit)
}