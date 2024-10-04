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

import com.catpuppyapp.puppygit.data.entity.CredentialEntity

/**
 * Repository that provides insert, update, delete, and retrieve of [Item] from a given data source.
 */
interface CredentialRepository {
    /**
     * Retrieve all the items from the the given data source.
     */
//    fun getAllStream(): Flow<List<CredentialEntity?>>
    /**
     * Retrieve an item from the given data source that matches with the [id].
     */
//    fun getStream(id: String): Flow<CredentialEntity?>


    suspend fun getAllWithDecrypt(includeNone:Boolean = false, includeMatchByDomain:Boolean = false): List<CredentialEntity>

    /**
     * 不加密也不解密 密码字段，把查出的数据简单返回
     */
    suspend fun getAll(includeNone:Boolean = false, includeMatchByDomain:Boolean = false): List<CredentialEntity>


    /**
     * Insert item in the data source
     */
    suspend fun insertWithEncrypt(item: CredentialEntity)
    /**
     * 不加密也不解密 密码字段，把传入的数据简单插入数据库
     */
    suspend fun insert(item: CredentialEntity)

    /**
     * Delete item from the data source
     */
    suspend fun delete(item: CredentialEntity)

    /**
     * Update item in the data source
     */
    suspend fun updateWithEncrypt(item: CredentialEntity)
    /**
     * 不加密也不解密 密码字段，把传入的数据简单更新到数据库
     */
    suspend fun update(item: CredentialEntity)

    suspend fun isCredentialNameExist(name: String): Boolean

    suspend fun getByIdWithDecrypt(id: String): CredentialEntity?

    /**
     * if id is `SpecialCredential.MatchByDomain.credentialId` will try match credential by url's domain
     *  if you are sure id is not match by domain id, just simple passing empty str as url
     */
    suspend fun getByIdWithDecryptAndMatchByDomain(id: String, url:String): CredentialEntity?

    /**
     * 不加密也不解密 密码字段，把查出的数据简单返回
     */
    suspend fun getById(id: String): CredentialEntity?

    //20241003 disabled, because only support https, get by type is nonsense for now
//    suspend fun getListByType(type:Int): List<CredentialEntity>

    suspend fun getSshList(): List<CredentialEntity>
    suspend fun getHttpList(includeNone:Boolean = false, includeMatchByDomain:Boolean = false): List<CredentialEntity>

    suspend fun deleteAndUnlink(item:CredentialEntity)

    fun encryptPassIfNeed(item:CredentialEntity?)
    fun decryptPassIfNeed(item:CredentialEntity?)
}
