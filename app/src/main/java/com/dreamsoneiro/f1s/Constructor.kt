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
import okio.IOException
import standing.ConstructorStanding
import standing.getConstructorStandings

var constructor: List<ConstructorStanding>? = null

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("DiscouragedApi")
class Constructor : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_constructor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val scheduleButton = findViewById<Button>(R.id.to_main)
        val driverButton = findViewById<Button>(R.id.to_driver)
        val constructorTitle = findViewById<TextView>(R.id.constructor_title)
        val refreshButton = findViewById<Button>(R.id.refresh_button)

        scheduleButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        driverButton.setOnClickListener {
            val intent = Intent(this, Driver::class.java)
            startActivity(intent)
        }

        fun newRow(num: Int): ConRows {
            val id1 = resources.getIdentifier("box_${num}_1", "id", this.packageName)
            val id2 = resources.getIdentifier("box_${num}_2", "id", this.packageName)
            val id3 = resources.getIdentifier("box_${num}_3", "id", this.packageName)
            val id4 = resources.getIdentifier("box_${num}_4", "id", this.packageName)
            val row = ConRows (
                findViewById(id1),
                findViewById(id2),
                findViewById(id3),
                findViewById(id4),
            )
            return row
        }

        val rows = mutableListOf<ConRows>()
        for (i in 2..11) {
            rows.add(newRow(i))
        }

        fun requestData() {
            if (constructor == null) {
                constructor = try {
                    getConstructorStandings()
                } catch (e: IOException) {
                    null
                }
            }
        }

        fun loadData() {
            if (constructor != null) {
                runOnUiThread {
                    constructorTitle.text = "${constructor!![0].year} ${constructor!![0].gp}"
                    for ((i, con) in constructor!!.withIndex()) {
                        if (i <= rows.size) {
                            val row = rows[i]
                            row.position.text = con.position.toString()
                            row.constructor.text = con.name
                            row.points.text = con.points.toString()
                            row.gained.text = con.gained.toString()
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

        refreshButton.setOnClickListener {
            if (constructor == null) {
                if (!thread1.isAlive) {
                    val thread2 = Thread {
                        requestData()
                        loadData()
                    }
                    thread2.start()
                }
            }
        }
    }
}

class ConRows (
    var position: TextView,
    var constructor: TextView,
    var points: TextView,
    var gained: TextView
)
