package com.perusdajepara.jadsqrcode

class Validasi {
    companion object {
        fun checkForm(namaQR: String, idIklan: String, namaIklan: String, latIklan: String,
                      lngIklan: String, nomorTelp: String, urlIklan: String): Boolean {
            var valid = true

            if(namaQR.trim().isEmpty() || idIklan.trim().isEmpty() || namaIklan.trim().isEmpty() || latIklan.trim().isEmpty()
            || lngIklan.trim().isEmpty() || nomorTelp.trim().isEmpty() || urlIklan.trim().isEmpty()) {

                valid = false
            }

            return valid
        }
    }
}