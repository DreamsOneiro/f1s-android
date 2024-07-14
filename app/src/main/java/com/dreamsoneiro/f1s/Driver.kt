package com.dreamsoneiro.f1s

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import driverStanding.DriverStanding
import driverStanding.getDriverStandings
import okio.IOException

var drivers: List<DriverStanding>? = null

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SetTextI18n")
class Driver : AppCompatActivity() {
    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_driver)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fun newRow(num: Int): Rows {
            val id1 = resources.getIdentifier("box_${num}_1", "id", this.packageName)
            val id2 = resources.getIdentifier("box_${num}_2", "id", this.packageName)
            val id3 = resources.getIdentifier("box_${num}_3", "id", this.packageName)
            val id4 = resources.getIdentifier("box_${num}_4", "id", this.packageName)
            val row = Rows (
                findViewById(id1),
                findViewById(id2),
                findViewById(id3),
                findViewById(id4),
            )
            return row
        }

        val scheduleButton = findViewById<Button>(R.id.to_main)
        scheduleButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val driverTitle = findViewById<TextView>(R.id.driver_title)

        val rows = mutableListOf<Rows>()
        for (i in 2..24) {
            rows.add(newRow(i))
        }

        fun requestData() {
            if (drivers == null) {
                drivers = try {
                    getDriverStandings()
                } catch (e: IOException) {
                    null
                }
            }
        }

        fun loadData() {
            if (drivers != null) {
                runOnUiThread {
                    driverTitle.text = "${drivers!![0].year} ${drivers!![0].gp} [Round ${drivers!![0].round}]"
                    for ((i, driver) in drivers!!.withIndex()) {
                        if (i <= rows.size) {
                            val row = rows[i]
                            row.position.text = driver.position.toString()
                            row.driver.text = driver.name
                            row.points.text = driver.points.toString()
                            row.gained.text = driver.gained.toString()
                        }
                    }
                }
            }
        }

        val thread1 = Thread {
            requestData()
            loadData()
        }
        thread1.start()
    }
}

class Rows (
    var position: TextView,
    var driver: TextView,
    var points: TextView,
    var gained: TextView
)
