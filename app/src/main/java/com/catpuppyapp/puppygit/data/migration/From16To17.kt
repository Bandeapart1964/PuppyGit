package com.catpuppyapp.puppygit.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

//手动迁移数据库示例代码
//写了这个东西，忘了添加到列表，启动app，数据库被清了
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //在这写sql就行了
        database.execSQL("ALTER TABLE repo ADD COLUMN isDetached INTEGER NOT NULL DEFAULT 0")
    }
}
