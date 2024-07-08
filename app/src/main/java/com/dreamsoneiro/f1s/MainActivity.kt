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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import okio.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

var races: List<Races>? = null
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
                val client = OkHttpClient()
                val url = "https://ergast.com/api/f1/current.json"
                val request = Request.Builder().url(url).build()

                try {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        races = toRaces(response.body!!.string())
                        val mutRacesName: MutableList<String> = ArrayList()
                        for ((i, race) in races!!.withIndex()) {
                            mutRacesName.add("${i + 1}. ${race.circuit.location.locality}, ${race.circuit.location.country}")
                        }
                        racesName = mutRacesName.toList()
                    }
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
                    val rtFormat = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss'Z'")
                    for ((i, race) in races!!.withIndex()) {
                        val raceTime = LocalDateTime.parse(race.date + race.time, rtFormat).atZone(utc)
                        if (timeNow.isBefore(raceTime)) {
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
                        seasonVal.text = "${race.season},"
                        roundVal.text = race.round
                        raceVal.text = race.raceName
                        circuitVal.text = race.circuit.circuitName
                        locationValLocality.text = "${race.circuit.location.locality},"
                        locationValCountry.text = race.circuit.location.country
                        race1.text = "FP1:"
                        race1Val.text = race.fp1.localDT()
                        race2.text = if (race.hasSprint()) {
                            "SQ:"
                        } else {
                            "FP2:"
                        }
                        race2Val.text = race.fp2.localDT()
                        race3.text = if (race.hasSprint()) {
                            "Sprint:"
                        } else {
                            "FP3:"
                        }
                        race3Val.text = if (race.hasSprint()) {
                            race.sprint!!.localDT()
                        } else {
                            race.fp3!!.localDT()
                        }
                        qualiVal1.text = race.quali.localDTvar1()
                        qualiVal2.text = race.quali.localDTvar2()
                        mainRace1.text = race.localDTvar1()
                        mainRace2.text = race.localDTvar2()
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
                    loadData(offset)
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

fun toRaces(jsonStr: String): List<Races> {
    val mapper = jacksonObjectMapper()
    val jsonObj = mapper.readValue<Ergast>(jsonStr)
    return jsonObj.mrData.raceTable.races
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Ergast (
    @JsonProperty("MRData") var mrData: MRData
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MRData (
    @JsonProperty("RaceTable") var raceTable: RaceTable
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RaceTable (
    @JsonProperty("Races") var races: List<Races>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Races (
    var season: String,
    var round: String,
    var raceName: String,
    @JsonProperty("Circuit") var circuit: Circuit,
    var date: String,
    var time: String,
    @JsonProperty("FirstPractice") var fp1: SubRace,
    @JsonProperty("SecondPractice") var fp2: SubRace,
    @JsonProperty("ThirdPractice") var fp3: SubRace?,
    @JsonProperty("Sprint") var sprint: SubRace?,
    @JsonProperty("Qualifying") var quali: SubRace,
) {
    fun hasSprint(): Boolean {
        return sprint != null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun localDTvar1(): String {
        val utc = ZoneId.of("UTC")
        val localZoneId = ZonedDateTime.now().zone
        val printFormat = DateTimeFormatter.ofPattern("eeee, MMM dd")
        val rtFormat = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss'Z'")
        return LocalDateTime.parse(date+time, rtFormat)
            .atZone(utc)
            .withZoneSameInstant(localZoneId)
            .format(printFormat)
            .toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun localDTvar2(): String {
        val utc = ZoneId.of("UTC")
        val localZoneId = ZonedDateTime.now().zone
        val printFormat = DateTimeFormatter.ofPattern("h:mma '['O']'")
        val rtFormat = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss'Z'")
        return LocalDateTime.parse(date+time, rtFormat)
            .atZone(utc)
            .withZoneSameInstant(localZoneId)
            .format(printFormat)
            .toString()
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Circuit (
    val circuitName: String,
    @JsonProperty("Location") var location: Location,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Location (
    var locality: String,
    var country: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubRace (
    var date: String,
    var time: String
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun localDT(): String {
        val utc = ZoneId.of("UTC")
        val localZoneId = ZonedDateTime.now().zone
        val printFormat = DateTimeFormatter.ofPattern("'('eee')' MMM dd | h:mma '['O']'")
        val rtFormat = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss'Z'")
        return LocalDateTime.parse(date+time, rtFormat)
            .atZone(utc)
            .withZoneSameInstant(localZoneId)
            .format(printFormat)
            .toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun localDTvar1(): String {
        val utc = ZoneId.of("UTC")
        val localZoneId = ZonedDateTime.now().zone
        val printFormat = DateTimeFormatter.ofPattern("eeee, MMM dd")
        val rtFormat = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss'Z'")
        return LocalDateTime.parse(date+time, rtFormat)
            .atZone(utc)
            .withZoneSameInstant(localZoneId)
            .format(printFormat)
            .toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun localDTvar2(): String {
        val utc = ZoneId.of("UTC")
        val localZoneId = ZonedDateTime.now().zone
        val printFormat = DateTimeFormatter.ofPattern("h:mma '['O']'")
        val rtFormat = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss'Z'")
        return LocalDateTime.parse(date+time, rtFormat)
            .atZone(utc)
            .withZoneSameInstant(localZoneId)
            .format(printFormat)
            .toString()
    }
}
