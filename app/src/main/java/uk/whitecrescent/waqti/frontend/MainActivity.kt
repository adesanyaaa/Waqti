@file:Suppress("NOTHING_TO_INLINE")

package uk.whitecrescent.waqti.frontend

import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.blank_activity.*
import kotlinx.android.synthetic.main.navigation_header.view.*
import org.jetbrains.anko.displayMetrics
import org.jetbrains.anko.textColor
import uk.whitecrescent.waqti.R
import uk.whitecrescent.waqti.addOnBackPressedCallback
import uk.whitecrescent.waqti.clearFocusAndHideKeyboard
import uk.whitecrescent.waqti.commitTransaction
import uk.whitecrescent.waqti.doInBackground
import uk.whitecrescent.waqti.doInBackgroundOnceWhen
import uk.whitecrescent.waqti.frontend.appearance.ColorScheme
import uk.whitecrescent.waqti.frontend.customview.AppBar
import uk.whitecrescent.waqti.frontend.fragments.other.AboutFragment
import uk.whitecrescent.waqti.frontend.fragments.other.SettingsFragment
import uk.whitecrescent.waqti.frontend.fragments.view.ViewBoardListFragment
import uk.whitecrescent.waqti.getViewModel
import uk.whitecrescent.waqti.invoke

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainActivityViewModel
    lateinit var waqtiPreferences: WaqtiPreferences
    val currentTouchPoint = Point()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blank_activity)

        viewModel = getViewModel()

        waqtiPreferences = WaqtiPreferences(this)

        setUpViews()

    }

    private inline fun setUpViews() {

        if (supportFragmentManager.fragments.isEmpty()) {
            supportFragmentManager.commitTransaction {
                @GoToFragment
                add(R.id.fragmentContainer, ViewBoardListFragment(), VIEW_BOARD_LIST_FRAGMENT)
            }
        }

        drawerLayout {
            addOnBackPressedCallback {
                appBar.clearFocusAndHideKeyboard()
                if (isDrawerOpen(navigationView)) {
                    closeDrawers()
                    return@addOnBackPressedCallback
                }
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else this@MainActivity.finish()
            }
        }

        navigationView.setNavigationItemSelectedListener {
            drawerLayout.closeDrawers()
            when (it.itemId) {
                R.id.allBoards_navDrawerItem -> {
                    doInBackgroundOnceWhen({ !drawerLayout.isDrawerOpen(navigationView) }, {
                        popAllFragmentsInBackStack()
                    })
                }
                R.id.settings_navDrawerItem -> {
                    doInBackgroundOnceWhen({ !drawerLayout.isDrawerOpen(navigationView) }, {
                        supportFragmentManager.commitTransaction {
                            @GoToFragment
                            replace(R.id.fragmentContainer, SettingsFragment(), SETTINGS_FRAGMENT)
                            addToBackStack(null)
                        }
                    })
                }
                R.id.about_navDrawerItem -> {
                    doInBackgroundOnceWhen({ !drawerLayout.isDrawerOpen(GravityCompat.START) }, {
                        @GoToFragment
                        supportFragmentManager.commitTransaction {
                            replace(R.id.fragmentContainer, AboutFragment(), ABOUT_FRAGMENT)
                            addToBackStack(null)
                        }
                    })
                }
            }
            true
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val spr = super.dispatchTouchEvent(event)
        currentTouchPoint.set(event.rawX.toInt(), event.rawY.toInt())
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (appBar.editTextView.isVisible) {
                val viewRect = Rect()
                appBar.editTextView.getGlobalVisibleRect(viewRect)
                if (!viewRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    appBar.editTextView.clearFocusAndHideKeyboard()
                }
            }
        }
        return spr
    }

    fun setAppBarColorScheme(colorScheme: ColorScheme) {
        appBar {
            color = colorScheme.main
            leftImage.setTint(colorScheme.text.toAndroidColor)
            editTextView.textColor = colorScheme.text.toAndroidColor
            rightImage.setTint(colorScheme.text.toAndroidColor)
        }
        window.statusBarColor = colorScheme.dark.toAndroidColor
        window.navigationBarColor = colorScheme.main.toAndroidColor

        navigationView.getHeaderView(0).apply {
            setBackgroundColor(colorScheme.main.toAndroidColor)
            navigation_header_title?.textColor = colorScheme.text.toAndroidColor
        }
    }

    inline fun resetNavBarStatusBarColor() {
        setAppBarColorScheme(ColorScheme.WAQTI_DEFAULT)
    }

    inline fun popAllFragmentsInBackStack() {
        supportFragmentManager.doInBackground {
            while (backStackEntryCount > 0) {
                popBackStackImmediate()
            }
        }
    }

    inline val appBar: AppBar
        get() = activity_appBar

    inline val dimensions: Pair<Int, Int>
        get() = this.resources
                .let { displayMetrics.widthPixels to displayMetrics.heightPixels }
}
