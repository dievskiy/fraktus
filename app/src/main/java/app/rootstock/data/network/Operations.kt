package app.rootstock.data.network


sealed class CreateOperation<T> {
    class Success<T>(val obj: T) : CreateOperation<T>()
    class Error<T>() : CreateOperation<T?>()
}