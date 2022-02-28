import android.net.Uri
import android.provider.ContactsContract
import com.example.messengerapplication.features.chat.domain.ChatInteractor
import com.example.messengerapplication.features.chat.domain.entity.CommonModel
import com.example.messengerapplication.features.user.domain.entity.User
import com.example.messengerapplication.utilits.*
import com.google.firebase.storage.StorageReference

class UserInteractor : IUserInteractor {

    private val chatInteractor: ChatInteractor = ChatInteractor()

    override fun createUser(url: String, function: () -> Unit) {
        mApplication.databaseFbRef
            .child(NODE_USERS)
            .child(mApplication.currentUserID)
            .child(CHILD_PHOTO_URL)
            .setValue(url)
            .addOnCompleteListener { function() }
            .addOnFailureListener { showToast(it.message.toString()) }
    }

    override fun updateUser(field: String, value: String) {
        mApplication.databaseFbRef
            .child(NODE_USERS)
            .child(mApplication.currentUserID)
            .child(field)
            .setValue(value)
            .addOnCompleteListener {
                if(!it.isSuccessful) {
                    showToast("Updated")
                }
            }
        if(field == CHILD_FULLNAME){
            mApplication.databaseFbRef
                .child(NODE_USERS)
                .child(mApplication.currentUserID)
                .child(CHILD_FULLNAME_LOWCASE)
                .setValue(value.lowercase())
                .addOnCompleteListener {
                    if(!it.isSuccessful) {
                        showToast("Updated")
                    }
                }
        }
    }

    override fun getUser(path: StorageReference, function: (url: String) -> Unit) {
        path.downloadUrl
            .addOnCompleteListener { function(it.result.toString()) }
            .addOnFailureListener { showToast(it.message.toString()) }
    }

    override fun createPicture(uri: Uri, path: StorageReference, function: () -> Unit) {
        path.putFile(uri)
            .addOnCompleteListener { function() }
            .addOnFailureListener { showToast(it.message.toString()) }
    }

    override fun getPicture(path: StorageReference, function: (url: String) -> Unit) {
        path.downloadUrl
            .addOnCompleteListener { function(it.result.toString()) }
            .addOnFailureListener { showToast(it.message.toString()) }
    }

    override fun initUserContacts() {
        if(checkPermission(READ_CONTACTS)){
            val contactsFromDevice = arrayListOf<CommonModel>()
            val cursor = appActivity.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null)
            contactNamesFromDevice = mutableMapOf()
            cursor?.let {
                while(it.moveToNext()){
                    val fullName = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val phone = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    val initialModel = CommonModel()
                    initialModel.fullname = fullName
                    initialModel.fullnameLowcase = fullName.lowercase()
                    initialModel.phone = phone.replace(Regex("[\\s,-]"), "")
                    contactsFromDevice.add(initialModel)

                    contactNamesFromDevice.put(initialModel.phone, fullName)
                }
            }
            cursor?.close()
            chatInteractor.updateChatContacts(contactsFromDevice)
        }
    }

    override fun changeUserName(fieldType: SettingsType, input: String) {
        mApplication.databaseFbRef.child(NODE_USERNAMES).child(input)
            .setValue(mApplication.currentUserID)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    updateUserName(fieldType, input)
                    deletePreUsername()
                }
            }
    }

    override fun deletePreUsername() {
        mApplication.databaseFbRef
            .child(NODE_USERNAMES)
            .child(mApplication.currentUser.username)
            .removeValue()
            .addOnFailureListener { it.message.toString() }
    }

    override fun updateUserName(fieldType: SettingsType, input: String) {
        updateUser(fieldType.toString().lowercase(), input)
    }


    override fun initCurrentUser(function: () -> Unit){
        mApplication.databaseFbRef
            .child(NODE_USERS)
            .child(mApplication.currentUserID)
            .addListenerForSingleValueEvent(AppValueEventListener{
                mApplication.currentUser = it.getValue(User::class.java) ?: User()
                function()
            })
    }

    override fun saveUrl(url: String, function: () -> Unit) {
        mApplication.databaseFbRef
            .child(NODE_USERS)
            .child(mApplication.currentUserID)
            .child(CHILD_PHOTO_URL)
            .setValue(url)
            .addOnCompleteListener { function() }
            .addOnFailureListener { showToast(it.message.toString()) }
    }
}
