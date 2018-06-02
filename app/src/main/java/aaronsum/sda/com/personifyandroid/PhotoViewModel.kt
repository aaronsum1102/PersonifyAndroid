package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.net.Uri

class PhotoViewModel : ViewModel() {
    private val photoRepository: PhotoRepository = PhotoRepository()
    val profilePhotoUrl: LiveData<String> = photoRepository.profilePhoto

    fun initProfilePhotoDocument(userId: String) {
        photoRepository.initDocument(userId)
    }

    fun uploadPhoto(file: Uri) = photoRepository.uploadProfilePhoto(file)

    fun writeUserProfilePictureURL(url: Uri) {
        photoRepository.writeUserProfilePictureURL(url)
    }

    fun deleteUserProfile() {
        photoRepository.deleteUserProfilePic()
    }

    fun clearProfilePicAfterUserSession() {
        photoRepository.clearProfilePic()
    }
}