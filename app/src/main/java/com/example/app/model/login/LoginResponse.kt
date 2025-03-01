import com.example.app.model.User
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: User,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)