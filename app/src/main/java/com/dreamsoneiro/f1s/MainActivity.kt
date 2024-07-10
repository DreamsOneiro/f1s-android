package com.dreamsoneiro.f1s

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okio.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import RaceObjects.Race
import RaceObjects.getRaceList
import RaceObjects.localDT

var races: List<Race>? = null
var racesName: List<String> = ArrayList()
var index: Int = 0
var offset: Int = 0
var firstRun: Boolean = true

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonRefresh = findViewById<Button>(R.id.refresh_button)
        val currentTitle = findViewById<TextView>(R.id.current_gp)
        val seasonVal = findViewById<TextView>(R.id.season_val)
        val roundVal = findViewById<TextView>(R.id.round_val)
        val raceVal = findViewById<TextView>(R.id.race_val)
        val circuitVal = findViewById<TextView>(R.id.circuit_val)
        val locationValLocality = findViewById<TextView>(R.id.location_val_locality)
        val locationValCountry = findViewById<TextView>(R.id.location_val_country)
        val race1 = findViewById<TextView>(R.id.race1)
        val race1Val = findViewById<TextView>(R.id.race1_val)
        val race2 = findViewById<TextView>(R.id.race2)
        val race2Val = findViewById<TextView>(R.id.race2_val)
        val race3 = findViewById<TextView>(R.id.race3)
        val race3Val = findViewById<TextView>(R.id.race3_val)
        val qualiVal1 = findViewById<TextView>(R.id.quali_val1)
        val qualiVal2 = findViewById<TextView>(R.id.quali_val2)
        val mainRace1 = findViewById<TextView>(R.id.main_val1)
        val mainRace2 = findViewById<TextView>(R.id.main_val2)
        val buttonPrev = findViewById<Button>(R.id.previous)
        val buttonNext = findViewById<Button>(R.id.next)
        val dropDownList = findViewById<Spinner>(R.id.drop_down_list)

        fun requestData() {
            if (races == null) {
                try {
                    races = getRaceList()
                    val mutRacesName: MutableList<String> = ArrayList()
                    for ((i, race) in races!!.withIndex()) {
                        mutRacesName.add("${i + 1}. ${race.locality}, ${race.country}")
                    }
                    racesName = mutRacesName.toList()
                } catch (e: IOException) {
                    races = null
                }
            }
        }

        fun loadData(offset: Int) {
            if (races != null) {
                val utc = ZoneId.of("UTC")
                val timeNow = ZonedDateTime.now().withZoneSameInstant(utc)
                if (races != null) {
                    for ((i, race) in races!!.withIndex()) {
                        if (timeNow.isBefore(race.mrDT)) {
                            index = i
                            break
                        }
                    }
                    val race = races!![index+offset]

                    if (firstRun) {
                        runOnUiThread {
                            val aa = ArrayAdapter (this, android.R.layout.simple_spinner_item, racesName)
                            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            dropDownList.adapter = aa
                            dropDownList.setSelection(index)
                        }
                        firstRun = false
                    }

                    runOnUiThread {
                        currentTitle.text = if (offset != 0) {
                            "Other GP"
                        } else {
                            "Current GP"
                        }
                        seasonVal.text = "${race.year},"
                        roundVal.text = race.round
                        raceVal.text = race.gpName
                        circuitVal.text = race.circuit
                        locationValLocality.text = "${race.locality},"
                        locationValCountry.text = race.country
                        race1.text = "FP1:"
                        race1Val.text = localDT(race.fp1DT)
                        if (race.hasSprint()) {
                            race2.text = "SQ:"
                            race2Val.text = localDT(race.sqDT)
                            race3.text = "Sprint:"
                            race3Val.text = localDT(race.sprintDT)
                        } else {
                            race2.text = "FP2:"
                            race2Val.text = localDT(race.fp2DT)
                            race3.text = "FP3:"
                            race3Val.text = localDT(race.fp3DT)
                        }
                        qualiVal1.text = race.qlStrVar1()
                        qualiVal2.text = race.qlStrVar2()
                        mainRace1.text = race.mrStrVar1()
                        mainRace2.text = race.mrStrVar2()
                    }
                }
            }
        }

        dropDownList.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // make toastof name of course
                // which is selected in spinner
                offset = position - index
                loadData(offset)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val thread1 = Thread {
            requestData()
            loadData(offset)
        }
        thread1.start()

        buttonRefresh.setOnClickListener {
            offset = 0
            if (!thread1.isAlive) {
                if (races == null) {
                    val thread2 = Thread {
                        requestData()
                        loadData(offset)
                    }
                    thread2.start()
                } else {
                    dropDownList.setSelection(index + offset)
                }
            }
        }

        buttonPrev.setOnClickListener {
            if (races != null) {
                if (((offset - 1 + index) < (races!!.size - 1)) && (offset - 1 + index >= 0)){
                    offset -= 1
                    dropDownList.setSelection(index + offset)
                }
            }
        }

        buttonNext.setOnClickListener {
            if (races != null) {
                if (((offset + 1 + index) < (races!!.size)) && (offset + 1 + index >= 0)){
                    offset += 1
                    dropDownList.setSelection(index + offset)
                }
            }
        }


    }

}
