package com.example.housemanagement.activites.Flat

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.housemanagement.activites.Counter.CountersActivity
import com.example.housemanagement.activites.Counter.WorkCounterActivity
import com.example.housemanagement.database.DatabaseRequests
import com.example.housemanagement.databinding.ActivityListFlatsBinding
import com.example.housemanagement.models.Flat
import com.example.housemanagement.models.Tenant
import com.google.gson.Gson

class ListFlatsActivity : AppCompatActivity() {
    lateinit var binding: ActivityListFlatsBinding

    private var flats = ArrayList<Flat>()
    private lateinit var databaseRequests: DatabaseRequests
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListFlatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRequests =  DatabaseRequests(this@ListFlatsActivity)

        fillData()

        binding.buttonThen.setOnClickListener(View.OnClickListener {
            val i = Intent(this, WorkCounterActivity::class.java)
            i.putExtra("id_flat", flats[binding.listFlats.checkedItemPosition].id)
            startActivity(i)
        })

        binding.buttonBack.setOnClickListener(View.OnClickListener {
            val i = Intent(this, CountersActivity::class.java)
            startActivity(i)
        })

        binding.serchViewFlat.setOnClickListener(View.OnClickListener {
            searchFlat()
        })
    }

    private fun searchFlat() {
        val newFlats = flats.filter { it.flat_number.contains(binding.serchViewFlat.query)  } as ArrayList<Flat>
        var values = emptyArray<String>()
        for (i in 0..newFlats.size - 1){
            values += ("Номер квартиры: " + newFlats[i].flat_number + ",\nЛицевой счет: " + newFlats[i].personal_account)
        }

        val adapter = ArrayAdapter(this@ListFlatsActivity, R.layout.simple_list_item_single_choice, values)
        binding.listFlats.choiceMode = ListView.CHOICE_MODE_SINGLE
        binding.listFlats.adapter = adapter
    }

    private fun fillData() {
        flats = databaseRequests.selectFlats()
        var values = emptyArray<String>()
        for (i in 0..flats.size - 1){
            values += ("Номер квартиры: " + flats[i].flat_number + ",\nЛицевой счет: " + flats[i].personal_account)
        }

        val adapter = ArrayAdapter(this@ListFlatsActivity, R.layout.simple_list_item_single_choice, values)
        binding.listFlats.choiceMode = ListView.CHOICE_MODE_SINGLE
        binding.listFlats.adapter = adapter

        val id_tenant = intent.getIntExtra("id_tenant", 0)
        if(id_tenant != 0){
            binding.listFlats.setItemChecked(flats.indexOfFirst { it.id == id_tenant }, true)
        }
        else{
            binding.listFlats.setItemChecked(0, true)
        }
        if(flats.size == 0) binding.buttonThen.isEnabled = false
    }
}