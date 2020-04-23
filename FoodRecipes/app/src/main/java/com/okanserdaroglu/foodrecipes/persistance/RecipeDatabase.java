package com.okanserdaroglu.foodrecipes.persistance;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.okanserdaroglu.foodrecipes.models.Recipe;

@Database(entities ={Recipe.class},version = 1)
public abstract class RecipeDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "recipes_db";

    private static RecipeDatabase instance;

    public static RecipeDatabase getInstance(final Context context) {
        if (instance == null){
            instance = Room.databaseBuilder
                    (context.getApplicationContext(),
                            RecipeDatabase.class,DATABASE_NAME).build();
        }
        return instance;
    }

}
