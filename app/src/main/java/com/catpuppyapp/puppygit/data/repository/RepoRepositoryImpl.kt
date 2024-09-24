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

import androidx.room.withTransaction
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.dao.RepoDao
import com.catpuppyapp.puppygit.data.entity.RemoteEntity
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.git.ImportRepoResult
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.dbIntToBool
import com.catpuppyapp.puppygit.utils.getNowInSecFormatted
import com.catpuppyapp.puppygit.utils.getSecFromTime
import com.catpuppyapp.puppygit.utils.getShortUUID
import com.catpuppyapp.puppygit.utils.isRepoReadyAndPathExist
import com.catpuppyapp.puppygit.utils.strHasIllegalChars
import com.github.git24j.core.Repository
import kotlinx.coroutines.flow.Flow
import java.io.File
private val TAG = "RepoRepositoryImpl"

class RepoRepositoryImpl(private val dao: RepoDao) : RepoRepository {
    @Deprecated("不用这个")
    override fun getAllStream(): Flow<List<RepoEntity?>> = dao.getAllStream()

    @Deprecated("不用这个")
    override fun getStream(id: String): Flow<RepoEntity?> = dao.getStream(id)

    override suspend fun insert(item: RepoEntity) {
        val funName = "insert"
        if(isRepoNameExist(item.repoName)) {
            MyLog.w(TAG, "#$funName: warn: item's repoName '${item.repoName}' already exists! operation abort...")

            throw RuntimeException("#$funName err: repoName already exists")

        }

        item.createErrMsg = addTimeStampIfErrMsgIsNotBlank(item.createErrMsg)
        item.latestUncheckedErrMsg = addTimeStampIfErrMsgIsNotBlank(item.latestUncheckedErrMsg)

        val timeNowInSec = getSecFromTime()
        item.baseFields.baseCreateTime = timeNowInSec
        item.baseFields.baseUpdateTime = timeNowInSec

        //清掉临时状态，这个不用往数据库存
        val tmpStatus = item.tmpStatus
        item.tmpStatus=""
        dao.insert(item)
        item.tmpStatus = tmpStatus
    }

    /**
     * 如果err msg不为空，添加时间戳，否则返回原字符串
     */
    private fun addTimeStampIfErrMsgIsNotBlank(errMsg:String):String {
        val timestampSuffix = ",timestamp)"
        // errmsg不为空且不以时间戳后缀结尾则追加时间戳和后缀，否则返回原字符串。注意：不要返回空字符串，因为有可能传入带时间戳的errMsg，这时传空字符串就不对了
        return if(errMsg.isBlank() || errMsg.endsWith(timestampSuffix)) errMsg else "$errMsg (${getNowInSecFormatted()}$timestampSuffix"
    }

    override suspend fun delete(item: RepoEntity, requireDelFilesOnDisk:Boolean, requireTransaction: Boolean) {
        MyLog.d(TAG, "will delete repo, repoId=${item.id}, repoFullPath=${item.fullSavePath}")

        val repoFullPath = item.fullSavePath  //其实不用在这保存，但我有点担心下面的函数太傻逼会修改item，所以先存上路径

        //删除数据库记录
        val errDb = AppModel.singleInstanceHolder.dbContainer.errorRepository
        val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
        val act = suspend {
            //其实NotReady的仓库应该没remote和error记录，不过这里不做检测，反正就算执行了也不会误删
            errDb.deleteByRepoId(item.id)  //删除仓库所有错误记录
            remoteDb.deleteByRepoId(item.id)  //删除仓库所有remote
            dao.delete(item)  //删除仓库本身

        }
        if(requireTransaction) {
            AppModel.singleInstanceHolder.dbContainer.db.withTransaction {
                act()
    //            throw RuntimeException("测试抛异常是否会“回滚”以及“不执行异常后位于本代码块外的代码”，结论：1会回滚，2不会执行异常后的代码无论是在本代码块内还是外。两者都符合直觉，很好。")
            }
        }else {
            act()
        }

//        println("抛异常我就不会被执行到了！！")
        //最后，删除仓库文件夹
        if(requireDelFilesOnDisk) {
            val file = File(repoFullPath)
            file.deleteRecursively()
        }

        //log
        MyLog.d(TAG, "success delete repo, repoId=${item.id}, repoFullPath=${repoFullPath}")
    }

