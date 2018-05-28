package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.net.Uri

class PhotoViewModel : ViewModel() {
    private val photoRepository: PhotoRepository = PhotoRepository()
    val profilePhotoUrl: LiveData<String> = photoRepository.profilePhoto

    fun uploadPhoto(file: Uri, userId: String) = photoRepository.uploadProfilePhoto(file, userId)

    fun writeUserProfilePictureURL(url: Uri, userId: String) {
        photoRepository.writeUserProfilePictureURL(url, userId)
    }

    fun loadUserProfilePic(userId: String) {
        photoRepository.loadUserProfile(userId)
    }

    fun deleteUserProfile(userId: String) {
        photoRepository.deleteUserProfilePic(userId)
    }

    fun clearProfilePicAfterUserSession(userId: String) {
        photoRepository.clearProfilePic(userId)
    }
}