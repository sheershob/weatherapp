package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.content.Context
import android.widget.AutoCompleteTextView
import retrofit2.http.GET
import retrofit2.http.Header
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var searchView: AutoCompleteTextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setCurrentDayAndDate()
        searchCity()
//        fetchData("Abu Dhabi")
    }

    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchData(query) // Call fetchData with the query
                }
                hideKeyboard()
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchData(cityName:String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName, "7479ab49e6129e42c52533f454ceed6e", "metric")
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temperature = responseBody.main.temp.toString()
                    val feelsLike = responseBody.main.feels_like
                    val humidity = responseBody.main.humidity
                    val windspeed = responseBody.wind.speed
                    val sunrise = responseBody.sys.sunrise.toLong()
                    val sunset = responseBody.sys.sunset.toLong()
                    val pressure = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main?:"unknown"
                    val max = responseBody.main.temp_max
                    val min = responseBody.main.temp_min
                    val visibility = (responseBody.visibility)/1000.0
                    binding.temp.text = "$temperature °C"
                    binding.feel.text = "feels like $feelsLike °C"
                    binding.Visibility.text = "$visibility km"
                    binding.weather.text = condition
                    binding.max.text = "Max: $max °C"
                    binding.min.text = "Min: $min °C"
                    binding.humidity.text = "$humidity %"
                    binding.wind.text = "$windspeed m/s"
                    binding.pressure.text = "$pressure mBar"
                    binding.sunrise.text = "${time(sunrise)}"
                    binding.sunset.text = "${time(sunset)}"
                    binding.cityName.text = "$cityName"
                    binding.date.text = date()
                    binding.day.text = dayName(System.currentTimeMillis())
//                    Log.d("TAG", "onResponse: $temperature")

                    weatherImg(condition, "${time(sunrise)}", "${time(sunset)}")
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                // Handle failure, log error, or show a message to the user
                Log.e("MainActivity", "Error fetching weather data: ${t.message}")
            }
        })
    }

    private fun weatherImg(condition: String, sunriseTime: String, sunsetTime: String) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val sunrise = LocalTime.parse(sunriseTime, formatter)
        val sunset = LocalTime.parse(sunsetTime, formatter)

        // Get the current time
        val currentTime = LocalTime.now()

        // Check if it's day or night
        if (currentTime.isAfter(sunrise) && currentTime.isBefore(sunset)){
        when(condition) {
            "Overcast", "Clouds" -> {
                binding.root.setBackgroundResource(R.drawable.cloud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }

            "Partly Clouds", "Few Clouds", "Broken Clouds", "Scattered Clouds" -> {
                binding.root.setBackgroundResource(R.drawable.cloud_background)
                binding.lottieAnimationView.setAnimation(R.raw.partly_cloudy)
            }

            "Haze", "Mist", "Foggy", "Smoke", "Dust", "Fog", "Sand", "Ash" -> {
                binding.root.setBackgroundResource(R.drawable.cloud_background)
                binding.lottieAnimationView.setAnimation(R.raw.haze)
            }

            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sunny)
            }

            "Light Rain", "Drizzle", "Showers", "Moderate Rain", "Shower Rain", "Overcast Clouds" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }

            "ThunderStorm" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.thunderstorm)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Snow", "Hail", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }

            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sunny)
            }
        }
        }
        else{
            when(condition) {
                "Partly Clouds", "Few Clouds", "Broken Clouds", "Scattered Clouds", "Overcast", "Clouds" -> {
                    binding.root.setBackgroundResource(R.drawable.night_background)
                    binding.lottieAnimationView.setAnimation(R.raw.cloudy_night)
                }

                "Haze", "Mist", "Foggy", "Smoke", "Dust", "Fog", "Sand", "Ash" -> {
                    binding.root.setBackgroundResource(R.drawable.night_background)
                    binding.lottieAnimationView.setAnimation(R.raw.haze)
                }

                "Clear Sky", "Sunny", "Clear" -> {
                    binding.root.setBackgroundResource(R.drawable.night_background)
                    binding.lottieAnimationView.setAnimation(R.raw.night)
                }

                "Light Rain", "Drizzle", "Showers", "Moderate Rain", "Shower Rain", "Overcast Clouds", "ThunderStorm" -> {
                    binding.root.setBackgroundResource(R.drawable.rain_background)
                    binding.lottieAnimationView.setAnimation(R.raw.night_rain)
                }

                "Light Snow", "Moderate Snow", "Heavy Snow", "Snow", "Hail", "Blizzard" -> {
                    binding.root.setBackgroundResource(R.drawable.night_background)
                    binding.lottieAnimationView.setAnimation(R.raw.snow)
                }

                else -> {
                    binding.root.setBackgroundResource(R.drawable.night_background)
                    binding.lottieAnimationView.setAnimation(R.raw.night)
                }
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus
        if (view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun setCurrentDayAndDate() {
        binding.date.text = date()
        binding.day.text = dayName(System.currentTimeMillis())
    }

    fun date():String{
        val sdf = SimpleDateFormat("DD MMMM YYYY", Locale.getDefault())
        return sdf.format(Date())
    }

    fun time(timeStamp: Long): String{
        val sdf = SimpleDateFormat("HH:MM", Locale.getDefault())
        return sdf.format(Date(timeStamp*1000))
    }

    fun dayName(time:Long):String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }
}
