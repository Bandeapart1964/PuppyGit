package com.catpuppyapp.puppygit.utils.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap

class CustomStateSaveable<T>(
    private val holder:Holder<MutableState<T>>
) {
    var value:T=holder.data.value
        get() {
//            return field
            return holder.data.value
        }
        set(value) {
            holder.data.value = value
            field = value
        }

}

class CustomStateListSaveable<T>(
    private val holder:Holder<SnapshotStateList<T>>
) {

    var value:SnapshotStateList<T> =holder.data
        get() {
//            return field
            return holder.data
        }
        //SnapShotStateList其实用不到set，不需要元素时可清空集合就行了
        private set(value) {
            holder.data = value
            field = value
        }

}

class CustomStateMapSaveable<K,V>(
    private val holder:Holder<SnapshotStateMap<K, V>>
) {

    var value:SnapshotStateMap<K, V> =holder.data
        get() {
//            return field
            return holder.data
        }
        //SnapshotStateMap也不需要set，原因同SnapshotList
        private set(value) {
            holder.data = value
            field = value
        }

}

@Composable
fun <T> mutableCustomStateOf(keyTag:String, keyName:String, initValue: T): CustomStateSaveable<T> {
    val stateHolder = rememberSaveable(saver = getSaver()) {
        getHolder(keyTag, keyName, data= mutableStateOf<T>(initValue))
    }
    return CustomStateSaveable(stateHolder)
}

@Composable
fun <T> mutableCustomStateOf(keyTag:String, keyName:String, getInitValue: ()->T): CustomStateSaveable<T> {
    val stateHolder = rememberSaveable(saver = getSaver()) {
        getHolder(keyTag, keyName, data= mutableStateOf<T>(getInitValue()))
    }
    return CustomStateSaveable(stateHolder)
}

@Composable
fun <T> mutableCustomStateListOf(keyTag:String, keyName:String, initValue: List<T>): CustomStateListSaveable<T> {
    val stateHolder = rememberSaveable(saver = getSaver()) {
        val list =  mutableStateListOf<T>()
        list.addAll(initValue)

        getHolder(keyTag, keyName, data=list)
    }
    return CustomStateListSaveable(stateHolder)
}

@Composable
fun <T> mutableCustomStateListOf(keyTag:String, keyName:String, getInitValue: ()->List<T>): CustomStateListSaveable<T> {
    val stateHolder = rememberSaveable(saver = getSaver()) {
        val list =  mutableStateListOf<T>()
        list.addAll(getInitValue())

        getHolder(keyTag, keyName, data=list)
    }
    return CustomStateListSaveable(stateHolder)
}

@Composable
fun <K,V> mutableCustomStateMapOf(keyTag:String, keyName:String, initValue: Map<K,V>): CustomStateMapSaveable<K,V> {
    val stateHolder = rememberSaveable(saver = getSaver()) {
        val map = mutableStateMapOf<K,V>()
        map.putAll(initValue)

        getHolder(keyTag, keyName, data=map)
    }
    return CustomStateMapSaveable(stateHolder)
}

@Composable
fun <K,V> mutableCustomStateMapOf(keyTag:String, keyName:String, getInitValue: ()->Map<K,V>): CustomStateMapSaveable<K,V> {
    val stateHolder = rememberSaveable(saver = getSaver()) {
        val map = mutableStateMapOf<K,V>()
        map.putAll(getInitValue())

        getHolder(keyTag, keyName, data=map)
    }
    return CustomStateMapSaveable(stateHolder)
}
