package com.example.municipalassistant.Retrofit;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface JsonPlaceHolderApi {

    @GET("posts")
    Call<List<Post>> getPosts();

    @GET("json.php?action=get_all_tickets")
    Call<List<Ticket>> getTickets();

    @GET("json.php?action=get_all_comments")
    Call<List<Komentar>> getKomentare();

    @GET("json.php?action=get_all_users")
    Call<List<Korisnik>> getKorisnike();

    @POST("json.php?action=validate_login")
    Call<ResponseBody> validateLogin(@Body Korisnik korisnik);

    @POST("json.php?action=add_ticket")
    Call<ResponseBody> createTicket(@Body TicketPost ticketPost);

    @POST("json.php?action=update_ticket")
    Call<ResponseBody> updateStatus(@Body TicketUpdate ticketUpdate);

    @POST("posts")
    Call<Post> createPost(@Body Post post);

    @FormUrlEncoded
    @POST("posts")
    Call<Post> createPost(
            @Field("userId") int userId,
            @Field("title") String title,
            @Field("body") String text
    );
}
