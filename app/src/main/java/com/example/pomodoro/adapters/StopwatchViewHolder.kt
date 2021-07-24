package com.example.pomodoro.adapters

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.*
import com.example.pomodoro.data.Stopwatch
import com.example.pomodoro.databinding.StopwatchItemBinding
import com.example.pomodoro.utils.StopwatchListener

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {
        if(stopwatch.isEnded){
            binding.stopwatchTimer.text = 0L.displayTime()
            updateCustomView(stopwatch.timeToCount - 1)
        } else{
            binding.stopwatchTimer.text = (stopwatch.timeToCount -  stopwatch.stopMs).displayTime()
            updateCustomView(stopwatch.timeToCount - (stopwatch.timeToCount -  stopwatch.stopMs))
        }
        binding.customViewTwo.setPeriod(stopwatch.timeToCount)

        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer()
        }

        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                val stopMs = System.currentTimeMillis() - stopwatch.startMs + stopwatch.stopMs
                listener.stop(stopwatch.id, stopMs)
            } else {
                listener.start(stopwatch.id)
            }
        }

        binding.deleteButton.setOnClickListener {
            if (stopwatch.isStarted){
                val stopMs = System.currentTimeMillis() - stopwatch.startMs + stopwatch.stopMs
                listener.stop(stopwatch.id, stopMs)
            }
            listener.delete(stopwatch.id)
        }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = STOP

        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer() {
        binding.startPauseButton.text = START

        timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {
            override fun onTick(millisUntilFinished: Long) {
                val currMs = stopwatch.timeToCount - (System.currentTimeMillis() - stopwatch.startMs + stopwatch.stopMs)
                if(currMs > 0) {
                    binding.stopwatchTimer.text = currMs.displayTime()
                    updateCustomView((System.currentTimeMillis() - stopwatch.startMs + stopwatch.stopMs))
                }
            }
            override fun onFinish() { }
        }
    }

    private fun updateCustomView(curMs: Long){
        binding.customViewTwo.setCurrent(curMs)
    }
}