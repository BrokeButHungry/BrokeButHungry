package com.appsbycarla.brokebuthungry
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var resultsTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        resultsTextView = findViewById(R.id.resultsTextView)

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                // Clear previous results
                resultsTextView.text = "Searching..."

                // Perform the API request
                FetchRecipesTask().execute(query)
            }
        }
    }

    inner class FetchRecipesTask : AsyncTask<String, Void, String>() {

        // This method is executed in the background thread.
        override fun doInBackground(vararg params: String?): String {
            // Extract the query from the task parameters.
            val query = params[0]

            // API credentials (app ID and app key) for accessing the Edamam API.
            val apiId = "456948c8"
            val apiKey = "da192cac6c51d23c7025297c6c6f78b4"

            // Construct the URL for the API request using the query and credentials.
            val apiUrl = "https://api.edamam.com/search?q=$query&app_id=$apiId&app_key=$apiKey"

            try {
                // Create a URL object from the constructed URL string.
                val url = URL(apiUrl)

                // Open a connection to the URL as an HttpURLConnection.
                val connection = url.openConnection() as HttpURLConnection

                // Set the request method to "GET."
                connection.requestMethod = "GET"

                // Get the HTTP response code from the server.
                val responseCode = connection.responseCode

                // Check if the response code indicates a successful response (HTTP OK).
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response data from the input stream.
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?

                    // Read each line of the response and append it to the StringBuilder.
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }

                    // Close the reader.
                    reader.close()

                    // Return the response data as a string.
                    return response.toString()
                }
            } catch (e: Exception) {
                // Handle exceptions, such as network errors or invalid URLs.
                e.printStackTrace()
            }

            // If there was an error or no data was received, return an empty string.
            return ""
        }

        // This method is executed on the main UI thread after doInBackground completes.
        override fun onPostExecute(result: String) {
            if (result.isNotEmpty()) {
                // If the result is not empty, call the displayResults method to show the data.
                displayResults(result)
            } else {
                // If there are no results, display a message in the resultsTextView.
                resultsTextView.text = "No results found."
            }
        }
    }


    private fun displayResults(jsonResult: String) {
        try {
            // Parse the incoming JSON string into a JSONObject.
            val jsonObject = JSONObject(jsonResult)

            // Get the "hits" array from the JSONObject.
            val hitsArray = jsonObject.getJSONArray("hits")

            // Create an ArrayList to store recipe information.
            val recipes = ArrayList<String>()

            // Loop through each item in the "hits" array.
            for (i in 0 until hitsArray.length()) {
                // Get the "recipe" object for the current item.
                val recipeObject = hitsArray.getJSONObject(i).getJSONObject("recipe")

                // Extract relevant data from the "recipe" object.
                val recipeLabel = recipeObject.getString("label")
                val recipeCalories = recipeObject.getDouble("calories")


                // Create a string containing recipe information.
                val recipeInfo = "$recipeLabel - Calories: $recipeCalories"

                // Add the recipe information to the ArrayList.
                recipes.add(recipeInfo)
            }

            // Join the recipe information strings with line breaks ("\n").
            val resultText = recipes.joinToString("\n")

            // Set the resulting text in a TextView (resultsTextView).
            resultsTextView.text = resultText
        } catch (e: Exception) {
            // Handle exceptions, such as JSON parsing errors.
            e.printStackTrace()

            // Display an error message in the TextView if an exception occurs.
            resultsTextView.text = "Error parsing JSON."
        }
    }
}

