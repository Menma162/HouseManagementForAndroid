package com.example.housemanagement.activites.Flat

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.housemanagement.database.DatabaseRequests
import com.example.housemanagement.databinding.ActivityFlatItemBinding
import com.example.housemanagement.models.Flat
import com.example.housemanagement.models.Tenant
import com.example.housemanagement.recyclerview.TenantViewAdapter

class FlatItemActivity : AppCompatActivity() {
    lateinit var binding: ActivityFlatItemBinding

    private var tenants = ArrayList<Tenant>()
    private var flat: Flat = Flat()

    var id: Int = 0
    var role = ""
    lateinit var settings: SharedPreferences

    private lateinit var databaseRequests: DatabaseRequests;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlatItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        settings = getSharedPreferences("my_storage", Context.MODE_PRIVATE)
        databaseRequests =  DatabaseRequests(this@FlatItemActivity)
        fillData(intent.getIntExtra("id", 0))
        if (flat.id_tenant != null) binding.recycleViewTenants.setLayoutManager(LinearLayoutManager(this@FlatItemActivity));
        binding.update.setOnClickListener(View.OnClickListener { updateFlat() })
        binding.delete.setOnClickListener(View.OnClickListener { deleteFlat() })
        chelUser()
    }

    private fun chelUser() {
        id = settings.getInt("id", 0)
        role = settings.getString("role", "").toString()
        if(role == "tenant"){
            binding.forAdmin.visibility = View.GONE
        }
    }

    private fun fillData(id: Int) {
        flat = databaseRequests.selectFlatFromId(id)

        binding.txtVwNumberFlat.setText(flat.flat_number)
        binding.txtVwPersonalAccount.setText(flat.personal_account)
        binding.txtVwTotalArea.setText(flat.total_area.toString())
        binding.txtVwUsableArea.setText(flat.usable_area.toString())
        binding.txtVwEntranceNumber.setText(flat.entrance_number)
        binding.txtVwNumberOfRooms.setText(flat.number_of_rooms)
        binding.txtVwNumberOfResidents.setText(flat.number_of_registered_residents.toString())
        binding.txtVwNumberOfOwners.setText(flat.number_of_owners.toString())

        if(flat.id_tenant != 0 && flat.id_tenant != null){
            tenants.add(databaseRequests.selectTenantsFromId(flat.id_tenant))
            val adapter = TenantViewAdapter(tenants, this, this)
            binding.recycleViewTenants.setAdapter(adapter)
        }
    }


    private fun updateFlat(){
        val i = Intent(this@FlatItemActivity, WorkFlatActivity::class.java)
        i.putExtra("id",flat.id)
        startActivity(i)
    }

    fun OnClickBtnBack (view: View?){
        val i = Intent(this, FlatsActivity::class.java)
        startActivity(i)
    }

    private fun deleteFlat(){
        val rows = databaseRequests.selectCountFlatsFromCountersAndPayments(flat.id)
        if(rows == 0)
        {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)

            builder.setTitle("Удаление квартиры")
            builder.setMessage("Вы уверены, что хотите удалить эту квартиру?")

            builder.setPositiveButton(
                "Да",
                DialogInterface.OnClickListener { dialog, which ->
                    val cursor = databaseRequests.deleteFlat(flat.id)
                    if (cursor == -1)  Toast.makeText(this@FlatItemActivity, "Ошибка удаления в базе данных!", Toast.LENGTH_SHORT).show()
                    else {
                        Toast.makeText(this@FlatItemActivity, "Квартира удалена", Toast.LENGTH_SHORT).show()
                        val i = Intent(this, FlatsActivity::class.java)
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
//            Toast.makeText(this@FlatItemActivity, "Удаление квартиры невозможно, так как она задействована в счетчиках или начислениях!", Toast.LENGTH_SHORT).
//            show()
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Ошибка")
            builder.setNegativeButton(
                "Ок",
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })
            builder.setMessage("Удаление квартиры невозможно, так как она задействована в счетчиках или начислениях")
            builder.show()
        }
    }
}