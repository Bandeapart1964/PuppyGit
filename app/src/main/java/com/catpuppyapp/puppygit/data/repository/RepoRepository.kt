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

import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.git.ImportRepoResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieve of [Item] from a given data source.
 */
interface RepoRepository {
    /**
     * Retrieve all the items from the the given data source.
     */
    fun getAllStream(): Flow<List<RepoEntity?>>

    /**
     * Retrieve an item from the given data source that matches with the [id].
     */
    fun getStream(id: String): Flow<RepoEntity?>

    /**
     * Insert item in the data source
     */
    suspend fun insert(item: RepoEntity)

    /**
     * Delete item from the data source
     * 注：requireTransaction: 我不确定room的事务策略是怎样，不知道若已存在事务是加入还是新建，所以用这个变量来控制，若外部调用者有事务，传false即可
     */
    suspend fun delete(item: RepoEntity, requireDelFilesOnDisk:Boolean=false, requireTransaction: Boolean=true)
    suspend fun getIdByRepoNameAndExcludeId(repoName:String, excludeId:String): String?
    suspend fun isRepoNameAlreadyUsedByOtherItem(repoName:String, excludeId:String): Boolean

    /**
     * Update item in the data source
     * @param requeryAfterUpdate 更新完db后重新查询仓库信息以获取git仓库的最新状态，会修改传入的参数！
     */
    suspend fun update(item: RepoEntity, requeryAfterUpdate:Boolean=true)

    suspend fun isRepoNameExist(repoName:String): Boolean

    suspend fun getById(id:String): RepoEntity?

    suspend fun getAll(updateRepoInfo:Boolean = true): List<RepoEntity>

    suspend fun cloneDoneUpdateRepoAndCreateRemote(item: RepoEntity)

    suspend fun getAReadyRepo():RepoEntity?
    suspend fun getReadyRepoList():List<RepoEntity>
    suspend fun updateCredentialIdByCredentialId(oldCredentialIdForClone:String, newCredentialIdForClone:String)
    suspend fun unlinkCredentialIdByCredentialId(credentialIdForClone:String)

    suspend fun updateErrFieldsById(repoId:String, hasUncheckedErr:Int, latestUncheckedErrMsg:String)
    suspend fun checkedAllErrById(repoId:String)
    suspend fun setNewErrMsg(repoId:String, errMsg:String)

    suspend fun updateBranchAndCommitHash(repoId:String, branch:String, lastCommitHash:String, isDetached:Int, upstreamBranch:String)
    suspend fun updateDetachedAndCommitHash(repoId:String, lastCommitHash:String, isDetached:Int)
    suspend fun updateCommitHash(repoId:String, lastCommitHash:String)
    suspend fun updateUpstream(repoId:String, upstreamBranch: String)
    suspend fun updateLastUpdateTime(repoId:String, lastUpdateTime:Long)
    suspend fun updateIsShallow(repoId:String, isShallow:Int)
    suspend fun getByStorageDirId(storageDirId:String): List<RepoEntity>
    suspend fun deleteByStorageDirId(storageDirId:String)
    suspend fun importRepos(dir: String, isReposParent: Boolean): ImportRepoResult

    /**
     * if reponame has illegal chars or exists in repo, return false, else return true
     */
    suspend fun isGoodRepoName(name:String):Boolean

    suspend fun updateRepoName(repoId:String, name: String)


}
