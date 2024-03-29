package com.stc.scanprint

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.stc.scanprint.databinding.ActivityLoadBinding
import com.stc.scanprint.utils.Shared
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class LoadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //binding.tvAppVersion.text = Shared.getAppVersion(this)
        binding.tvAppVersion.text = getString(R.string.txt_demo)

        /*Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)*/

        CoroutineScope(Dispatchers.Main).launch {
            try {
                Handler(Looper.getMainLooper()).postDelayed({
                    checkExpired()
                },300)
            } catch (t: Throwable) {
            }
        }
    }

    private fun checkExpired() {
        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val saved = sharedPreferences.getBoolean("saved", false)
        val dateFirst = sharedPreferences.getString("dateFirst", "")

        Log.d(LoadActivity::class.simpleName, "Date saved: $dateFirst")

        var count = 0
        val myFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val date1 = myFormat.parse(dateFirst)
            val date2 = myFormat.parse(Shared.getDateNow())
            val diff = date2.time - date1.time
            count = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
            Log.d(LoadActivity::class.simpleName, "count: $count")
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        if (!saved) {
            val myEdit = sharedPreferences.edit()
            myEdit.putBoolean("saved", true)
            myEdit.putString("dateFirst", Shared.getDateNow())
            myEdit.apply()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            if(count >= 0 && count < 29){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Trial has expired")
                //builder.setMessage("If you want to continue to use. Please re-install")
                builder.setCancelable(true)
                builder.setPositiveButton(getString(R.string.txt_ok)) { dialog, id ->
                    //finish()
                    val intent = Intent(Intent.ACTION_DELETE)
                    intent.data = Uri.parse("package:com.stc.printbt")
                    startActivity(intent)
                }
                val alert = builder.create()
                alert.setCanceledOnTouchOutside(false)
                alert.show()
            }
        }
    }
}