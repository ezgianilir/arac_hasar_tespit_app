package com.example.fp_v2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import com.example.fp_v2.databinding.FragmentHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private companion object{
        private const val TAG="HOME_TAG"

        private const val MAX_DISTANCE_TO_LOAD_ADS_KM=10
    }

    private lateinit var mContext:Context

    private lateinit var adArrayList:ArrayList<ModelAd>

    private lateinit var adapterAd: AdapterAd

    private lateinit var locationSp:SharedPreferences

    private var currentLatitude=0.0
    private var currentLongitude=0.0
    private var currentAddress=""

    override fun onAttach(context: Context) {
        mContext=context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding=FragmentHomeBinding.inflate(LayoutInflater.from(mContext),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View,savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        locationSp=mContext.getSharedPreferences("LOCATION_SP",Context.MODE_PRIVATE)

        currentLatitude=locationSp.getFloat("CURRENT_LATITUDE",0.0f).toDouble()
        currentLongitude=locationSp.getFloat("CURRENT_LONGITUDE",0.0f).toDouble()
        currentAddress=locationSp.getString("CURRENT_ADDRESS","")!!

        if (currentLatitude!=0.0 && currentLongitude!=0.0){
            binding.locationTv.text=currentAddress
        }

        loadCategories()

        loadAds("All")

        binding.searchEt.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val query=s.toString()
                    adapterAd.filter.filter(query)
                }catch (e:Exception){
                    Log.e(TAG,"onTextChanged",e)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                TODO("Not yet implemented")
            }
        })

        binding.locationCv.setOnClickListener {
            val intent= Intent(mContext,LocationPickerActivity::class.java)
            locationPickerActivityResultLauncher.launch(intent)
        }
    }

    private val locationPickerActivityResultLauncher=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result->

        if (result.resultCode== Activity.RESULT_OK){
            val data=result.data

            if(data!=null){
                currentLatitude=data.getDoubleExtra("latitude",0.0)
                currentLatitude=data.getDoubleExtra("longtitude",0.0)
                currentAddress=data.getStringExtra("adress").toString()

                locationSp.edit()
                    .putFloat("CURRENT_LATITUDE",currentLatitude.toFloat())
                    .putFloat("CURRENT_LONGITUDE",currentLongitude.toFloat())
                    .putString("CURRENT_ADDRESS",currentAddress)
                    .apply()

                binding.locationTv.text=currentAddress
                loadAds("All")
            }
        }else{
            Utils.toast(mContext,"Cancelled")
        }
    }

    private fun loadAds(category:String){
        adArrayList= ArrayList()

        val ref=FirebaseDatabase.getInstance().getReference("Ads")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                adArrayList.clear()

                for (ds in snapshot.children){
                    try {
                        val modelAd=ds.getValue(ModelAd::class.java)
                        val distance=calculateDistanceKm(modelAd?.latitude?:0.0,modelAd?.longitude?:0.0)

                        if (category=="All"){
                            if (distance<= MAX_DISTANCE_TO_LOAD_ADS_KM){
                                adArrayList.add(modelAd!!)
                            }
                        }else{
                            if (modelAd!!.category.equals(category)){
                                if (distance<= MAX_DISTANCE_TO_LOAD_ADS_KM){
                                    adArrayList.add(modelAd!!)
                                }
                            }
                        }

                    }catch (e:java.lang.Exception){
                        Log.e(TAG,"onDataChange:",e)
                    }
                }

                adapterAd=AdapterAd(mContext,adArrayList)
                binding.adsRv.adapter=adapterAd
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun calculateDistanceKm(adLatitude:Double,adLongitude:Double):Double{

        val startPoint= Location(LocationManager.NETWORK_PROVIDER)
        startPoint.latitude=currentLatitude
        startPoint.longitude=currentLongitude

        val endPoint= Location(LocationManager.NETWORK_PROVIDER)
        endPoint.latitude=adLatitude
        endPoint.longitude=adLongitude

        val distanceInMeters=startPoint.distanceTo(endPoint).toDouble()

        return distanceInMeters/1000

    }

    private fun loadCategories(){
        val categoryArrayList=ArrayList<ModelCategory>()

        for (i in 0 until Utils.categories.size){
            val modelCategory=ModelCategory(Utils.categories[i],Utils.categoryIcons[i])
            categoryArrayList.add(modelCategory)
        }
        val adapterCategory=AdapterCategory(mContext,categoryArrayList, object:RvListenerCategory{
            override fun onCategoryClick(modelCategory: ModelCategory) {

                val selectedCategory=modelCategory.category
                loadAds(selectedCategory)
            }
        })
        binding.categoriesRv.adapter=adapterCategory
    }


}