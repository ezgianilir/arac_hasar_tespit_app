package com.example.fp_v2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.fp_v2.databinding.FragmentAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.checkerframework.checker.units.qual.m
import java.lang.Exception

class AccountFragment : Fragment() {

    private lateinit var binding:FragmentAccountBinding

    private companion object{
        private const val TAG="ACCOUNT_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var mContext: Context

    override fun onAttach(context: Context){
        mContext=context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentAccountBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth=FirebaseAuth.getInstance()

        loadMyInfo()

        binding.logoutCv.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(mContext,MainActivity::class.java))
            activity?.finishAffinity()
        }

        binding.editProfileCv.setOnClickListener{
            startActivity(Intent(mContext,EditProfileActivity::class.java))
        }
    }

    private fun loadMyInfo(){
        val ref =FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object:ValueEventListener{
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

                    if (timestamp=="null"){
                        timestamp="0"
                    }

                    val formattedDate=Utils.FormatTimestampDate(timestamp.toLong())


                    binding.emailTv.text=email
                    binding.nameTv.text=name
                    binding.dobTv.text=dob
                    binding.phoneTv.text=phone
                    binding.memberSinceTv.text=formattedDate


                    if (userType==email){
                        val isVerified=firebaseAuth.currentUser!!.isEmailVerified
                        if (isVerified){
                            binding.verificationTv.text="Hesap Doğrulandı"
                        }else{
                            binding.verificationTv.text="Hesap Doğrulanmadı"
                        }
                    }else{
                        binding.verificationTv.text="Hesap Doğrulandı"
                    }

                    try {
                        Glide.with(mContext)
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

}