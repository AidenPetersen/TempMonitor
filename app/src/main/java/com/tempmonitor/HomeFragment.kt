package com.tempmonitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import java.lang.Exception
import com.beust.klaxon.*

class HomeFragment : Fragment() {

    private var address: String = "";
    private var temperatureView: TextView? = null
    private val temperature = MutableLiveData("error")
    private var job: Job? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fab_settings.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }
        settings()
        job = repeatBackground()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }


    private fun repeatBackground(): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val (request, response, result) = address
                        .httpGet()
                        .responseString()

                    when (result) {
                        is Result.Success -> {
                            val data = result.get()

                            val parser: Parser = Parser.default()
                            val sb: StringBuilder = StringBuilder(data)
                            val json: JsonObject = parser.parse(sb) as JsonObject

                            val t = json.string("temp")

                            if (t != null) {
                                setTemperature(t)
                            } else {
                                throw Exception()
                            }
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    setTemperature("error")
                }

                delay(5000)
            }
        }
    }

    fun setTemperature(str: String) {
        if (str == "error") {
            temperature.postValue("error");
        }
        temperature.postValue("$str°F")
    }

    private fun settings() {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        this.temperatureView = view?.findViewById(R.id.temperature)
        this.address = sp.getString("address", "°F").toString()
        temperature.observe(viewLifecycleOwner, { newTemp -> temperatureView?.text = newTemp })

    }
}

