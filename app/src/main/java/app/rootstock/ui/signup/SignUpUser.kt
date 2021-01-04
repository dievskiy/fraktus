package app.rootstock.ui.signup

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import app.rootstock.BR
import app.rootstock.data.user.UserWithPassword
import java.io.Serializable


class SignUpUser :
    BaseObservable(), Serializable, UserWithPassword {

    override fun toString(): String {
        return "User: ..."
    }

    companion object {
        fun build(): SignUpUser {
            return SignUpUser()
        }

        // only ascii chars with no spaces or tabs
        private val passwordRegex =
            """^([a-zA-Z0-9!@#$%^&*()_+§}|?~`=\\/<>,.\-\'\"]{6,32})$""".toRegex()

        // standard email regex
        private val emailRegex =
            """(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])""".toRegex()
    }

    @Bindable
    var emailValid: Boolean = false
        get() = field

    @Bindable
    var passwordValid: Boolean = false
        get() = field

    @Bindable
    var passwordRepeatValid: Boolean = false
        get() = field


    @Bindable
    var allValid: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.allValid)
        }
        get() = isDataValid()


    @Bindable
    override var email: String = String()
        set(value) {
            field = value
            checkEmail()
            notifyPropertyChanged(BR.email)
            notifyPropertyChanged(BR.allValid)
        }
        get() = field

    @Bindable
    override var password: String = String()
        set(value) {
            field = value
            checkPassword()
            notifyPropertyChanged(BR.password)
            notifyPropertyChanged(BR.allValid)
        }
        get() = field

    @Bindable
    var passwordRepeat: String = String()
        set(value) {
            field = value
            checkPasswordRepeat()
            notifyPropertyChanged(BR.passwordRepeat)
            notifyPropertyChanged(BR.allValid)
        }
        get() = field

    private fun checkEmail() {
        emailValid = isEmailValid()
        notifyPropertyChanged(BR.emailValid)
    }

    private fun checkPassword() {
        passwordValid = isPasswordValid()
        notifyPropertyChanged(BR.passwordValid)
        if (password.length > 5) {
            checkPasswordRepeat()
        }
    }

    private fun checkPasswordRepeat() {
        passwordRepeatValid = isPasswordRepeatValid()
        notifyPropertyChanged(BR.passwordRepeatValid)
    }

    private fun isEmailValid(): Boolean =
        email.matches(emailRegex)

    private fun isPasswordValid(): Boolean =
        password.matches(passwordRegex)

    private fun isPasswordRepeatValid(): Boolean =
        password == passwordRepeat

    private fun isDataValid() = isEmailValid() && isPasswordValid() && isPasswordRepeatValid()
}