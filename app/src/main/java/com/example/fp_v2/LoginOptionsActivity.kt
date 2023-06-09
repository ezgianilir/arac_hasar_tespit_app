package com.example.fp_v2

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.example.fp_v2.databinding.ActivityLoginOptionsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.lang.Exception

class LoginOptionsActivity : AppCompatActivity() {

    private lateinit var binding:ActivityLoginOptionsBinding

    private companion object{
        private const val TAG="LOGIN_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private lateinit var mGoogleSignInClient:GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLoginOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Lütfen Bekleyin...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth =FirebaseAuth.getInstance()

        val gso=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)


        binding.closeBtn.setOnClickListener{
            onBackPressed()
        }

        binding.loginEmailBtn.setOnClickListener{
            startActivity(Intent(this,LoginEmailActivity::class.java))

        }

        binding.loginGoogleBtn.setOnClickListener {
            beginGoogleLogin()

        }

        binding.loginPhoneBtn.setOnClickListener {
            startActivity(Intent(this,LoginPhoneActivity::class.java))
        }


    }
    private fun beginGoogleLogin(){
        Log.d(TAG,"beingGoogleIn: ")

        val googleSignInIntent=mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInIntent)

    }
    private val googleSignInARL=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ result->
        Log.d(TAG,"googleSignInARL: ")

        if (result.resultCode== RESULT_OK){
            val data=result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {

                val account=task.getResult(ApiException::class.java)
                Log.d(TAG,"googleSignInARL: ACCOUNTID: ${account.id}")
                firebaseAuthWithGoogleAccount(account.idToken)

            }catch (e:Exception){
                Log.e(TAG,"googleSignInARL:",e)
                Utils.toast(this,"${e.message}")
            }
        }else{
            Utils.toast(this,"Engellendi...")
        }

    }

    private fun firebaseAuthWithGoogleAccount(idToken: String?) {
        Log.d(TAG,"firebaseAuthWithGoogleAccount: idToken:$idToken")

        val credential=GoogleAuthProvider.getCredential(idToken,null)

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {authResult->
                if (authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG,"firebaseAuthWithGoogleAccount: Yeni Kullanıcı Oluşturuldu")
                }else{
                    Log.d(TAG,"firebaseAuthWithGoogleAccount: Zaten kayıtlı kullanıcı, giriş yapıldı")
                    startActivity(Intent(this,MainActivity::class.java))
                    finishAffinity()
                }

             }
            .addOnFailureListener { e->
                Log.e(TAG,"firebaseAuthWithGoogleAccount: ",e)
                Utils.toast(this,"${e.message}")
            }
    }

    private fun updateUserInfoDb(){
        Log.d(TAG,"updateUserInfoDb: ")
        progressDialog.setMessage("Kullanıcı Bilgileri Kaydediliyor")
        progressDialog.show()

        val timestamp=Utils.getTimestamp()
        val registeredUserEmail=firebaseAuth.currentUser?.email
        val registeredUserUid=firebaseAuth.uid
        val name = firebaseAuth.currentUser?.displayName

        val hashMap= HashMap<String, Any>()
        hashMap["name"] = "$name"
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"]
        hashMap["profileImageUrl"]
        hashMap["dob"] = ""
        hashMap["userType"] ="Google"
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = "$registeredUserEmail"
        hashMap["uid"]="$registeredUserUid"

        val ref = FirebaseDatabase.getInstance().getReference ("Users")
        ref.child(registeredUserUid!!)
                .setValue (hashMap)
        .addOnSuccessListener {
            Log.d(TAG,  "updateUser InfoDb: User info saved")
            progressDialog.dismiss()

            startActivity(Intent(this,MainActivity::class.java))
            finishAffinity()

        }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Log.e(TAG, "updateUserInfoDb: ", e)
                Utils.toast(this, "Failed to save user info due to ${e.message}")
            }

    }


}