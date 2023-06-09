package com.example.fp_v2

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fp_v2.databinding.RowAdBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterAd : RecyclerView.Adapter<AdapterAd.HolderAd>,Filterable {


    private lateinit var binding:RowAdBinding

    private companion object{
        private const val TAG="ADAPTER_AD_TAG"
    }

    private var context:Context
    var adArrayList:ArrayList<ModelAd>

    private var filterList:ArrayList<ModelAd>

    private var filter:FilterAd?=null

    private var firebaseAuth:FirebaseAuth

    constructor(context: Context, adArrayList: ArrayList<ModelAd>) {
        this.context = context
        this.adArrayList = adArrayList
        this.filterList=adArrayList

        firebaseAuth=FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAd {
        binding=RowAdBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderAd(binding.root)
    }

    override fun getItemCount(): Int {
        return adArrayList.size
    }

    override fun onBindViewHolder(holder: HolderAd, position: Int) {
        val modelAd = adArrayList[position]
        val title= modelAd.title
        val description= modelAd.description
        val address = modelAd.address
        val condition = modelAd.condition
        val price=modelAd.price
        val timestamp = modelAd.timestamp
        val formattedDate = Utils.FormatTimestampDate(timestamp)

        loadAdFirstImage(modelAd,holder)

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.addressTv.text = address
        holder.conditionTv.text = condition
        holder.priceTv.text = price
        holder.dateTv.text = formattedDate


    }

    private fun loadAdFirstImage(modelAd: ModelAd,holder: HolderAd){

        val adId=modelAd.id

        val reference=FirebaseDatabase.getInstance().getReference("Ads")
        reference.child("Images").limitToFirst(1)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val imageUrl="${ds.child("imageUrl").value}"

                        try {
                            Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_image_gray)
                                .into(holder.imageIv)
                        }catch (e:Exception){
                            Log.e(TAG,"onDataChange: ",e)
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    override fun getFilter(): Filter {
        if (filter==null){
            filter= FilterAd(this,filterList)
        }

        return filter as FilterAd
    }


    inner class HolderAd (itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imageIv = binding.imageIv
        var titleTv=binding.titleTv
        var descriptionTv = binding.descriptionTv
        var favBtn = binding.favBtn
        var addressTv = binding.addressTv
        var conditionTv= binding.conditionTv
        var priceTv=binding.priceTv
        var dateTv= binding.dateTv
        }


}






