/*
 * GameMetadataProvider.kt
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

package com.codebutler.retrograde.lib.library.metadata

import com.codebutler.retrograde.lib.library.db.entity.Game
import com.codebutler.retrograde.lib.storage.StorageFile
import com.gojuno.koptional.Optional
import io.reactivex.ObservableTransformer

interface GameMetadataProvider {

    fun transformer(startedAtMs: Long): ObservableTransformer<StorageFile, Optional<Game>>
}
