/*
 * LocalGameLibraryProvider.kt
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

package com.codebutler.odyssey.lib.library.provider.local

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.support.v17.preference.LeanbackPreferenceFragment
import com.codebutler.odyssey.common.kotlin.calculateCrc32
import com.codebutler.odyssey.lib.R
import com.codebutler.odyssey.lib.library.GameLibraryFile
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.codebutler.odyssey.lib.library.provider.GameLibraryProvider
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File

class LocalGameLibraryProvider(private val context: Context) : GameLibraryProvider {

    override val name: String = context.getString(R.string.local_storage)

    override val uriSchemes = listOf("file")

    override val prefsFragmentClass: Class<LeanbackPreferenceFragment>? = null

    override fun listFiles(): Single<Iterable<GameLibraryFile>> = Single.fromCallable {
        Environment.getExternalStorageDirectory()
                .walk()
                .maxDepth(1)
                .filter { file -> file.isFile && file.name.startsWith(".").not() }
                .map { file -> GameLibraryFile(
                        name = file.name,
                        size = file.length(),
                        crc = file.calculateCrc32().toUpperCase(),
                        uri = Uri.parse(file.toURI().toString()))
                }
                .asIterable()
    }

    override fun getGameRom(game: Game): Single<File> = Single.fromCallable {
        File(game.fileUri.path)
    }

    override fun getGameSave(game: Game): Single<Optional<ByteArray>> {
        val saveFile = getSaveFile(game)
        return if (saveFile.exists()) {
            Single.just(saveFile.readBytes().toOptional())
        } else {
            Single.just(None)
        }
    }

    override fun setGameSave(game: Game, data: ByteArray): Completable
            = Completable.fromCallable {
        val saveFile = getSaveFile(game)
        saveFile.writeBytes(data)
    }

    private fun getSaveFile(game: Game): File {
        val odysseyDir = File(Environment.getExternalStorageDirectory(), "odyssey")
        val savesDir = File(odysseyDir, "saves")
        savesDir.mkdirs()
        return File(savesDir, game.fileName + ".sram")
    }
}
