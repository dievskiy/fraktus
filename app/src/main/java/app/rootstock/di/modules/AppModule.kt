package app.rootstock.di.modules


import android.content.Context
import app.rootstock.api.ColorService
import app.rootstock.data.channel.PatternsDelegate
import app.rootstock.data.db.AppDatabase
import app.rootstock.data.network.*
import app.rootstock.data.prefs.SharedPrefsController
import app.rootstock.data.token.TokenDao
import app.rootstock.data.token.TokenRepository
import app.rootstock.data.token.TokenRepositoryImpl
import app.rootstock.data.token.TokenService
import app.rootstock.data.user.UserDao
import app.rootstock.data.user.UserRepository
import app.rootstock.data.user.UserRepositoryImpl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import javax.inject.Singleton


@InstallIn(ApplicationComponent::class)
@Module
class AppModule {

    companion object {
        const val URL_PRIVACY_POLICY = "https://fraktus.app/privacy-policy"
        const val SERVER_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
        const val SHARED_PREFS = "Fraktus"
//        const val API_BASE_URL = "http://192.168.43.116:8000"
        const val API_BASE_URL = "https://fraktus-ccmn54qy7q-nw.a.run.app"
    }

    @Singleton
    @Provides
    fun provideColorsDelegate(retrofit: Retrofit): PatternsDelegate {
        return PatternsDelegate(retrofit.create(ColorService::class.java))
    }

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    fun provideTokenDao(appDatabase: AppDatabase): TokenDao {
        return appDatabase.tokenDao()
    }

    @Singleton
    @Provides
    fun provideUserRepository(
        userDao: UserDao
    ): UserRepository {
        return UserRepositoryImpl(userDao = userDao)
    }

    @Singleton
    @Provides
    fun provideSpController(
        @ApplicationContext context: Context
    ): SharedPrefsController {
        val sp = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        return SharedPrefsController(sp)
    }

    @Singleton
    @Provides
    fun provideRetrofit(
        client: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(gsonConverterFactory)
            .client(client)
            .build()
    }


    @Singleton
    @Provides
    fun provideReLogInObservable(): ReLogInObservable {
        return ReLogInObservableImpl()
    }

    /**
     * Provide Gson and specify date format to correctly convert date object from server's to [java.util.Date]
     */
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setDateFormat(SERVER_DATE_PATTERN).create()

    @Singleton
    @Provides
    fun provideGsonConverter(gson: Gson): GsonConverterFactory {
        return GsonConverterFactory.create(gson)
    }


    @Singleton
    @Provides
    fun provideTokenRepository(
        tokenDao: TokenDao,
        tokenService: TokenService,
        userRepository: UserRepository
    ): TokenRepository {
        return TokenRepositoryImpl(tokenDao, tokenService, userRepository)
    }

    @Provides
    fun provideTokenRemote(gsonConverterFactory: GsonConverterFactory): TokenService {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(TokenService::class.java)
    }

    @Singleton
    @Provides
    fun provideTokenInterceptor(tokenRepository: TokenRepository): TokenInterceptor {
        return TokenInterceptor(tokenRepository)
    }

    @Singleton
    @Provides
    fun provideServerAuthenticator(
        tokenRepository: TokenRepository,
        tokenInterceptor: TokenInterceptor,
        reLogInObservable: ReLogInObservable
    ): ServerAuthenticator {
        return ServerAuthenticator(tokenRepository, tokenInterceptor, reLogInObservable)
    }

    @Singleton
    @Provides
    fun provideJsonInterceptor(): JsonInterceptor {
        return JsonInterceptor()
    }

    @Provides
    fun provideCacheInterceptor(): CacheInterceptor {
        return CacheInterceptor()
    }

    @Provides
    @Singleton
    fun getCache(@ApplicationContext context: Context): Cache {
        val httpCacheDirectory = File(context.cacheDir, "http-cache")
        val cacheSize = 20 * 1024 * 1024 // 20 MiB
        return Cache(httpCacheDirectory, cacheSize.toLong())
    }

    @Provides
    @Singleton
    fun getCacheCleaner(okHttpClient: OkHttpClient): CacheCleaner {
        return CacheCleaner(okHttpClient)
    }

    @Provides
    fun getClient(
        tokenInterceptor: TokenInterceptor,
        authenticator: ServerAuthenticator,
        jsonInterceptor: JsonInterceptor,
        cacheInterceptor: CacheInterceptor,
        cache: Cache

    ): OkHttpClient {
        return OkHttpClient.Builder()
            // add JSON header interceptor
            .addNetworkInterceptor(cacheInterceptor)
            .addInterceptor(jsonInterceptor)
            .addInterceptor(tokenInterceptor)
            .authenticator(authenticator)
            .cache(cache)
            .build()

    }

}
