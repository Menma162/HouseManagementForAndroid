package com.example.housemanagement.activites.Counter

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.housemanagement.R
import com.example.housemanagement.activites.Flat.FlatItemActivity
import com.example.housemanagement.activites.Flat.ListFlatsActivity
import com.example.housemanagement.database.DatabaseRequests
import com.example.housemanagement.databinding.ActivityWorkCounterBinding
import com.example.housemanagement.models.Counter
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class WorkCounterActivity : AppCompatActivity() {
    lateinit var binding: ActivityWorkCounterBinding
    lateinit var counter: Counter
    lateinit var types: Array<String>

    private lateinit var databaseRequests: DatabaseRequests;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkCounterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        databaseRequests = DatabaseRequests(this@WorkCounterActivity )
        types = resources.getStringArray(R.array.typesCounter)
        fillData(intent.getIntExtra("id", 0), intent.getIntExtra("id_flat", 0))

        binding.buttonBack.setOnClickListener(View.OnClickListener {
            if(counter.id == 0 || counter.id == null){
                val i = Intent(this, ListFlatsActivity::class.java)
                i.putExtra("id_tenant", intent.getIntExtra("id_flat", 0))
                startActivity(i)
        } else{
            val i = Intent(this, CounterItemActivity::class.java)
            i.putExtra("id", counter.id)
            startActivity(i)
        } })
        binding.buttonWork.setOnClickListener(View.OnClickListener { workCounter() })
    }

    private fun workCounter() {
        counter.number = binding.editTextNumberCounter.text.toString()
        counter.type = types.get(types.indexOfFirst {  it.equals(binding.spinnerType.selectedItem)})
        counter.used = binding.checkUsed.isChecked

        var error: String? = null

        if(counter.number.length < 6) error = "number"

        var count = 0
        if (counter.id != null && counter.id != 0) {
            count += databaseRequests.selectCountCountersWhereNumberAndTypeNotId(counter.id, counter.number, counter.type)
        }
        else {
            count += databaseRequests.selectCountCountersWhereNumberAndType(counter.number, counter.type)
        }
        if(count != 0) error = "count"

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setTitle("Ошибка")
        builder.setNegativeButton(
            "Ок",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })

        if(error == null)
        {
            if(counter.id != null && counter.id != 0) databaseRequests.updateCounter(counter)
            else databaseRequests.createCounter(counter)
            transition()
        }
        else{
            when(error){
                "count" ->
                    builder.setMessage("Счетчик такого типа с таким номером уже существует.")
                "number" ->
                    builder.setMessage("Неверный ввод номера счетчика: минимальное количество символов строки - 6.")
            }
            builder.show()
        }
    }

    private fun transition() {
        if(counter.id == 0 || counter.id == null){
                val i = Intent(this, CountersActivity::class.java)
                startActivity(i)
        } else{
            val i = Intent(this, CounterItemActivity::class.java)
            i.putExtra("id", counter.id)
            startActivity(i)
        }
    }

    private fun fillData(id: Int, id_flat: Int) {
        if(id == 0){
            counter = Counter()
            counter.id_flat = id_flat
            binding.editTextNumberFlat.setText(databaseRequests.selectFlatNumberFromId(id_flat))
            binding.textView.text = "Добавление"
            binding.buttonWork.text = "Добавить"
        } else {
            binding.textView.text = "Редактирование"
            binding.buttonWork.text = "Изменить"

            counter = databaseRequests.selectCounterFromId(id)

            binding.editTextNumberFlat.setText(databaseRequests.selectFlatNumberFromId(counter.id_flat))
            binding.editTextNumberCounter.setText(counter.number)
            binding.checkUsed.isChecked = counter.used
            binding.spinnerType.setSelection(types.indexOfFirst { it.equals(counter.type)})
        }
    }
}