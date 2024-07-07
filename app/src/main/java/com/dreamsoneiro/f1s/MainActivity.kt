package com.dreamsoneiro.f1s

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

var races: List<Races>? = null

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val seasonVal = findViewById<TextView>(R.id.season_val)
        val roundVal = findViewById<TextView>(R.id.round_val)
        val raceVal = findViewById<TextView>(R.id.race_val)
        val circuitVal = findViewById<TextView>(R.id.circuit_val)
        val locationValLocality = findViewById<TextView>(R.id.location_val_locality)
        val locationValCountry = findViewById<TextView>(R.id.location_val_country)
        val thread = Thread {
            val client = OkHttpClient()
            val url = "https://ergast.com/api/f1/current.json"
            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: java.io.IOException) {
                    e.printStackTrace()
                }

                @RequiresApi(Build.VERSION_CODES.O)
                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        races = toRaces(response.body!!.string())
                        val zone = ZoneId.of("UTC")
                        val localZoneId = ZonedDateTime.now().zone
                        val timeNow = ZonedDateTime.now().withZoneSameInstant(zone)
                        var index = 0;
                        if (races != null) {
                            for ((i, race) in races!!.withIndex()) {
                                val rtFormat = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss'Z'")
                                val raceTime = LocalDateTime.parse(race.date + race.time, rtFormat).atZone(zone)
                                if (timeNow.isBefore(raceTime)) {
                                    index = i
                                    break
                                }
                            }
                        }
                        runOnUiThread {
                            if (races != null) {
                                seasonVal.text = "${races!![index].season},"
                                roundVal.text = races!![index].round
                                raceVal.text = races!![index].raceName
                                circuitVal.text = races!![index].circuit.circuitName
                                locationValLocality.text = "${races!![index].circuit.location.locality},"
                                locationValCountry.text = races!![index].circuit.location.country
                            }
                        }
                    }
                }
            })
        }
        thread.start()
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
)

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
)