package standing

import android.os.Build
import androidx.annotation.RequiresApi
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import raceObjects.requestAPI

const val driverURL = "https://raw.githubusercontent.com/DreamsOneiro/f1s-api/main/driver_standings.json"
const val constructorURL = "https://raw.githubusercontent.com/DreamsOneiro/f1s-api/main/constructor_standings.json"

@RequiresApi(Build.VERSION_CODES.O)
fun getDriverStandings(): List<DriverStanding> {
    val mapper = jacksonObjectMapper()
    val driverJson = requestAPI(driverURL)
    return mapper.readValue<List<DriverStanding>>(driverJson)
}

@RequiresApi(Build.VERSION_CODES.O)
fun getConstructorStandings(): List<ConstructorStanding> {
    val mapper = jacksonObjectMapper()
    val constructorJson = requestAPI(constructorURL)
    return mapper.readValue<List<ConstructorStanding>>(constructorJson)
}

@JsonIgnoreProperties(ignoreUnknown = true)
class DriverStanding (
    @JsonProperty("position_number") var position: Int,
    var name: String,
    var points: Int,
    var round: Int,
    @JsonProperty("full_name") var gp: String,
    @JsonProperty("positions_gained") var gained: Int,
    var year: Int
)

@JsonIgnoreProperties(ignoreUnknown = true)
class ConstructorStanding (
    @JsonProperty("position_number") var position: Int,
    var name: String,
    var points: Int,
    @JsonProperty("full_name") var gp: String,
    @JsonProperty("positions_gained") var gained: Int,
    var year: Int
)
