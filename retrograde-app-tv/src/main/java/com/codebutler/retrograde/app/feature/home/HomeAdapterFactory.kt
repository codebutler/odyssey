/*
 * HomeAdapterFactory.kt
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

package com.codebutler.retrograde.app.feature.home

import android.content.Context
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.paging.LivePagedListBuilder
import com.codebutler.retrograde.R
import com.codebutler.retrograde.app.shared.GamePresenter
import com.codebutler.retrograde.app.shared.ui.ItemViewLongClickListener
import com.codebutler.retrograde.app.shared.ui.PagedListObjectAdapter
import com.codebutler.retrograde.lib.library.GameSystem
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import com.codebutler.retrograde.lib.library.db.dao.GameLibraryCounts
import com.codebutler.retrograde.lib.library.db.entity.Game
import com.codebutler.retrograde.lib.ui.SimpleItem
import com.codebutler.retrograde.lib.ui.SimpleItemPresenter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class HomeAdapterFactory(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val retrogradeDb: RetrogradeDatabase,
    longClickListener: ItemViewLongClickListener
) {

    data class GameSystemItem(val system: GameSystem) : SimpleItem(system.titleResId, system.imageResId)
    object HelpItem : SimpleItem(R.string.help, R.drawable.ic_help_outline_white_64dp)
    object RescanItem : SimpleItem(R.string.rescan, R.drawable.ic_refresh_white_64dp)
    object AllGamesItem : SimpleItem(R.string.all_games, R.drawable.ic_all_games_white_64dp)
    object SettingsItem : SimpleItem(R.string.settings, R.drawable.ic_settings_white_64dp)
    object NoGamesItem : SimpleItem(R.string.no_games, R.drawable.ic_no_games_white_64dp)

    private val gamePresenter = GamePresenter(context, longClickListener)

    fun buildFavoritesAdapter(): PagedListObjectAdapter<Game> {
        val favoritesAdapter = PagedListObjectAdapter(gamePresenter, Game.DIFF_CALLBACK)
        LivePagedListBuilder(retrogradeDb.gameDao().selectFavorites(), 50)
                .build()
                .observe(lifecycleOwner, Observer {
                    pagedList -> favoritesAdapter.pagedList = pagedList
                })
        return favoritesAdapter
    }

    fun buildRecentsAdapter(): PagedListObjectAdapter<Game> {
        val recentsAdapter = PagedListObjectAdapter(gamePresenter, Game.DIFF_CALLBACK)
        LivePagedListBuilder(retrogradeDb.gameDao().selectRecentlyPlayed(), 50)
                .build()
                .observe(lifecycleOwner, Observer {
                    pagedList -> recentsAdapter.pagedList = pagedList
                })
        return recentsAdapter
    }

    fun buildSystemsAdapter(counts: GameLibraryCounts): ArrayObjectAdapter {
        val systemsAdapter = ArrayObjectAdapter(SimpleItemPresenter(context))
        if (counts.totalCount == 0L) {
            systemsAdapter.add(NoGamesItem)
        } else {
            retrogradeDb.gameDao().selectSystems()
                    .flattenAsObservable { it }
                    .map { GameSystem.findById(it)!! }
                    .toSortedList { o1, o2 -> o1.sortKey.compareTo(o2.sortKey) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .autoDisposable(lifecycleOwner.scope())
                    .subscribe { systems ->
                        systemsAdapter.clear()
                        systemsAdapter.addAll(0, systems.map { system -> GameSystemItem(system) })
                        systemsAdapter.add(AllGamesItem)
                    }
        }
        return systemsAdapter
    }

    fun buildSettingsAdapter(): ArrayObjectAdapter {
        val settingsAdapter = ArrayObjectAdapter(SimpleItemPresenter(context))
        settingsAdapter.add(SettingsItem)
        settingsAdapter.add(RescanItem)
        settingsAdapter.add(HelpItem)
        return settingsAdapter
    }
}
