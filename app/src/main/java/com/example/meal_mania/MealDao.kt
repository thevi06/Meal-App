package com.example.meal_mania


import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao // to mark the interface as a Data Access Object
interface MealDao{
    /* @Insert annotation to specify that the insertAll() method is used to insert a list of Meal objects into the meals table in the database.
     OnConflictStrategy.REPLACE tells Room to replace a row if there is a conflict with a new row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(meals: List<Meal>)

    /* @Query annotation is used to declare a query method for selecting meals based on a search query.
    The method returns a Flow object that emits a list of Meal objects whenever the data in the meals table changes.*/

    @Query("SELECT * FROM meals WHERE lower(name) LIKE '%' || :query || '%' OR lower(ingredients) LIKE '%' || :query || '%'")
    fun searchMeals(query: String): Flow<List<Meal>>


    @Query("SELECT * FROM meals WHERE ingredients LIKE :ingredient")


    fun searchMealsByIngredient(ingredient: String): Flow<List<Meal>>
    // @Insert annotation is used to declare a query method for inserting a single Meal object into the meals table in the database.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(meals: List<Meal>)

}
