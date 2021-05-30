package com.arsvechkarev.timerxexample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_timer.btn_reset
import kotlinx.android.synthetic.main.fragment_timer.btn_start
import kotlinx.android.synthetic.main.fragment_timer.btn_stop
import kotlinx.android.synthetic.main.fragment_timer.text_time
import timerx.Timer
import timerx.buildTimer
import java.util.concurrent.TimeUnit.SECONDS

/**
 * Example of using a timer
 */
class TimerFragment : Fragment() {
  
  private lateinit var timer: Timer
  
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_timer, container, false)
  }
  
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  
    timer = buildTimer {
      startFormat("MM:SS")
      startTime(60, SECONDS)
      onTick { time -> text_time.text = time }
      onFinish { showToast("Finished!") }
    }
  
    text_time.text = timer.formattedStartTime
  
    btn_start.setOnClickListener { timer.start() }
  
    btn_stop.setOnClickListener {
      timer.stop()
      showToast("Remaining time in seconds = " + timer.getRemainingTimeIn(SECONDS))
    }
    
    btn_reset.setOnClickListener {
      text_time.text = timer.formattedStartTime
      timer.reset()
    }
  }
  
  override fun onDestroyView() {
    super.onDestroyView()
    timer.release()
  }
}
