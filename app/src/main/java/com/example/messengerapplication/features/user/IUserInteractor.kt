import android.net.Uri
import com.example.messengerapplication.utilits.*
import com.google.firebase.storage.StorageReference

interface IUserInteractor {

    fun createUser(url: String, function: () -> Unit)

    fun updateUser(field: String, value: String)

    fun getUser(path: StorageReference, function: (url: String) -> Unit)

    fun createPicture(uri: Uri, path: StorageReference, function: () -> Unit)

    fun getPicture(path: StorageReference, function: (url: String) -> Unit)

    fun initUserContacts()

    fun changeUserName(fieldType: SettingsType, input: String)

    fun deletePreUsername()

    fun updateUserName(fieldType: SettingsType, input: String)

    fun initCurrentUser(function: () -> Unit)

    fun saveUrl(url: String, function: () -> Unit)

}