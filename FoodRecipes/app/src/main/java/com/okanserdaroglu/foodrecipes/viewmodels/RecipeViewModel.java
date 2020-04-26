package com.okanserdaroglu.foodrecipes.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.okanserdaroglu.foodrecipes.models.Recipe;
import com.okanserdaroglu.foodrecipes.repositories.RecipeRepository;
import com.okanserdaroglu.foodrecipes.util.Resource;

public class RecipeViewModel extends AndroidViewModel {

    /** difference between androidViewModel and ViewModel is
     *  when you use androidViewModel you get an application instance
     *  in AndroidViewModel constructor. In ViewModel constructor there is
     *  no default application instance
     * @param application
     */

    private RecipeRepository recipeRepository;

    public RecipeViewModel(@NonNull Application application) {
        super(application);
        recipeRepository = RecipeRepository.getInstance(application);
    }

    public LiveData<Resource<Recipe>> searchRecipeApi (String recipeID){
        return recipeRepository.searchRecipeAPI(recipeID);
    }

}





















