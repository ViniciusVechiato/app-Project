package com.ostap.prog4app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class listAdapter extends RecyclerView.Adapter<listAdapter.ViewHolder>{

    public ArrayList<File> dataset;


    public static class ViewHolder  extends RecyclerView.ViewHolder  {
        public TextView item;
        //Must pass in a formatted TextView of the data from dataset to display it in the RecyclerView
        public ViewHolder(View newItem) {
            super(newItem);
            item = newItem.findViewById(R.id.file_name);
        }

    }

    //retrieves
    public listAdapter(ArrayList<File> dSet) {
        dataset = dSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View fileView = inflater.inflate(R.layout.item, parent,false);
        ViewHolder data = new ViewHolder(fileView);
        return data;
    }

    @Override
    public void onBindViewHolder(listAdapter.ViewHolder viewHolder, int position) {
        final File file = dataset.get(position);
        //textView is the name of the file displayed
        TextView textView = viewHolder.item;
        textView.setText(file.getFile_name());
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Allow us to see which file has been clicked
                Context context = v.getContext();
                Intent intent = new Intent(context,viewFile.class);
                intent.putExtra("Clicked",file);
                context.startActivity(intent);
            }

        });
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }



}
