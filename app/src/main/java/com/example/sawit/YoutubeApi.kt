import com.example.sawit.YoutubeResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("playlistItems")
    suspend fun getPlaylistVideos(
        @Query("part") part: String = "snippet",
        @Query("maxResults") max: Int = 10,
        @Query("playlistId") playlistId: String,
        @Query("key") apiKey: String
    ): YoutubeResponse
}

object RetrofitClient {
    val instance: YouTubeApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YouTubeApiService::class.java)
    }
}
