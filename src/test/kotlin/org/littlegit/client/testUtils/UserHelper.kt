package org.littlegit.client.testUtils

import org.littlegit.client.engine.model.Language
import org.littlegit.client.engine.model.User

object UserHelper {

    fun createUser(id: Int = 10, firstName: String = "Ned", surname: String = "Stark") = User(id, "$firstName@$surname.com".trim(), firstName, surname, 0, Language.English.code, firstName)
}