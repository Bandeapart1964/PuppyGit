package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

//克隆错误卡片，显示编辑仓库和删除仓库和重试克隆按钮
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ErrRepoCard(
//    showBottomSheet: MutableState<Boolean>,
//    curRepo: CustomStateSaveable<RepoEntity>,  //当前仓库 stage
    repoDto: RepoEntity,  //当前卡片使用的仓库
    repoDtoList: MutableList<RepoEntity>,

    idx: Int,
    needRefreshList: MutableState<String>,
    requireDelRepo:(RepoEntity)->Unit,
    requireBlinkIdx: MutableIntState,
    copyErrMsg:(String)->Unit,
) {
    val navController = AppModel.singleInstanceHolder.navController
    val haptic = AppModel.singleInstanceHolder.haptic

    val appContext = LocalContext.current
    val inDarkTheme = Theme.inDarkTheme

    val cardColor = UIHelper.defaultCardColor()
    val highlightColor = if(inDarkTheme) Color(0xFF9D9C9C) else Color(0xFFFFFFFF)

    val dbContainer = AppModel.singleInstanceHolder.dbContainer
    val lineHeight = 30
    Column (modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ){

        Card(
            //0.9f 占父元素宽度的百分之90
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(0.95F)
//            .combinedClickable(
//                enabled = false,  //错误卡片，禁用长按功能
//                onClick = {},
////                onLongClick = {
////                    //震动反馈
//////                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
//////                    curRepo.value = repoDto
//////                    //显示底部菜单
//////                    showBottomSheet.value = true
////                },
//            )
//            .defaultMinSize(minHeight = 100.dp)

            ,
            colors = CardDefaults.cardColors(
                //如果是请求闪烁的索引，闪烁一下
                containerColor = if (requireBlinkIdx.intValue != -1 && requireBlinkIdx.intValue == idx) {
                    //高亮2s后解除
                    doJobThenOffLoading {
                        delay(UIHelper.getHighlightingTimeInMills())  //解除高亮倒计时
                        requireBlinkIdx.intValue = -1  //解除高亮
                    }
                    highlightColor
                } else {
                    cardColor
                }

            ),
//        border = BorderStroke(1.dp, Color.Black),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )

        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.fillMaxWidth(.6F)) {
                    Text(
                        text = repoDto.repoName,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(
                            start = 10.dp,
                            top = 5.dp,
                            bottom = 0.dp,
                            end = 1.dp
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

//                Text(text = repoDto.remoteName,
//                    textAlign = MyStyleKt.clickableText.textAlign,
//                    fontSize = MyStyleKt.clickableText.fontSize,
//                    modifier = MyStyleKt.clickableText.modifier,
//                    style = MyStyleKt.clickableText.style,
//                    color = MyStyleKt.clickableText.color
//                )
//
//                Text(text = ":"+repoDto.branch,
//                    fontSize = MyStyleKt.clickableText.fontSize,
//                    textAlign = MyStyleKt.clickableText.textAlign,
//                    modifier = MyStyleKt.clickableText.modifier,
//                    style = MyStyleKt.clickableText.style,
//                    color = MyStyleKt.clickableText.color
//                )
                }

            }
            HorizontalDivider()
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Row(
//                modifier = Modifier.height(min=lineHeight.dp),
                    modifier = Modifier.fillMaxWidth(.9f).combinedClickable(onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        copyErrMsg(repoDto.createErrMsg)
                    }) {  },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = stringResource(R.string.error)+":"+repoDto.createErrMsg,
                        color= Color.Red,
                        textAlign = TextAlign.Left,

                        )
                }

                Spacer(Modifier.height(10.dp))
//
//                Row(
//                    modifier = Modifier.height(lineHeight.dp),
//
//                ) {
//                    Text(text=stringResource(R.string.copy_msg),
//                        style = MyStyleKt.ClickableText.style,
//                        color = MyStyleKt.ClickableText.color,
//                        fontWeight = FontWeight.Light,
//                        modifier = MyStyleKt.ClickableText.modifier.clickable(onClick = {
//                            copyErrMsg(repoDto.createErrMsg)
//                        }),
//                    )
//                }

                Row(
                    modifier = Modifier.height(lineHeight.dp),

                ) {
                    Text(
                        text = stringResource(R.string.retry),
                        style = MyStyleKt.ClickableText.style,
                        color = MyStyleKt.ClickableText.color,
                        fontWeight = FontWeight.Light,
                        modifier = MyStyleKt.ClickableText.modifier.clickable(onClick = {
//                        repoDtoList[idx].tmpStatus=""  //err状态，tmpStatus本来就没值，不用设
                            doJobThenOffLoading {
                                val key = repoDto.id
                                val repoLock = Cons.repoLockMap.getOrPut(key) {
                                    Mutex()
                                }
                                repoLock.withLock {
                                    val repoRepository=dbContainer.repoRepository
                                    val repoFromDb = repoRepository.getById(key)?:return@withLock
                                    if(repoFromDb.workStatus == Cons.dbRepoWorkStatusCloneErr) {
                                        repoFromDb.workStatus = Cons.dbRepoWorkStatusNotReadyNeedClone
//                                    repoFromDb.tmpStatus = appContext.getString(R.string.cloning)
                                        repoFromDb.createErrMsg = ""
                                        repoRepository.update(repoFromDb)

                                        //这个可有可无，反正后面会刷新页面，刷新页面必须有，否则会变成cloning状态，然后就不更新了，还得手动刷新页面，麻烦
                                        repoDtoList[idx]=repoFromDb
//                                    repoDtoList.requireRefreshView()
                                    }
                                    changeStateTriggerRefreshPage(needRefreshList)
                                }
                            }
                        }),
                    )
                }



                Row(
                    modifier = Modifier.height(lineHeight.dp),

                ) {
                    Text(text=stringResource(R.string.edit_repo),
                        style = MyStyleKt.ClickableText.style,
                        color = MyStyleKt.ClickableText.color,
                        fontWeight = FontWeight.Light,
                        modifier = MyStyleKt.ClickableText.modifier.clickable(onClick = {
                            navController.navigate(Cons.nav_CloneScreen+"/"+repoDto.id)
                        }),
                    )
                }
                Row(
                    modifier = Modifier.height(lineHeight.dp),

                ) {
                    Text(text=stringResource(R.string.del_repo),
                        style = MyStyleKt.ClickableText.style,
                        color = MyStyleKt.ClickableText.color,
                        fontWeight = FontWeight.Light,
                        modifier = MyStyleKt.ClickableText.modifier.clickable(onClick = {
                            requireDelRepo(repoDto)
                        }),
                    )
                }
            }
        }


    }

}
