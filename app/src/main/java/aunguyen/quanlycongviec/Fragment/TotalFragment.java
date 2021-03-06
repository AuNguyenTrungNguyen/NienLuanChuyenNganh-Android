package aunguyen.quanlycongviec.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import aunguyen.quanlycongviec.Adapter.StatisticAdapter;
import aunguyen.quanlycongviec.Object.Constant;
import aunguyen.quanlycongviec.Object.JobObject;
import aunguyen.quanlycongviec.R;

public class TotalFragment extends Fragment {

    private View view;
    private Activity context;

    private PieChart pcTotal;
    private ArrayList<PieEntry> totalList = new ArrayList<>();
    private ProgressDialog progressDialog;

    private List<JobObject> listJobs;
    private StatisticAdapter jobAdapter;
    private RecyclerView rvJobEmployee;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_total, container, false);

        pcTotal = view.findViewById(R.id.pc_total);
        rvJobEmployee = view.findViewById(R.id.rv_total_job_employee);
        listJobs = new ArrayList<>();
        jobAdapter = new StatisticAdapter(context, listJobs, Constant.TYPE_TOTAL);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(
                context, LinearLayoutManager.VERTICAL, false);

        rvJobEmployee.setAdapter(jobAdapter);
        rvJobEmployee.setLayoutManager(manager);

        statistic();

        return view;
    }

    public void statistic(){
        SharedPreferences preferences = context.getSharedPreferences(Constant.PREFERENCE_NAME, context.MODE_PRIVATE);
        String id = preferences.getString(Constant.KEY_ID_STATISTIC, null);
        String start = preferences.getString(Constant.KEY_START_STATISTIC, null);
        String end = preferences.getString(Constant.KEY_END_STATISTIC, null);

        loadData(id, start, end);

    }

    private void loadData(final String id, final String start, final String end) {

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(context.getResources().getString(R.string.dialog));
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (id != null) {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constant.NODE_CONG_VIEC);
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    listJobs.clear();
                    jobAdapter.notifyDataSetChanged();
                    totalList.clear();

                    int count = 0;
                    int countReceived = 0;
                    int countUnReceived = 0;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        JobObject jobObject = snapshot.getValue(JobObject.class);

                        for (int i = 0; i < jobObject.getListIdMember().size(); i++) {
                            if (id.equals(jobObject.getListIdMember().get(i).getIdMember())) {
                                if (testDate(jobObject.getEndDateJob(), start, end)) {
                                    count += 1;
                                    String statusJob = jobObject.getListIdMember().get(i).getStatus();
                                    String status = statusJob.substring(0, statusJob.indexOf("/"));
                                    if (status.equals(Constant.NOT_RECEIVED)) {
                                        countUnReceived += 1;
                                    } else {
                                        countReceived += 1;
                                    }
                                    listJobs.add(jobObject);
                                    jobAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }

                    progressDialog.dismiss();

                    handlingGraph(countReceived, countUnReceived, count);
                }


                @Override
                public void onCancelled(DatabaseError error) {
                    progressDialog.dismiss();
                }
            });

        } else {
            progressDialog.dismiss();
        }
    }

    //Graph
    private SpannableString generateCenterSpannableText(int sumJob) {

        SpannableString s = new SpannableString(sumJob + " " + context.getString(R.string.spannable_total));
        s.setSpan(new RelativeSizeSpan(1f), 0, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.colorPrimary)), 0, s.length(), 0);

        return s;
    }

    private void handlingGraph(float value1, float value2, int sumJob) {
        if (value1 != 0) {
            totalList.add(new PieEntry(value1, Constant.RECEIVED));
        }
        if (value2 != 0) {
            totalList.add(new PieEntry(value2, Constant.NOT_RECEIVED));
        }

        pieChart(pcTotal, totalList, sumJob);
    }

    private void pieChart(PieChart pieChart, ArrayList arrayList, int sumJob) {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.99f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.setCenterText(generateCenterSpannableText(sumJob));
        pieChart.setDrawCenterText(true);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        pieChart.setEntryLabelColor(context.getResources().getColor(R.color.startBlue));
        pieChart.setEntryLabelTextSize(12f);
        PieDataSet dataSet = new PieDataSet(arrayList, context.getResources().getString(R.string.message_graph_state));
        dataSet.setDrawIcons(false);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(6f);

        ArrayList<Integer> colors = new ArrayList<>();

        colors.add(context.getResources().getColor(R.color.jobReceive));
        colors.add(context.getResources().getColor(R.color.jobNotReceive));

        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(10f);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextColor(context.getResources().getColor(R.color.startBlue));
        pieChart.setData(data);
        pieChart.animateX(1000);
        pieChart.invalidate();
    }

    private boolean testDate(String timeDate, String start, String end) {

        String split[] = timeDate.split("/");
        int day = Integer.parseInt(split[0]);
        int mouth = Integer.parseInt(split[1]);
        int year = Integer.parseInt(split[2]);

        String splitStart[] = start.split("/");
        int dayStart = Integer.parseInt(splitStart[0]);
        int mouthStart = Integer.parseInt(splitStart[1]);
        int yearStart = Integer.parseInt(splitStart[2]);

        String splitEnd[] = end.split("/");
        int dayEnd = Integer.parseInt(splitEnd[0]);
        int mouthEnd = Integer.parseInt(splitEnd[1]);
        int yearEnd = Integer.parseInt(splitEnd[2]);

        if (year >= yearStart && mouth >= mouthStart && day >= dayStart) {
            if (yearEnd > year) {
                return true;
            } else if (yearEnd < year) {
                return false;
            } else {
                if (mouthEnd > mouth) {
                    return true;
                } else if (mouthEnd < mouth) {
                    return false;
                } else {
                    return dayEnd >= day;
                }
            }
        }
        return false;
    }

}
