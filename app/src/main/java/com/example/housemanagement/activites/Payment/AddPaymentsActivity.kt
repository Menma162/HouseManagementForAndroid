package com.example.housemanagement.activites.Payment

import android.R
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.housemanagement.database.DatabaseRequests
import com.example.housemanagement.databinding.ActivityAddPaymentsBinding
import com.example.housemanagement.models.Counter
import com.example.housemanagement.models.Flat
import com.example.housemanagement.models.Indication
import com.example.housemanagement.models.Payment
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.RoundingMode
import java.nio.file.Path
import java.time.LocalDate


class AddPaymentsActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddPaymentsBinding

    private lateinit var payment : Payment
    private lateinit var databaseRequests: DatabaseRequests

    private var periods = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPaymentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        databaseRequests =  DatabaseRequests(this@AddPaymentsActivity)
        payment = Payment()
        binding.buttonBack.setOnClickListener(View.OnClickListener {
            val i = Intent(this, PaymentsActivity::class.java)
            startActivity(i)
        })
        binding.buttonWork.setOnClickListener(View.OnClickListener {
            if(intent.getIntExtra("report", 0) == 1)
                createReport()
            else addPayments()
        })
        fillPeriod()
    }

    private fun createReport() {
        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet("Начисления")
        val payments = databaseRequests.selectPaymentsFromPeriod(payment.period)

        var rowNum = 0;

        val row: Row = sheet.createRow(rowNum)
        row.createCell(0).setCellValue("Период")
        row.createCell(1).setCellValue("Услуга")
        row.createCell(2).setCellValue("Номер квартиры")
        row.createCell(3).setCellValue("Сумма")
        row.createCell(4).setCellValue("Статус оплаты")

        // заполняем лист данными
        for (payment_item in payments) {
            createSheetHeader(sheet, ++rowNum, payment_item);
        }

        // записываем созданный в памяти Excel документ в файл
        try {
            FileOutputStream(File(getExternalPath().path)).use { out -> workbook.write(out) }
            Toast.makeText(this, "Отчет сохранен в загрузки", Toast.LENGTH_SHORT).
            show()
        } catch (e: IOException) {
            Toast.makeText(this, "Не удалось создать отчет", Toast.LENGTH_SHORT).
            show()
        }

        val i = Intent(this, PaymentsActivity::class.java)
        startActivity(i)
    }

    private fun getExternalPath(): File {
        return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path, "Отчет по неоплаченным начислениям.xls")
    }

    private fun createSheetHeader(sheet: HSSFSheet, rowNum: Int, payment: Payment) {
        val row = sheet.createRow(rowNum);

        row.createCell(0).setCellValue(payment.period);
        row.createCell(1).setCellValue(databaseRequests.selectNameRateFromId(payment.id_rate));
        row.createCell(2).setCellValue(databaseRequests.selectFlatNumberFromId(payment.id_flat));
        row.createCell(3).setCellValue(payment.amount.toString().toDouble())
        if(payment.status == true) row.createCell(4).setCellValue("Оплачено")
        else row.createCell(4).setCellValue("Не оплачено")
    }

    private fun fillPeriod() {
        val date = LocalDate.now()
        val monthNames = arrayOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
        var period = ""
        if(date.monthValue - 1 == 0) period = (monthNames[11] + " " + (date.year - 1))
        else period = (monthNames[date.monthValue - 2] + " " + (date.year))
        payment.period = period
        periods = databaseRequests.selectPeriodsFromPayments()
        if(databaseRequests.selectCountPaymentsWherePeriod(period) == 0) periods.add(period)
        binding.editPeriod.adapter = ArrayAdapter(this, R.layout.simple_list_item_1, periods)
        binding.editPeriod.setSelection(periods.indexOf(period))
        binding.editPeriod.isEnabled = false

        if(intent.getIntExtra("report", 0) == 1){
            binding.buttonWork.setText("Создать отчет")
            binding.textViewP.setText("Создание отчета")
            binding.textView.setText("по неуплате")
            binding.editPeriod.isEnabled = true
        }
    }

    private fun addPayments() {
        val count = databaseRequests.selectCountPaymentsWherePeriod(payment.period)
        if (count != 0){
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Ошибка")
            builder.setNegativeButton(
                "Ок",
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })
            builder.setMessage("Начисление новый платежей невозможно, так как начисления за этот период уже произведены!")
            builder.show()
        }
        else{
//////////////////////////------------------------------------------------------------------------------------------------------------

            val flats = databaseRequests.selectFlats()
            val rates = databaseRequests.selectRates()
            val normatives = databaseRequests.selectNormatives()

            if (flats.size != 0) {
                for (flat: Flat in flats) {
                    val counters = databaseRequests.selectCountersWhereUsedAndIdFlat(flat.id)

                    val indications = ArrayList<Indication>()

                    for(counter: Counter in counters) {
                        indications.add(databaseRequests.selectIndicationFromCounterWherePeriod(counter.id, payment.period))
                    }

                    val payment_cw = Payment()
                    val payment_hw = Payment()
                    val payment_ee = Payment()
                    val payment_g = Payment()
                    val payment_te = Payment()
                    var count_cw = 0
                    var count_hw = 0
                    var count_ee = 0
                    var count_g = 0
                    var count_te = 0
                    var sum_ind_cw = 0f
                    var sum_noind_cw = 0f
                    var sum_ind_hw = 0f
                    var sum_noind_hw = 0f
                    var sum_ind_ee = 0f
                    var sum_noind_ee = 0f
                    var sum_ind_g = 0f
                    var sum_noind_g = 0f
                    var sum_ind_te = 0f
                    val sum_noind_te = 0f
                    for (counter: Counter in counters) {
                        when (counter.type) {
                            "Счетчик холодной воды" -> {
                                val rate = rates.first { it.name == "Холодная вода" }
                                val normative = normatives.first { it.name == "Холодная вода" }

                                val indication = indications.firstOrNull { it.id_counter == counter.id }
                                payment_cw.period = payment.period
                                payment_cw.status = false
                                payment_cw.cheque = null
                                payment_cw.id_flat = flat.id
                                payment_cw.id_rate = rate.id
                                payment_cw.id_normative = normative.id
                                if (indication==(null)) {
                                    if (flat.number_of_registered_residents != 0) sum_noind_cw = rate.value * normative.value * flat.number_of_registered_residents
                                    else sum_noind_cw = rate.value * normative.value * flat.number_of_owners
                                    count_cw++
                                } else {
                                    sum_ind_cw += indication.value * rate.value
                                }
                            }
                            "Счетчик горячей воды" -> {
                                val rate = rates.first { it.name == "Горячая вода" }
                                val normative = normatives.first { it.name == "Горячая вода" }

                                val indication = indications.firstOrNull { it.id_counter == counter.id }
                                payment_hw.period = payment.period
                                payment_hw.status = false
                                payment_hw.cheque = null
                                payment_hw.id_flat = flat.id
                                payment_hw.id_rate = rate.id
                                payment_hw.id_normative = normative.id

                                if (indication == null) {
                                        if (flat.number_of_registered_residents != 0) sum_noind_hw = rate.value * normative.value * flat.number_of_registered_residents
                                        else sum_noind_hw = rate.value * normative.value * flat.number_of_owners
                                        count_hw++
                                    } else {
                                        sum_ind_hw += indication.value * rate.value
                                    }
                            }
                            "Счетчик электрической энергии" -> {
                                val rate = rates.first { it.name == "Электроэнергия" }
                                val normative = normatives.first { it.name == "Электроэнергия" }

                                val indication = indications.firstOrNull { it.id_counter == counter.id }
                                payment_ee.period = payment.period
                                payment_ee.status = false
                                payment_ee.cheque = null
                                payment_ee.id_flat = flat.id
                                payment_ee.id_rate = rate.id
                                payment_ee.id_normative = normative.id

                                if (indication==(null)) {
                                    if (flat.number_of_registered_residents != 0) sum_noind_ee = rate.value * normative.value * flat.number_of_registered_residents
                                    else sum_noind_ee = rate.value * normative.value * flat.number_of_owners
                                    count_ee++
                                } else {
                                    sum_ind_ee += indication.value * rate.value
                                }
                            }
                            "Газовый счетчик" -> {
                                val rate = rates.first { it.name == "Газ" }
                                val normative = normatives.first { it.name == "Газ" }

                                val indication =
                                    indications.firstOrNull { it.id_counter == counter.id }
                                payment_g.period = payment.period
                                payment_g.status = false
                                payment_g.cheque = null
                                payment_g.id_flat = flat.id
                                payment_g.id_rate = rate.id
                                payment_g.id_normative = normative.id

                                if (indication==(null)) {
                                    if (flat.number_of_registered_residents != 0) sum_noind_g = rate.value * normative.value * flat.number_of_registered_residents
                                    else sum_noind_g = rate.value * normative.value * flat.number_of_owners
                                    count_g++
                                } else {
                                    sum_ind_g += indication.value * rate.value
                                }
                            }
                            "Счетчик отопления" -> {
                                val rate = rates.first { it.name == "Тепловая энергия" }
                                val normative = normatives.first { it.name == "Тепловая энергия" }

                                val indication =
                                    indications.firstOrNull { it.id_counter == counter.id }
                                payment_te.period = payment.period
                                payment_te.status = false
                                payment_te.cheque = null
                                payment_te.id_flat = flat.id
                                payment_te.id_rate = rate.id
                                payment_te.id_normative = normative.id

                                if (indication==(null)) {
                                    sum_ind_te = normative.value * flat.usable_area
                                    count_te++
                                } else {
                                    sum_ind_te += indication.value * rate.value
                                }
                            }
                        }
                    }
                    var found = 0
                    found = Math.toIntExact(counters.stream().filter { x -> x.type.equals("Счетчик холодной воды") }.count())
                    if (found == 0) {
                        val rate = rates.first { it.name == "Холодная вода" }
                        val normative = normatives.first { it.name == "Холодная вода" }

                        var amount: Float? = null
                        if (flat.number_of_registered_residents != 0)
                            amount = rate.value * normative.value * flat.number_of_registered_residents
                        else amount = rate.value * normative.value * flat.number_of_owners
                        amount = amount.toBigDecimal().setScale(2, RoundingMode.UP).toFloat()

                        payment.period = payment.period
                        payment.status = false
                        payment.cheque = null
                        payment.id_flat = flat.id
                        payment.id_rate = rate.id
                        payment.id_normative = normative.id
                        payment.amount = amount
                        databaseRequests.createPayment(payment)
                    } else {
                        if (count_cw != 0) payment_cw.amount = (sum_ind_cw + (sum_noind_cw / count_cw)).toBigDecimal().setScale(2, RoundingMode.UP).toFloat()
                        else payment_cw.amount = (sum_ind_cw).toBigDecimal().setScale(2, RoundingMode.UP).toFloat()
                        databaseRequests.createPayment(payment_cw)
                    }
                    found = 0
                    found = Math.toIntExact(counters.stream().filter { x -> x.type.equals("Счетчик горячей воды")
                    }.count())
                    if (found == 0) {
                        val rate = rates.filter { it.name == "Горячая вода" }.first()
                        val normative = normatives.filter { it.name == "Горячая вода" }.first()

                        var amount: Float? = null
                        if (flat.number_of_registered_residents != 0)
                            amount = rate.value * normative.value * flat.number_of_registered_residents
                        else amount = rate.value * normative.value * flat.number_of_owners
                        amount = amount.toBigDecimal().setScale(2, RoundingMode.UP).toFloat()

                        payment.period = payment.period
                        payment.status = false
                        payment.cheque = null
                        payment.id_flat = flat.id
                        payment.id_rate = rate.id
                        payment.id_normative = normative.id
                        payment.amount = amount
                        databaseRequests.createPayment(payment)
                    } else {
                        if (count_hw != 0) payment_hw.amount = (sum_ind_hw + (sum_noind_hw / count_hw)).toBigDecimal().setScale(2, RoundingMode.UP).toFloat()
                        else payment_hw.amount = (sum_ind_hw).toBigDecimal().setScale(2, RoundingMode.UP).toFloat()
                        databaseRequests.createPayment(payment_hw)
                    }
                    found = 0
                    found = Math.toIntExact(counters.stream().filter { x -> x.type.equals("Счетчик электрической энергии") }.count())
                    if (found == 0) {
                        val rate = rates.filter { it.name == "Электроэнергия" }.first()
                        val normative = normatives.filter { it.name == "Электроэнергия" }.first()

                        var amount: Float? = null
                        if (flat.number_of_registered_residents != 0)
                            amount = rate.value * normative.value * flat.number_of_registered_residents
                        else amount = rate.value * normative.value * flat.number_of_owners
                        amount = amount.toBigDecimal().setScale(2, RoundingMode.UP).toFloat()

                        payment.period = payment.period
                        payment.status = false
                        payment.cheque = null
                        payment.id_flat = flat.id
                        payment.id_rate = rate.id
                        payment.id_normative = normative.id
                        payment.amount = amount
                        databaseRequests.createPayment(payment)
                    } else {
                        if (count_ee != 0) payment_ee.amount = (sum_ind_ee + (sum_noind_ee / count_ee)).toBigDecimal().setScale(2, RoundingMode.UP).toFloat()
                        else payment_ee.amount = (sum_ind_ee).toBigDecimal().setScale(2, RoundingMode.UP).toFloat()
                        databaseRequests.createPayment(payment_ee)
                    }
                    found = 0
                    found = Math.toIntExact(counters.stream().filter { x -> x.type.equals("Газовый счетчик") }.count())
                    if (found == 0) {
                        val rate = rates.filter { it.name == "Газ" }.first()
                        val normative = normatives.filter { it.name == "Газ" }.first()

                        var amount: Float? = null
                        if (flat.number_of_registered_residents != 0)
                            amount = rate.value * normative.value * flat.number_of_registered_residents
                        else amount = rate.value * normative.value * flat.number_of_owners
                        amount = amount.toBigDecimal().setScale(2, RoundingMode.UP).toFloat()

                        payment.period = payment.period
                        payment.status = false
                        payment.cheque = null
                        payment.id_flat = flat.id
                        payment.id_rate = rate.id
                        payment.id_normative = normative.id
                        payment.amount = amount
                        databaseRequests.createPayment(payment)
                    } else {
                        if (count_g != 0) payment_g.amount = (sum_ind_g + (sum_noind_g / count_g)).toBigDecimal().setScale(2, RoundingMode.UP).toFloat()
                        else payment_g.amount = (sum_ind_g).toBigDecimal().setScale(2, RoundingMode.UP).toFloat()
                        databaseRequests.createPayment(payment_g)
                    }
                    found = 0
                    found = Math.toIntExact(counters.stream().filter { x -> x.type.equals("Счетчик отопления") }.count())
                    if (found == 0) {
                        val rate = rates.filter { it.name == "Тепловая энергия" }.first()
                        val normative = normatives.filter { it.name == "Тепловая энергия" }.first()

                        var amount: Float = normative.value * flat.usable_area
                        amount = amount.toBigDecimal().setScale(2, RoundingMode.UP).toFloat()

                        payment.period = payment.period
                        payment.status = false
                        payment.cheque = null
                        payment.id_flat = flat.id
                        payment.id_rate = rate.id
                        payment.id_normative = normative.id
                        payment.amount = amount
                        databaseRequests.createPayment(payment)
                    } else {
                        if (count_te != 0) payment_te.amount = (sum_ind_te + (sum_noind_te / count_te)).toBigDecimal().setScale(2, RoundingMode.UP).toFloat()
                        else payment_te.amount = (sum_ind_te).toBigDecimal().setScale(2, RoundingMode.UP).toFloat()
                        databaseRequests.createPayment(payment_te)
                    }
                }
            }
            Toast.makeText(this, "Начисления произведены", Toast.LENGTH_SHORT).show()
            val i = Intent(this, PaymentsActivity::class.java)
            startActivity(i)
//////////////////////////----------------------------------------------------------------------------------------------------------------
        }
    }
}