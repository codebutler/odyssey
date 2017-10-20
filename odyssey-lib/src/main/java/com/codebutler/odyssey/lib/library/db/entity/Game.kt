/*
 * Game.kt
 *
 * Copyright (C) 2017 Odyssey Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.odyssey.lib.library.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.net.Uri
import android.support.v7.recyclerview.extensions.DiffCallback

@Entity(
        tableName = "games",
        indices = arrayOf(
                Index("id", unique = true),
                Index("fileUri", unique = true),
                Index( "title"),
                Index("systemId"),
                Index("lastIndexedAt"),
                Index("lastPlayedAt"),
                Index("isFavorite")
        ))
data class Game(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,
        val fileName: String,
        val fileUri: Uri,
        val title: String,
        val systemId: String,
        val developer: String?,
        val coverFrontUrl: String?,
        val lastIndexedAt: Long,
        val lastPlayedAt: Long? = null,
        val isFavorite: Boolean = false) {

        companion object {
                val DIFF_CALLBACK = object : DiffCallback<Game>() {
                        override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
                                return oldItem.id == newItem.id
                        }

                        override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
                                return oldItem == newItem
                        }
                }
        }
}
