package com.perusdajepara.jadsqrcode.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.perusdajepara.jadsqrcode.Constant
import com.perusdajepara.jadsqrcode.R
import com.perusdajepara.jadsqrcode.adapter.MainTabAdapter
import com.perusdajepara.jadsqrcode.fragment.ListIklanFragment
import com.perusdajepara.jadsqrcode.fragment.TambahIklanFragment
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.ctx


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Paper.init(ctx)
        Paper.book().delete(Constant.CLIPBOARD)

        val tabAdapter = MainTabAdapter(supportFragmentManager)
        tabAdapter.addFragment("Tambah", TambahIklanFragment())
        tabAdapter.addFragment("Iklan", ListIklanFragment())

        main_pager.adapter = tabAdapter
        main_tab.setupWithViewPager(main_pager)
    }

}
