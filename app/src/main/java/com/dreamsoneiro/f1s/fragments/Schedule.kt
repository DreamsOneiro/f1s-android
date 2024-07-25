package com.dreamsoneiro.f1s.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.dreamsoneiro.f1s.R
import okio.IOException
import raceObjects.Race
import raceObjects.getRaceList
import raceObjects.localDT
import raceObjects.tzRefresh
import java.time.ZoneId
import java.time.ZonedDateTime

var races: List<Race>? = null
var racesName: List<String> = ArrayList()
var index: Int = 0
var offset: Int = 0

class Schedule : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonRefresh = view.findViewById<ImageButton>(R.id.refresh_button)
        val currentTitle = view.findViewById<TextView>(R.id.current_gp)
        val seasonVal = view.findViewById<TextView>(R.id.season_val)
        val roundVal = view.findViewById<TextView>(R.id.round_val)
        val raceVal = view.findViewById<TextView>(R.id.race_val)
        val circuitVal = view.findViewById<TextView>(R.id.circuit_val)
        val locationValLocality = view.findViewById<TextView>(R.id.location_val_locality)
        val locationValCountry = view.findViewById<TextView>(R.id.location_val_country)
        val race1 = view.findViewById<TextView>(R.id.race1)
        val race1Val = view.findViewById<TextView>(R.id.race1_val)
        val race2 = view.findViewById<TextView>(R.id.race2)
        val race2Val = view.findViewById<TextView>(R.id.race2_val)
        val race3 = view.findViewById<TextView>(R.id.race3)
        val race3Val = view.findViewById<TextView>(R.id.race3_val)
        val qualiVal1 = view.findViewById<TextView>(R.id.quali_val1)
        val qualiVal2 = view.findViewById<TextView>(R.id.quali_val2)
        val mainRace1 = view.findViewById<TextView>(R.id.main_val1)
        val mainRace2 = view.findViewById<TextView>(R.id.main_val2)
        val buttonPrev = view.findViewById<ImageButton>(R.id.previous)
        val buttonNext = view.findViewById<ImageButton>(R.id.next)
        val dropDownList = view.findViewById<Spinner>(R.id.drop_down_list)


        fun calculate() {
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
                }
            }
        }

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
                calculate()
            }
        }

        fun loadData(offset: Int) {
            val race = races!![index + offset]
            activity?.runOnUiThread {
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

        dropDownList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                offset = position - index
                loadData(offset)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val thread1 = Thread {
            requestData()
            activity?.runOnUiThread {
                val aa = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, racesName)
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dropDownList.adapter = aa
                dropDownList.setSelection(index + offset)
            }
        }
        thread1.start()

        buttonRefresh.setOnClickListener {
            offset = 0
            tzRefresh()
            if (!thread1.isAlive) {
                if (races == null) {
                    val thread2 = Thread {
                        requestData()
                        loadData(offset)
                    }
                    thread2.start()
                } else {
                    calculate()
                    dropDownList.setSelection(index+offset)
                    loadData(offset)
                }
            }
        }

        buttonPrev.setOnClickListener {
            if (races != null) {
                if (((offset - 1 + index) < (races!!.size - 1)) && (offset - 1 + index >= 0)) {
                    offset -= 1
                    dropDownList.setSelection(index+offset)
                }
            }
        }

        buttonNext.setOnClickListener {
            if (races != null) {
                if (((offset + 1 + index) < (races!!.size)) && (offset + 1 + index >= 0)) {
                    offset += 1
                    dropDownList.setSelection(index+offset)
                }
            }
        }
    }
}