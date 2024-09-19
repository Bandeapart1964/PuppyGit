package com.catpuppyapp.puppygit.utils

import kotlinx.serialization.json.Json

object JsonUtil {

    //忽略对象里没有的key和保存默认值，不然，只有当类的字段值和其默认值不一样时，才会被保存到配置文件，那样有些逻辑实现起来就麻烦了，所以直接记住默认值
//    val j = Json{ ignoreUnknownKeys = true; encodeDefaults=true}
    val j = Json{ ignoreUnknownKeys = true; encodeDefaults=false}
}
