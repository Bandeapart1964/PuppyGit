package com.catpuppyapp.puppygit.utils

import androidx.compose.runtime.MutableState
import com.catpuppyapp.puppygit.utils.cache.Cache

/**
 * 这个类的作用是为某些字符串state关联外部数据，目前20240428仅用于为needRefresh状态变量附带数据，实际上过度设计了，根本没必要整这么复杂
 */

private val storage = Cache

object StateRequestType {
    val invalid = -1
    //强制重新加载数据，就算有能恢复的数据也不使用，一般用来加载更多的那种列表上，例如 CommitListScreen
    val forceReload = 1
}

//改变值触发执行刷新页面的代码
fun changeStateTriggerRefreshPage(needRefresh: MutableState<String>, requestType:Int = StateRequestType.invalid, data:Any? =null, newStateValue: String = getShortUUID()) {
    setStateWithRequestData(state = needRefresh, requestType=requestType, data = data, newStateValue = newStateValue)
}

fun setStateWithRequestData(state: MutableState<String>, requestType:Int = StateRequestType.invalid, data:Any? =null, newStateValue:String = getShortUUID()) {
    if(requestType != StateRequestType.invalid) {
        storage.set(newStateValue, Pair(requestType, data))
    }

    state.value = newStateValue
}


fun<T> getRequestDataByState(stateValue:String, getThenDel:Boolean=false):Pair<Int,T?> {
    //这样不行，不能保证每个请求只执行一次
    if(stateValue.isBlank()) {
        return Pair(StateRequestType.invalid, null)
    }

    val requestTypeAndData = if(getThenDel) storage.getByTypeThenDel<Pair<Int,T?>>(stateValue) else storage.getByType<Pair<Int,T?>>(stateValue)

    return requestTypeAndData ?: Pair(StateRequestType.invalid, null)
}

fun delRequestDataByState(stateValue:String) {
    //这样不行，不能保证每个请求只执行一次
    if(stateValue.isBlank()) {
        return
    }
    storage.del(stateValue)
}
