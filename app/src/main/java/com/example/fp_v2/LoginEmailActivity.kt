package com.example.fp_v2

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.example.fp_v2.databinding.ActivityLoginEmailBinding
import com.example.fp_v2.databinding.ActivityLoginOptionsBinding
import com.google.firebase.auth.FirebaseAuth

class LoginEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginEmailBinding

    private companion object{
        private const val TAG="LOGIN_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog:ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding=ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth=FirebaseAuth.getInstance()
        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Lütfen Bekleyiniz")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }

        binding.noAccountTv.setOnClickListener{
            startActivity(Intent(this,RegisterEmailActivity::class.java))
        }

        binding.LoginBtn.setOnClickListener{
            validateData()
        }

    }

    private var email=""
    private var password=""

    private fun validateData(){
        email=binding.emailEt.text.toString()
        password=binding.passwordEt.text.toString()

        Log.d(TAG,"validateData: email: $email")
        Log.d(TAG,"validateData: password: $password")

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.error="Geçersiz E mail Formatı"
            binding.emailEt.requestFocus()
        }
        else if(password.isEmpty()){
            binding.passwordEt.error="Lütfen Parola Giriniz"
            binding.passwordEt.requestFocus()
        }
        else{
            loginUser()
        }
    }

    private fun loginUser(){
        Log.d(TAG,"loginUser: ")
        progressDialog.setMessage("Giriş Yapılıyor")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                Log.d(TAG,"logginUser: Logged In....")
                progressDialog.dismiss()

                startActivity(Intent(this,MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener {e->
                Log.e(TAG,"LoginUser: ", e)
                progressDialog.dismiss()
                Utils.toast(this,"${e.message} Hatası Yüzünden Giriş Yapılamadı")
            }
    }
}