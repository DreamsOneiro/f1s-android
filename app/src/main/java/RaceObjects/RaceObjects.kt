package RaceObjects

import android.os.Build
import androidx.annotation.RequiresApi
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val client =  OkHttpClient()

val raceURL = "https://raw.githubusercontent.com/DreamsOneiro/f1s-api/main/race.json"
val circuitURL = "https://raw.githubusercontent.com/DreamsOneiro/f1s-api/main/circuit.json"
val gpURL = "https://raw.githubusercontent.com/DreamsOneiro/f1s-api/main/grand_prix.json"
val countryURL = "https://raw.githubusercontent.com/DreamsOneiro/f1s-api/main/country.json"
@RequiresApi(Build.VERSION_CODES.O)
val localZoneId = ZonedDateTime.now().zone

@RequiresApi(Build.VERSION_CODES.O)
fun getRaceList(): List<Race> {
    val circuits = toCircuitMap(requestAPI(circuitURL))
    val grandPrix = toGPMap(requestAPI(gpURL))
    val countries = toCountryMap(requestAPI(countryURL))
    val races = toRaceList(requestAPI(raceURL), circuits, grandPrix, countries)
    return races
}

fun requestAPI(url: String): String {
    val request = Request.Builder().url(url).build()
    var apiStr: String
    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        apiStr = response.body!!.string()
    }
    return apiStr
}

@RequiresApi(Build.VERSION_CODES.O)
fun toRaceList(raceJson: String,
               circuitMap: HashMap<String, Circuit>,
               gpMap: HashMap<String, GrandPrix>,
               countryMap: HashMap<String, Country>
): List<Race> {
    val mapper = jacksonObjectMapper()
    val races = mapper.readValue<List<Race>>(raceJson)
    for (race in races) {
        race.initCircuit(circuitMap)
        race.initGrandPrix(gpMap)
        race.initCountry(countryMap)
        race.initTime()
    }
    return races
}

fun toCircuitMap(jsonStr: String): HashMap<String, Circuit> {
    val mapper = jacksonObjectMapper()
    val circuits = mapper.readValue<List<Circuit>>(jsonStr)
    val cMap = HashMap<String, Circuit>()
    for (c in circuits) {
        cMap[c.id] = c
    }
    return cMap
}

fun toGPMap(jsonStr: String): HashMap<String, GrandPrix> {
    val mapper = jacksonObjectMapper()
    val gPrix = mapper.readValue<List<GrandPrix>>(jsonStr)
    val gpMap = HashMap<String, GrandPrix>()
    for (gp in gPrix) {
        gpMap[gp.id] = gp
    }
    return gpMap
}

fun toCountryMap(jsonStr: String): HashMap<String, Country> {
    val mapper = jacksonObjectMapper()
    val countries = mapper.readValue<List<Country>>(jsonStr)
    val countryMap = HashMap<String, Country>()
    for (count in countries) {
        countryMap[count.id] = count
    }
    return countryMap
}

@RequiresApi(Build.VERSION_CODES.O)
fun localDT(dt: ZonedDateTime?): String {
    val printFormat = DateTimeFormatter.ofPattern("MMM dd (eee) | h:mma '['O']'")
    return dt!!.withZoneSameInstant(localZoneId).format(printFormat).toString()
}


@JsonIgnoreProperties(ignoreUnknown = true)
class Race (
    var year: String,
    var round: String,
    @JsonProperty("grand_prix_id") var gpID: String,
    @JsonProperty("circuit_id") var circuitID: String,
    @JsonProperty("date") var mrDate: String,
    @JsonProperty("time") var mrTime: String,
    var mrDT: ZonedDateTime?,
    @JsonProperty("qualifying_date") var qualiDate: String,
    @JsonProperty("qualifying_time") var qualiTime: String,
    var qualiDT: ZonedDateTime?,
    @JsonProperty("free_practice_1_date") var fp1Date: String,
    @JsonProperty("free_practice_1_time") var fp1Time: String,
    var fp1DT: ZonedDateTime?,
    @JsonProperty("free_practice_2_date") var fp2Date: String?,
    @JsonProperty("free_practice_2_time") var fp2Time: String?,
    var fp2DT: ZonedDateTime?,
    @JsonProperty("free_practice_3_date") var fp3Date: String?,
    @JsonProperty("free_practice_3_time") var fp3Time: String?,
    var fp3DT: ZonedDateTime?,
    @JsonProperty("sprint_qualifying_date") var sqDate: String?,
    @JsonProperty("sprint_qualifying_time") var sqTime: String?,
    var sqDT: ZonedDateTime?,
    @JsonProperty("sprint_race_date") var sprintDate: String?,
    @JsonProperty("sprint_race_time") var sprintTime: String?,
    var sprintDT: ZonedDateTime?,
    var countryID: String?,
    var country: String?,
    var locality: String?,
    var gpName: String?,
    var circuit: String?,
    var circuitType: String?
) {
    fun initCircuit(circuitMap: HashMap<String, Circuit>) {
        if (circuitID in circuitMap){
            val localCircuit = circuitMap[circuitID]
            circuit = localCircuit!!.name
            circuitType = localCircuit.type
            countryID = localCircuit.countryID
            locality = localCircuit.locality
        }
    }
    fun initGrandPrix(gpMap: HashMap<String, GrandPrix>) {
        if (gpID in gpMap){
            val localGP = gpMap[gpID]
            gpName = localGP!!.name
        }
    }

    fun initCountry(countryMap: HashMap<String, Country>) {
        if (countryID in countryMap){
            val localCountry = countryMap[countryID]
            country = localCountry!!.name
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initTime() {
        val sourceFormat = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm")
        val utc = ZoneId.of("UTC")

        fun toUTC (date: String?, time: String?): ZonedDateTime? {
            val dt: ZonedDateTime? = if (date != null) {
                LocalDateTime.parse(date+time, sourceFormat).atZone(utc)
            } else {
                null
            }
            return dt
        }
        mrDT = toUTC(mrDate, mrTime)!!
        fp1DT = toUTC(fp1Date, fp1Time)!!
        qualiDT = toUTC(qualiDate, qualiTime)!!
        fp2DT = toUTC(fp2Date, fp2Time)
        fp3DT = toUTC(fp3Date, fp3Time)
        sqDT = toUTC(sqDate, sqTime)
        sprintDT = toUTC(sprintDate, sprintTime)
    }

    fun hasSprint(): Boolean {
        return sprintDate != null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun mrStrVar1(): String {
        val printFormat = DateTimeFormatter.ofPattern("eeee, MMM dd")
        return mrDT!!.withZoneSameInstant(localZoneId).format(printFormat).toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun mrStrVar2(): String {
        val printFormat = DateTimeFormatter.ofPattern("h:mma '['O']'")
        return mrDT!!.withZoneSameInstant(localZoneId).format(printFormat).toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun qlStrVar1(): String {
        val printFormat = DateTimeFormatter.ofPattern("eeee, MMM dd")
        return qualiDT!!.withZoneSameInstant(localZoneId).format(printFormat).toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun qlStrVar2(): String {
        val printFormat = DateTimeFormatter.ofPattern("h:mma '['O']'")
        return qualiDT!!.withZoneSameInstant(localZoneId).format(printFormat).toString()
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Circuit (
    var id: String,
    var type: String,
    @JsonProperty("full_name") var name: String,
    @JsonProperty("country_id")var countryID: String,
    @JsonProperty("place_name") var locality: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GrandPrix (
    var id: String,
    @JsonProperty("full_name") var name: String,
    @JsonProperty("name") var locality: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Country (
    var id: String,
    var name: String
)
