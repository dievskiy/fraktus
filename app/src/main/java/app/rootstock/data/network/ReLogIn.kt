package app.rootstock.data.network

/**
 * Observer pattern for relogin
 */
interface ReLogInObservable {
    fun addObserver(o: ReLogInObserver)
    fun removeObserver(o: ReLogInObserver)
    fun notifyObservers()
}

interface ReLogInObserver {
    fun submit()
}

class ReLogInObservableImpl : ReLogInObservable {
    private val observers: MutableList<ReLogInObserver> = mutableListOf()

    override fun addObserver(o: ReLogInObserver) {
        observers.add(o)
    }

    override fun removeObserver(o: ReLogInObserver) {
        if (observers.contains(o))
            observers.remove(o)
    }

    override fun notifyObservers() {
        observers.forEach {
            it.submit()
        }
    }

}