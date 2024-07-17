package com.example.financialmanagement;

import android.graphics.Color;
import android.icu.text.StringPrepParseException;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.A;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import Model.Data;

public class AnalyticsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference myDatabase;
    private int year;
    private boolean isExpense;
    private TextView tvYear;
    private Button btnNext;
    private Button btnPrev;
    private BarChart barChart;
    private ArrayList<Integer> totalByMonths = new ArrayList<>();
    private ArrayList<Data> lstData;
    private final ArrayList<String> months = new ArrayList<>();

    private String databaseUrl = "https://expense-manager-6ccad-default-rtdb.asia-southeast1.firebasedatabase.app";
    public AnalyticsFragment(boolean _isExpense) {
        isExpense = _isExpense;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            year = Year.now().getValue();
        }
        lstData = new ArrayList<>();
        months.add("Jan");
        months.add("Feb");
        months.add("March");
        months.add("April");
        months.add("May");
        months.add("June");
        months.add("July");
        months.add("Aug");
        months.add("Sep");
        months.add("Oct");
        months.add("Nov");
        months.add("Dec");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myview = inflater.inflate(R.layout.fragment_analytics, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();
        if(isExpense){
            myDatabase = FirebaseDatabase.getInstance(databaseUrl).getReference().child("ExpenseData").child(uid);
        }else{
            myDatabase = FirebaseDatabase.getInstance(databaseUrl).getReference().child("IncomeData").child(uid);
        }
        btnNext = myview.findViewById(R.id.btnNext);
        btnPrev = myview.findViewById(R.id.btnPrev);
        tvYear = myview.findViewById(R.id.tvYear);
        tvYear.setText(year + "");
        barChart = myview.findViewById(R.id.barchart);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                year += 1;
                tvYear.setText(year+"");
                GetListTransactionsByYear();
                SetBarData();
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                year -= 1;
                tvYear.setText(year+"");
                GetListTransactionsByYear();
                SetBarData();
            }
        });

        myDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    for (DataSnapshot mysnapshot : task.getResult().getChildren()) {
                        Data data = mysnapshot.getValue(Data.class);
                        lstData.add(data);
                    }
                    GetListTransactionsByYear();
                    barChart.getDescription().setText(isExpense ? "Expense Analytics" : "Income Analytics");
                    barChart.getDescription().setTextColor(Color.BLACK);
                    SetBarData();
                }
            }
        });
        return myview;
    }


    private void SetBarData(){
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (int i = 1; i <= totalByMonths.size(); i++) {
            barEntries.add(new BarEntry(i,totalByMonths.get(i - 1)));
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "Months");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(14f);
        XAxis xAxis = barChart.getXAxis();

        xAxis.setDrawLabels(true);
        xAxis.setLabelCount(months.size());
        xAxis.setLabelRotationAngle(45);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        barChart.setData(new BarData(barDataSet));
        barChart.setFitBars(true);
        barChart.animateY(2000);
    }

    private void GetListTransactionsByYear(){
        totalByMonths.clear();
        for (int i = 1; i <= 12; i ++){
            GetTotalByMonth(i);
        }
    }

    private void GetTotalByMonth(int month){
        ArrayList<Data> lstByMonth = new ArrayList<>();
        for (Data data: lstData) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                LocalDate date = LocalDate.parse(data.getDate(),formatter);
                if(date.getMonthValue() == month && date.getYear() == year){
                    lstByMonth.add(data);
                }
            }
        }
        int total = 0;
        for (Data data: lstByMonth) {
            total += data.getAmount();
        }
        totalByMonths.add(total);
    }

}