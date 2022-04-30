package com.websarva.wings.android.mycreatures

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    // クラス内のprivate定数を宣言するためにcompanion objectブロックとする
    companion object {
        // データベースファイル名の定数
        private const val DATABASE_NAME = "plantpedia.db"
        // バージョン情報の定数
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // テーブル作成用SQL文字列の作成
        val sb = StringBuilder()
        sb.append("CREATE TABLE speicies (")
        sb.append("_id INTEGER PRIMARY KEY AUTOINCREMENT, ")
        sb.append("parentId INTEGER, ")
        sb.append("children TEXT, ") // 子のidをテキストで保存
        sb.append("name TEXT, ")
        sb.append("explanation TEXT")
        sb.append(");")
        val sql = sb.toString()

        // SQLの実行
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}