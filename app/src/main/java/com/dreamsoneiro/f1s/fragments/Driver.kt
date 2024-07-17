package com.dreamsoneiro.f1s.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.dreamsoneiro.f1s.R
import standing.DriverStanding
import standing.getDriverStandings
import okio.IOException

var drivers: List<DriverStanding>? = null

@SuppressLint("DiscouragedApi")
class Driver : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val refreshButton = view.findViewById<ImageButton>(R.id.refresh_button)
        val driverTitle = view.findViewById<TextView>(R.id.driver_title)

        fun newRow(num: Int): Rows {
            val id1 = resources.getIdentifier("box_${num}_1", "id", activity?.packageName)
            val id2 = resources.getIdentifier("box_${num}_2", "id", activity?.packageName)
            val id3 = resources.getIdentifier("box_${num}_3", "id", activity?.packageName)
            val id4 = resources.getIdentifier("box_${num}_4", "id", activity?.packageName)
            val row = Rows (
                view.findViewById(id1),
                view.findViewById(id2),
                view.findViewById(id3),
                view.findViewById(id4),
            )
            return row
        }

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
                activity?.runOnUiThread {
                    driverTitle.text = "${drivers!![0].year} ${drivers!![0].gp}"
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

        refreshButton.setOnClickListener {
            if (drivers == null) {
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

class Rows (
    var position: TextView,
    var driver: TextView,
    var points: TextView,
    var gained: TextView
)
