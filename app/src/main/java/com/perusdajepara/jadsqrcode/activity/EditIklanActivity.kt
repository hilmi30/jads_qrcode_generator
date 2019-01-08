package com.perusdajepara.jadsqrcode.activity

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.perusdajepara.jadsqrcode.Constant
import com.perusdajepara.jadsqrcode.Constant.Companion.WRITE_STORAGE_CODE
import com.perusdajepara.jadsqrcode.model.IklanData
import com.perusdajepara.jadsqrcode.R
import com.perusdajepara.jadsqrcode.Validasi
import kotlinx.android.synthetic.main.activity_edit_iklan.*
import kotlinx.android.synthetic.main.activity_tambah_iklan.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

        generateQrCode(rawIklan)

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

        copy_edit_btn.onClick {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("copytext", edit_raw_iklan.text)
            clipboard.primaryClip = clip

            toast("Tersalin")
        }

        edit_simpan_qrcode.onClick {
            if(ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@EditIklanActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_STORAGE_CODE)
            } else {
                alert {
                    title = "Simpan QR Code"
                    message = "Yakin ingin simpan?"
                    positiveButton(getString(R.string.ya)) {
                        saveImageToStorage()
                    }
                    negativeButton(getString(R.string.tidak)) {

                    }
                }.show()
            }
        }
    }

    private fun generateQrCode(raw: String) {
        val qrcode = QRCode.from(raw)
                .withSize(600, 600)
                .bitmap()

        qrcode_image.setImageBitmap(qrcode)
    }

    private fun saveImageToStorage() {
        doAsync {

            val idIklan = intent.getStringExtra(Constant.ID)

            val bitmap = (qrcode_image?.drawable as BitmapDrawable).bitmap
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

            val root = Environment.getExternalStorageDirectory()
            val refDir = File(root, "/JADS_QRCODES")
            refDir.mkdir()
            var fileOutputStream: FileOutputStream? = null

            try {
                val file = File(refDir, "$idIklan.jpg")
                file.createNewFile()
                fileOutputStream = FileOutputStream(file)
                fileOutputStream.write(bytes.toByteArray())

                MediaScannerConnection.scanFile(ctx, arrayOf(file.absolutePath), null, null)
                MediaStore.Images.Media.insertImage(ctx.contentResolver, bitmap, "", null)
                uiThread {
                    ctx.toast(file.absolutePath)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if(fileOutputStream != null){
                    try {
                        fileOutputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
