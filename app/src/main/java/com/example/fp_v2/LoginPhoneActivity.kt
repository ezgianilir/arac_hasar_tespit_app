package com.example.fp_v2

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowId
import com.example.fp_v2.databinding.ActivityLoginPhoneBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class LoginPhoneActivity : AppCompatActivity() {

    private lateinit var binding:ActivityLoginPhoneBinding

    private companion object{
        private const val TAG="PHONE_LOGIN_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var forceRefreshingToken:ForceResendingToken?= null

    private var mVerificationId: String?=null

    private lateinit var mCallBacks:OnVerificationStateChangedCallbacks


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityLoginPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phoneInputRl.visibility= View.VISIBLE
        binding.otpInputRl.visibility= View.GONE

        firebaseAuth=FirebaseAuth.getInstance()
        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Lütfen Bekleyiniz")
        progressDialog.setCanceledOnTouchOutside(false)

        phoneLoginCallBacks()

        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }

        binding.sendOtpBtn.setOnClickListener {
            validateData()
        }
        binding.resend0tpTv.setOnClickListener {
            resendVerificationCode(forceRefreshingToken)
        }
        binding.verifyOtpBtn.setOnClickListener {

            val otp =binding.otpEt.text.toString().trim()
            Log.d(TAG,"onCreate: otp: $otp")

            if (otp.isEmpty()){
                binding.otpEt.error="Lütfen doğrulama kodunu giriniz"
                binding.otpEt.requestFocus()
            }else if (otp.length<6){
                binding.otpEt.error="Doğrulama Kodu 6 Karakterden Kısa Olamaz"
                binding.otpEt.requestFocus()
            }else{
                verifyPhoneNumberWithCode(mVerificationId,otp)
            }
        }

    }

    private var phoneCode=""
    private var phoneNumber=""
    private var phoneNumberWithCode=""

    private fun validateData(){
        phoneCode=binding.phoneCodeTil.selectedCountryCodeWithPlus
        phoneNumber=binding.phoneNumberEt.text.toString().trim()
        phoneNumberWithCode=phoneCode+phoneNumber

        Log.d(TAG, "validateData: phoneCode: $phoneCode")
        Log.d(TAG, "validateData: phoneNumber: $phoneNumber")
        Log.d(TAG, "validateData: phoneNumberWithCode: $phoneNumberWithCode")

        if (phoneNumber.isEmpty()){
            binding.phoneNumberEt.error="Lütfen Telefon Numarası Giriniz"
        }else{
            startPhoneNumberVerification()
        }

    }

    private fun startPhoneNumberVerification(){
        Log.d(TAG, "validateData: startPhoneNumberVerification:")
        progressDialog.setMessage("Doğrulama Kodu $phoneNumberWithCode numarasına gönderildi")
        progressDialog.show()

        val options=PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumberWithCode)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private fun phoneLoginCallBacks() {
        Log.d(TAG,"PhoneLoginCallbacks: ")

        mCallBacks=object :OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG,"OnverificationCompleted: ")
                SignInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG,"OnverificationFailed: ",e)
                progressDialog.dismiss()
                Utils.toast(this@LoginPhoneActivity," ${e.message} ")
            }

            override fun onCodeSent(verificationId: String, token: ForceResendingToken) {
                Log.e(TAG,"OnCodeSent: verificationId: $verificationId")

                mVerificationId=verificationId
                forceRefreshingToken=token
                progressDialog.dismiss()

                binding.phoneInputRl.visibility= View.GONE
                binding.otpInputRl.visibility= View.VISIBLE

                Utils.toast(this@LoginPhoneActivity,"Doğrulama Kodu $phoneNumberWithCode gönderildi")

                binding.loginLabelTv.text="Lütfen Doğrulama Kodunu Giriniz"

            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {

            }
        }
    }



    private fun verifyPhoneNumberWithCode(verificationId: String?,otp:String){
        Log.d(TAG,  "verifyPhoneNumberWithCode: verificationId: $verificationId")
        Log.d(TAG, "verifyPhoneNumberWithCode: otp: $otp")
        progressDialog.setMessage("Verifying OTP")
        progressDialog.show()
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        SignInWithPhoneAuthCredential (credential)

    }

    private fun resendVerificationCode(token: ForceResendingToken?){
        Log.d(TAG, "resendVerificationCode: ")
        progressDialog.setMessage("Doğrulama Kodu $phoneNumberWithCode numarasına yeniden gönderildi")
        progressDialog.show()

        val options=PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumberWithCode)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBacks)
            .setForceResendingToken(token!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun SignInWithPhoneAuthCredential(credential: PhoneAuthCredential){
        Log.d(TAG,  "SignInWithPhoneAuthCredential:")
        progressDialog.setMessage("Giriş Yapılıyor")
        progressDialog.show()

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {authResult->
                Log.d(TAG,"SignInWithPhoneAuthCredential")

                if (authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG,"Yeni Kullanıcı Hesabı Oluşturuldu")
                    updateUserInfoDb()
                }else{
                    Log.d(TAG,"Zaten kayıt olmuş. Giriş Yapıldı")
                    startActivity(Intent(this,MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener {e->
                Log.e(TAG,"SignInWithPhoneAuthCredential",e)
                progressDialog.dismiss()
                Utils.toast(this," ${e.message} hatası yüzünden giriş yapılamadı")
            }

    }

    private fun updateUserInfoDb(){
        Log.d(TAG,"updateUserInfoDb")
        progressDialog.setMessage("Kullanıcı Bilgileri Kaydediliyor")
        progressDialog.show()

        val timestamp = Utils.getTimestamp()
        val registeredUserEmail = firebaseAuth.currentUser!!.email
        val registeredUserUid = firebaseAuth.uid
        val hashMap= HashMap<String, Any>()
        hashMap["name"] = ""
        hashMap["phoneCode"] = "$phoneCode"
        hashMap["phoneNumber"]="$phoneNumber"
        hashMap["profileImageUrl"]
        hashMap["dob"] = ""
        hashMap["userType"] ="Phone"
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = ""
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