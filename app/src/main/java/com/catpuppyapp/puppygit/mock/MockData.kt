package com.catpuppyapp.puppygit.mock

import com.catpuppyapp.puppygit.data.entity.CredentialEntity
import com.catpuppyapp.puppygit.data.entity.ErrorEntity
import com.catpuppyapp.puppygit.git.CommitDto
import com.catpuppyapp.puppygit.dto.RemoteDtoForCredential
import com.catpuppyapp.puppygit.utils.getShortUUID

class MockData {
    companion object {
        fun getCommitList(repoId:String?, branch:String?,start:Int, end:Int):List<CommitDto> {
            println("getCommitList#repoId/branch:::"+repoId+"/"+branch)
            val list = mutableListOf<CommitDto>()
            for(i in start..end) {
//                if(i%3==0) {
//                    list.add(
//                        CommitDto(""+i,"abc1234db12decafa1234928d8c1aef29838289", listOf("dev"),"2024-01-13 11:12:12","author1","abfffffffffffc@123.com","commit msg:bug1\nbug2,fixabcdefghikjkdjljgkjkkdjkdjf"),
//                    )
//                }else {
//
//                    list.add(
//                        CommitDto(""+i,"abc1234fde111223455544333", if(i==1) listOf("main", "Origin/main")else listOf(),"2024-01-13 11:12:12","author1","abfffffffffffc@123.com","commit msg"),
//                    )
//                }

            }
            return list
        }

        fun getCommitSum(repoId:String?, branch: String?) :Int{
            return 3
        }
        fun getErrorSum(repoId:String?) :Int{
            return 3
        }

        fun getErrorList(repoId:String?,start:Int, end:Int):List<ErrorEntity> {
            println("getErrorList#repoId:::"+repoId)
            val list = mutableListOf<ErrorEntity>()
            for(i in start..end) {
                list.add(
                    ErrorEntity(id=""+i,date="2024-01-13 11:12:12",msg="Push Failed",repoId="1"),
                )

            }
            return list
        }

        fun getAllCredentialList(type:Int):List<CredentialEntity> {
            val list = mutableListOf<CredentialEntity>()

            val end = 30;
            for(i in 0..end) {
                list.add(
                    CredentialEntity(id= getShortUUID(),
                        name = ""+type+"_credential"+i,
                        value = "abc",
                        pass = "def",
                        type = type,
                        )
                )
            }
            return list
        }

        fun getCredentialRemoteList():List<RemoteDtoForCredential> {
            val list = mutableListOf<RemoteDtoForCredential>()

            val end = 30;
            for(i in 0..end) {
                list.add(
                    if(i%2==0) {

                        RemoteDtoForCredential(
                            remoteId = "remoteid123",
                            remoteName = "remtename123",
                            repoId = "id123",
                            repoName = "reponame123",
                            credentialName = "credentialName"
                        )
                    }else {
                        RemoteDtoForCredential(
                            remoteId = "remoteid123",
                            remoteName = "remtename123",
                            repoId = "id123",
                            repoName = "reponame123",
                            credentialId = "creid123",
                            credentialName = "credenname123"
                        )
                    }
                )
            }
            return list
        }

    }
}