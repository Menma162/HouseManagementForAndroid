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
import com.example.housemanagement.activites.Tenant.TenantItemActivity;
import com.example.housemanagement.database.DatabaseRequests;
import com.example.housemanagement.models.Counter;
import com.example.housemanagement.models.Tenant;

import java.util.ArrayList;

public class CounterViewAdapter extends RecyclerView.Adapter<CounterViewAdapter.MyViewHolder>{

    ArrayList<Counter> dataholder;
    private final Context context;
    private final Activity activity;
    private final DatabaseRequests databaseRequests;

    public CounterViewAdapter(ArrayList<Counter> dataholder, Context context, Activity activity) {
        this.dataholder = dataholder;
        this.context = context;
        this.activity = activity;
        databaseRequests = new DatabaseRequests(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_counter, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.flat_number.setText("Номер квартиры: " + databaseRequests.selectFlatNumberFromId(dataholder.get(position).getId_flat()));
        holder.type.setText("Тип счетчика: " + dataholder.get(position).getType());
        if(dataholder.get(position).getUsed()) holder.used.setText("Используется ли: да");
        else  holder.used.setText("Используется ли: нет");
        holder.id = dataholder.get(position).getId();
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CounterItemActivity.class);
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
        TextView flat_number, type, used;
        int id;
        LinearLayout linearLayout;
        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            flat_number = (TextView)itemView.findViewById(R.id.displayNumberFlat);
            type = (TextView)itemView.findViewById(R.id.displayTypeCounter);
            used = itemView.findViewById(R.id.displayUsed);
            linearLayout = itemView.findViewById(R.id.linearLayout);
        }
    }
}
