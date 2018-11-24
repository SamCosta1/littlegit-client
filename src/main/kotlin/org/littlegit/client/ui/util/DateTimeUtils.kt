package org.littlegit.client.ui.util

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
fun OffsetDateTime.format(): String {
    return this.format(org.littlegit.client.ui.util.formatter)
}