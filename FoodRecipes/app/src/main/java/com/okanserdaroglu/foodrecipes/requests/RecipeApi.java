package com.okanserdaroglu.foodrecipes.requests;

import android.arch.lifecycle.LiveData;

import com.okanserdaroglu.foodrecipes.requests.responses.ApiResponse;
import com.okanserdaroglu.foodrecipes.requests.responses.RecipeResponse;
import com.okanserdaroglu.foodrecipes.requests.responses.RecipeSearchResponse;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RecipeApi {

    // SEARCH
    @GET("api/search")
    LiveData<ApiResponse<RecipeSearchResponse>> searchRecipe(
            @Query("q") String query,
            @Query("page") String page
    );

    // GET RECIPE REQUEST
    @GET("api/get")
    LiveData<ApiResponse<RecipeResponse>> getRecipe(
            @Query("key") String key,
            @Query("rId") String recipe_id
    );
}
