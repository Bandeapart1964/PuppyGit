package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CodeOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.UIHelper


private val iconShape = RoundedCornerShape(8.dp)
private val borderWidth = 2.dp

private val oursColor = UIHelper.getAcceptOursIconColor()
private val theirsColor = UIHelper.getAcceptTheirsIconColor()
private val acceptBothColor = UIHelper.getAcceptBothIconColor()
private val rejectBothColor = UIHelper.getRejectBothIconColor()


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
            icon = Icons.Filled.ChevronLeft,
            iconContentDesc = stringResource(R.string.accept_ours),
            iconColor = oursColor,
            iconModifier = Modifier.border(width = borderWidth, color = oursColor, shape = iconShape)
        ) {
            prepareAcceptBlock(true, false, lineIndex, lineText)
        }


        LongPressAbleIconBtn(
            tooltipText = stringResource(R.string.accept_theirs),
            icon = Icons.Filled.ChevronRight,
            iconContentDesc = stringResource(R.string.accept_theirs),
            iconColor = theirsColor,
            iconModifier = Modifier.border(width = borderWidth, color = theirsColor, shape = iconShape)
        ) {
            prepareAcceptBlock(false, true, lineIndex, lineText)
        }


        LongPressAbleIconBtn(
            tooltipText = stringResource(R.string.accept_both),
            icon = Icons.Filled.Code,
            iconContentDesc = stringResource(R.string.accept_both),
            iconColor = acceptBothColor,
            iconModifier = Modifier.border(width = borderWidth, color = acceptBothColor, shape = iconShape)
        ) {
            prepareAcceptBlock(true, true, lineIndex, lineText)
        }


        LongPressAbleIconBtn(
            tooltipText = stringResource(R.string.reject_both),
            icon = Icons.Filled.CodeOff,
            iconContentDesc = stringResource(R.string.reject_both),
            iconColor = rejectBothColor,
            iconModifier = Modifier.border(width = borderWidth, color = rejectBothColor, shape = iconShape)
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

