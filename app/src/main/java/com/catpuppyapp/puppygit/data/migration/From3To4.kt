package com.catpuppyapp.puppygit.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

//手动迁移数据库示例代码
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //在这写sql就行了
        database.execSQL("ALTER TABLE repo ADD COLUMN testMigra Text")
    }
}
