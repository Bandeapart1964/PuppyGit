package com.catpuppyapp.puppygit.settings

import com.catpuppyapp.puppygit.constants.StorageDirCons
import kotlinx.serialization.Serializable

@Serializable
data class StorageDir (
    var defaultStorageDirId:String = StorageDirCons.DefaultStorageDir.puppyGitRepos.id
)
