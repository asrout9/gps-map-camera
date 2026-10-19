package com.offline.maptimestampcamera.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.offline.maptimestampcamera.data.repository.PhotoRepository
import com.offline.maptimestampcamera.model.PhotoMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    val photos: StateFlow<List<PhotoMetadata>> = photoRepository.allPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPhoto = MutableStateFlow<PhotoMetadata?>(null)
    val selectedPhoto: StateFlow<PhotoMetadata?> = _selectedPhoto.asStateFlow()

    fun loadPhotoDetails(id: Long) {
        viewModelScope.launch {
            _selectedPhoto.value = photoRepository.getPhotoById(id)
        }
    }

    fun deletePhoto(id: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            photoRepository.deletePhoto(id)
            onDeleted()
        }
    }

    class Factory(
        private val photoRepository: PhotoRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GalleryViewModel(photoRepository) as T
        }
    }
}
