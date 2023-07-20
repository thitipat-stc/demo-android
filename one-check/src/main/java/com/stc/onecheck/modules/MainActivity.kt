package com.stc.onecheck.modules

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.stc.onecheck.R
import com.stc.onecheck.databinding.ActivityMainBinding
import com.stc.onecheck.utils.Mockup
import com.stc.onecheck.utils.Shared

class MainActivity : AppCompatActivity(), View.OnKeyListener, View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private var mAnim: Animation? = null
    private var keyKeyboard: Int = 0
    private var keyEffect: Int = 0
    private var countOK: Int = 0
    private var countNG: Int = 0

    companion object {
        const val T_SMALL = 24f
        const val T_MEDIUM = 64f
        const val T_LARGE = 128f
    }

    override fun onResume() {
        super.onResume()
        val settingPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        keyKeyboard = settingPrefs.getString(getString(R.string.txt_key_keyboard), "0")?.toInt() ?: 0
        keyEffect = settingPrefs.getString(getString(R.string.txt_key_effect), "0")?.toInt() ?: 0

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvAppVersion.text = "Demo"

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(callback)

        initEvent()
    }

    private fun initEvent() {
        binding.edtScan1.requestFocus()
        binding.edtScan1.setOnKeyListener(this)
        binding.edtScan2.setOnKeyListener(this)
        binding.btnClear.setOnClickListener(this)
        binding.btnSettings.setOnClickListener(this)
        Shared.setEdittextChange(binding.edtScan1, binding.tilScan1)
        Shared.setEdittextChange(binding.edtScan2, binding.tilScan2)
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        when (v?.id) {
            R.id.edtScan1 -> {
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

            R.id.edtScan2 -> {
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
            R.id.btnClear -> {
                clearActivity()
            }

            R.id.btnSettings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun clearActivity() {
        binding.apply {
            tilScan1.error = null
            tilScan1.isErrorEnabled = false
            tilScan2.error = null
            tilScan2.isErrorEnabled = false

            edtScan1.text?.clear()
            edtScan2.text?.clear()

            tvOKCount.text = "0"
            tvNGCount.text = "0"

            countOK = 0
            countNG = 0

            clearEffect()

            edtScan1.requestFocus()
        }
    }

    private fun checkBox(value1: String) {
        val rs = Mockup.getBox().filter { s -> s.boxId == value1 }.toList().size
        if (rs <= 0) {
            //Toast.makeText(this, "ไม่พบข้อมูลล่อง: $value1", Toast.LENGTH_LONG).show()
            setTextResult(false, "Not found master: $value1")
            binding.edtScan1.selectAll()
            return
        }
        setTextResult()
        binding.edtScan2.requestFocus()
    }

    private fun compareValue(value1: String, value2: String) {
        if (!value1.contentEquals(value2)) {
            setTextResult(false, "Product incorrect")
            binding.edtScan2.selectAll()
            return
        }

        setTextResult(true)
        binding.edtScan2.text?.clear()
        binding.edtScan2.requestFocus()

    }

    private fun setTextResult(bool: Boolean? = null, msg: String? = null) {
        if (bool != null) {
            if (bool) {
                binding.tvOK.setTextColor(ContextCompat.getColor(this, R.color.alert_success))

                if (keyEffect == 1) {
                    if (binding.tvOK.textSize == 128f && binding.tvNG.textSize == 128f) {
                        setFullText(binding.tvNG, T_MEDIUM, T_SMALL)
                        setFullText(binding.tvOK, T_MEDIUM, T_LARGE)
                    } else if (binding.tvOK.textSize == 48f && binding.tvNG.textSize == 256f) {
                        setFullText(binding.tvOK, T_SMALL, T_LARGE)
                        setFullText(binding.tvNG, T_LARGE, T_SMALL)
                    }
                }

                countOK++
                binding.tvOKCount.text = countOK.toString()
            } else {
                binding.tvNG.setTextColor(ContextCompat.getColor(this, R.color.alert_danger))

                if (keyEffect == 1) {
                    if (binding.tvOK.textSize == 128f && binding.tvNG.textSize == 128f) {
                        setFullText(binding.tvOK, T_MEDIUM, T_SMALL)
                        setFullText(binding.tvNG, T_MEDIUM, T_LARGE)
                    } else if (binding.tvOK.textSize == 256f && binding.tvNG.textSize == 48f) {
                        setFullText(binding.tvNG, T_SMALL, T_LARGE)
                        setFullText(binding.tvOK, T_LARGE, T_SMALL)
                    }
                }

                countNG++
                binding.tvNGCount.text = countNG.toString()
            }
        } else {
            binding.tvOK.setTextColor(ContextCompat.getColor(this, R.color.gainsboro))
            binding.tvNG.setTextColor(ContextCompat.getColor(this, R.color.gainsboro))
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

    private fun clearEffect() {
        if (binding.tvOK.textSize == 256f && binding.tvNG.textSize == 48f) {
            setFullText(binding.tvOK, T_LARGE, T_MEDIUM)
            setFullText(binding.tvNG, T_SMALL, T_MEDIUM)
        } else if (binding.tvOK.textSize == 48f && binding.tvNG.textSize == 256f) {
            setFullText(binding.tvNG, T_LARGE, T_MEDIUM)
            setFullText(binding.tvOK, T_SMALL, T_MEDIUM)
        }
        binding.tvOK.setTextColor(ContextCompat.getColor(this, R.color.gainsboro))
        binding.tvNG.setTextColor(ContextCompat.getColor(this, R.color.gainsboro))
    }
}