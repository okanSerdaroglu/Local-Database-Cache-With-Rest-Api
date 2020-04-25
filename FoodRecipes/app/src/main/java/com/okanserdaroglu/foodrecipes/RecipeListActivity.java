package com.okanserdaroglu.foodrecipes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.okanserdaroglu.foodrecipes.adapters.OnRecipeListener;
import com.okanserdaroglu.foodrecipes.adapters.RecipeRecyclerAdapter;
import com.okanserdaroglu.foodrecipes.models.Recipe;
import com.okanserdaroglu.foodrecipes.util.Resource;
import com.okanserdaroglu.foodrecipes.util.VerticalSpacingItemDecorator;
import com.okanserdaroglu.foodrecipes.viewmodels.RecipeListViewModel;

import java.util.List;

import static com.okanserdaroglu.foodrecipes.viewmodels.RecipeListViewModel.QUERY_EXHAUSTED;


public class RecipeListActivity extends BaseActivity implements OnRecipeListener {

    private static final String TAG = "RecipeListActivity";

    private RecipeListViewModel mRecipeListViewModel;
    private RecyclerView mRecyclerView;
    private RecipeRecyclerAdapter mAdapter;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        mRecyclerView = findViewById(R.id.recipe_list);
        mSearchView = findViewById(R.id.search_view);

        mRecipeListViewModel = ViewModelProviders.of(this).get(RecipeListViewModel.class);

        subscribeObservers();
        initRecyclerView();
        initSearchView();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        Resource<String> resource =
                new Resource<>(Resource.Status.ERROR, "type", "some message");
    }

    private void displaySearchCategories() {
        mAdapter.displaySearchCategories();
    }

    private void subscribeObservers() {
        mRecipeListViewModel.getRecipes().observe(this, new Observer<Resource<List<Recipe>>>() {
            @Override
            public void onChanged(@Nullable Resource<List<Recipe>> listResource) {
                if (listResource != null) {
                    Log.d(TAG, "onChanged: status: " + listResource.status);
                    if (listResource.data != null) {
                        switch (listResource.status) {
                            case LOADING: {
                                if (mRecipeListViewModel.getPageNumber() > 1) {
                                    mAdapter.displayLoading();
                                } else {
                                    mAdapter.displayOnlyLoading();
                                }
                                break;
                            }
                            case ERROR: {
                                Log.e(TAG, "onChanged :  cannot refresh the cache");
                                Log.e(TAG, "onChanged : Error message : " + listResource.message);
                                Log.e(TAG, "onChanged : status: Error, #recipes" + listResource.data.size());
                                mAdapter.hideLoading();
                                mAdapter.setRecipes(listResource.data);
                                Toast.makeText(RecipeListActivity.this, listResource.message, Toast.LENGTH_LONG).show();

                                if (listResource.message.equals(QUERY_EXHAUSTED)) {
                                    mAdapter.setQueryExhausted();
                                }
                                break;
                            }
                            case SUCCESS: {
                                Log.e(TAG, "onChanged : cache has been refreshed");
                                Log.e(TAG, "onChanged : status: Success, #Recipes " + listResource.data.size());
                                mAdapter.setRecipes(listResource.data);
                                break;

                            }
                        }
                    }
                }
            }
        });
        mRecipeListViewModel.getViewStateMutableLiveData().observe(this, new Observer<RecipeListViewModel.ViewState>() {
            @Override
            public void onChanged(@Nullable RecipeListViewModel.ViewState viewState) {
                if (viewState != null) {
                    switch (viewState) {
                        case RECIPES: {
                            break;
                        }
                        case CATEGORIES: {
                            displaySearchCategories();
                            break;
                        }
                    }
                }
            }
        });
    }


    private RequestManager initGlide() {
        RequestOptions options =
                new RequestOptions().
                        placeholder(R.drawable.white_background).error(R.drawable.white_background);
        return Glide.with(this).setDefaultRequestOptions(options);

    }

    private void searchRecipesAPI(String query) {
        mRecyclerView.smoothScrollToPosition(0);
        mRecipeListViewModel.searchRecipesApi(query, 1);
        mSearchView.clearFocus();
    }

    private void initRecyclerView() {
        mAdapter = new RecipeRecyclerAdapter(this, initGlide());
        VerticalSpacingItemDecorator itemDecorator = new VerticalSpacingItemDecorator(30);
        mRecyclerView.addItemDecoration(itemDecorator);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (mRecyclerView.canScrollVertically(1)
                        && mRecipeListViewModel.getViewStateMutableLiveData().getValue()
                        == RecipeListViewModel.ViewState.RECIPES) {
                    mRecipeListViewModel.searchNextPage();
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initSearchView() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchRecipesAPI(s);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    public void onRecipeClick(int position) {
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra("recipe", mAdapter.getSelectedRecipe(position));
        startActivity(intent);
    }

    @Override
    public void onCategoryClick(String category) {
        searchRecipesAPI(category);
    }

    @Override
    public void onBackPressed() {
        if (mRecipeListViewModel.getViewStateMutableLiveData().getValue()
                == RecipeListViewModel.ViewState.CATEGORIES) {
            super.onBackPressed();
        } else {
            mRecipeListViewModel.cancelSearchRequest();
            mRecipeListViewModel.setViewCategories();
        }
    }
}

















