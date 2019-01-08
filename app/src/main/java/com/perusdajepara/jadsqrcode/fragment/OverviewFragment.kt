package com.perusdajepara.jadsqrcode.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.perusdajepara.jadsqrcode.Constant
import com.perusdajepara.jadsqrcode.OverviewModel

import com.perusdajepara.jadsqrcode.R
import com.perusdajepara.jadsqrcode.activity.TambahEditOverviewActivity
import kotlinx.android.synthetic.main.fragment_overview.*
import kotlinx.android.synthetic.main.row_overview.view.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast


class OverviewFragment : Fragment() {

    var mDatabase: DatabaseReference? = null
    var firebaseAdapter: FirebaseRecyclerAdapter<OverviewModel, ViewHolder>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_overview, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mDatabase = FirebaseDatabase.getInstance().reference

        val query = mDatabase?.child(Constant.EDISI_BULAN_INI)?.orderByChild("item")

        val options = FirebaseRecyclerOptions.Builder<OverviewModel>()
                .setQuery(query!!, OverviewModel::class.java).build()

        firebaseAdapter = object : FirebaseRecyclerAdapter<OverviewModel, ViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.row_overview, parent, false)
                return ViewHolder(v)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int, model: OverviewModel) {
                holder.nameOverview.text = model.item

                val key = getRef(position).key

                holder.editOverview.onClick {
                    startActivity<TambahEditOverviewActivity>(
                            Constant.KEY to key,
                            Constant.ITEM to model.item
                    )
                }

                holder.hapusOverview.onClick {
                    alert {
                        title = "Hapus Item"
                        message = "Yakin ingin menghapus item?"
                        positiveButton(getString(R.string.ya)) {
                            mDatabase?.child(Constant.EDISI_BULAN_INI)?.child(key)?.removeValue()?.addOnCompleteListener {
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

        overview_recy.layoutManager = LinearLayoutManager(ctx)
        overview_recy.adapter = firebaseAdapter

    }

    class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val nameOverview = v.overview_name
        val editOverview = v.overview_edit
        val hapusOverview = v.overview_hapus
    }

    override fun onStop() {
        super.onStop()
        firebaseAdapter?.stopListening()
    }

    override fun onResume() {
        super.onResume()
        firebaseAdapter?.startListening()
    }

    override fun onStart() {
        super.onStart()
        firebaseAdapter?.startListening()
    }
}
