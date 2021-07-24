package com.example.pomodoro.utils

interface StopwatchListener {

    fun start(id: Int)

    fun stop(id: Int, stopMs: Long)

    fun reset(id: Int)

    fun delete(id: Int)
}