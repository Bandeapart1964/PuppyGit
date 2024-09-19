package com.catpuppyapp.puppygit.utils.compare

import com.catpuppyapp.puppygit.utils.compare.param.CompareParam
import com.catpuppyapp.puppygit.utils.compare.result.IndexModifyResult
import com.catpuppyapp.puppygit.utils.compare.result.IndexStringPart
import com.catpuppyapp.puppygit.utils.compare.search.Search
import com.catpuppyapp.puppygit.utils.compare.search.SearchDirection

class SimilarCompareImpl: SimilarCompare {
    override fun doCompare(
        add: CompareParam,
        del: CompareParam,
        emptyAsMatch:Boolean,
        emptyAsModified:Boolean,
        onlyLineSeparatorAsEmpty:Boolean,
        searchDirection: SearchDirection,
        requireBetterMatching: Boolean,
        search: Search,
        betterSearch: Search
    ): IndexModifyResult {
        if(add.isEmpty() || del.isEmpty() || (onlyLineSeparatorAsEmpty && (add.isOnlyLineSeparator() || del.isOnlyLineSeparator()))){ //其中一个为空或只有换行符，不比较，直接返回结果，当作无匹配
            return IndexModifyResult(matched = emptyAsMatch, matchedByReverseSearch = false,
                listOf(IndexStringPart(0, add.getLen(), emptyAsModified)),
                listOf(IndexStringPart(0, del.getLen(), emptyAsModified)))
        }

        val reverse = searchDirection == SearchDirection.REVERSE || searchDirection == SearchDirection.REVERSE_FIRST

        val reverseMatchIfNeed = searchDirection == SearchDirection.REVERSE_FIRST || searchDirection == SearchDirection.FORWARD_FIRST

        //用On算法最坏的情况是正向匹配一次，然后逆向匹配一次，时间复杂度为 O(2n)，正常情况时间复杂度为O(n)，O(nm)若匹配两次，也会翻倍，最坏时间复杂度变成O(2nm)
        var result = if(requireBetterMatching) {
            betterSearch.doSearch(add, del, reverse)
        }else {
            search.doSearch(add, del, reverse)
        }

        //反向查找，如果需要的话。判断条件是：如果 “允许反向匹配 且 正向没匹配到”
        if(reverseMatchIfNeed && !result.matched) {
            result = if(requireBetterMatching) betterSearch.doSearch(add, del, !reverse) else search.doSearch(add, del, !reverse)
        }

        return result

    }
}
