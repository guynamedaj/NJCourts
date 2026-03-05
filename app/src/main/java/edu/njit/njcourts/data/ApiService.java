package edu.njit.njcourts.data;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Task 14 & 31: Define REST API Contract + Retrofit REST Upload.
 */
public interface ApiService {
    @Multipart
    @POST("upload")
    Call<ResponseBody> uploadPhoto(
        @Part("ticketNumber") RequestBody ticketNumber,
        @Part("timestamp") RequestBody timestamp,
        @Part MultipartBody.Part photo
    );
}
