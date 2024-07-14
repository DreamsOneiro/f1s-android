package driverStanding

import android.os.Build
import androidx.annotation.RequiresApi
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

const val driverURL = "https://raw.githubusercontent.com/DreamsOneiro/f1s-api/main/driver_standings.json"
const val constructorURL = "https://raw.githubusercontent.com/DreamsOneiro/f1s-api/main/constructor_standings.json"

private val client =  OkHttpClient()
private fun requestAPI(url: String): String {
    val request = Request.Builder().url(url).build()
    var apiStr: String
    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        apiStr = response.body!!.string()
    }
    return apiStr
}

@RequiresApi(Build.VERSION_CODES.O)
fun getDriverStandings(): List<DriverStanding> {
    val mapper = jacksonObjectMapper()
    val driverJson = requestAPI(driverURL)
    return mapper.readValue<List<DriverStanding>>(driverJson)
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