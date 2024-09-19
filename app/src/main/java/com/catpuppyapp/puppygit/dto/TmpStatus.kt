package com.catpuppyapp.puppygit.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * example:
 *      beforeStatus:NeedPush
 *      curStatus:Pushing
 *      nextStatus:up-to-date
 * update RepoDto#status when curStatus done
 */
@Deprecated("好像暂时用不上了")
@Parcelize
class TmpStatus(var beforeStatus: String = "",
                var curStatus: String = "",
                var nextStatus: String = ""
) : Parcelable {

}
