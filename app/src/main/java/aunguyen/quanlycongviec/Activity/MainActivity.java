package aunguyen.quanlycongviec.Activity;


import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import aunguyen.quanlycongviec.Adapter.JobAdapter;
import aunguyen.quanlycongviec.Object.Constant;
import aunguyen.quanlycongviec.Object.EmployeeObject;
import aunguyen.quanlycongviec.Object.JobObject;
import aunguyen.quanlycongviec.Object.StatusJob;
import aunguyen.quanlycongviec.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbarMain;
    private TextView tvMessage;

    private FloatingActionButton btnAddJod;

    private RecyclerView rvJob;
    private List<JobObject> listJobs;
    private JobAdapter jobAdapter;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpToolbar();

        addControls();

        pushNotification();

        setupNavigation();

        addEvents();

    }

    private void pushNotification() {

        SharedPreferences preferences = this.getSharedPreferences(Constant.PREFERENCE_NAME, MODE_PRIVATE);
        final String id = preferences.getString(Constant.PREFERENCE_KEY_ID, null);
        final int[] count = {0};

        if (id != null) {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constant.NODE_CONG_VIEC);
            myRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    JobObject jobObject = dataSnapshot.getValue(JobObject.class);
                    String idJob = jobObject.getIdJob();

                    List<StatusJob> list = jobObject.getListIdMember();
                    for (int i = 0; i < list.size(); i++) {
                        String notify = list.get(i).getNotify();
                        if (id.equals(list.get(i).getIdMember())
                                && notify.equals(Constant.NOT_NOTIFY)) {
                            Intent intent = new Intent(MainActivity.this, DetailJobEmployeeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("IDJob", idJob);
                            PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, count[0], intent, PendingIntent.FLAG_ONE_SHOT);
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
                                    .setSmallIcon(R.drawable.ic_notify)
                                    .setContentTitle(getString(R.string.notification_title))
                                    .setContentText(getString(R.string.notification_text) + " " + jobObject.getTitleJob())
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true);
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
                            notificationManager.notify(count[0]++, mBuilder.build());

                            FirebaseDatabase.getInstance().getReference(Constant.NODE_CONG_VIEC)
                                    .child(jobObject.getIdJob())
                                    .child("listIdMember")
                                    .child(String.valueOf(i))
                                    .child("notify")
                                    .setValue(Constant.NOTIFY);

                            break;
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void loadDataFromFireBase() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getResources().getString(R.string.dialog));
        progressDialog.setCancelable(false);
        progressDialog.show();
        SharedPreferences preferences = this.getSharedPreferences(Constant.PREFERENCE_NAME, MODE_PRIVATE);
        final String id = preferences.getString(Constant.PREFERENCE_KEY_ID, null);

        View view =  navigationView.getHeaderView(0);
        final ImageView imgNav = view.findViewById(R.id.img_nav);
        final TextView tvName = view.findViewById(R.id.tv_name_nav);
        final TextView tvUsername = view.findViewById(R.id.tv_username_nav);

        if (id != null) {

            DatabaseReference myAccount = FirebaseDatabase.getInstance().getReference(Constant.NODE_NHAN_VIEN);
            myAccount.child(id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    EmployeeObject employeeObject = dataSnapshot.getValue(EmployeeObject.class);
                    if (employeeObject != null) {
                        Glide.with(getApplicationContext())
                                .load(employeeObject.getUrlAvatar())
                                .into(imgNav);
                        tvName.setText(employeeObject.getNameEmployee());
                        tvUsername.setText(employeeObject.getUsernameEmployee());
                        if (id.equals(employeeObject.getIdEmployee())) {
                            String accountType = employeeObject.getAccountType();
                            if (accountType.equals("0")) {
                                tvMessage.setText(getString(R.string.job_message_admin));
                                tvMessage.setVisibility(View.VISIBLE);
                                rvJob.setVisibility(View.GONE);
                                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constant.NODE_CONG_VIEC);
                                myRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        listJobs.clear();
                                        jobAdapter.notifyDataSetChanged();

                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            JobObject jobObject = snapshot.getValue(JobObject.class);

                                            if (id.equals(jobObject.getIdManageJob())) {
                                                listJobs.add(jobObject);
                                                jobAdapter.notifyDataSetChanged();
                                            }
                                        }

                                        if (listJobs.size() > 0) {
                                            tvMessage.setVisibility(View.GONE);
                                            rvJob.setVisibility(View.VISIBLE);
                                        }

                                        progressDialog.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        progressDialog.dismiss();
                                        Log.i("ANTN", "onCancelled() - Main", error.toException());
                                    }
                                });
                            } else {
                                tvMessage.setVisibility(View.VISIBLE);
                                rvJob.setVisibility(View.GONE);
                                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constant.NODE_CONG_VIEC);
                                myRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        listJobs.clear();
                                        jobAdapter.notifyDataSetChanged();

                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            JobObject jobObject = snapshot.getValue(JobObject.class);
                                            List<StatusJob> list = jobObject.getListIdMember();

                                            for (int i = 0; i < list.size(); i++) {
                                                if (id.equals(list.get(i).getIdMember())) {
                                                    String status = list.get(i).getStatus();
                                                    jobObject.setStatusJob(status);
                                                    listJobs.add(jobObject);
                                                    jobAdapter.notifyDataSetChanged();
                                                    break;
                                                }
                                            }
                                        }

                                        if (listJobs.size() > 0) {
                                            tvMessage.setVisibility(View.GONE);
                                            rvJob.setVisibility(View.VISIBLE);
                                        }

                                        progressDialog.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        progressDialog.dismiss();
                                        Log.i("ANTN", "onCancelled() - Main", error.toException());
                                    }
                                });
                            }
                        }
                    } else {
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progressDialog.dismiss();
                }
            });

            jobAdapter.notifyDataSetChanged();

        } else {
            progressDialog.dismiss();
        }
    }

    private void setupNavigation() {
        //Setup drawer
        drawerLayout = findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        toolbarMain.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView = findViewById(R.id.navigation);

        SharedPreferences preferences = this.getSharedPreferences(Constant.PREFERENCE_NAME, MODE_PRIVATE);
        String id = preferences.getString(Constant.PREFERENCE_KEY_ID, null);

        if (id != null) {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constant.NODE_NHAN_VIEN).child(id);
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    EmployeeObject employeeObject = dataSnapshot.getValue(EmployeeObject.class);

                    if (employeeObject != null) {
                        String accountType = employeeObject.getAccountType();

                        if (accountType.equals("0")) {
                            navigationAdmin();
                        } else {
                            navigationEmployee();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
        }

    }

    private void navigationAdmin() {

        btnAddJod.setVisibility(View.VISIBLE);

        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.item_navigation_admin);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.mn_my_account:
                        manageMyAccount();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.mn_my_employees:
                        manageEmployeesAccount();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.mn_my_job:
                        myJob();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.mn_sign_out:
                        signOut();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.mn_information:
                        information();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                }
                return true;
            }
        });
    }

    private void navigationEmployee() {

        btnAddJod.setVisibility(View.INVISIBLE);

        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.item_navigation_employee);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.mn_statistic:
                        statistic();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.mn_sign_out:
                        signOut();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.mn_information:
                        information();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                }
                return true;
            }
        });

    }

    private void addEvents() {
        btnAddJod.setOnClickListener(this);
    }

    private void addControls() {
        btnAddJod = findViewById(R.id.btn_add_job);
        tvMessage = findViewById(R.id.tv_message);

        rvJob = findViewById(R.id.rv_job);
        listJobs = new ArrayList<>();

        jobAdapter = new JobAdapter(this, listJobs);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        rvJob.setAdapter(jobAdapter);
        rvJob.setLayoutManager(manager);
    }

    private void setUpToolbar() {
        //Setup layout_toolbar
        toolbarMain = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbarMain);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void manageMyAccount() {
        Intent intent = new Intent(this, ManageMyAccountActivity.class);
        startActivity(intent);
    }

    private void manageEmployeesAccount() {
        Intent intent = new Intent(this, ManageMyEmployeesActivity.class);
        startActivity(intent);
    }

    private void myJob() {
        Intent intent = new Intent(this, MyJobActivity.class);
        startActivity(intent);
    }

    private void statistic() {
        SharedPreferences preferences = getSharedPreferences(Constant.PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Intent intent = new Intent(this, StatisticActivity.class);
        String id = preferences.getString(Constant.PREFERENCE_KEY_ID, "");
        intent.putExtra("ID", id);
        editor.putString(Constant.KEY_ID_EMPLOYEE_STATISTIC, id);
        editor.apply();
        startActivity(intent);
    }

    private void information() {
        Intent intent = new Intent(this, InformationActivity.class);
        startActivity(intent);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences preferences = this.getSharedPreferences(Constant.PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(Constant.PREFERENCE_KEY_ID, null);
        editor.putString(Constant.PREFERENCE_DOMAIN, null);
        editor.apply();

        Intent intentSignIn = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intentSignIn);
        finish();
    }

    private void addJob() {
        Intent intentAddJob = new Intent(this, AddJobActivity.class);
        startActivity(intentAddJob);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDataFromFireBase();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_job:
                addJob();
                break;
        }
    }

}