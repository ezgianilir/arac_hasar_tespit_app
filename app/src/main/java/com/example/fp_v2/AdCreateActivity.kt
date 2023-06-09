package com.example.fp_v2

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import com.example.fp_v2.databinding.ActivityAdCreateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AdCreateActivity : AppCompatActivity() {

    private lateinit var binding:ActivityAdCreateBinding

    private companion object{
        private const val TAG="ADD_CREATE_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var imageUri: Uri?=null

    private lateinit var imagePickedArrayList: ArrayList<ModelImagePicked>
    private lateinit var adapterImagePicked:AdapterImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityAdCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth=FirebaseAuth.getInstance()

        val adapterCategories=ArrayAdapter<String>(this,R.layout.row_categoy_act,Utils.categories)
        binding.categoryAct.setAdapter(adapterCategories)

        val adapterConditions=ArrayAdapter<String>(this,R.layout.row_condition_act,Utils.conditions)
        binding.conditionAct.setAdapter(adapterConditions)

        imagePickedArrayList= ArrayList()

        loadImages()


        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Lütfen Bekleyiniz")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }

        binding.toolbarAdImageBtn.setOnClickListener {
            showImagePickOptions()
        }

        binding.postAdBtn.setOnClickListener {
            validateData()
        }

        binding.locationAct.setOnClickListener {
            val intent=Intent(this,LocationPickerActivity::class.java)
            LocationPickerActivityResultLauncher.launch(intent)
        }

    }

    private val LocationPickerActivityResultLauncher=
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->

            Log.d(TAG,"LocationPickerActivityResultLauncher")

            if (result.resultCode==Activity.RESULT_OK){

                val data=result.data

                if (data!=null){
                    latitude=data.getDoubleExtra("latitude",0.0)
                    longtitude=data.getDoubleExtra("longtitude",0.0)
                    adress=data.getStringExtra("adress") ?: ""

                    Log.d(TAG,"LocationPickerActivityResultLauncher:latitude: $latitude")
                    Log.d(TAG,"LocationPickerActivityResultLauncher:longtitude: $longtitude")
                    Log.d(TAG,"LocationPickerActivityResultLauncher:adress: $adress")

                    binding.locationAct.setText(adress)
                }
            }else{
                Log.d(TAG,"Cancelled")
                Utils.toast(this,"Engellendi")
            }

        }

    private fun loadImages(){
        Log.d(TAG,"loadImages")

        adapterImagePicked=AdapterImage(this,imagePickedArrayList)
        binding.imagesRv.adapter=adapterImagePicked
    }

    private fun showImagePickOptions(){
        Log.d(TAG,"showImagePickOptions")

        val popupMenu=PopupMenu(this,binding.toolbarAdImageBtn)
        popupMenu.menu.add(Menu.NONE, 1, 1,  "Camera")
        popupMenu.menu.add(Menu.NONE,  2,  2, "Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId= item.itemId

            if (itemId == 1) {
                Log.d(TAG, "imagePickDialog: Camera Clicked, check if camera permission(s) granted or not")
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                    val cameraPermissions= arrayOf(android.Manifest.permission.CAMERA)
                    requestCameraPermissions.launch(cameraPermissions)

                }else{
                    val cameraPermissions= arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestCameraPermissions.launch(cameraPermissions)
                }
            }
            else if(itemId == 2) {
                Log.d(TAG,"imagePickDialog: Gallery Clicked")
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                    pickImageGallery()
                }else{
                    val storagePermission=android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    requestStoragePermission.launch(storagePermission)
                }

            }
            true
        }
    }

    private val requestStoragePermission=registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isGranted->
        Log.d(TAG,  "requestCameraPermissions: result: $isGranted")

        if (isGranted){
            pickImageGallery()
        }else{
            Log.d(TAG, "requestStoragePermissions: All or either one is denied...:")
            Utils.toast(this,"Storage permission denied")
        }

    }

    private val requestCameraPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Log.d(TAG,  "requestCameraPermissions: result: $result")

        var areAllGranted=true
        for(isGranted in result.values){
            areAllGranted=areAllGranted && isGranted
        }

        if(areAllGranted){
            Log.d(TAG,  "requestCameraPermissions: All granted e.g. Camera, Storage")
            pickImageCamera()
        }else{
            Log.d(TAG, "requestCameraPermissions: All or either one is denied...:")
            Utils.toast(this,"Camera or Storage or both permissions denied")
        }
    }

    private fun pickImageCamera(){
        Log.d(TAG, "pickImageCamera")
        val contentValues= ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE,"temp image title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"temp image description")

        imageUri=contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery(){

        val intent= Intent(Intent.ACTION_PICK)
        intent.type="image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result->
        Log.d(TAG,"galleryActivityResultLauncher")
        if (result.resultCode==Activity.RESULT_OK){
            val data=result.data

            imageUri=data!!.data
            Log.d(TAG,"galleryActivityResultLauncher: İmageUri: $imageUri")
            val timestamp="${Utils.getTimestamp()}"

            val modelImagePicked=ModelImagePicked(timestamp,imageUri,null,false)

            imagePickedArrayList.add(modelImagePicked)

            loadImages()
        }else{
            Utils.toast(this,"cancelled")
        }


    }

    private val cameraActivityResultLauncher=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result->
        Log.d(TAG,"cameraActivityResultLauncher")
        if (result.resultCode==Activity.RESULT_OK){
            Log.d(TAG,"cameraActivityResultLauncher: İmageUri: $imageUri")
            val timestamp="${Utils.getTimestamp()}"

            val modelImagePicked=ModelImagePicked(timestamp,imageUri,null,false)

            imagePickedArrayList.add(modelImagePicked)

            loadImages()
        }else{
            Utils.toast(this,"cancelled")
        }
    }

    private var brand=""
    private var category=""
    private var condition=""
    private var adress=""
    private var price=""
    private var title=""
    private var description=""
    private var latitude=0.0
    private var longtitude=0.0

    private fun validateData(){
        brand= binding.brandEt.text.toString().trim()
        category=binding.categoryAct.text.toString().trim()
        condition= binding.conditionAct.text.toString().trim()
        adress = binding. locationAct.text.toString().trim()
        price=binding.priceEt.text.toString().trim()
        title = binding.titleEt.text.toString().trim()
        description=binding.descriptionEt.text.toString().trim()

        if (brand.isEmpty()){
            binding.brandEt.error="Marka Giriniz"
            binding.brandEt.requestFocus()
        }else if(category.isEmpty()){
            binding.categoryAct.error="Kategori seçiniz"
            binding.categoryAct.requestFocus()
        }else if(condition.isEmpty()){
            binding.conditionAct.error="Şart seçiniz"
            binding.conditionAct.requestFocus()
        }else if(title.isEmpty()){
            binding.titleEt.error="Şart seçiniz"
            binding.titleEt.requestFocus()
        }else if(description.isEmpty()){
            binding.descriptionEt.error="Şart seçiniz"
            binding.descriptionEt.requestFocus()
        }else{
            postAd()
        }
    }

    private fun postAd(){
        Log.d(TAG,"postAd")
        progressDialog.setMessage("İlan Yayınlanıyor...")
        progressDialog.show()

        val timestamp=Utils.getTimestamp()

        val refAds=FirebaseDatabase.getInstance().getReference("Ads")

        val keyId=refAds.push().key

        val hashMap=HashMap<String,Any>()
        hashMap["id"]="$keyId"
        hashMap["uid"]="${firebaseAuth.uid}"
        hashMap["brand"]="$brand"
        hashMap["category"]="$category"
        hashMap["condition"]="$condition"
        hashMap["adress"]="$adress"
        hashMap["price"]="$price"
        hashMap["title"]="$title"
        hashMap["description"]="$description"
        hashMap["status"]="${Utils.AD_STATUS_AVALIABLE}"
        hashMap["timestamp"]=timestamp
        hashMap["latitude"]=latitude
        hashMap["longtitude"]=longtitude


        refAds.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG,"postAd published")
                uploadImages(keyId)
            }
            .addOnFailureListener {
                Log.d(TAG,"postAd")
                progressDialog.dismiss()
            }

    }

    private fun uploadImages(adId:String){

        for(i in imagePickedArrayList.indices){
            val modelImagePicked=imagePickedArrayList[i]
            val imageName=modelImagePicked.id

            val fileNameAndPath="Ads/$imageName"
            val imageIndexForProgress=i+1

            val storageReference=FirebaseStorage.getInstance().getReference(fileNameAndPath)
            storageReference.putFile(modelImagePicked.imageUri!!)
                .addOnProgressListener { snapshot->
                    val progress=100.0*snapshot.bytesTransferred/snapshot.totalByteCount
                    Log.d(TAG,"uploadProfileImageStore: Progress: $progress")
                    val message="Uploading image"

                    progressDialog.setMessage(message)
                    progressDialog.show()
                }
                .addOnSuccessListener { taskSnapshot->
                    Log.d(TAG,"uploadProfileImageStore: onSuccess")
                    val uriTask=taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val uploadImageUrl=uriTask.result

                    if (uriTask.isSuccessful){
                        val hashMap=HashMap<String,Any>()
                        hashMap["id"]="${modelImagePicked.id}"
                        hashMap["imageUrl"]="$uploadImageUrl"

                        val ref =FirebaseDatabase.getInstance().getReference("Ads")
                        ref.child(adId).child("Images")
                            .child(imageName)
                            .updateChildren(hashMap)

                    }
                    progressDialog.dismiss()
                }
                .addOnFailureListener{e->
                    Log.e(TAG,"uploadImagesStorage: ",e)
                    progressDialog.dismiss()

                }
        }

    }

}