package com.perusdajepara.j_adsqrcodegenerator

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.tozny.crypto.android.AesCbcWithIntegrity
import se.simbio.encryption.Encryption
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    var kataString: EditText? = null
    var buatQrCodeBtn: Button? = null
    var hapusBtn: Button? = null
    var qrCodeImg: ImageView? = null
    var simpanQrCodeBtn: Button? = null
    var key: String? = null
    var salt: String? = null
    var iv: ByteArray? = null
    var encryption: Encryption? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        kataString = findViewById(R.id.string_edit_text)
        buatQrCodeBtn = findViewById(R.id.buat_qr_code_btn)
        hapusBtn = findViewById(R.id.hapus_btn)
        qrCodeImg = findViewById(R.id.qr_code_image)
        qrCodeImg?.visibility = View.GONE
        simpanQrCodeBtn = findViewById(R.id.simpan_qr_code_btn)
        simpanQrCodeBtn?.visibility = View.GONE

        key = "PerusdaJeparaAdvertiser"
        salt = "InformationInARightWay"
        iv = ByteArray(16)
        encryption = Encryption.getDefault(key, salt, iv)

        buatQrCodeBtn?.setOnClickListener {
            generateQrCode()
        }

        simpanQrCodeBtn?.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                alertSaveImage()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
            }
        }

        hapusBtn?.setOnClickListener {
            kataString?.setText(R.string.https)
            qrCodeImg?.visibility = View.GONE
            simpanQrCodeBtn?.visibility = View.GONE
        }
    }

    private fun alertSaveImage(){
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Simpan QR Code").setMessage("Yakin ingin menyimpan gambar QR Code?")
        alert.setPositiveButton("Ya", { _, _ ->
            saveImageToStorage()
        }).setNegativeButton("Tidak", { _, _ ->
        })
        alert.show()
    }

    private fun saveImageToStorage(){
        val bitmap = (qrCodeImg?.drawable as BitmapDrawable).bitmap
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

        val root = Environment.getExternalStorageDirectory()
        val refDir = File(root, "/JADS_QRCODES")
        refDir.mkdir()
        var fileOutputStream: FileOutputStream? = null

        try {
            val text = kataString?.text.toString()
            val file = File(refDir, "$text.jpg")
            file.createNewFile()
            fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(bytes.toByteArray())

            MediaScannerConnection.scanFile(this, arrayOf(file.absolutePath), null, null)
            Toast.makeText(this, file.absolutePath, Toast.LENGTH_LONG).show()
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

    private fun generateQrCode() {
        val text = kataString?.text.toString()
        val encryptText = encryption?.encryptOrNull(text)
        val finalText = "$encryptText Download Jepara Advertiser in Google Play Store"

        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(finalText, BarcodeFormat.QR_CODE, 600, 600)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            qrCodeImg?.visibility = View.VISIBLE
            qrCodeImg?.setImageBitmap(bitmap)
            simpanQrCodeBtn?.visibility = View.VISIBLE
        } catch (e: WriterException){
            e.printStackTrace()
        }
    }
}
