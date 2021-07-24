package com.example.pomodoro

const val START_TIME = "00:00:00:00"
const val INVALID = "INVALID"
const val COMMAND_START = "COMMAND_START"
const val COMMAND_STOP = "COMMAND_STOP"
const val COMMAND_ID = "COMMAND_ID"
const val STARTED_TIMER_TIME_MS = "STARTED_TIMER_TIME"
const val STOPPED_TIMER_TIME_MS = "STOPPED_TIMER_TIME"
const val TIME_TO_COUNT = "TIME_TO_COUNT"
const val UNIT_TEN_MS = 10L
const val PERIOD = 1000L * 60L * 60L * 24L

const val START = "Start"
const val STOP = "Stop"

fun Long.displayTime(): String {
    if (this <= 0L) {
        return START_TIME
    }
    val h = this / 1000 / 3600
    val m = this / 1000 % 3600 / 60
    val s = this / 1000 % 60
    val ms = this % 1000 / 10

    return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}:${displaySlot(ms)}"
}

private fun displaySlot(count: Long): String {
    return if (count / 10L > 0) {
        "$count"
    } else {
        "0$count"
    }
}