    override suspend fun update(item: RepoEntity, requeryAfterUpdate:Boolean) {
        val funName ="update"
        if(isRepoNameAlreadyUsedByOtherItem(item.repoName, item.id)) {
            MyLog.w(TAG, "#$funName: warn: item's repoName '${item.repoName}' already used by other item! operation abort...")
            throw RuntimeException("#$funName err: repoName already exists")

        }

        //粗略判断，如果错误信息不为空且没追加时间戳（以英文半角括号结尾）则追加时间戳
        item.createErrMsg = addTimeStampIfErrMsgIsNotBlank(item.createErrMsg)
        item.latestUncheckedErrMsg = addTimeStampIfErrMsgIsNotBlank(item.latestUncheckedErrMsg)

        item.baseFields.baseUpdateTime = getSecFromTime()

        val tmpStatus = item.tmpStatus
        item.tmpStatus=""
        dao.update(item)
        item.tmpStatus = tmpStatus

        //重新查询仓库信息
        if(requeryAfterUpdate){
            Libgit2Helper.updateRepoInfo(item)
        }

    }

    override suspend fun isRepoNameExist(repoName: String): Boolean {
        return dao.getIdByRepoName(repoName) != null
    }

    override suspend fun getById(id: String): RepoEntity? {
        val repoFromDb = dao.getById(id)?:return null
        Libgit2Helper.updateRepoInfo(repoFromDb)
        return repoFromDb
    }

    override suspend fun getAll(updateRepoInfo:Boolean): List<RepoEntity> {
        val list = dao.getAll()

        if(updateRepoInfo) {
            list.forEach {
                Libgit2Helper.updateRepoInfo(it)
            }
        }

        return list
    }

    override suspend fun cloneDoneUpdateRepoAndCreateRemote(item: RepoEntity) {
        val remoteRepository = AppModel.singleInstanceHolder.dbContainer.remoteRepository
        val db = AppModel.singleInstanceHolder.dbContainer.db

        val remoteForSave = RemoteEntity(
                                        remoteName = item.pullRemoteName,
                                        remoteUrl = item.pullRemoteUrl,
                                        isForPull = Cons.dbCommonTrue,
                                        isForPush = Cons.dbCommonTrue,
                                        credentialId = item.credentialIdForClone,
                                        pushCredentialId = item.credentialIdForClone,
                                        repoId = item.id,
                            )

        //判断是否是singleBranch模式
        if(dbIntToBool(item.isSingleBranch)) {
            remoteForSave.fetchMode=Cons.dbRemote_Fetch_BranchMode_SingleBranch
            remoteForSave.singleBranch=item.branch
        }

        //在事务中新增remote和更新repo
        db.withTransaction {
            //疑惑：这里是否有必要删除一下remote表中repoId为当前item.id的记录？正常来说是不需要删的，
            // 因为克隆只会成功一次，这个方法也只会被调用一次，所以应该不会重复创建相同remoteName和repoId的Remote，
            // 但删一下的话，容错性似乎更好？尤其是在测试的时候，可能直接改数据库，导致这个方法被调用多次，
            // 插入多个重名的remote，删一下似乎更好
            // 但正常来说应该是不需要删的
            remoteRepository.deleteByRepoId(item.id)  //这个其实正常来说不需要调用，但也有可能发生并发问题，导致错误调用这个代码块两次，那样remote表的记录会有错误，所以删一下保险，正常来说刚克隆完的仓库本来就是要有且只有一个remote origin的

            remoteRepository.insert(remoteForSave)
            update(item)
        }
    }

    override suspend fun getAReadyRepo(): RepoEntity? {
        val repos = getAll()
        if(repos.isEmpty()) {
            return null;
        }
        for(r in repos) {
            if (isRepoReadyAndPathExist(r)){
                Libgit2Helper.updateRepoInfo(r)
                return r
            }
        }
        return null;
    }

    override suspend fun getReadyRepoList(): List<RepoEntity> {
        val repoList = mutableListOf<RepoEntity>()

        val repos = getAll()

        for(r in repos) {
            if (isRepoReadyAndPathExist(r)){
                Libgit2Helper.updateRepoInfo(r)
                repoList.add(r)
            }
        }

        return repoList;
    }

