package com.example.pomodoro.data

data class Stopwatch(
    val id: Int,
    var timeToCount: Long,
    var startMs: Long,
    var stopMs: Long,
    var isStarted: Boolean,
    var isEnded: Boolean
)