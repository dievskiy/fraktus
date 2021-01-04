package app.rootstock.exceptions

class NoUserException : Exception(){
    override val message: String?
        get() = "No user exists"
}