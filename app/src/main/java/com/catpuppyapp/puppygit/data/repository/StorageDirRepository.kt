/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.catpuppyapp.puppygit.data.repository

import com.catpuppyapp.puppygit.constants.StorageDirCons
import com.catpuppyapp.puppygit.data.entity.StorageDirEntity

/**
 * Repository that provides insert, update, delete, and retrieve of [StorageDirEntity] from a given data source.
 */
interface StorageDirRepository {
    /**
     * Insert item in the data source
     */
    suspend fun insert(item: StorageDirEntity)

    /**
     * Delete item from the data source
     */
    suspend fun delete(item: StorageDirEntity, requireDelFilesOnDisk:Boolean=false, requireTransaction: Boolean=true)

    /**
     * Update item in the data source
     */
    suspend fun update(item: StorageDirEntity)
    
    suspend fun getById(id:String): StorageDirEntity?
    suspend fun getAll(): List<StorageDirEntity>

    suspend fun getListByStatus(status:Int = StorageDirCons.Status.ok): List<StorageDirEntity>

    /**
     * 获取一个列表，可用通过路径匹配反查sd条目
     */
    suspend fun getListForMatchFullPath(): List<StorageDirEntity>
    suspend fun getByFullPath(fullPath:String): StorageDirEntity?

    suspend fun isFullPathExists(fullPath:String): Boolean
    suspend fun getByName(name:String): StorageDirEntity?
    suspend fun isNameExists(name:String): Boolean
    suspend fun getByNameOrFullPath(name:String, fullPath:String): StorageDirEntity?
    suspend fun isNameOrFullPathExists(name:String, fullPath:String): Boolean
    suspend fun getByNameOrFullPathExcludeId(name: String, fullPath: String, excludeId:String): StorageDirEntity?
    suspend fun isNameOrFullPathAlreadyUsedByOtherItem(name: String, fullPath: String, excludeId: String): Boolean
    suspend fun getAllNoDefault(): List<StorageDirEntity>
    suspend fun reGenVirtualPathForAllItemsInDb()


}
