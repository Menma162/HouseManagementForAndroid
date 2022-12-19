package com.example.housemanagement.recyclerview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemanagement.R;
import com.example.housemanagement.activites.Counter.CounterItemActivity;
import com.example.housemanagement.activites.Payment.PaymentItemActivity;
import com.example.housemanagement.activites.Payment.PaymentWorkActivity;
import com.example.housemanagement.database.DatabaseRequests;
import com.example.housemanagement.models.Counter;
import com.example.housemanagement.models.Payment;

import java.util.ArrayList;

public class PaymentViewAdapter extends RecyclerView.Adapter<PaymentViewAdapter.MyViewHolder>{

    ArrayList<Payment> dataholder;
    private final Context context;
    private final Activity activity;
    private final DatabaseRequests databaseRequests;

    public PaymentViewAdapter(ArrayList<Payment> dataholder, Context context, Activity activity) {
        this.dataholder = dataholder;
        this.context = context;
        this.activity = activity;
        databaseRequests = new DatabaseRequests(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_payment, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.flat_number.setText("Номер квартиры: " + databaseRequests.selectFlatNumberFromId(dataholder.get(position).getId_flat()));
        holder.period.setText("Период: " + dataholder.get(position).getPeriod());
        if(dataholder.get(position).getStatus() == true) holder.status.setText("Оплачено ли: да");
        else  holder.status.setText("Оплачено ли: нет");
        holder.service.setText("Услуга: " + databaseRequests.selectNameRateFromId(dataholder.get(position).getId_rate()));
        holder.amount.setText("Сумма: " + dataholder.get(position).getAmount().toString());

        holder.id = dataholder.get(position).getId();
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PaymentItemActivity.class);
                intent.putExtra("id",dataholder.get(position).getId());
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataholder.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView flat_number, period, service, amount, status;
        int id;
        LinearLayout linearLayout;
        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            flat_number = (TextView)itemView.findViewById(R.id.displayNumberFlatPayment);
            period = (TextView)itemView.findViewById(R.id.displayPeriodPayment);
            service = itemView.findViewById(R.id.displayServicePayment);
            amount = itemView.findViewById(R.id.displayAmountPayment);
            status = itemView.findViewById(R.id.displayStatusPayment);
            linearLayout = itemView.findViewById(R.id.linearLayout);
        }
    }
}
