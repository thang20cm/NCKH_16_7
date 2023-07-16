package com.example.nghincukhoahc;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapterAdmin extends RecyclerView.Adapter<MyViewHolderAdmin> {

    private Context context;
    private List<DataClass> dataList;









    public MyAdapterAdmin(Context context, List<DataClass> dataList) {
        this.context = context;
        this.dataList = dataList;

    }

    @NonNull
    @Override
    public MyViewHolderAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolderAdmin(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolderAdmin holder, int position) {

        DataClass data = dataList.get(position);

        holder.recTitle.setText(data.getDataTitle());
        holder.recDesc.setText(data.getDataDesc());


        //Sắp xếp theo time đăng bài giảm dần
        Collections.sort(dataList, new Comparator<DataClass>() {
            @Override
            public int compare(DataClass data1, DataClass data2) {
                return Long.compare(data2.getDateTime(),data1.getDateTime());
            }
        });


        Glide.with(context).load(dataList.get(position).getDataImage()).into(holder.recImage);
        holder.recFile.setText(dataList.get(position).getDataFile());
        holder.recTitle.setText(dataList.get(position).getDataTitle());
        holder.recDesc.setText(dataList.get(position).getDataDesc());
        holder.recLang.setText(dataList.get(position).getDataLang());
        holder.recDateTime.setText(getFormattedDateTime(dataList.get(position).getDateTime()));



        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailActivityAdmin.class);
                intent.putExtra("Image", dataList.get(holder.getAdapterPosition()).getDataImage());
                intent.putExtra("File", dataList.get(holder.getAdapterPosition()).getDataFile());
                intent.putExtra("Description", dataList.get(holder.getAdapterPosition()).getDataDesc());
                intent.putExtra("Title", dataList.get(holder.getAdapterPosition()).getDataTitle());
                intent.putExtra("Key",dataList.get(holder.getAdapterPosition()).getKey());
                intent.putExtra("Language", dataList.get(holder.getAdapterPosition()).getDataLang());
                context.startActivity(intent);
            }
        });
    }






    @Override
    public int getItemCount() {
        return dataList.size();
    }
    public void setDataList(List<DataClass> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    public void searchDataList(ArrayList<DataClass> searchList){
        dataList = searchList;
        notifyDataSetChanged();
    }
    private String getFormattedDateTime(long dateTime) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - dateTime;

        long seconds = timeDifference / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " ngày trước";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else if (minutes > 0) {
            return minutes + " phút trước";
        } else {
            return "Vừa xong";
        }
    }

}


class MyViewHolderAdmin extends RecyclerView.ViewHolder{



    ImageView recImage;
    TextView recTitle, recDesc, recLang,recDateTime,recFile;
    CardView recCard;



    public MyViewHolderAdmin(@NonNull View itemView) {

        super(itemView);

        recImage = itemView.findViewById(R.id.recImage);
        recFile = itemView.findViewById(R.id.recFile);
        recCard = itemView.findViewById(R.id.recCard);
        recDesc = itemView.findViewById(R.id.recDesc);
        recLang = itemView.findViewById(R.id.recLang);
        recTitle = itemView.findViewById(R.id.recTitle);
        recDateTime = itemView.findViewById(R.id.recDateTime);


    }

}