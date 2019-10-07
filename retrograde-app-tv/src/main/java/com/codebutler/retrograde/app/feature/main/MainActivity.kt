/*
 * MainActivity.kt
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

package com.codebutler.retrograde.app.feature.main

import android.Manifest
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import com.codebutler.retrograde.R
import com.codebutler.retrograde.app.feature.home.GamesGridFragment
import com.codebutler.retrograde.app.feature.home.HomeFragment
import com.codebutler.retrograde.app.feature.onboarding.OnboardingFragment
import com.codebutler.retrograde.app.feature.search.GamesSearchFragment
import com.codebutler.retrograde.lib.android.RetrogradeActivity
import com.codebutler.retrograde.lib.injection.PerActivity
import com.codebutler.retrograde.lib.injection.PerFragment
import com.tbruyelle.rxpermissions2.RxPermissions
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import dagger.Lazy
import dagger.Provides
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class MainActivity : RetrogradeActivity(), HasSupportFragmentInjector, OnboardingFragment.Listener {

    companion object {
        private const val PREF_ONBOARDING_COMPLETE = "completed_onboarding"
    }

    @Inject lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var rxPermissions: Lazy<RxPermissions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        rxPermissions.get().request(*permissions)
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(scope())
                .subscribe { granted ->
                    if (granted) {
                        loadContent()
                    } else {
                        finish()
                    }
                }
    }

    override fun onOnboardingComplete() {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putBoolean(PREF_ONBOARDING_COMPLETE, true)
        editor.apply()

        loadContent()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector

    private fun loadContent() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (prefs.getBoolean(PREF_ONBOARDING_COMPLETE, false)) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.content, HomeFragment())
                    .commit()
        } else {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.content, OnboardingFragment())
                    .commit()
        }
    }

    @dagger.Module
    abstract class Module {

        @PerFragment
        @ContributesAndroidInjector(modules = [HomeFragment.Module::class])
        abstract fun homeFragment(): HomeFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [GamesGridFragment.Module::class])
        abstract fun gamesGridFragment(): GamesGridFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [GamesSearchFragment.Module::class])
        abstract fun gamesSearchFragment(): GamesSearchFragment

        @dagger.Module
        companion object {

            @Provides
            @PerActivity
            @JvmStatic
            fun rxPermissions(activity: MainActivity): RxPermissions = RxPermissions(activity)
        }
    }
}
