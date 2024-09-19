package com.catpuppyapp.puppygit.utils.state

import androidx.compose.runtime.saveable.Saver
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.getShortUUID

// random是用来在某些情况下刷新数据的，不过一般用不到
data class Holder<T>(var key: String, var data:T, var random:String= getShortUUID())

//fun <T> refreshState(state:MutableState<Holder<T>>) {
////    state.value = state.value
//    state.value = state.value.copy(random = getShortUUID())
//}

//如果null，怎么办？
//怎么判断什么时候需要清key？
//如果是引用类型，指
fun <T> getSaver():Saver<Holder<T>, String> {
    return Saver(
        save = { holder ->
//            if(debugModeOn) {
//                println("save:$holder")
//            }
            Cache.set(holder.key, holder)
            holder.key
        },
        restore = { key ->
            val holder = Cache.getByType<Holder<T>>(key)
//            if(debugModeOn) {
//                println("restore value:$holder")
//            }
            holder
        }
    )
}

//keyTag+keyName 注意事项：1 组合起来必须唯一，不然数据会错乱；2 必须是常量，不然会造成内存泄漏，即使数据没用了依然保存在内存
fun <T : Any> getHolder(keyTag:String, keyName:String, data: T):Holder<T> {
    val holder = Holder<T>(key = genKey(keyTag, keyName), data=data, random=getShortUUID())
    return holder
}

fun genKey(keyTag:String, keyName:String):String {
    return "$keyTag:$keyName"
}
