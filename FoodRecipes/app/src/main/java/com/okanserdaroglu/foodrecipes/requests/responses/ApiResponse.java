package com.okanserdaroglu.foodrecipes.requests.responses;

import java.io.IOException;

import retrofit2.Response;

import static com.okanserdaroglu.foodrecipes.viewmodels.RecipeListViewModel.QUERY_EXHAUSTED;

public class ApiResponse<T> {

    public ApiResponse<T> create(Throwable error){
        return new ApiErrorResponse<>
                (!error.getMessage().equals("") ?  error.getMessage()
                        : "Unknown error \n check network connection");
    }

    public ApiResponse<T> create (Response<T>response){
        if (response.isSuccessful()){
            T body = response.body();
            if (body instanceof RecipeSearchResponse){
                if (((RecipeSearchResponse) body).getCount() == 0){
                    return new ApiErrorResponse<>(QUERY_EXHAUSTED);
                }
            }
            if (body == null || response.code() == 204){ // 204 means empty response code
                return new ApiEmptyResponse<>();
            }else {
                return new ApiSuccessResponse<>(body);
            }
        } else {
            String errorMsg = "";
            try {
                errorMsg = response.errorBody().string();
            }catch (IOException e){
                e.printStackTrace();
                errorMsg = response.message();
            }
            return new ApiErrorResponse<>(errorMsg);
        }
    }

    public class ApiSuccessResponse<T> extends ApiResponse<T>{ // api success
        private T body;

        ApiSuccessResponse(T body){
            this.body = body;
        }

        public T getBody() {
            return body;
        }
    }

    public class ApiErrorResponse<T>extends ApiResponse<T>{ // api error

        private String errorMessage;

        ApiErrorResponse(String errorMessage){
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

    }

    public class ApiEmptyResponse<T> extends ApiResponse<T>{ // api return empty

    }

}
