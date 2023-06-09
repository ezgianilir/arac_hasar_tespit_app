package com.example.fp_v2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fp_v2.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var  firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth=FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser==null){
            startLoginOptions()
        }

        showHomeFragment()


        binding.bottomNv.setOnItemSelectedListener{item->

            when(item.itemId){
               R.id.menu_home ->{
                   showHomeFragment()
                   true
               }
                R.id.menu_chats ->{
                    if (firebaseAuth.currentUser==null){
                        Utils.toast(this,"Giriş Yapmanız Gerekmektedir")
                        startLoginOptions()

                        false
                    }else{
                        showChatsFragment()
                        true
                    }


                }
                R.id.menu_my_ads ->{
                    if (firebaseAuth.currentUser==null){
                        Utils.toast(this,"Giriş Yapmanız Gerekmektedir")
                        startLoginOptions()

                        false
                    }else{
                        showMyAdsFragment()
                        true
                    }
                }
                R.id.menu_account ->{
                    if (firebaseAuth.currentUser==null){
                        Utils.toast(this,"Giriş Yapmanız Gerekmektedir")
                        startLoginOptions()

                        false
                    }else{
                        showAccountFragment()
                        true
                    }
                }
                else ->{
                    false
                }

            }
        }

        binding.sellFab.setOnClickListener {
            startActivity(Intent(this,AdCreateActivity::class.java))
        }
    }

    private fun showHomeFragment(){
        binding.toolbarTitleTv.text="Anasayfa"

        val fragment=HomeFragment()
        val fragmentTransaction=supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFL.id, fragment,"HomeFragment")
        fragmentTransaction.commit()

    }
    private fun showChatsFragment(){
        binding.toolbarTitleTv.text="Mesajlar"
        val fragment=ChatsFragment()
        val fragmentTransaction=supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFL.id, fragment,"ChatsFragment")
        fragmentTransaction.commit()
    }
    private fun showMyAdsFragment(){
        binding.toolbarTitleTv.text="İlanlar"
        val fragment=MyAdsFragment()
        val fragmentTransaction=supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFL.id, fragment,"MyAdsFragment")
        fragmentTransaction.commit()
    }
    private fun showAccountFragment(){
        binding.toolbarTitleTv.text="Hesap"
        val fragment=AccountFragment()
        val fragmentTransaction=supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFL.id, fragment,"AccountFragment")
        fragmentTransaction.commit()
    }

    private fun startLoginOptions(){
        startActivity(Intent(this,LoginOptionsActivity::class.java))
    }
}