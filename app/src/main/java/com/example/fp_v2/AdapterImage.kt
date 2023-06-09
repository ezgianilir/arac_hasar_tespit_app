package com.example.fp_v2

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.fp_v2.databinding.ActivityLoginPhoneBinding
import com.example.fp_v2.databinding.RowImagesPickedBinding
import java.lang.Exception

class AdapterImage(
    private val context:Context,
    private val imagesPickedArraylist:ArrayList<ModelImagePicked>
    ) : Adapter<AdapterImage.HolderImagePicked>() {

    private lateinit var binding: RowImagesPickedBinding

    private companion object{
        private const val TAG="IMAGES TAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagePicked {
        binding=RowImagesPickedBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderImagePicked(binding.root)

    }

    override fun getItemCount(): Int {
        return imagesPickedArraylist.size
    }

    override fun onBindViewHolder(holder: HolderImagePicked, position: Int) {

        val model=imagesPickedArraylist[position]

        val imageUri=model.imageUri
        Log.d(TAG,"onBindViewHolder: imageUri: $imageUri")

        try {
            Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.ic_image_gray)
                .into(holder.imageTv)
        }catch (e:Exception){
            Log.e(TAG,"onBindViewHolder: ",e)
        }

        holder.closeBtn.setOnClickListener {
            imagesPickedArraylist.remove(model)
            notifyDataSetChanged()
        }

    }


    inner class HolderImagePicked(itemView: View): ViewHolder(itemView){

        var imageTv=binding.ImageTv
        var closeBtn=binding.closeBtn

    }




}