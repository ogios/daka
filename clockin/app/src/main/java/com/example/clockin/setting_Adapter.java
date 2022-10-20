package com.example.clockin;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class setting_Adapter extends RecyclerView.Adapter<setting_Adapter.viewHolder> {

    private List<setting_item> setting_items;
    private Context context;

    public setting_Adapter(List<setting_item> setting_items, Context context){
        this.setting_items = setting_items;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.json_item, parent, false);
        viewHolder viewHolder = new viewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.vname.setText(setting_items.get(position).getVname());
        holder.value.setText(setting_items.get(position).getValue());
        holder.value.setHint(setting_items.get(position).getHint());

        holder.value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setting_items.get(holder.getAdapterPosition()).setValue(editable.toString());
//                Toast.makeText(context, "已改变"+editable.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return setting_items.size();
    }

    public List<setting_item> getItems(){
        return setting_items;
    }

    public void add(setting_item setting_item){
        setting_items.add(setting_item);
        notifyItemInserted(setting_items.size());
    }

    public void clear(){
        setting_items.clear();
        notifyDataSetChanged();
    }



    class viewHolder extends RecyclerView.ViewHolder{
        TextView vname;
        EditText value;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            this.vname = itemView.findViewById(R.id.json_vname);
            this.value = itemView.findViewById(R.id.json_value);
        }
    }
}