    override suspend fun updateCredentialIdByCredentialId(
        oldCredentialIdForClone: String,
        newCredentialIdForClone: String
    ) {
        dao.updateCredentialIdByCredentialId(oldCredentialIdForClone,newCredentialIdForClone)
    }

    override suspend fun unlinkCredentialIdByCredentialId(credentialIdForClone: String) {
        updateCredentialIdByCredentialId(credentialIdForClone, "")
    }

    override suspend fun updateErrFieldsById(
        repoId: String,
        hasUncheckedErr: Int,
        latestUncheckedErrMsg: String
    ) {
        dao.updateErrFieldsById(
            repoId,
            hasUncheckedErr,  //这个字段其实有点多余，err字段不为空就等于有错误否则就当作没有不就行了？
            // "errorMsg (错误记录创建时间戳,时间戳为了方便检查有无加时间戳而加的后缀)"
            addTimeStampIfErrMsgIsNotBlank(latestUncheckedErrMsg)
        )
    }

    override suspend fun checkedAllErrById(repoId: String) {
        //更新repo表
        //设置hasUncheckedErr为假，最新未检消息为空字符串
        updateErrFieldsById(repoId, Cons.dbCommonFalse, "")

        //更新error表
        val errDb = AppModel.singleInstanceHolder.dbContainer.errorRepository
        //更新error表所有匹配repoId的条目的isChecked状态为true
        errDb.updateIsCheckedByRepoId(repoId, Cons.dbCommonTrue)
    }

    override suspend fun setNewErrMsg(repoId: String, errMsg: String) {
        updateErrFieldsById(repoId, Cons.dbCommonTrue, errMsg)
    }

    override suspend fun updateBranchAndCommitHash(
        repoId: String,
        branch: String,
        lastCommitHash: String,
        isDetached:Int,
        upstreamBranch:String
    ) {
        dao.updateBranchAndCommitHash(repoId, branch, lastCommitHash, isDetached, upstreamBranch)
    }

    //checkout commit和tag用的
    override suspend fun updateDetachedAndCommitHash(
        repoId: String,
        lastCommitHash: String,
        isDetached: Int
    ) {
        dao.updateDetachedAndCommitHash(repoId, lastCommitHash, isDetached)
    }

    override suspend fun updateCommitHash(repoId: String, lastCommitHash: String) {
        dao.updateCommitHash(repoId,lastCommitHash)
    }

    override suspend fun updateUpstream(repoId:String, upstreamBranch: String) {
        dao.updateUpstream(repoId, upstreamBranch)
    }

    override suspend fun updateLastUpdateTime(repoId: String, lastUpdateTime: Long) {
        // 更新最后和服务器同步数据的时间，只更新时间就行，workStatus状态会在查询repo时更新
        dao.updateLastUpdateTime(repoId,lastUpdateTime)
    }

    override suspend fun updateIsShallow(repoId: String, isShallow: Int) {
        dao.updateIsShallow(repoId, isShallow)
    }

    override suspend fun getByStorageDirId(storageDirId: String): List<RepoEntity> {
        return dao.getByStorageDirId(storageDirId)
    }

    override suspend fun deleteByStorageDirId(storageDirId: String) {
        dao.deleteByStorageDirId(storageDirId)
    }

    override suspend fun importRepos(dir: String, isReposParent: Boolean): ImportRepoResult {
        val repos = getAll(updateRepoInfo = false).toMutableList()

        var all = 0
        var success=0
        var existed = 0
        var failed = 0


        if(isReposParent) {
            val subdirs = File(dir).listFiles { it -> it.isDirectory }
            //has sub directories, try scan folder
            if(subdirs!=null && subdirs.isNotEmpty()) {
                subdirs.forEach { sub ->
                    val repoExisted = repos.indexOfFirst { it.fullSavePath ==  sub.canonicalPath} != -1
                    if(repoExisted) {
                        all++
                        existed++
                    }else {
                        val isGitRepo = Libgit2Helper.isGitRepo(sub)

                        if(isGitRepo) {
                            all++
                            val importSuccess = importSingleRepo(sub, addRepoToThisListIfSuccess = repos)
                            if(importSuccess) {
                                success++
                            }else {
                                failed++
                            }
                        }
                    }

                }
            }
        }else {
            all=1
            val dirFile = File(dir)
            // repo already exists
            if(repos.indexOfFirst { it.fullSavePath == dirFile.canonicalPath} != -1) {
                existed = 1
            }else { // repo not exist, import
                val isGitRepo = Libgit2Helper.isGitRepo(dirFile)
                if(isGitRepo) {
                    val importSuccess = importSingleRepo(dirFile)
                    if(importSuccess) {
                        success = 1
                    }else {
                        failed = 1
                    }
                }else {
                    all=0
                }
            }
        }

        return ImportRepoResult(all=all, success=success, existed=existed, failed=failed)

    }

