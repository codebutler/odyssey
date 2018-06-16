package com.codebutler.retrograde.lib.library.db.dao

import android.arch.paging.DataSource
import android.arch.persistence.db.SimpleSQLiteQuery
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.SupportSQLiteQuery
import android.arch.persistence.room.Dao
import android.arch.persistence.room.RawQuery
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import com.codebutler.retrograde.lib.library.db.entity.Game

class GameSearchDao(private val internalDao: Internal) {
    object CALLBACK : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            MIGRATION.migrate(db)
        }
    }

    object MIGRATION : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE VIRTUAL TABLE fts_games USING FTS4(
                  tokenize=unicode61 "remove_diacritics=1",
                  content="games",
                  title);
                """)
            database.execSQL("""
                CREATE TRIGGER games_bu BEFORE UPDATE ON games BEGIN
                  DELETE FROM fts_games WHERE docid=old.id;
                END;
                """)
            database.execSQL("""
                CREATE TRIGGER games_bd BEFORE DELETE ON games BEGIN
                  DELETE FROM fts_games WHERE docid=old.id;
                END;
                """)
            database.execSQL("""
                CREATE TRIGGER games_au AFTER UPDATE ON games BEGIN
                  INSERT INTO fts_games(docid, title) VALUES(new.id, new.title);
                END;
                """)
            database.execSQL("""
                CREATE TRIGGER games_ai AFTER INSERT ON games BEGIN
                  INSERT INTO fts_games(docid, title) VALUES(new.id, new.title);
                END;
                """)
            database.execSQL("""
                INSERT INTO fts_games(docid, title) SELECT id, title FROM games;
                """)
        }
    }

    fun search(query: String): DataSource.Factory<Int, Game> =
            internalDao.rawSearch(SimpleSQLiteQuery("""
                SELECT games.*
                    FROM fts_games
                    JOIN games ON games.id = fts_games.docid
                    WHERE fts_games MATCH ?
            """, arrayOf(query)))

    @Dao
    interface Internal {
        @RawQuery(observedEntities = [(Game::class)])
        fun rawSearch(query: SupportSQLiteQuery): DataSource.Factory<Int, Game>
    }
}
