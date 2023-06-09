package com.example.fp_v2

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.util.Util
import com.example.fp_v2.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.lang.Exception

class EditProfileActivity : AppCompatActivity() {


    private lateinit var binding: ActivityEditProfileBinding

    private companion object{
        private const val TAG="PROFILE_EDIT_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var myUserType=""

    private var imageUri:Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth=FirebaseAuth.getInstance()
        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Lütfen Bekleyiniz")
        progressDialog.setCanceledOnTouchOutside(false)


        loadMyInfo()

        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }

        binding.profileImagePickFab.setOnClickListener {
            imagePickDialog()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }

    }


    private var name =""
    private var dob =""
    private var email =""
    private var phoneCode =""
    private var phoneNumber =""

    private fun validateData(){

        name =binding.nameEt.text.toString().trim()
        dob=binding.dobEt.text.toString().trim()
        email=binding.emailEt.text.toString().trim()
        phoneCode = binding.countryCodePicker.selectedCountryCodeWithPlus
        phoneNumber=binding.phoneNumberEt.text.toString().trim()

        if (imageUri==null){
            updateProfileDb("null")
        }else{
            uploadProfileImageStore()
        }

    }

    private fun uploadProfileImageStore(){
        Log.d(TAG,"uploadProfileImageStore: ")
        progressDialog.setMessage("Kullanıcı Profil Resmi Güncellendi")
        progressDialog.show()

        val filePathAndName="UserProfile/profile${firebaseAuth.uid}"

        val ref=FirebaseStorage.getInstance().reference.child(filePathAndName)
        ref.putFile(imageUri!!)
            .addOnProgressListener { snapshot->
                val progress=100.0*snapshot.bytesTransferred/snapshot.totalByteCount
                Log.d(TAG,"uploadProfileImageStore: Progress: $progress")
                progressDialog.setMessage("Profil Resmi Yüklendi")
                progressDialog.show()
            }
            .addOnSuccessListener { taskSnapshot->
                Log.d(TAG,"uploadProfileImageStore: Image uploaded")

                var uriTask=taskSnapshot.storage.downloadUrl

                while (uriTask.isSuccessful);

                val uploadedImageUrl=uriTask.result.toString()
                if (uriTask.isSuccessful){
                    updateProfileDb(uploadedImageUrl)
                }
            }
            .addOnFailureListener{e->
                Log.e(TAG,"uploadProfileImageStore: ",e)
                progressDialog.dismiss()
                Utils.toast(this,"${e.message} hatası yüzünden fotoğraf yüklenemedi")
            }

    }

    private fun updateProfileDb(uploadedImageUrl:String){
        Log.d(TAG,"updateProfileDb uploadedImageUrl: $uploadedImageUrl")

        progressDialog.setMessage("Kullancı Bilgileri Güncelleniyor")
        progressDialog.show()

        val hashMap= HashMap<String, Any>()
        hashMap["name"] = "$name"
        hashMap["dob"] = "$dob"
        hashMap["email"] = "$email"
        hashMap["phoneCode"] = "$phoneCode"
        hashMap["phoneNumber"] = "$phoneNumber"
        if (uploadedImageUrl!=null){
            hashMap["profileImgUrl"] = "$uploadedImageUrl"
        }

        if (myUserType.equals("Phone",true)){
            hashMap["email"] = "$email"
        }else if(myUserType.equals("Email",true) || myUserType.equals("Google",true)){
            hashMap["phoneCode"] = "$phoneCode"
            hashMap["phoneNumber"] = "$phoneNumber"
        }

        val reference=FirebaseDatabase.getInstance().getReference("Users")
        reference.child("${firebaseAuth.uid}")
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG,"updateProfileDb: Updated... ")
                progressDialog.dismiss()
                Utils.toast(this,"Updated")
                imageUri=null
            }
            .addOnFailureListener {e->
                Log.e(TAG,"updateProfileDb: ",e)
                progressDialog.dismiss()
                Utils.toast(this,"${e.message} hatası yüzünden fotoğraf yüklenemedi")
            }



    }


    private fun loadMyInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dob="${snapshot.child("dob").value}"
                    val email="${snapshot.child("email").value}"
                    val name="${snapshot.child("name").value}"
                    val phoneCode="${snapshot.child("phoneCode").value}"
                    val phoneNumber="${snapshot.child("phoneNumber").value}"
                    val profileImgUrl="${snapshot.child("profileImgUrl").value}"
                    var timestamp="${snapshot.child("timestamp").value}"
                    val userType="${snapshot.child("userType").value}"

                    val phone=phoneCode+phoneNumber

                    if (userType.equals("Email",true) || userType.equals("Google",true)){
                        binding.emailTil.isEnabled=false
                        binding.emailEt.isEnabled=false
                    }else{
                        binding.phoneNumberEt.isEnabled=false
                        binding.phoneNumberEt.isEnabled=false
                        binding.countryCodePicker.isEnabled=false
                    }

                    val formattedDate=Utils.FormatTimestampDate(timestamp.toLong())


                    binding.emailEt.setText(email)
                    binding.dobEt.setText(dob)
                    binding.nameEt.setText(name)
                    binding.phoneNumberEt.setText(phoneNumber)


                    try {
                        val phoneCodeInt=phoneCode.replace("+","").toInt()
                        binding.countryCodePicker.setCountryForPhoneCode(phoneCodeInt)
                    }catch (e: Exception){
                        Log.e(TAG,"onDataChange: ", e)
                    }

                    try {
                        Glide.with(this@EditProfileActivity)
                            .load(profileImgUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.profileTv)

                    }catch (e:Exception){
                        Log.e(TAG,"onDataChange: ", e)
                    }


                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


    private fun imagePickDialog(){
        val popupMenu = PopupMenu( this, binding.profileImagePickFab)
        popupMenu.menu.add(Menu.NONE, 1, 1,  "Camera")
        popupMenu.menu.add(Menu.NONE,  2,  2, "Gallery")
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->
            val itemId= item.itemId

            if (itemId == 1) {
                Log.d(TAG, "imagePickDialog: Camera Clicked, check if camera permission(s) granted or not")
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                    requestCameraPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))
                }else{
                    requestCameraPermissions.launch(arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            }
            else if(itemId == 2) {
                Log.d(TAG,"imagePickDialog: Gallery Clicked")
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                    pickImageGallery()
                }else{
                    requestStoragePermissions.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }

            }
            return@setOnMenuItemClickListener true
        }
    }

    private val requestCameraPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
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

    private val requestStoragePermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.d(TAG,  "requestCameraPermissions: result: $isGranted")

            if(isGranted){
                Log.d(TAG,  "requestCameraPermissions: All granted e.g. Camera, Storage")
                pickImageGallery()
            }else{
                Log.d(TAG, "requestCameraPermissions: All or either one is denied...:")
                Utils.toast(this,"Storage permission denied")
            }
        }

    private fun pickImageCamera(){
        Log.d(TAG, "pickImageCamera")
        val contentValues=ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE,"temp image title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"temp image description")

        imageUri=contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)




    }

    private val cameraActivityResultLauncher=
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->

            if (result.resultCode==Activity.RESULT_OK){
                Log.d(TAG,"Image captured: imageUri: $imageUri")

                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_person_white)
                        .into(binding.profileTv)

                }catch (e:Exception){
                    Log.e(TAG,"cameraActivityResultLauncher: ",e)
                }
            }else{
                Utils.toast(this,"Cancelled")
            }
        }

    private fun pickImageGallery(){
        Log.d(TAG,"pickImageGallery")
        val intent=Intent(Intent.ACTION_PICK)
        intent.type="image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher=
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->

            if (result.resultCode==Activity.RESULT_OK){
                val data =result.data
                imageUri=data!!.data


                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_person_white)
                        .into(binding.profileTv)

                }catch (e:java.lang.Exception){
                    Log.e(TAG,"galleryActivityResultLauncher: ",e)
                }
            }

        }




}