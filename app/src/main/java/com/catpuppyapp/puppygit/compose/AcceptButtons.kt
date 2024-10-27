package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowCircleLeft
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.UIHelper


@Composable
fun AcceptButtons(
    lineIndex: Int,
    lineText: String,
    prepareAcceptBlock: (Boolean, Boolean, Int, String) -> Unit,
    conflictOursBlockBgColor: Color,
    conflictTheirsBlockBgColor: Color,
    conflictSplitLineBgColor: Color,
    useLongPressedIconVersion:Boolean = true,
) {
    if(useLongPressedIconVersion) {
        AcceptButtons_LongPressedIcon(
            lineIndex = lineIndex,
            lineText = lineText,
            prepareAcceptBlock = prepareAcceptBlock,
        )
    }else {
        AcceptButtons_TextButton(
            lineIndex = lineIndex,
            lineText = lineText,
            prepareAcceptBlock = prepareAcceptBlock,
            conflictOursBlockBgColor = conflictOursBlockBgColor,
            conflictTheirsBlockBgColor = conflictTheirsBlockBgColor,
            conflictSplitLineBgColor = conflictSplitLineBgColor
        )
    }
}


/**
 * this smaller than iconText, usually will not over-sized
 */
@Composable
private fun AcceptButtons_LongPressedIcon(
    lineIndex: Int,
    lineText: String,
    prepareAcceptBlock: (Boolean, Boolean, Int, String) -> Unit,
) {
    Row {
        LongPressAbleIconBtn(
            tooltipText = stringResource(R.string.accept_ours),
            icon = Icons.Outlined.ArrowCircleLeft,
            iconContentDesc = stringResource(R.string.accept_ours),
            iconColor = UIHelper.getAcceptOursIconColor()
        ) {
            prepareAcceptBlock(true, false, lineIndex, lineText)
        }


        LongPressAbleIconBtn(
            tooltipText = stringResource(R.string.accept_theirs),
            icon = Icons.Outlined.ArrowCircleRight,
            iconContentDesc = stringResource(R.string.accept_theirs),
            iconColor = UIHelper.getAcceptTheirsIconColor()
        ) {
            prepareAcceptBlock(false, true, lineIndex, lineText)
        }


        LongPressAbleIconBtn(
            tooltipText = stringResource(R.string.accept_both),
            icon = Icons.Outlined.CheckCircle,
            iconContentDesc = stringResource(R.string.accept_both),
            iconColor = UIHelper.getAcceptBothIconColor()
        ) {
            prepareAcceptBlock(true, true, lineIndex, lineText)
        }


        LongPressAbleIconBtn(
            tooltipText = stringResource(R.string.reject_both),
            icon = Icons.Outlined.RemoveCircleOutline,
            iconContentDesc = stringResource(R.string.reject_both),
            iconColor = UIHelper.getRejectBothIconColor()
        ) {
            prepareAcceptBlock(false, false, lineIndex, lineText)
        }

    }
}


/**
 * if text long, this may over-sized
 */
@Composable
private fun AcceptButtons_TextButton(
    lineIndex: Int,
    lineText: String,
    prepareAcceptBlock: (Boolean, Boolean, Int, String) -> Unit,
    conflictOursBlockBgColor: Color,
    conflictTheirsBlockBgColor: Color,
    conflictSplitLineBgColor: Color
) {
    Row {
        TextButton(
            colors = ButtonDefaults.textButtonColors().copy(containerColor = conflictOursBlockBgColor),
            onClick = {
                prepareAcceptBlock(true, false, lineIndex, lineText)
            },
        ) {
            Text(stringResource(R.string.accept_ours))
        }
        TextButton(
            colors = ButtonDefaults.textButtonColors().copy(containerColor = conflictTheirsBlockBgColor),
            onClick = {
                prepareAcceptBlock(false, true, lineIndex, lineText)
            }
        ) {
            Text(stringResource(R.string.accept_theirs))
        }
        TextButton(
            colors = ButtonDefaults.textButtonColors().copy(containerColor = conflictSplitLineBgColor.copy(alpha = .5f)),
            onClick = {
                prepareAcceptBlock(true, true, lineIndex, lineText)
            }
        ) {
            Text(stringResource(R.string.accept_both))
        }
        TextButton(
            colors = ButtonDefaults.textButtonColors().copy(containerColor = MyStyleKt.TextColor.danger.copy(alpha = .3f)),
            onClick = {
                prepareAcceptBlock(false, false, lineIndex, lineText)
            }
        ) {
            Text(stringResource(R.string.reject_both))
        }
    }
}

