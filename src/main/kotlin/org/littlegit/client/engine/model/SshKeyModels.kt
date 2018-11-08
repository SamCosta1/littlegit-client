package org.littlegit.client.engine.model

data class SshKeyRequest(val publicKey: String = "", val userId: Int = 0)