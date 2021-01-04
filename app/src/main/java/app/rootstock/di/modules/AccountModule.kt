package app.rootstock.di.modules

import app.rootstock.api.UserDeleteService
import app.rootstock.api.UserServices
import app.rootstock.api.UserSignUpService
import app.rootstock.data.network.JsonInterceptor
import app.rootstock.ui.signup.AccountRepository
import app.rootstock.ui.signup.AccountRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named

/**
 * Module for AccountActivity
 */
@InstallIn(ActivityComponent::class)
@Module
object AccountModule {

    @ActivityScoped
    @Provides
    fun provideAccountRepository(
        signUpLoader: UserSignUpService,
        logInLoader: UserServices,
        userInfoService: UserServices,
        userDelete: UserDeleteService
    ): AccountRepository {
        return AccountRepositoryImpl(signUpLoader, logInLoader, userInfoService, userDelete)
    }


    @Provides
    @Named("okhttp_client_account")
    fun getClient(
        jsonInterceptor: JsonInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(jsonInterceptor)
            .build()

    @Provides
    @Named("retrofit_account")
    fun provideRetrofit(
        @Named("okhttp_client_account") client: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppModule.API_BASE_URL)
            .addConverterFactory(gsonConverterFactory)
            .client(client)
            .build()
    }

    @Provides
    fun provideUserSignUp(@Named("retrofit_account") retrofit: Retrofit): UserSignUpService {
        return retrofit.create(UserSignUpService::class.java)
    }

    @Provides
    fun provideUserLogInService(@Named("retrofit_account") retrofit: Retrofit): UserServices {
        return retrofit.create(UserServices::class.java)
    }

    @Provides
    fun provideUserUserDeleteService(retrofit: Retrofit): UserDeleteService {
        return retrofit.create(UserDeleteService::class.java)
    }


}