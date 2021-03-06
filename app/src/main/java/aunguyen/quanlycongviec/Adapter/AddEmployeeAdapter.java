package aunguyen.quanlycongviec.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import aunguyen.quanlycongviec.Activity.DetailAccountActivity;
import aunguyen.quanlycongviec.Object.EmployeeObject;
import aunguyen.quanlycongviec.R;

/**
 * Created by Sang on 16/03/2018.
 */



public class AddEmployeeAdapter extends RecyclerView.Adapter<AddEmployeeAdapter.EmployeeViewHolder>{

    private Context context;
    private List<EmployeeObject> employeeObjectList;

    public AddEmployeeAdapter(Context context, List<EmployeeObject> employeeObjectList) {
        this.context = context;
        this.employeeObjectList = employeeObjectList;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_employee, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, final int position) {

        final EmployeeObject employeeObject = employeeObjectList.get(position);

        holder.tvNameEmployee.setText(employeeObject.getNameEmployee());

        Glide.with(context)
                .load(employeeObject.getUrlAvatar())
                .into(holder.imgAvatar);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailAccountActivity.class);
                String id = employeeObject.getIdEmployee();
                intent.putExtra("ID", id);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return employeeObjectList.size();
    }

    public class EmployeeViewHolder extends RecyclerView.ViewHolder{

        private TextView tvNameEmployee;
        private ImageView imgAvatar;

        public EmployeeViewHolder (View itemView){
            super(itemView);
            tvNameEmployee = itemView.findViewById(R.id.tv_name_employee);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
        }

    }
}