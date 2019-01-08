package com.perusdajepara.jadsqrcode.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.perusdajepara.jadsqrcode.Constant
import com.perusdajepara.jadsqrcode.R
import com.perusdajepara.jadsqrcode.adapter.MainTabAdapter
import com.perusdajepara.jadsqrcode.fragment.ListIklanFragment
import com.perusdajepara.jadsqrcode.fragment.OverviewFragment
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.startActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Paper.init(ctx)
        Paper.book().delete(Constant.CLIPBOARD)

        val tabAdapter = MainTabAdapter(supportFragmentManager)
        // tabAdapter.addFragment("Tambah", TambahIklanFragment())
        tabAdapter.addFragment("Iklan", ListIklanFragment())
        tabAdapter.addFragment("Overview", OverviewFragment())

        main_pager.adapter = tabAdapter
        main_pager.offscreenPageLimit = 2
        main_tab.setupWithViewPager(main_pager)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId) {
            R.id.tambah_iklan -> {
                startActivity<TambahIklanActivity>()
            }
            R.id.tambah_overview -> {
                startActivity<TambahEditOverviewActivity>()
            }
            else -> {
                finish()
            }
        }

        return true
    }

}
