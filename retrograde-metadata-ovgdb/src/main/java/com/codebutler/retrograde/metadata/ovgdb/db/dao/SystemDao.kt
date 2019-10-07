/*
 * SystemDao.kt
 *
 * Copyright (C) 2017 Retrograde Project
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

package com.codebutler.retrograde.metadata.ovgdb.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.codebutler.retrograde.metadata.ovgdb.db.entity.OvgdbSystem
import io.reactivex.Maybe

@Dao
interface SystemDao {

    @Query("SELECT * FROM systems WHERE systemID = :systemId")
    fun findById(systemId: Int): Maybe<OvgdbSystem>
}
