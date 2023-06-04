package com.example.meal_mania


import android.os.AsyncTask
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
This is a data class for storing meal details as an entity in Room database
 * */

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey val id: Int, // Primary key for the meals table
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val tags: String,
    val youtubeLink: String,
    val measures: List<String>,
    @TypeConverters(ListStringConverter::class)
    val ingredients: List<String>, // List of ingredients for the meal
    val mealThumb: String
)


class RetrieveMealsTask(private val ingredient: String, private val callback: (List<Meal>?) -> Unit) :
    AsyncTask<Void, Void, List<Meal>?>() {

    override fun doInBackground(vararg params: Void?): List<Meal>? {
        var meals: List<Meal>? = null
        // Connect to the API URL to retrieve the meals data
        try {
            val url = URL("https://www.themealdb.com/api/json/v1/1/filter.php?i=$ingredient") // API endpoint URL for retrieving meals with a specific ingredient
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            // Check if the response code is HTTP_OK (200), indicating that the connection was successful.
            val responseCode = connection.responseCode // HTTP response code
            if (responseCode == HttpURLConnection.HTTP_OK) { // Check if response code is successful
                val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                val stringBuilder = StringBuilder()
                var line: String? = null
                while ({ line = bufferedReader.readLine(); line }() != null) { // Read the response into a string
                    stringBuilder.append(line)
                }
                bufferedReader.close()

                // Parse the JSON response into a list of Meal objects.
                val jsonObject = JSONObject(stringBuilder.toString()) // Parse the response string into a JSON object
                val jsonArray = jsonObject.getJSONArray("meals") // Get the meals array from the JSON object
                meals = mutableListOf()
                meals = mutableListOf()
                for (i in 0 until jsonArray.length()) {
                    val mealObject = jsonArray.getJSONObject(i)
                    val mealId = mealObject.getString("idMeal")  // Get the meal ID for each meal in the array
                    val mealDetailsResponse =
                        URL("https://www.themealdb.com/api/json/v1/1/lookup.php?i=$mealId").readText() // API endpoint URL for retrieving details of a specific meal
                    val mealDetails = JSONObject(mealDetailsResponse).getJSONArray("meals").getJSONObject(0) // Get the meal details JSON object
                    val id = mealDetails.getString("idMeal").toInt()
                    val name = mealDetails.getString("strMeal")
                    val category = mealDetails.getString("strCategory")
                    val area = mealDetails.getString("strArea")
                    val instructions = mealDetails.getString("strInstructions")
                    val tags = mealDetails.optString("strTags", "")
                    val youtubeLink = mealDetails.optString("strYoutube", "")
                    val mealThumb = mealDetails.optString("strMealThumb", "")
                    val ingredients = mutableListOf<String>()
                    val measures = mutableListOf<String>()
                    for (j in 1..20) { // Iterate over each ingredient and measure for the meal
                        val ingredient = mealDetails.optString("strIngredient$j", "")
                        if (ingredient.isNotEmpty()) {
                            ingredients.add(ingredient)
                            measures.add(mealDetails.optString("strMeasure$j", ""))
                        }
                    }
                    val meal = Meal(
                        id=id,
                        name = name,
                        category = category,
                        area = area,
                        instructions = instructions,
                        tags = tags,
                        youtubeLink = youtubeLink,
                        measures = measures,
                        ingredients = ingredients,
                        mealThumb = mealThumb
                    )
                    meals.add(meal)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return meals
    }

    override fun onPostExecute(result: List<Meal>?) {
        callback(result)
    }
}

class ListStringConverter {
    // @TypeConverter tells Room to use them for converting between the types in the Room database.
    @TypeConverter
    fun fromListString(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toListString(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }
}
