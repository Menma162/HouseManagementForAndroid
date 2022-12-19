package com.example.housemanagement.activites.other

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.housemanagement.activites.users.LoginActivity
import com.example.housemanagement.database.DatabaseRequests
import com.example.housemanagement.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var settings: SharedPreferences;
    lateinit var binding: ActivityMainBinding
    lateinit var databaseRequests: DatabaseRequests
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        settings = getSharedPreferences("my_storage", Context.MODE_PRIVATE)
        if(settings.getBoolean("is_logged", false) == true){
            val i = Intent(this, MenuActivity::class.java)
            startActivity(i)
        }

        databaseRequests =  DatabaseRequests(this@MainActivity)

        binding.buttonLogin.setOnClickListener(View.OnClickListener { onClickBtnLogin() })
    }

    private fun onClickBtnLogin (){
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
    }
}