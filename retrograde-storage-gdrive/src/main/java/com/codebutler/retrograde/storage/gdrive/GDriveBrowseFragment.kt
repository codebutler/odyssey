/*
 * GDriveBrowseFragment.kt
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.retrograde.storage.gdrive

import android.content.Context
import android.os.Bundle
import android.support.v17.leanback.app.BrowseSupportFragment
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.ImageCardView
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.Presenter
import android.view.ViewGroup
import com.codebutler.retrograde.lib.ui.SimpleItem
import com.codebutler.retrograde.lib.ui.SimpleItemPresenter
import com.google.api.services.drive.model.File
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class GDriveBrowseFragment : BrowseSupportFragment() {

    companion object {
        private const val FOLDER_ID_ROOT = "root"
    }

    private object UseFolderItem : SimpleItem(R.string.gdrive_use_this_folder)

    @Inject lateinit var gdriveBrowser: GDriveBrowser

    private val navigationStack = mutableListOf<String>()

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setOnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is File -> navigateDownTo(item.id)
                is UseFolderItem -> {
                    val currentFolderId = navigationStack[navigationStack.lastIndex]
                    (activity as Listener).onGDriveFolderSelected(currentFolderId)
                }
            }
        }

        title = getString(R.string.gdrive_select_folder)
        headersState = HEADERS_DISABLED

        navigateDownTo(FOLDER_ID_ROOT)
    }

    fun onBackPressed() = navigateUp()

    private fun navigateDownTo(folderId: String) {
        navigationStack.add(folderId)
        browseFolder(folderId)
    }

    private fun navigateUp(): Boolean {
        navigationStack.removeAt(navigationStack.lastIndex)
        if (navigationStack.isEmpty()) {
            return false
        }
        browseFolder(navigationStack.last())
        return true
    }

    private fun browseFolder(parentId: String) {
        progressBarManager.show()
        gdriveBrowser.list(parentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(scope())
                .subscribe({ files ->
                    val folders = files.filter { file -> file.mimeType == "application/vnd.google-apps.folder" }
                    val foldersAdapter = ArrayObjectAdapter(FilePresenter())
                    foldersAdapter.addAll(0, folders)

                    val actionsAdapter = ArrayObjectAdapter(SimpleItemPresenter())
                    actionsAdapter.add(UseFolderItem)

                    val categoryRowAdapter = ArrayObjectAdapter(ListRowPresenter())
                    categoryRowAdapter.add(ListRow(actionsAdapter))
                    categoryRowAdapter.add(ListRow(foldersAdapter))

                    adapter = categoryRowAdapter

                    progressBarManager.hide()
                }, { error ->
                    Timber.e(error)
                    (activity as Listener).onGDriveError(error)
                })
    }

    private class FilePresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            val resources = parent.context.resources

            val padding = resources.getDimensionPixelSize(R.dimen.gdrive_padding_folder)
            val width = resources.getDimensionPixelSize(R.dimen.card_width)
            val height = resources.getDimensionPixelSize(R.dimen.card_height)

            val cardView = ImageCardView(parent.context)
            cardView.isFocusable = true
            cardView.isFocusableInTouchMode = true
            cardView.setMainImageDimensions(width, height)
            cardView.mainImageView.setPadding(padding, padding, padding, padding)
            cardView.mainImage = resources.getDrawable(R.drawable.ic_folder_white_48dp, parent.context.theme)

            return Presenter.ViewHolder(cardView)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
            val cardView = viewHolder.view as ImageCardView
            val file = item as File
            cardView.titleText = file.name
        }

        override fun onUnbindViewHolder(viewHolder: ViewHolder?) = Unit
    }

    interface Listener {
        fun onGDriveFolderSelected(folderId: String)
        fun onGDriveError(error: Throwable)
    }
}
