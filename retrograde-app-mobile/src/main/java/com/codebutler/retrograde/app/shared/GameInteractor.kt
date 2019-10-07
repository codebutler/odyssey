package com.codebutler.retrograde.app.shared

import android.content.Context
import com.codebutler.retrograde.app.feature.game.GameLauncherActivity
import com.codebutler.retrograde.lib.library.db.RetrogradeDatabase
import com.codebutler.retrograde.lib.library.db.dao.updateAsync
import com.codebutler.retrograde.lib.library.db.entity.Game

class GameInteractor(private val context: Context, private val retrogradeDb: RetrogradeDatabase) {
    fun onGameClick(game: Game) {
        GameLauncherActivity.launchGame(context, game)
    }

    fun onFavoriteToggle(game: Game, isFavorite: Boolean) {
        retrogradeDb.gameDao().updateAsync(game.copy(isFavorite = isFavorite)).subscribe()
    }
}