    private suspend fun importSingleRepo(repoDir:File, addRepoToThisListIfSuccess:MutableList<RepoEntity>?=null):Boolean {
        val funName = "importSingleRepo"

        try {
            // make sure repoName not exists
            var repoName = repoDir.name
            if(isRepoNameExist(repoName)) {
                repoName = repoDir.name+ "_"+getShortUUID(6)
                if(isRepoNameExist(repoName)) {
                    repoName = repoDir.name+ "_"+getShortUUID(8)
                    if(isRepoNameExist(repoName)) {
                        repoName = repoDir.name+ "_"+getShortUUID(10)
                        if(isRepoNameExist(repoName)) {
                            repoName = repoDir.name+ "_"+getShortUUID(12)
                            if(isRepoNameExist(repoName)) {
                                repoName = repoDir.name+ "_"+getShortUUID(16)
                            }
                        }
                    }
                }
            }

            val repoEntity = RepoEntity()
            val remoteEntityList = mutableListOf<RemoteEntity>()

            repoEntity.repoName = repoName
            Repository.open(repoDir.canonicalPath).use { repo->
                repoEntity.fullSavePath = repoDir.canonicalPath
                repoEntity.workStatus = Cons.dbRepoWorkStatusUpToDate
                repoEntity.createBy = Cons.dbRepoCreateByImport

                val remotes = Libgit2Helper.getRemoteList(repo)
                remotes.forEach { remoteName ->
                    val remoteEntity = RemoteEntity()
                    remoteEntity.remoteName = remoteName
                    remoteEntity.repoId = repoEntity.id
                    remoteEntityList.add(remoteEntity)
                }
            }

            val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
            AppModel.singleInstanceHolder.dbContainer.db.withTransaction {
                // insert repo
                insert(repoEntity)

                // insert remotes
                remoteEntityList.forEach {remote ->
                    remoteDb.insert(remote)
                }
            }

            // in most case, this list is all repos list, if success, add repo into it, or dont add if is not necessary
            try {
                addRepoToThisListIfSuccess?.add(repoEntity)
            }catch (addToListException:Exception) {
                // concurrent exception, maybe, but is ok, because repo is inserted, so ignore add to list exception
                MyLog.e(TAG, "#$funName: err when add repo to list, err=${addToListException.localizedMessage}")
            }

            return true
        }catch (e:Exception) {
            MyLog.e(TAG, "#$funName: import err, err=${e.localizedMessage}")
            return false
        }
    }

    override suspend fun isGoodRepoName(name: String): Boolean {
        return !strHasIllegalChars(name) && !isRepoNameExist(name)
    }

    override suspend fun updateRepoName(repoId:String, name: String) {
        dao.updateRepoName(repoId, name)
    }


    /*
        suspend fun exampleWithTransaction(){
            //get other dao
            val otherDao = AppModel.singleInstanceHolder.dbContainer.db.credentialDao()
            //get other repository
            val otherRepository = AppModel.singleInstanceHolder.dbContainer.credentialRepository

            // do transaction
            AppModel.singleInstanceHolder.dbContainer.db.withTransaction {
                //use dao
                dao.insert(RepoEntity())
                otherDao.delete(CredentialEntity())

                //use repository function
                insert(RepoEntity())  // this repository's function
                otherRepository.delete(CredentialEntity()) // other repository's function
            }
        }

         */
    override suspend fun getIdByRepoNameAndExcludeId(repoName: String, excludeId: String): String? {
        return dao.getIdByRepoNameAndExcludeId(repoName, excludeId)
    }

    override suspend fun isRepoNameAlreadyUsedByOtherItem(repoName: String, excludeId: String): Boolean {
        return getIdByRepoNameAndExcludeId(repoName, excludeId) != null
    }
}
