package com.catpuppyapp.puppygit.utils.state
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.MutableState
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.saveable.rememberSaveable
//import com.catpuppyapp.puppygit.utils.cache.Cache
//import com.catpuppyapp.puppygit.utils.debugModeOn
//import com.catpuppyapp.puppygit.utils.getShortUUID
//import kotlinx.coroutines.sync.Mutex
//
//private val storage = Cache
////警告：有可能内存泄漏，因为即使remembersaveale被释放了，这变量也不知道，缓存里还存着之前的数据
////不太优雅的解决内存泄漏的方案：在每个页面里都注册BackHandler，返回的时候，释放本类持有的key关联的内存。
////不推荐直接构造此类，推荐用 `mutableCustomStateOf<T>()` 创建此类的实例
//
////20240416更新：把key改成了唯一的，把之前的key改成了state变量，state变量负责刷新，key负责存储某一类数据的唯一一份拷贝，例如commit list，永远都只需要一份，用新的覆盖旧的即可，这样可一定程度避免内存泄漏
////T不能为null，因为null代表值不存在，如果t可为null，处理起来有点麻烦
//class CustomStateSaveable_RememberVersion<T>(
//    uniKey_:String,  //key前缀+state值组合成唯一key，相同key前缀+state值的数据会覆盖，避免创建多份拷贝
//    state_:MutableState<T>,
//) {
////    private val lock = Mutex()
//
////    companion object {
//////        private val cacheStorage = Cache.ConcurrentMap
////
////        private fun genKey(keyPrefix: String, stateValue: String):String {
////            return keyPrefix + Cache.keySeparator + stateValue
////        }
////    }
//
//    //fields(properties)
////    private var isInit:Boolean = true  //为true时设置value不会更新缓存，此值必须在执行完实例的init代码块后设为假，不然value不会更新
////    private var requireSetNewValue = true  //此值如果为false，修改value 时不会set新值，只会更新key，如果此值为true，会多执行把新value set给新key的操作，但没坏影响，如果为false，可能会导致value没有刷新（除非在外部错误修改此值，否则这种情况不可能发生，而因为是private，所以外部修改不了，因此，实际上，不会发生这种情况），故将此值默认值设为true
//    private val uniKey = uniKey_
////    private val key:String = genKey(keyPrefix,initState.value)
//    private val state:MutableState<T> = state_
//    var value:T = state_.value
//        get() {
////            return field  //这个获取的值，有误！
//            return storage.get(uniKey) as T
////            return state.value
//        }
//        set(value) {
//            //更新field
//            state.value = value
//            storage.set(uniKey, value)
//            field = value
//
//        }
//
//    /**
//     * 触发页面更新，调用此方法后，页面应该能通过当前类实例的 .value 获取到最新数据
//     * 注：只有在不需要调用setter，但又想令页面重新获取当前类实例的数据时调用此方法。
//     * 应用举例：
//     * 例如 T 为list，你执行了list.clear()，但这并不会使当前实例的状态改变，因为没调用setter，这时就可调用本方法来触发页面更新，或者手动调用setter，但手动调setter看起来很奇怪，所以建议调用此方法(虽然此方法也是利用setter实现的...)
//     */
//    fun requireRefreshView(){
//        //调用setter，触发state改变，导致页面更新，注意：不能只更新state，因为缓存key依赖state，如果只更新state，key就变了，就取不出存储的数据了
////        requireSetNewValue = false  //只刷新，值没变，所以不需要设置新值
//        value = value  //更新state触发刷新，同时会重新生成key
////        requireSetNewValue = true  //必须重新设为true，不然外部调用 value的setter值不会更新
//
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as CustomStateSaveable_RememberVersion<*>
//
//        return uniKey == other.uniKey
//    }
//
//    override fun hashCode(): Int {
//        return uniKey.hashCode()
//    }
//
//    override fun toString(): String {
//        return "CustomStateSaveable(uniKey='$uniKey', state=$state)"
//    }
//
//
//}
//
///** 可存储并在配置改变（例如旋转屏幕）后恢复状态的state保存器
// * 不要滥用这个对象，只有用来存复杂的变量数据时才用这个变量(例如存储仓库类的list)，像是菜单列表之类的常量，直接用remember就行
// * 推荐使用场景（基本需求为希望在屏幕旋转后恢复状态）：
// *  所有实现 @Parcelize 的类
// *  所有之前用json字符串存到rememberSaveable的类
// *  所有rememberSaveable不能存且不会在LaunchedEffect中更新的复杂类型(例如选中文件列表，希望在旋转屏幕后依然选中之前选好的条目，但这个条目又不会在LaunchedEffected里更新，而且选中列表也不能用rememberSaveable类型存储，正是此类的绝佳用例)
// *
// * 不推荐使用场景：
// *      会在LaunchedEffect中更新数据的类，屏幕旋转之类的操作执行后会重新执行LaunchedEffected，数据会被重新查询并设置，因此没必要恢复
// */
//@Composable
//fun <T> mutableCustomStateOf_CustomStateSaveable_RememberVersion(uniKey:String, initValue:T):CustomStateSaveable_RememberVersion<T> {
//    val willSaveVal = (storage.get(uniKey) as? T) ?: initValue
//    if(debugModeOn) {
//        println("uniKey="+uniKey)
//        println("willSaveVal="+willSaveVal)
//    }
//    val state = remember { mutableStateOf(willSaveVal) }
//    storage.set(uniKey, willSaveVal)
////    val refreshState = remember {
////        mutableStateOf(getShortUUID())
////    }
//    return CustomStateSaveable_RememberVersion(uniKey, state)
//}
