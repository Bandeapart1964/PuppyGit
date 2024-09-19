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

package com.catpuppyapp.puppygit.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Database access object to access the Inventory database
 */
@Dao
interface RepoDao {

    //Flow不需要suspend吗？也就是说flow不会阻塞，会立即返回吗？对这玩意不懂，先不用了
    @Query("SELECT * from repo WHERE baseIsDel=0 ORDER BY baseCreateTime DESC")
    fun getAllStream(): Flow<List<RepoEntity?>>

    @Query("SELECT * from repo WHERE id = :id")
    fun getStream(id: String): Flow<RepoEntity?>

    @Insert
    suspend fun insert(item: RepoEntity)

    //update by id
    @Update
    suspend fun update(item: RepoEntity)

    //delete by id
    @Delete
    suspend fun delete(item: RepoEntity)


    @Query("SELECT id from repo where repoName=:repoName LIMIT 1")
    suspend fun getIdByRepoName(repoName:String): String?

    /**
     * 更新时用来检查仓库名是否冲突的，如果其他条目存在相同仓库名，就冲突，排除id是需要更新的仓库的id，修改仓库信息时，自己的名字可以和自己的名字一样，所以检查时需要排除自己的id
     */
    @Query("SELECT id from repo where id!=:excludeId and repoName=:repoName LIMIT 1")
    suspend fun getIdByRepoNameAndExcludeId(repoName:String, excludeId:String): String?

    @Query("SELECT * from repo where id=:id")
    suspend fun getById(id:String): RepoEntity?

    @Query("SELECT * from repo where baseIsDel=0 and storageDirId=:storageDirId ORDER BY baseCreateTime DESC")
    suspend fun getByStorageDirId(storageDirId:String): List<RepoEntity>

    @Query("delete from repo WHERE storageDirId = :storageDirId")
    suspend fun deleteByStorageDirId(storageDirId:String)

    @Query("SELECT * from repo WHERE baseIsDel=0 ORDER BY baseCreateTime DESC")
    suspend fun getAll(): List<RepoEntity>

    @Query("update repo set credentialIdForClone = :newCredentialIdForClone where credentialIdForClone = :oldCredentialIdForClone")
    suspend fun updateCredentialIdByCredentialId(oldCredentialIdForClone:String, newCredentialIdForClone:String)

    @Query("update repo set hasUncheckedErr = :hasUncheckedErr , latestUncheckedErrMsg = :latestUncheckedErrMsg where id = :repoId")
    suspend fun updateErrFieldsById(repoId:String, hasUncheckedErr:Int, latestUncheckedErrMsg:String)

    @Query("update repo set branch = :branch , lastCommitHash = :lastCommitHash , isDetached=:isDetached, upstreamBranch=:upstreamBranch where id = :repoId")
    suspend fun updateBranchAndCommitHash(repoId:String, branch:String, lastCommitHash:String, isDetached:Int, upstreamBranch:String)

    @Query("update repo set lastCommitHash = :lastCommitHash , isDetached=:isDetached where id = :repoId")
    suspend fun updateDetachedAndCommitHash(repoId:String, lastCommitHash:String, isDetached:Int)

    @Query("update repo set lastCommitHash = :lastCommitHash where id = :repoId")
    suspend fun updateCommitHash(repoId:String, lastCommitHash:String)

    @Query("update repo set upstreamBranch = :upstreamBranch where id = :repoId")
    suspend fun updateUpstream(repoId:String, upstreamBranch: String)

    @Query("update repo set lastUpdateTime = :lastUpdateTime where id = :repoId")
    suspend fun updateLastUpdateTime(repoId:String, lastUpdateTime:Long)

    @Query("update repo set isShallow = :isShallow where id = :repoId")
    suspend fun updateIsShallow(repoId:String, isShallow:Int)

}
