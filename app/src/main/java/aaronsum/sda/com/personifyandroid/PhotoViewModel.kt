package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.net.Uri

class PhotoViewModel : ViewModel() {
    private val photoRepository: PhotoRepository = PhotoRepository()
    val profilePhotoMetadata: LiveData<PicMetadata> = photoRepository.profilePhotoMetadata

    fun initProfilePhotoDocument(userId: String) {
        photoRepository.initDocument(userId)
    }

    fun uploadPhoto(file: Uri) = photoRepository.uploadProfilePhoto(file)

    fun writeUserProfilePictureURL(picMetadata: PicMetadata) {
        photoRepository.writeUserProfilePictureURL(picMetadata)
    }

    fun deleteUserProfile() {
        photoRepository.deleteUserProfilePic()
        clearProfilePicWhenNoUser()
    }

    fun clearProfilePicWhenNoUser() {
        photoRepository.clearProfilePic()
    }
}