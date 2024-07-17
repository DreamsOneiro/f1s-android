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
import okio.IOException
import standing.ConstructorStanding
import standing.getConstructorStandings

var constructor: List<ConstructorStanding>? = null

@SuppressLint("DiscouragedApi")
class Constructor : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_constructor, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val constructorTitle = view.findViewById<TextView>(R.id.constructor_title)
        val refreshButton = view.findViewById<ImageButton>(R.id.refresh_button)

        fun newRow(num: Int): ConRows {
            val id1 = resources.getIdentifier("box_${num}_1", "id", activity?.packageName)
            val id2 = resources.getIdentifier("box_${num}_2", "id", activity?.packageName)
            val id3 = resources.getIdentifier("box_${num}_3", "id", activity?.packageName)
            val id4 = resources.getIdentifier("box_${num}_4", "id", activity?.packageName)
            val row = ConRows (
                view.findViewById(id1),
                view.findViewById(id2),
                view.findViewById(id3),
                view.findViewById(id4),
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
                activity?.runOnUiThread {
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
