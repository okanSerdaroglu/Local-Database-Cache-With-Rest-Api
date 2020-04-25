package com.okanserdaroglu.foodrecipes.viewmodels;


import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.okanserdaroglu.foodrecipes.models.Recipe;
import com.okanserdaroglu.foodrecipes.repositories.RecipeRepository;
import com.okanserdaroglu.foodrecipes.util.Resource;

import java.util.List;

public class RecipeListViewModel extends AndroidViewModel {

    private static final String TAG = "RecipeListViewModel";

    public enum ViewState {CATEGORIES, RECIPES}

    private MediatorLiveData<Resource<List<Recipe>>> recipes = new MediatorLiveData<>();
    private RecipeRepository recipeRepository;
    public static final String QUERY_EXHAUSTED = "Query Exhausted";

    // query extras
    private boolean isQueryExhausted;
    private boolean isPerformingQuery;
    private int pageNumber;
    private String query;
    private boolean cancelRequest;
    private long requestStartTime;

    public LiveData<ViewState> getViewStateMutableLiveData() {
        return viewStateMutableLiveData;
    }

    private MutableLiveData<ViewState> viewStateMutableLiveData;

    public RecipeListViewModel(@NonNull Application application) {
        super(application);
        recipeRepository = RecipeRepository.getInstance(application);
        init();
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setViewCategories() {
        viewStateMutableLiveData.setValue(ViewState.CATEGORIES);
    }


    private void init() {
        if (viewStateMutableLiveData == null) {
            viewStateMutableLiveData = new MutableLiveData<>();
            viewStateMutableLiveData.setValue(ViewState.CATEGORIES);
        }
    }

    public LiveData<Resource<List<Recipe>>> getRecipes() {
        return recipes;
    }

    public void searchRecipesApi(String query, int pageNumber) {
        if (!isPerformingQuery) {
            if (pageNumber == 0) {
                pageNumber = 1;
            }
            this.pageNumber = pageNumber;
            this.query = query;
            isQueryExhausted = false;
            executeSearch();
        }
    }

    public void searchNextPage() {
        if (!isQueryExhausted && !isPerformingQuery) {
            pageNumber++;
            executeSearch();
        }
    }

    private void executeSearch() {
        requestStartTime = System.currentTimeMillis();
        cancelRequest = false;
        isPerformingQuery = true;
        viewStateMutableLiveData.setValue(ViewState.RECIPES);
        final LiveData<Resource<List<Recipe>>> repositorySource =
                recipeRepository.searchRecipesApi(query, pageNumber);
        recipes.addSource(repositorySource, new Observer<Resource<List<Recipe>>>() {
            @Override
            public void onChanged(@Nullable Resource<List<Recipe>> listResource) {
                if (!cancelRequest) {
                    if (listResource != null) {
                        recipes.setValue(listResource);
                        if (listResource.status == Resource.Status.SUCCESS) {
                            Log.d(TAG, "onChanged: Request Time: "
                                    + (System.currentTimeMillis() - requestStartTime) / 1000 + " seconds");
                            isPerformingQuery = false;
                            if (listResource.data != null) {
                                if (listResource.data.size() == 0) {
                                    Log.d(TAG, "onChanged : query is exhausted...");
                                    recipes.setValue(new
                                            Resource<List<Recipe>>(Resource.Status.ERROR,
                                            listResource.data, QUERY_EXHAUSTED));
                                }
                            }
                            recipes.removeSource(repositorySource);
                        } else if (listResource.status == Resource.Status.ERROR) {
                            isPerformingQuery = false;
                            recipes.removeSource(repositorySource);
                        }
                    } else {
                        recipes.removeSource(repositorySource);
                    }
                } else {
                    recipes.removeSource(repositorySource);
                }
            }
        });
    }

    public void cancelSearchRequest() {
        if (isPerformingQuery) {
            Log.d(TAG, "cancelSearchRequest: cancelling the search request");
            cancelRequest = true;
            isPerformingQuery = false;
            pageNumber = 1;
        }
    }

}















