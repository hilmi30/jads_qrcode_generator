package com.perusdajepara.jadsqrcode.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.perusdajepara.jadsqrcode.Constant
import com.perusdajepara.jadsqrcode.R
import kotlinx.android.synthetic.main.activity_tambah_overview.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast

class TambahEditOverviewActivity : AppCompatActivity() {

    var mDatabase: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_overview)

        val item = intent.getStringExtra(Constant.ITEM)
        val key = intent.getStringExtra(Constant.KEY)

        overview_edt.setText(item)

        supportActionBar?.title = getString(R.string.tambah_overview)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mDatabase = FirebaseDatabase.getInstance().reference

        overview_btn.onClick {

            if(overview_edt.text.isNotEmpty()) {

                alert {
                    title = getString(R.string.simpan)
                    message = getString(R.string.simpan_item)
                    positiveButton(getString(R.string.ya)) {

                        val edisiRef = if(key.isNullOrEmpty()) {
                            mDatabase?.child(Constant.EDISI_BULAN_INI)?.push()
                        } else {
                            mDatabase?.child(Constant.EDISI_BULAN_INI)?.child(key)
                        }

                        edisiRef?.child(Constant.ITEM)?.setValue(overview_edt.text.toString())?.addOnCompleteListener {
                            if(it.isSuccessful) {
                                toast(getString(R.string.berhasil_menyimpan))
                            } else {
                                toast(getString(R.string.terjadi_kesalahan))
                            }
                        }

                    }
                    negativeButton(getString(R.string.tidak)) {

                    }
                }.show()
            } else {
                toast(getString(R.string.item_kosong))
            }
        }

    }
}
