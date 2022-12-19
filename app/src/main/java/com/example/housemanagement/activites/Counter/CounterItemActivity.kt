package com.example.housemanagement.activites.Counter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.housemanagement.activites.Flat.WorkFlatActivity
import com.example.housemanagement.activites.Tenant.TenantsActivity
import com.example.housemanagement.database.DatabaseRequests
import com.example.housemanagement.databinding.ActivityCounterItemBinding
import com.example.housemanagement.databinding.ActivityTenantItemBinding
import com.example.housemanagement.models.Counter
import com.example.housemanagement.models.Flat
import com.example.housemanagement.models.Tenant
import com.google.gson.Gson

class CounterItemActivity : AppCompatActivity() {
    lateinit var binding: ActivityCounterItemBinding

    private var counter = Counter()
    var id: Int = 0
    var role = ""
    lateinit var settings: SharedPreferences
    private lateinit var databaseRequests: DatabaseRequests

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCounterItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        databaseRequests =  DatabaseRequests(this@CounterItemActivity)
        settings = getSharedPreferences("my_storage", Context.MODE_PRIVATE)

        fillData(intent.getIntExtra("id", 0))
        binding.update.setOnClickListener(View.OnClickListener {
            updateCounter()
        })
        binding.delete.setOnClickListener(View.OnClickListener {
            deleteCounter()
        })

        binding.buttonBack.setOnClickListener(View.OnClickListener {
            val i = Intent(this, CountersActivity::class.java)
            startActivity(i)
        })
        checkUser()
    }

    private fun checkUser() {
        id = settings.getInt("id", 0)
        role = settings.getString("role", "").toString()
        if(role == "tenant"){
            binding.forAdmin.visibility = View.GONE
        }
    }

    private fun deleteCounter() {
        val rows = databaseRequests.selectCountersFromIndications(counter.id)
        if(rows == 0)
        {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)

            builder.setTitle("Удаление счетчика")
            builder.setMessage("Вы уверены, что хотите удалить этот счетчик?")

            builder.setPositiveButton(
                "Да",
                DialogInterface.OnClickListener { dialog, which ->
                    val cursor = databaseRequests.deleteCounter(counter.id)
                    if (cursor == -1)  Toast.makeText(this@CounterItemActivity, "Ошибка удаления в базе данных!", Toast.LENGTH_SHORT).show()
                    else {
                        Toast.makeText(this@CounterItemActivity, "Счетчик удален", Toast.LENGTH_SHORT).show()
                        val i = Intent(this, CountersActivity::class.java)
                        startActivity(i)
                    }
                    dialog.dismiss()
                })

            builder.setNegativeButton(
                "Нет",
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })

            builder.show()
        }
        else
        {
//            Toast.makeText(this@CounterItemActivity, "Удаление счетчика невозможно, так как он задействован в показаниях!", Toast.LENGTH_LONG).
//            show()
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Ошибка")
            builder.setNegativeButton(
                "Ок",
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })
            builder.setMessage("Удаление счетчика невозможно, так как он задействован в показаниях!")
            builder.show()
        }
    }

    private fun updateCounter() {
        val i = Intent(this, WorkCounterActivity::class.java)
        i.putExtra("id", counter.id)
        startActivity(i)
    }

    private fun fillData(id: Int) {
        counter = databaseRequests.selectCounterFromId(id)

        binding.txtVwNumberFlat.text = databaseRequests.selectFlatNumberFromId(counter.id_flat)
        binding.txtVwType.text = counter.type
        binding.txtVwNumberCounter.text = counter.number
        if(counter.used == true) binding.txtVwUsed.text = "Да"
        else binding.txtVwUsed.text = "Нет"
    }
}