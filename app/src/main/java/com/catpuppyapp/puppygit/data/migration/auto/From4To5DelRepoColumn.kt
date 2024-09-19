package com.catpuppyapp.puppygit.data.migration.auto

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

//自动迁移数据库示例代码
@DeleteColumn(tableName = "repo", columnName = "testMigra")
class From4To5DelRepoColumn : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        super.onPostMigrate(db)
        //迁移完成后执行的操作写到这里
        //执行sql之类的，可以用 db 对象执行
    }
}