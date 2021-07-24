package com.example.pomodoro.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.*
import com.example.pomodoro.adapters.StopwatchAdapter
import com.example.pomodoro.data.Stopwatch
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.services.ForegroundService
import com.example.pomodoro.utils.StopwatchListener
import java.util.*


class MainActivity : AppCompatActivity(), LifecycleObserver, StopwatchListener {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0
    private var startedTimerId = -1

    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.addNewStopwatchButton.setOnClickListener {
            val minutes = binding.etMinutes.text.toString()
            if (minutes.isEmpty()) return@setOnClickListener
            try {
                val timeToCount = minutes.toLong() * 60000L
                stopwatches.add(
                    Stopwatch(
                        id = nextId++,
                        timeToCount = timeToCount,
                        startMs = 0L,
                        stopMs = 0L,
                        isStarted = false,
                        isEnded = false
                    )
                )
                stopwatchAdapter.submitList(stopwatches.toList())
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Invalid input", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()
    }

    private fun stopTimer() {
        timer?.cancel()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object :
            CountDownTimer(PERIOD, UNIT_TEN_MS) {
            override fun onTick(millisUntilFinished: Long) {
                val currMs =
                    stopwatch.timeToCount - (System.currentTimeMillis() - stopwatch.startMs + stopwatch.stopMs)
                if (currMs <= 0) {
                    end(stopwatch.id)
                    Toast.makeText(
                        applicationContext,
                        "Timer #${stopwatch.id} is done",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            override fun onFinish() {}
        }
    }

    private fun startParallel(id: Int) {
        stopwatches.find { it.id == id }?.let { startTimer(it) }
    }

    private fun stopParallel(id: Int) {
        stopwatches.find { it.id == id }?.let { stopTimer() }
    }


    override fun start(id: Int) {
        if (startedTimerId == -1) {
            val startMs = System.currentTimeMillis()
            changeStopwatch(
                id = id,
                startMs = startMs,
                stopMs = null,
                isStarted = true,
                isEnded = false
            )
        } else {
            val curTimer = stopwatches.find { it.id == startedTimerId }
            curTimer?.let {
                // stop current timer
                val stopMs = System.currentTimeMillis() - curTimer.startMs + curTimer.stopMs
                stop(startedTimerId, stopMs)
                // start new timer
                val startMs = System.currentTimeMillis()
                changeStopwatch(
                    id = id,
                    startMs = startMs,
                    stopMs = null,
                    isStarted = true,
                    isEnded = false
                )
            }
        }
        startedTimerId = id
    }

    override fun stop(id: Int, stopMs: Long) {
        startedTimerId = -1
        changeStopwatch(id = id, startMs = null, stopMs = stopMs, isStarted = false, isEnded = false)
    }

    override fun reset(id: Int) {
        changeStopwatch(id = id, startMs = 0L, stopMs = 0L, isStarted = false, isEnded = false)
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

   private fun end(id: Int) {
        startedTimerId = -1
        changeStopwatch(id = id, startMs = 0L, stopMs = 0L, isStarted = false, isEnded = true)
    }


    private fun changeStopwatch(
        id: Int,
        startMs: Long?,
        stopMs: Long?,
        isStarted: Boolean,
        isEnded: Boolean
    ) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(
                    Stopwatch(
                        id = it.id,
                        timeToCount = it.timeToCount,
                        startMs = startMs ?: it.startMs,
                        stopMs = stopMs ?: it.stopMs,
                        isStarted = isStarted,
                        isEnded = isEnded
                    )
                )
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)

        if (isStarted) {
            startParallel(id)
        } else {
            stopParallel(id)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if (startedTimerId == -1)
            return
        val curTimer = stopwatches.find { it.id == startedTimerId }
        curTimer?.let {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, curTimer.startMs)
            startIntent.putExtra(STOPPED_TIMER_TIME_MS, curTimer.stopMs)
            startIntent.putExtra(TIME_TO_COUNT, curTimer.timeToCount)
            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }
}
