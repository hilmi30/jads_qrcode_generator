package com.perusdajepara.jadsqrcode.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.perusdajepara.jadsqrcode.Constant
import com.perusdajepara.jadsqrcode.model.IklanData
import com.perusdajepara.jadsqrcode.R
import com.perusdajepara.jadsqrcode.Validasi
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_edit_iklan.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.ctx
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast

class EditIklanActivity : AppCompatActivity() {

    private lateinit var dataRef: DatabaseReference
    private lateinit var kategori: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_iklan)

        dataRef = FirebaseDatabase.getInstance().reference

        val namaIklan = intent.getStringExtra(Constant.NAME)
        supportActionBar?.title = namaIklan
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val idIklan = intent.getStringExtra(Constant.ID)
        val nomorIklan = intent.getStringExtra(Constant.NOMOR)
        val urlIklan = intent.getStringExtra(Constant.URL)
        val latIklan = intent.getStringExtra(Constant.LAT)
        val lngIklan = intent.getStringExtra(Constant.LNG)
        val kategoriIklan = intent.getStringExtra(Constant.KATEGORI)
        val rawIklan = intent.getStringExtra(Constant.RAW)

        val kategoriData = listOf("IKLAN", "ARTIKEL")
        val kategoriAdapter = ArrayAdapter<String>(ctx, R.layout.support_simple_spinner_dropdown_item, kategoriData)
        kategoriAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        edit_kategori_spinner.adapter = kategoriAdapter
        val itemPos = kategoriAdapter.getPosition(kategoriIklan.toUpperCase())
        edit_kategori_spinner.setSelection(itemPos)

        edit_kategori_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                kategori = parent?.getItemAtPosition(position).toString().toLowerCase()

                when(kategori) {
                    Constant.IKLAN -> {
                        edit_lat.isEnabled = true
                        edit_lng.isEnabled = true
                        edit_nomor_telp.isEnabled = true

                        edit_lat.setText(latIklan)
                        edit_lng.setText(lngIklan)
                        edit_nomor_telp.setText(nomorIklan)
                    }
                    Constant.ARTIKEL -> {

                        edit_lat.isEnabled = false
                        edit_lng.isEnabled = false
                        edit_nomor_telp.isEnabled = false

                        edit_lat.setText("0.0")
                        edit_lng.setText("0.0")
                        edit_nomor_telp.setText("not")
                    }
                }
            }
        }

        // masukkan data lama
        edit_raw_iklan.setText(rawIklan, TextView.BufferType.EDITABLE)
        edit_nama_iklan.setText(namaIklan, TextView.BufferType.EDITABLE)
        edit_nomor_telp.setText(nomorIklan, TextView.BufferType.EDITABLE)
        edit_link_iklan.setText(urlIklan, TextView.BufferType.EDITABLE)
        edit_lat.setText(latIklan, TextView.BufferType.EDITABLE)
        edit_lng.setText(lngIklan, TextView.BufferType.EDITABLE)

        edit_simpan_btn.onClick {

            val namaQRCode = edit_nama_iklan.text.toString()
            val url = edit_link_iklan.text.toString()
            val raw = edit_raw_iklan.text.toString()

            val nomor = edit_nomor_telp.text.toString()
            val lat = edit_lat.text.toString()
            val lng = edit_lng.text.toString()

            if(Validasi.checkForm(namaQRCode, idIklan.toString(), namaIklan.toString(), lat, lng, nomor, url) && !raw.isEmpty()){

                alert {
                    title = "Simpan"
                    message = "Simpan data iklan?"
                    positiveButton(getString(R.string.ya)) {
                        val data = IklanData(kategori, lat.toDouble(), lng.toDouble(), namaQRCode, nomor, url, raw)

                        val iklan = dataRef.child(Constant.DATA_IKLAN).child(idIklan)
                        iklan.setValue(data)

                        toast(getString(R.string.berhasil_simpan))
                    }
                    negativeButton(getString(R.string.tidak)) {
                    }
                }.show()
            } else {
                toast(getString(R.string.form_tidak_boleh_kosong))
            }
        }

        paste_btn.onClick {
            edit_raw_iklan.setText(Paper.book().read<String>(Constant.CLIPBOARD))
            toast(getString(R.string.berhasil_paste))
        }
    }
}
