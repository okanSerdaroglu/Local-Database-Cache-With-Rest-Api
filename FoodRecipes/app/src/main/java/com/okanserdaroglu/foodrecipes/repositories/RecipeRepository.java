package com.okanserdaroglu.foodrecipes.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.okanserdaroglu.foodrecipes.AppExecutors;
import com.okanserdaroglu.foodrecipes.models.Recipe;
import com.okanserdaroglu.foodrecipes.persistance.RecipeDao;
import com.okanserdaroglu.foodrecipes.persistance.RecipeDatabase;
import com.okanserdaroglu.foodrecipes.requests.ServiceGenerator;
import com.okanserdaroglu.foodrecipes.requests.responses.ApiResponse;
import com.okanserdaroglu.foodrecipes.requests.responses.RecipeSearchResponse;
import com.okanserdaroglu.foodrecipes.util.NetworkBoundResource;
import com.okanserdaroglu.foodrecipes.util.Resource;

import java.util.List;

public class RecipeRepository {

    private static RecipeRepository instance;

    private RecipeDao recipeDao;
    private static final String TAG = "RecipeRepository";

    public static RecipeRepository getInstance(Context context) {
        if (instance == null) {
            instance = new RecipeRepository(context);
        }
        return instance;
    }

    private RecipeRepository(Context context) {
        recipeDao = RecipeDatabase.getInstance(context).getRecipeDao();
    }

    public LiveData<Resource<List<Recipe>>> searchRecipesApi
            (final String query, final int pageNumber) {
        return new NetworkBoundResource<List<Recipe>,
                RecipeSearchResponse>(AppExecutors.getInstance()) {

            @Override
            protected void saveCallResult(@NonNull RecipeSearchResponse item) {
                if (item.getRecipes() != null) {
                    Recipe[] recipes = new Recipe[item.getRecipes().size()];
                    int index = 0;
                    for (long rowId : recipeDao.insertRecipes((item.getRecipes().toArray(recipes)))) {
                        if (rowId == -1) {
                            Log.d(TAG, "saveCallResult: Conflict... This recipe is already in cache");
                            // if the recipe already exists ...
                            // I don't want to send ingredients or timeStamp b/c
                            // they will be erased
                            recipeDao.updateRecipe(recipes[index].getRecipe_id(),
                                    recipes[index].getTitle(),
                                    recipes[index].getPublisher(),
                                    recipes[index].getImage_url(),
                                    recipes[index].getSocial_rank());
                        }
                        index++;
                    }
                }

            }

            @Override
            protected boolean shouldFetch(@Nullable List<Recipe> data) {
                return true;
            }

            @NonNull
            @Override
            protected LiveData<List<Recipe>> loadFromDb() {
                return recipeDao.searchRecipes(query, pageNumber);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<RecipeSearchResponse>> createCall() {
                return ServiceGenerator.getRecipeApi().
                        searchRecipe(query, String.valueOf(pageNumber));
            }
        }.getAsLiveData();
    }

}
