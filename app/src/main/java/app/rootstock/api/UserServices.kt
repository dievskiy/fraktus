package app.rootstock.api

import app.rootstock.data.token.Token
import app.rootstock.data.user.DeleteUser
import app.rootstock.data.user.User
import app.rootstock.ui.signup.SignUpUser
import retrofit2.Response
import retrofit2.http.*

interface UserSignUpService {
    @POST("/users/create")
    suspend fun createUser(@Body userSignUp: SignUpUser): Response<User>
}

interface UserServices {
    @POST("/authenticate")
    @FormUrlEncoded
    suspend fun logIn(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<Token>

    @GET("/users/me")
    suspend fun getUser(@Header("Authorization") token: String): Response<User>
}


interface UserDeleteService {
    @POST("/users/delete")
    suspend fun delete(
        @Body userDelete: DeleteUser
    ): Response<Void>
}