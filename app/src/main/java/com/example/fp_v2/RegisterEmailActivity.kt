package com.example.fp_v2

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.example.fp_v2.databinding.ActivityLoginEmailBinding
import com.example.fp_v2.databinding.ActivityRegisterEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class RegisterEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterEmailBinding

    private companion object{
        private const val TAG="LOGIN_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth=FirebaseAuth.getInstance()
        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Lütfen Bekleyiniz")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }
        binding.haveAccountTv.setOnClickListener{
            startActivity(Intent(this,LoginEmailActivity::class.java))
        }

        binding.RegisterBtn.setOnClickListener{
            validateData()
        }
    }

    private var email=""
    private var password=""
    private var cpassword=""

    private fun validateData(){
        email=binding.emailEt.text.toString().trim()
        password=binding.passwordEt.text.toString().trim()
        cpassword=binding.cpasswordEt.text.toString().trim()

        Log.d(RegisterEmailActivity.TAG,"validateData: email: $email")
        Log.d(RegisterEmailActivity.TAG,"validateData: password: $password")
        Log.d(RegisterEmailActivity.TAG,"validateData: confirm password: $cpassword")

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.error="Geçersiz E mail Formatı"
            binding.emailEt.requestFocus()
        }
        else if(password.isEmpty()){
            binding.passwordEt.error="Lütfen Parola Giriniz"
            binding.passwordEt.requestFocus()
        }else if(cpassword.isEmpty()){
            binding.cpasswordEt.error="Lütfen Parolayı Doğrulayınız"
            binding.cpasswordEt.requestFocus()
        }else if(password!=cpassword){
            binding.cpasswordEt.error="Girdiğiniz Parolalar Aynı Değil"
            binding.cpasswordEt.requestFocus()
        }
        else{
            registerUser()
        }

    }

    private fun registerUser(){
        Log.d(RegisterEmailActivity.TAG,"registerUser: ")
        progressDialog.setMessage("Hesap Oluşturuluyor")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                Log.d(RegisterEmailActivity.TAG,"registerUser: Register Success")
                updateUserInfo()

                progressDialog.dismiss()


            }
            .addOnFailureListener {e->
                Log.e(RegisterEmailActivity.TAG,"registerUser: ", e)
                progressDialog.dismiss()
                Utils.toast(this,"${e.message} Hatası Yüzünden Hesap Oluşturulamadı")
            }
    }

    private fun updateUserInfo(){
        Log.d(TAG,"updateUserInfo: ")
        progressDialog.setMessage("Kullanıcı Bilgileri Kaydediliyor")

        val timestamp = Utils.getTimestamp()
        val registeredUserEmail = firebaseAuth.currentUser!!.email
        val registeredUserUid = firebaseAuth.uid
        val hashMap= HashMap<String, Any>()
        hashMap["name"] = ""
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"]
        hashMap["profileImageUrl"]
        hashMap["dob"] = ""
        hashMap["userType"] ="Email"
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = "$registeredUserEmail"
        hashMap["uid"]="$registeredUserUid"

        val reference= FirebaseDatabase.getInstance().getReference( "Users")
        reference.child(registeredUserUid!!)
                .setValue (hashMap)
        .addOnSuccessListener {

            Log.d(TAG,  "updateUserInfo: User registered...")
            progressDialog.dismiss()
            startActivity(Intent(this,MainActivity::class.java))
            finishAffinity()
        }
            .addOnFailureListener {e ->
                Log.e(TAG, "updateUserInfo: ", e)
                progressDialog.dismiss()
                Utils.toast(this,"${e.message} hatası yüzünden kullanıcı bilgileri kaydedilemedi")
            }





    }
}