package com.catpuppyapp.puppygit.utils.state
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.MutableState
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.saveable.rememberSaveable
//import com.catpuppyapp.puppygit.utils.cache.Cache
//import com.catpuppyapp.puppygit.utils.debugModeOn
//import com.catpuppyapp.puppygit.utils.getShortUUID
//import kotlinx.coroutines.sync.Mutex
//
////警告：有可能内存泄漏，因为即使remembersaveale被释放了，这变量也不知道，缓存里还存着之前的数据
////不太优雅的解决内存泄漏的方案：在每个页面里都注册BackHandler，返回的时候，释放本类持有的key关联的内存。
////不推荐直接构造此类，推荐用 `mutableCustomStateOf<T>()` 创建此类的实例
//
////20240416更新：把key改成了唯一的，把之前的key改成了state变量，state变量负责刷新，key负责存储某一类数据的唯一一份拷贝，例如commit list，永远都只需要一份，用新的覆盖旧的即可，这样可一定程度避免内存泄漏
////T不能为null，因为null代表值不存在，如果t可为null，处理起来有点麻烦
//class CustomStateSaveable_RememberSaveableVersion<T:Any>(
//    initKeyPrefix:String,  //key前缀+state值组合成唯一key，相同key前缀+state值的数据会覆盖，避免创建多份拷贝
//    initValue:T,
//    initState:MutableState<String>,
//) {
//    private val lock = Mutex()
//
//    companion object {
//        private val cacheStorage = Cache
//
//        private fun genKey(keyPrefix: String, stateValue: String):String {
//            return keyPrefix + Cache.keySeparator + stateValue
//        }
//    }
//
//    //fields(properties)
//    private var isInit:Boolean = true  //为true时设置value不会更新缓存，此值必须在执行完实例的init代码块后设为假，不然value不会更新
//    private var requireSetNewValue = true  //此值如果为false，修改value 时不会set新值，只会更新key，如果此值为true，会多执行把新value set给新key的操作，但没坏影响，如果为false，可能会导致value没有刷新（除非在外部错误修改此值，否则这种情况不可能发生，而因为是private，所以外部修改不了，因此，实际上，不会发生这种情况），故将此值默认值设为true
//    private val keyPrefix = initKeyPrefix
////    private val key:String = genKey(keyPrefix,initState.value)
//    private val state:MutableState<String> = initState
//    fun getStateVal():String{
//        return state.value
//    }
//    var value:T = initValue
//        get() {
////            return field  //这个获取的值，有误！
//            return cacheStorage.get(genKey(keyPrefix, state.value)) as T
//        }
//        set(value) {
//            //更新field
//            field = value
//
//            if(!isInit) {  //初始化时（构造对象时）因为上面已经赋了默认值，所以如果是第一次往缓存存值，就不需要调用set；但如果缓存里有值，需要更新对象值为缓存里的值，但这时候并不需要更新key，因为key是从app Bundle里恢复的而不是通过我的代码里的赋值操作更新的，不过其实更新key也无所谓，但逻辑上来说，不需要更新key，只要确保能拿到之前的数据就行，也就是实现“恢复”的语义即可
//                val oldKey = genKey(keyPrefix, state.value)
//                val newStateVal = getShortUUID()
//                val newKey = genKey(keyPrefix, newStateVal)
//                cacheStorage.updateKey(oldKey, newKey)
//                if(debugModeOn) {
//                    println("oldKey=$oldKey, newKey=$newKey, value=$value")
//                }
//
//                if(requireSetNewValue) {  //这里不删旧值，因为恢复的时候有可能不是新值，而是新值的上一个值
//                    cacheStorage.set(oldKey,value)
//                    cacheStorage.set(newKey,value)  //更新缓存
//                }
//
//                //修改state变量触发页面刷新
//                state.value = newStateVal
//
//                if(debugModeOn) {
//                    println("new state.value="+state.value)
//                    println("try get newValue from storage:"+ cacheStorage.get(newKey))
//                }
//            }
//
//
//        }
//
////    x 20240416 解决了）有bug：同一个页面，不同条目，数据会获取错，需要用 key+state 组合出一个唯一的key，同时，一旦state变化(查无旧数据)，就把所有相同key的条目删除（或清空，如果删除会导致并发冲突的话）
//    //实例的初始化代码块
//    init {
//        isInit = true;  //这个操作好像意义不大？因为默认值就是true啊！不过，如果加了这个操作，就算改默认值，代码依然正常工作，不过我为什么要改默认值？算了，这代码暂且留着吧
//
//        //注意：上面给field赋值不会调用setter，所以必须在init代码块里更新变量！
//        val restoreOrInitKey = genKey(keyPrefix, state.value)
//        val oldV = cacheStorage.get(restoreOrInitKey)
////        if(debugModeOn) {
////            println("restore state.value="+state.value)
////            println("restore key="+restoreOrInitKey)
////            println("oldV="+oldV)
////        }
//        val oldOrInitValue = if(oldV==null) initValue else {value = oldV as T; oldV}
//        //这里有可能获取到旧值，比如在setter里更新state为2，旧state为1，那恢复状态的时候，这里可能取到的是1，所以，我调整了恢复策略，即使调用setter更新key，也不会删除旧key的数据，而是把新旧key一并更新，立刻页面，下次重进时，恢复值失败，这时就会清除之前所有的key，这样有点费内存，但没办法，因为无法取得有效的state最新值
//        //如果存在旧值，使用旧值，否则使用默认值
//        //有可能要恢复的值本来就是null，所以这里判断一下，只有当新旧值不同且旧值为null，才认为旧值不存在，使用初始化的值
////        if (oldV == null) {  //无数据，存入状态并且清空之前同类数据，如果有的话
////            //这时候value已经内初始化为initValue并且不会调用setter，所以不需要再次更新value
////            if(debugModeOn) {
////                println("clearByKeyPrefix:"+(keyPrefix+Cache.keySeparator))
////            }
////        }else
////        if(oldV!=null){  //有数据，恢复
////            //这时需要更新value，但因为这时候是从缓存“恢复”数据，而不是set更新缓存的数据，所以不需要更新cache，只设置value即可，用isInit变量实现这点
////            value = oldV as T  //此时会调用上面的setter，但由于isInit=true，所以不会更新state
////            oldOrInitValue = oldV
////        }
//        //            查出数据，一样清key，减少内存占用，不过会有点性能损耗
//        cacheStorage.clearByKeyPrefix(keyPrefix+Cache.keySeparator)  //清空之前存储的相同key前缀的值，如果有的话，这个操作一定要放在set前面，不然新key也会被清掉
//        cacheStorage.set(restoreOrInitKey, oldOrInitValue)  //清完key重新设置value
//
//        isInit = false
//
//
//    }
//
//    /**
//     * 触发页面更新，调用此方法后，页面应该能通过当前类实例的 .value 获取到最新数据
//     * 注：只有在不需要调用setter，但又想令页面重新获取当前类实例的数据时调用此方法。
//     * 应用举例：
//     * 例如 T 为list，你执行了list.clear()，但这并不会使当前实例的状态改变，因为没调用setter，这时就可调用本方法来触发页面更新，或者手动调用setter，但手动调setter看起来很奇怪，所以建议调用此方法(虽然此方法也是利用setter实现的...)
//     */
//    fun requireRefreshView(){
//        //调用setter，触发state改变，导致页面更新，注意：不能只更新state，因为缓存key依赖state，如果只更新state，key就变了，就取不出存储的数据了
//        requireSetNewValue = false  //只刷新，值没变，所以不需要设置新值
//        value = value  //更新state触发刷新，同时会重新生成key
//        requireSetNewValue = true  //必须重新设为true，不然外部调用 value的setter值不会更新
//
//    }
//
//    override fun toString(): String {
//        return "CustomStateSaveable(isInit=$isInit, requireSetNewValue=$requireSetNewValue, keyPrefix='$keyPrefix', state=$state, value=$value)"
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as CustomStateSaveable_RememberSaveableVersion<*>
//
//        if (keyPrefix != other.keyPrefix) return false
//        if (state != other.state) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = keyPrefix.hashCode()
//        result = 31 * result + state.hashCode()
//        return result
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
//fun <T:Any> mutableCustomStateOf_RememberSaveableVersion(keyPrefix:String, initValue:T):CustomStateSaveable_RememberSaveableVersion<T> {
//    val state = rememberSaveable { mutableStateOf(getShortUUID()) }
//
//    return CustomStateSaveable_RememberSaveableVersion(keyPrefix, initValue, state)
//}
