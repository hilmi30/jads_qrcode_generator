package com.perusdajepara.jadsqrcode.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.perusdajepara.jadsqrcode.Constant
import com.perusdajepara.jadsqrcode.activity.EditIklanActivity
import com.perusdajepara.jadsqrcode.model.IklanModel
import com.perusdajepara.jadsqrcode.R
import kotlinx.android.synthetic.main.fragment_list_iklan.*
import kotlinx.android.synthetic.main.row_list_iklan.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.uiThread

class ListIklanFragment : Fragment() {

    private lateinit var databaseRef: DatabaseReference
    var firebaseAdapter: FirebaseRecyclerAdapter<IklanModel, ListViewHolder>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_iklan, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        databaseRef = FirebaseDatabase.getInstance().reference

        showAllData()

        cari_iklan_edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = databaseRef.child(Constant.DATA_IKLAN).orderByChild(Constant.NAME).startAt(s.toString()).endAt(s.toString() + "\uf8ff")
                setFirebaseList(query)
            }

        })
    }

    private fun showAllData() {
        val query = databaseRef.child(Constant.DATA_IKLAN).orderByChild(Constant.NAME)
        setFirebaseList(query)
    }

    private fun setFirebaseList(query: Query) {
        doAsync {
            val options = FirebaseRecyclerOptions.Builder<IklanModel>()
                    .setQuery(query, IklanModel::class.java).build()

            firebaseAdapter = object : FirebaseRecyclerAdapter<IklanModel, ListViewHolder>(options) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
                    val v = LayoutInflater.from(parent.context).inflate(R.layout.row_list_iklan, parent, false)
                    return ListViewHolder(v)
                }

                override fun onBindViewHolder(holder: ListViewHolder, position: Int, model: IklanModel) {
                    val idIklan = getRef(position).key
                    holder.namaIklan.text = model.name
                    holder.editIklan.onClick {
                        startActivity<EditIklanActivity>(
                                Constant.ID to idIklan,
                                Constant.KATEGORI to model.kategori,
                                Constant.LAT to model.lat.toString(),
                                Constant.LNG to model.lng.toString(),
                                Constant.NAME to model.name,
                                Constant.NOMOR to model.nomor,
                                Constant.RAW to model.raw,
                                Constant.URL to model.url
                        )
                    }

                    holder.hapusIklan.onClick {
                        alert {
                            title = getString(R.string.hapus_iklan)
                            message = getString(R.string.yakin_hapus)
                            positiveButton(getString(R.string.ya)) {
                                databaseRef.child(Constant.DATA_IKLAN).child(idIklan).removeValue().addOnCompleteListener {
                                    if(it.isSuccessful) {
                                        toast(getString(R.string.hapus_berhasil))
                                    } else {
                                        toast(getString(R.string.terjadi_kesalahan))
                                    }
                                }
                            }
                            negativeButton(getString(R.string.tidak)) {

                            }
                        }.show()
                    }
                }

            }

            firebaseAdapter?.startListening()

            uiThread {
                list_iklan_recy.layoutManager = LinearLayoutManager(ctx)
                list_iklan_recy.setHasFixedSize(true)
                list_iklan_recy.adapter = firebaseAdapter
            }
        }
    }

    class ListViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val namaIklan = v.nama_list_iklan
        val editIklan = v.edit_iklan
        val hapusIklan = v.hapus_iklan
    }

    override fun onResume() {
        super.onResume()
        firebaseAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        firebaseAdapter?.stopListening()
    }

    override fun onStart() {
        super.onStart()
        firebaseAdapter?.startListening()
    }

}
