package com.enso.angry_vote.model

import java.util.Date

data class Vote(
    val id: String = "",
    val nickname: String = "",
    val reason: String = "",
    val timestamp: Long = Date().time
)