package com.catpuppyapp.puppygit.data.repository

import androidx.room.withTransaction
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.dao.CredentialDao
import com.catpuppyapp.puppygit.data.entity.CredentialEntity
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.encrypt.PassEncryptHelper
import kotlinx.coroutines.sync.withLock

private val TAG = "CredentialRepositoryImpl"

//注： 不带decrypt和encrypt的查出的都是数据库中的原始数据
class CredentialRepositoryImpl(private val dao: CredentialDao) : CredentialRepository {
//    @Deprecated("dont use")
//    override fun getAllStream(): Flow<List<CredentialEntity?>> = dao.getAllStream()

//    @Deprecated("dont use")
//    override fun getStream(id: String): Flow<CredentialEntity?> = dao.getStream(id)


    override suspend fun getAllWithDecrypt(includeNone:Boolean, includeMatchByDomain:Boolean): List<CredentialEntity> {
        val all = dao.getAll().toMutableList()
        for(item in all) {
            decryptPassIfNeed(item)
        }

        prependSpecialItemIfNeed(list = all, includeNone = includeNone, includeMatchByDomain = includeMatchByDomain)

        return all
    }

    override suspend fun getAll(includeNone:Boolean, includeMatchByDomain:Boolean): List<CredentialEntity> {
        val list =  dao.getAll().toMutableList()

        prependSpecialItemIfNeed(list = list, includeNone = includeNone, includeMatchByDomain = includeMatchByDomain)

        return list
    }

    private fun prependSpecialItemIfNeed(list: MutableList<CredentialEntity>, includeNone:Boolean, includeMatchByDomain:Boolean) {
        if (includeMatchByDomain) {
            list.add(0, Cons.getSpecialCredential_MatchByDomain())
        }

        if (includeNone) {
            list.add(0, Cons.getSpecialCredential_NONE())
        }
    }

    override suspend fun insertWithEncrypt(item: CredentialEntity) {
        val funName = "insertWithEncrypt"
        Cons.credentialInsertLock.withLock {
            //如果名称已经存在则不保存
            if(isCredentialNameExist(item.name)) {
                MyLog.w(TAG, "#insertWithEncrypt(): Credential name exists, item will NOT insert! name is:"+item.name)
                throw RuntimeException("#$funName err: name already exists")

            }

            encryptPassIfNeed(item)

            dao.insert(item)
        }
    }

    override suspend fun insert(item: CredentialEntity) {
        val funName = "insert"

        Cons.credentialInsertLock.withLock {
            if (isCredentialNameExist(item.name)) {  //如果名称已经存在则不保存
                MyLog.w(TAG, "#insert(): Credential name exists, item will NOT insert! name is:" + item.name)
                throw RuntimeException("#$funName err: name already exists")

            }
            dao.insert(item)
        }
    }

    override suspend fun delete(item: CredentialEntity) = dao.delete(item)

    override suspend fun updateWithEncrypt(item: CredentialEntity) {
        if(Cons.isAllowedCredentialName(item.name).not()) {
            throw RuntimeException("credential name disallowed")
        }

        //如果密码不为空，加密密码。
        encryptPassIfNeed(item)

        dao.update(item)
    }

    override suspend fun update(item: CredentialEntity) {
        if(Cons.isAllowedCredentialName(item.name).not()) {
            throw RuntimeException("credential name disallowed")
        }

        dao.update(item)
    }

    override suspend fun isCredentialNameExist(name: String): Boolean {
        // disallowed name treat as existed
        if(Cons.isAllowedCredentialName(name).not()) {
            return true
        }

        val id = dao.getIdByCredentialName(name)
//        return id != null && id.isNotBlank()  // don't check blank may be better
        return id != null
    }

    override suspend fun getByIdWithDecrypt(id: String): CredentialEntity? {
        if(id.isBlank() || id==Cons.dbCredentialSpecialId_NONE) {
            return null
        }

        val item = dao.getById(id)
        if(item == null) {
            return null
        }

        decryptPassIfNeed(item)

        return item
    }

    override suspend fun getById(id: String): CredentialEntity? {
        if(id.isBlank() || id==Cons.dbCredentialSpecialId_NONE) {
            return null
        }

        return dao.getById(id)
    }

//    override suspend fun getListByType(type: Int): List<CredentialEntity> {
//        return dao.getListByType(type)
//    }

    override suspend fun getSshList(): List<CredentialEntity> {
        return dao.getListByType(Cons.dbCredentialTypeSsh)
    }

    override suspend fun getHttpList(includeNone:Boolean, includeMatchByDomain:Boolean): List<CredentialEntity> {
        val list = dao.getListByType(Cons.dbCredentialTypeHttp).toMutableList()

        prependSpecialItemIfNeed(list = list, includeNone = includeNone, includeMatchByDomain = includeMatchByDomain)

        return list
    }

    override suspend fun deleteAndUnlink(item:CredentialEntity) {
        val db = AppModel.singleInstanceHolder.dbContainer.db
        val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
        val remoteDb = AppModel.singleInstanceHolder.dbContainer.remoteRepository
        db.withTransaction {
            //删除凭据需要：删除凭据、解除关联remote、解除关联仓库（未克隆的那种，简单用credentialId匹配下仓库所有条目credentialId字段即可，因为不管克隆成功与否，反正这凭据要删除了，就该解除关联）
            remoteDb.updateFetchAndPushCredentialIdByCredentialId(item.id, item.id, "", "")  //解除关联remote
            repoDb.unlinkCredentialIdByCredentialId(item.id)  //解除关联repo克隆时使用的凭据
            delete(item)  //删除凭据
        }
    }

    override fun encryptPassIfNeed(item:CredentialEntity?) {
        //用户名不用加密，不过私钥呢？感觉也用不着加密，暂时只加密密码吧。
        if(item!=null && !item.pass.isNullOrEmpty()) {
            item.pass = PassEncryptHelper.encryptWithCurrentEncryptor(item.pass)
        }
    }
    override fun decryptPassIfNeed(item:CredentialEntity?) {
        if (item != null && !item.pass.isNullOrEmpty()) {
            //如果密码不为空，解密密码。
            item.pass = PassEncryptHelper.decryptWithCurrentEncryptor(item.pass)
        }
    }
}
