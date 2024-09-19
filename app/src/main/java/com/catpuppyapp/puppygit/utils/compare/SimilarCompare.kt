package com.catpuppyapp.puppygit.utils.compare

import com.catpuppyapp.puppygit.utils.compare.param.CompareParam
import com.catpuppyapp.puppygit.utils.compare.result.IndexModifyResult
import com.catpuppyapp.puppygit.utils.compare.search.Search
import com.catpuppyapp.puppygit.utils.compare.search.SearchDirection

interface SimilarCompare {
    companion object {
        val INSTANCE: SimilarCompare = SimilarCompareImpl()
    }

    /**
     * try find `del` in `add`, if you want to find `add` in `del` just simple swap params order when calling.
     *
     * must match:
     * add:12345, del: 1245, matched: 12, 45
     * add:12356, del: 12, matched:12
     *
     * if `requireBetterMatching` is true,  matching:
     * add: 123a45, del:123b45,
     *
     * in most case, when `requireBetterMatching` is true, means bad performance and good matching, in other word, when it is false, good performance and bad matching
     *
     * if `reverseMatchIfNeed`, maybe will try reverse matching when forward matching failed, if do reverse matching, the time complex may x2
     */
    fun doCompare(
        add: CompareParam,
        del: CompareParam,
        emptyAsMatch: Boolean = false,
        emptyAsModified: Boolean = true,
        onlyLineSeparatorAsEmpty: Boolean = true,
        searchDirection: SearchDirection = SearchDirection.FORWARD_FIRST,
        requireBetterMatching: Boolean = false,
        search: Search = Search.INSTANCE,
        betterSearch: Search = Search.INSTANCE_BETTER_MATCH_BUT_SLOW
    ): IndexModifyResult

}
