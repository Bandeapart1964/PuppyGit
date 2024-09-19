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

import com.catpuppyapp.puppygit.data.entity.PassEncryptEntity

/**
 * Repository that provides insert, update, delete, and retrieve of [Item] from a given data source.
 */
interface PassEncryptRepository {
    /**
     * Insert item in the data source
     */
    suspend fun insert(item: PassEncryptEntity)

    /**
     * Delete item from the data source
     */
    suspend fun delete(item: PassEncryptEntity)

    /**
     * Update item in the data source
     */
    suspend fun update(item: PassEncryptEntity)


    suspend fun getById(id: Int): PassEncryptEntity?
    suspend fun getOrInsertIdOne(): PassEncryptEntity

    /**
     * 如果更新密钥，需要执行此方法把数据库里的密码全部重新用新密钥加密一次
     */
    suspend fun migrateIfNeed(credentialDb:CredentialRepository)

}
