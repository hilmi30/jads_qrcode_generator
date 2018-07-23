package com.perusdajepara.jadsqrcode.fragment

import android.Manifest
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.fragment_tambah_data.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import se.simbio.encryption.Encryption
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class TambahIklanFragment : Fragment() {

    var iv: ByteArray? = null
    var encryption: Encryption? = null
    private lateinit var namaQR: String
    var finalText: String? = null
    private lateinit var databaseRef: DatabaseReference

    private lateinit var kategori: String
    private lateinit var idIklan: String
    private lateinit var namaIklan: String
    private lateinit var latIklan: String
    private lateinit var lngIklan: String
    private lateinit var nomorTelp: String
    private lateinit var urlIklan: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tambah_data, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        visibility(View.GONE)
        setKategoriSpinner()

        databaseRef = FirebaseDatabase.getInstance().reference

        iv = ByteArray(16)
        encryption = Encryption.getDefault(Constant.KEY, Constant.SALT, iv)

        buat_qr_code_btn?.setOnClickListener {
            generateQrCode()
        }

        copy_btn.onClick {
            Paper.book().write(Constant.CLIPBOARD, finalText)
            toast(getString(R.string.berhasil_copy))
        }

        // simpan qrcode listener
        simpan_qr_code_btn?.setOnClickListener {
            if(ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){

                namaQR = nama_qrcode.text.toString()

                idIklan = id_iklan.text.toString()
                namaIklan = nama_iklan.text.toString()
                urlIklan = link_iklan.text.toString()

                latIklan = lat.text.toString()
                lngIklan = lng.text.toString()
                nomorTelp = nomor_telp.text.toString()

                if(Validasi.checkForm(namaQR, idIklan, namaIklan, latIklan, lngIklan, nomorTelp, urlIklan)){
                    alertSaveImage()
                } else {
                    ctx.toast(getString(R.string.form_tidak_boleh_kosong))
                }
            } else {
                ActivityCompat.requestPermissions(ctx as Activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
            }
        }

        hapus_btn?.setOnClickListener {
            id_iklan?.text?.clear()
            nama_qrcode?.text?.clear()
            nama_iklan.text.clear()
            lat.text.clear()
            lng.text.clear()
            nomor_telp.text.clear()
            link_iklan.text.clear()
            raw_qrcode.text.clear()

            visibility(View.GONE)
        }
    }

    private fun setKategoriSpinner() {
        val kategoriData = listOf("IKLAN", "ARTIKEL")
        val adapterSpinner = ArrayAdapter<String>(ctx, R.layout.support_simple_spinner_dropdown_item, kategoriData)
        adapterSpinner.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        kategoriSpinner.adapter = adapterSpinner

        kategoriSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                kategori = parent?.getItemAtPosition(position).toString().toLowerCase()

                when (kategori) {
                    Constant.IKLAN -> {
                        lat.isEnabled = true
                        lng.isEnabled = true
                        nomor_telp.isEnabled = true

                        lat.setText("")
                        lng.setText("")
                        nomor_telp.setText("")
                    }
                    Constant.ARTIKEL -> {
                        lat.isEnabled = false
                        lng.isEnabled = false
                        nomor_telp.isEnabled = false

                        lat.setText("0.0")
                        lng.setText("0.0")
                        nomor_telp.setText("not")
                    }
                }
            }

        }
    }

    private fun visibility(v: Int) {
        qr_code_image?.visibility = v
        simpan_qr_code_btn?.visibility = v
        nama_qrcode?.visibility = v
        nama_qrcode?.text = id_iklan.text
        kategoriSpinner.visibility = v
        nama_iklan.visibility = v
        lat.visibility = v
        lng.visibility = v
        nomor_telp.visibility = v
        link_iklan.visibility = v
        katText.visibility = v
        raw_qrcode.visibility = v
        raw_text.visibility = v
        raw_qrcode.setText(finalText, TextView.BufferType.EDITABLE)

        copy_btn.visibility = v
    }

    private fun alertSaveImage(){
        ctx.alert {
            title = getString(R.string.simpal_qr_code)
            message = getString(R.string.yakin_simpan_qr)
            positiveButton(getString(R.string.ya)){
                saveImageToStorage()
                saveAdsDataToFirebase()
            }
            negativeButton(getString(R.string.tidak)){
                it.dismiss()
            }
        }.show()
    }

    private fun saveAdsDataToFirebase() {

        val data = IklanData(kategori, latIklan.toDouble(), lngIklan.toDouble(),
                namaIklan, nomorTelp, urlIklan, finalText.toString())

        val dataIklan = databaseRef.child(Constant.DATA_IKLAN).child(idIklan)
        dataIklan.setValue(data)

    }

    private fun saveImageToStorage() {
        doAsync {
            val bitmap = (qr_code_image?.drawable as BitmapDrawable).bitmap
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

            val root = Environment.getExternalStorageDirectory()
            val refDir = File(root, "/JADS_QRCODES")
            refDir.mkdir()
            var fileOutputStream: FileOutputStream? = null

            try {
                val file = File(refDir, "$namaQR.jpg")
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

    private fun generateQrCode() {
        doAsync {
            val text = id_iklan?.text.toString()
            val encryptText = encryption?.encryptOrNull(text)
            finalText = "$encryptText Download Jepara Advertiser di Google Play Store"

            val qrcode = QRCode.from(finalText)
                    .withSize(600, 600)
                    .bitmap()

            uiThread {
                qr_code_image?.setImageBitmap(qrcode)
                visibility(View.VISIBLE)
            }
        }
    }
}
