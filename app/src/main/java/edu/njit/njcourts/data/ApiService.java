package edu.njit.njcourts.data;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Task 14 & 31: REST API contract for the backend.
 */
public interface ApiService {
    @GET("tickets")
    Call<List<ApiTicket>> getTickets();

    @GET("tickets/{id}/evidence")
    Call<List<ApiEvidence>> getTicketEvidence(@Path("id") String ticketId);

    @Multipart
    @POST("upload")
    Call<ResponseBody> uploadPhoto(
        @Part("ticketNumber") RequestBody ticketNumber,
        @Part("timestamp") RequestBody timestamp,
        @Part MultipartBody.Part photo
    );
}
