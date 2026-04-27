package edu.njit.njcourts.data;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {
    private static volatile ApiService INSTANCE;

    public static ApiService get() {
        if (INSTANCE == null) {
            synchronized (RetrofitClient.class) {
                if (INSTANCE == null) {
                    OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .addInterceptor(chain -> chain.proceed(
                            chain.request().newBuilder()
                                .addHeader("X-API-Key", ApiConstants.API_KEY)
                                .build()
                        ))
                        .build();

                    Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(ApiConstants.BASE_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                    INSTANCE = retrofit.create(ApiService.class);
                }
            }
        }
        return INSTANCE;
    }

    private RetrofitClient() {}
}
