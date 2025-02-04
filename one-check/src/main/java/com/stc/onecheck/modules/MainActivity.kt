package com.stc.onecheck.modules

import android.animation.ValueAnimator
import android.content.Intent
import android.net.http.NetworkException
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.stc.onecheck.R
import com.stc.onecheck.databinding.ActivityMainBinding
import com.stc.onecheck.services.ControlResponse
import com.stc.onecheck.services.HttpManager
import com.stc.onecheck.utils.Shared
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), View.OnKeyListener, View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private var mAnim: Animation? = null
    private var keyMaster: Int = 0//1
    private var keyKeyboard: Int = 0
    private var keyEffect: Int = 0
    private var countOK: Int = 0
    private var countNG: Int = 0
    private var rm: Int = 0

    companion object {
        const val T_SMALL = 24f
        const val T_MEDIUM = 64f
        const val T_LARGE = 128f
        var keyAddress: String = ""
    }

    private var clickCount = 0
    private val clickThreshold = 8
    private val clickTimeout: Long = 3000 // 3 seconds to complete 8 clicks
    private val handler = Handler(Looper.getMainLooper())
    private var pattern: String = ""

    override fun onResume() {
        super.onResume()

        clearActivity()

        val settingPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        keyMaster = settingPrefs.getString(getString(R.string.txt_key_master), "1")?.toInt() ?: 1
        keyKeyboard = settingPrefs.getString(getString(R.string.txt_key_keyboard), "0")?.toInt() ?: 0
        keyEffect = settingPrefs.getString(getString(R.string.txt_key_effect), "0")?.toInt() ?: 0
        keyAddress = settingPrefs.getString(getString(R.string.txt_key_address), "") ?: ""

        if (keyKeyboard == 0) {
            binding.edtScan1.inputType = InputType.TYPE_NULL
            binding.edtScan2.inputType = InputType.TYPE_NULL
        } else {
            binding.edtScan1.inputType = InputType.TYPE_CLASS_TEXT
            binding.edtScan2.inputType = InputType.TYPE_CLASS_TEXT
        }

        if (keyEffect == 0) {
            clearEffect()
        }

        binding.btnClear.setOnLongClickListener {
            sendLight(false, "1")
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        val bool = extras?.getBoolean("IsExpire") ?: false
        val remain = extras?.getInt("IsRemain") ?: 0
        if (bool) {
            rm = 29 - remain
            binding.tvRemain.text = getString(R.string.txt_remain_day_s, rm)
            showWarning()
        }

        binding.tvAppVersion.text = getString(R.string.txt_demo)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(callback)

        initEvent()
    }

    private fun showWarning() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("One Check Demo")
        builder.setMessage(getString(R.string.txt_remain_day_s, rm))
        builder.setCancelable(true)
        builder.setPositiveButton(getString(R.string.txt_ok)) { dialog, id -> dialog.dismiss() }
        val alert = builder.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    private fun initEvent() {
        binding.edtScan1.requestFocus()
        binding.edtScan1.setOnKeyListener(this)
        binding.edtScan2.setOnKeyListener(this)
        binding.btnClear.setOnClickListener(this)
        binding.btnSettings.setOnClickListener(this)
        binding.tvRemain.setOnClickListener(this)
        Shared.setEdittextChange(binding.edtScan1, binding.tilScan1)
        Shared.setEdittextChange(binding.edtScan2, binding.tilScan2)
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        when (v?.id) {
            R.id.edt_scan_1 -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event?.action == KeyEvent.ACTION_UP) {
                    val value1 = binding.edtScan1.text.toString()
                    when {
                        value1.isEmpty() -> {
                            binding.edtScan1.requestFocus()
                            binding.tilScan1.error = getString(R.string.txt_please_input_value)
                            binding.tilScan1.isErrorEnabled = true
                        }

                        else -> {
                            checkBox(value1)
                        }
                    }
                    return true
                }
            }

            R.id.edt_scan_2 -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event?.action == KeyEvent.ACTION_UP) {
                    val value1 = binding.edtScan1.text.toString()
                    val value2 = binding.edtScan2.text.toString()
                    when {
                        value1.isEmpty() -> {
                            binding.edtScan1.requestFocus()
                            binding.tilScan1.error = getString(R.string.txt_please_input_value)
                            binding.tilScan1.isErrorEnabled = true
                        }

                        value2.isEmpty() -> {
                            binding.edtScan2.requestFocus()
                            binding.tilScan2.error = getString(R.string.txt_please_input_value)
                            binding.tilScan2.isErrorEnabled = true
                        }

                        else -> {
                            setTextResult()
                            Handler(Looper.getMainLooper()).postDelayed({
                                compareValue(value1, value2)
                            }, 500)
                        }
                    }
                    return true
                }
            }
        }
        return false
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_clear -> {
                clearActivity()
            }

            R.id.btn_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }

            R.id.tv_remain -> {
                clickCount++
                if (clickCount == 1) {
                    handler.postDelayed(resetClickCountRunnable, clickTimeout)
                }

                if (clickCount == clickThreshold) {
                    handler.removeCallbacks(resetClickCountRunnable)
                    performAction()
                }
            }
        }
    }

    private val resetClickCountRunnable = Runnable {
        clickCount = 0
    }

    private fun performAction() {

    }

    private fun clearActivity() {
        binding.apply {
            tilScan1.error = null
            tilScan1.isErrorEnabled = false
            tilScan2.error = null
            tilScan2.isErrorEnabled = false

            edtScan1.text?.clear()
            edtScan2.text?.clear()

            tvOkCount.text = "0"
            tvNgCount.text = "0"

            countOK = 0
            countNG = 0

            clearEffect()

            edtScan1.requestFocus()
        }
    }

    private fun checkBox(value1: String) {
        /*if (keyMaster == 1) {
            val rs = Mockup.getBox().filter { s -> s.boxId == value1 }.toList().size
            if (rs <= 0) {
                setTextResult()
                binding.edtScan2.text?.clear()
                binding.tilScan2.error = ""
                binding.tilScan2.isErrorEnabled = false

                binding.tilScan1.error = "Not found master: $value1"
                binding.tilScan1.isErrorEnabled = true
                binding.edtScan1.selectAll()

                clearEffect()
                return
            }
        }*/
        setTextResult()
        binding.edtScan2.requestFocus()
    }

    private fun compareValue(value1: String, value2: String) {
        if (keyMaster == 1) {
            /*val rs = Mockup.getBox().filter { s -> s.boxId == value1 && s.envelopeId == value2 }.toList().size
            if (rs <= 0) {
                setTextResult(false)
                *//*binding.tilScan2.error = "Product: $value2 incorrect"
                binding.tilScan2.isErrorEnabled = true*//*
                binding.edtScan2.selectAll()
                return
            }*/
            if (!value1.contentEquals(value2)) {
                setTextResult(false)
                binding.tilScan2.error = "Product: $value2 incorrect"
                binding.tilScan2.isErrorEnabled = true
                binding.edtScan2.selectAll()
                return
            }
            setTextResult(true)
            binding.edtScan2.text?.clear()
            binding.edtScan2.requestFocus()
        } else {
            if (!value1.contentEquals(value2)) {
                setTextResult(false)
                binding.tilScan2.error = "Product: $value2 incorrect"
                binding.tilScan2.isErrorEnabled = true
                binding.edtScan2.selectAll()
                return
            }
            setTextResult(true)
            binding.edtScan2.text?.clear()
            binding.edtScan1.text?.clear()
            binding.edtScan1.requestFocus()
        }
    }

    private fun setTextResult(bool: Boolean? = null, msg: String? = null) {
        if (bool != null) {
            if (bool) {
                Shared.soundAlert(this, Shared.SND.INFO, true)
                binding.tvOk.setTextColor(ContextCompat.getColor(this, R.color.alert_success))

                if (keyEffect == 1) {
                    if (binding.tvOk.textSize == 128f && binding.tvNg.textSize == 128f) {
                        setFullText(binding.tvNg, T_MEDIUM, T_SMALL)
                        setFullText(binding.tvOk, T_MEDIUM, T_LARGE)
                    } else if (binding.tvOk.textSize == 48f && binding.tvNg.textSize == 256f) {
                        setFullText(binding.tvOk, T_SMALL, T_LARGE)
                        setFullText(binding.tvNg, T_LARGE, T_SMALL)
                    }
                }

                countOK++
                binding.tvOkCount.text = countOK.toString()

                pattern = "002000"
                sendLight(true, pattern)

                Handler(Looper.getMainLooper()).postDelayed({
                    pattern = "1"
                    sendLight(false, pattern)
                }, 4000)

            } else {
                Shared.soundAlert(this, Shared.SND.ERROR, true)
                binding.tvNg.setTextColor(ContextCompat.getColor(this, R.color.alert_danger))

                if (keyEffect == 1) {
                    if (binding.tvOk.textSize == 128f && binding.tvNg.textSize == 128f) {
                        setFullText(binding.tvOk, T_MEDIUM, T_SMALL)
                        setFullText(binding.tvNg, T_MEDIUM, T_LARGE)
                    } else if (binding.tvOk.textSize == 256f && binding.tvNg.textSize == 48f) {
                        setFullText(binding.tvNg, T_SMALL, T_LARGE)
                        setFullText(binding.tvOk, T_LARGE, T_SMALL)
                    }
                }

                countNG++
                binding.tvNgCount.text = countNG.toString()

//                pattern = "200000" //RED
                pattern = "330000"
                sendLight(true, pattern)

            }
        } else {
            binding.tvOk.setTextColor(ContextCompat.getColor(this, R.color.gainsboro))
            binding.tvNg.setTextColor(ContextCompat.getColor(this, R.color.gainsboro))
        }
    }

    private fun setBlinkingText(textView: TextView) {
        mAnim = AlphaAnimation(0.0f, 3.0f)
        mAnim?.duration = 300 // Time of the blink
        mAnim?.startOffset = 20
        mAnim?.repeatMode = Animation.REVERSE
        mAnim?.repeatCount = Animation.RESTART
        textView.startAnimation(mAnim)
    }

    private fun removeBlinkingText(textView: TextView) {
        textView.clearAnimation()
    }

    private fun setFullText(textView: TextView, startSize: Float, endSize: Float) {
        val animationDuration: Long = 100 // Animation duration in ms

        val animator = ValueAnimator.ofFloat(startSize, endSize)
        animator.duration = animationDuration

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            textView.textSize = animatedValue
        }
        animator.start()
    }

    private fun sendLight(bool: Boolean, pattern: String) {
        try {
            if (bool) {
                Log.d(MainActivity::class.java.simpleName, "-SEND:$pattern-")
                HttpManager.instance?.api?.send(pattern)?.enqueue(object : Callback<String?> {
                    override fun onResponse(call: Call<String?>, response: Response<String?>) {
                        if (!response.isSuccessful) Toast.makeText(this@MainActivity, "failed message: ${response.message()}, code: ${response.code()}", Toast.LENGTH_SHORT).show()
                        else Log.i(MainActivity::class.java.simpleName, "Send Receive ${response.message()}")
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "onFailure message: $t, code: ", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Log.d(MainActivity::class.java.simpleName, "-CLEAR-")
                HttpManager.instance?.api?.clear(pattern)?.enqueue(object : Callback<String?> {
                    override fun onResponse(call: Call<String?>, response: Response<String?>) {
                        if (!response.isSuccessful) Toast.makeText(this@MainActivity, "failed message: ${response.message()}, code: ${response.code()}", Toast.LENGTH_SHORT).show()
                        else Log.i(MainActivity::class.java.simpleName, "Clear Receive ${response.message()}")
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "onFailure message: $t, code: ", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "Exception: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearEffect() {
        if (binding.tvOk.textSize == 256f && binding.tvNg.textSize == 48f) {
            setFullText(binding.tvOk, T_LARGE, T_MEDIUM)
            setFullText(binding.tvNg, T_SMALL, T_MEDIUM)
        } else if (binding.tvOk.textSize == 48f && binding.tvNg.textSize == 256f) {
            setFullText(binding.tvNg, T_LARGE, T_MEDIUM)
            setFullText(binding.tvOk, T_SMALL, T_MEDIUM)
        }
        binding.tvOk.setTextColor(ContextCompat.getColor(this, R.color.gainsboro))
        binding.tvNg.setTextColor(ContextCompat.getColor(this, R.color.gainsboro))
    }
}