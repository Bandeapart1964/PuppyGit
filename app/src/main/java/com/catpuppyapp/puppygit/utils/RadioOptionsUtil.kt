package com.catpuppyapp.puppygit.utils

class RadioOptionsUtil {
    companion object {
        //用于选项编号和数字的分隔符，选项类似： 1: abc
//        val radioLabelNumSeparator = ":"

//        //有些单选框，没编号属性，只能对比字符串判断是否选中，如果用户语言奇怪，可能会很麻烦，所以强制在所有选项前加个数字编号，一般编号只需要占1位，选项类似：1:abc, 2:def
////本函数获取其中的编号，就是 “1: abc” 中的 “1”
//        fun getKeyOfOptions(optionText:String):String {
////    return optionText.substring(0,1)
//            val indexOf = optionText.indexOf(radioLabelNumSeparator)
//            return optionText.substring(0, indexOf).trim()
//        }
//        //获取 "1: abc" 中的abc
//        fun getValueOfOptions(optionText:String):String {
//            val indexOf = optionText.indexOf(radioLabelNumSeparator)
//            return optionText.substring(indexOf+1).trim()
//        }
//
//        //适用于写代码时就知道有多少个选择项的列表，在代码里直接创建list，创建map，只是利用这个方法合并下key和选项文本
//        //输入：1，abc，输出类似："1: abc"
//        fun formatOptionKeyAndText(key:String, text:String):String {
//            return key+radioLabelNumSeparator+" "+text
//        }

        //适用于写代码时不确定有多少个选择项的那种列表，用户选择后，取出他选的选项对应的idx，再用idx从list中取出对应的Pair，再取出Pair的value，就得到选项值了
        //如果是已确定项的那种list，直接把idx和value都写到代码里即可，例如：listOf(Pair(optNumNoCredential, noCredential), Pair(optNumNewCredential,newCredential), Pair(optNumSelectCredential,selectCredential))
        //根据文案列表生成选项列表，输入例如：{"a","b"}，输出类似：{"1":"a", "2":"b"}
//        fun genOptionNumAndTextListByTextList(textList:List<String>):List<Pair<Int, String>> {
//            val ret = mutableListOf<Pair<Int, String>>()
//            for((idx,value) in textList.withIndex()) {
//                // put("1", "option text")
//                ret.add(Pair(idx ,value))
//            }
//            return ret
//        }
    }
}