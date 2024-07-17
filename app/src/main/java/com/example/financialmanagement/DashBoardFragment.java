package com.example.financialmanagement;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;

import Model.Data;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashBoardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashBoardFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FloatingActionButton fab_main_btn;
    private FloatingActionButton fab_income_btn;
    private FloatingActionButton fab_expense_btn;
    private TextView fab_income_txt;
    private TextView fab_expense_txt;
    private boolean isOpen = false;

    //Animation
    private Animation FadeOpen, FadeClose;
    //Dashboard income and expense result
    private TextView totalIncomeResult;
    private TextView totalExpenseResult;

    private Button btnViewInAnalytics;
    private Button btnViewExAnalytics;

    //Recycle View
    private RecyclerView mRecycleIncome;
    private RecyclerView mRecycleExpense;

    //FireBase
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private DatabaseReference mExpenseDatabase;

    private FirebaseRecyclerAdapter<Data, IncomeViewHolder> incomeAdapter;
    private FirebaseRecyclerAdapter<Data, ExpenseViewHolder> expenseAdapter;

    public DashBoardFragment() {
        // Required empty public constructor
    }

    public static DashBoardFragment newInstance(String param1, String param2) {
        DashBoardFragment fragment = new DashBoardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_dash_board, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();

        if (mUser != null) {
            String uid = mUser.getUid();
            String databaseUrl = "https://expense-manager-6ccad-default-rtdb.asia-southeast1.firebasedatabase.app";
            mIncomeDatabase = FirebaseDatabase.getInstance(databaseUrl).getReference().child("IncomeData").child(uid);
            mExpenseDatabase = FirebaseDatabase.getInstance(databaseUrl).getReference().child("ExpenseData").child(uid);
            Log.d("Firebase", "Database references initialized");

            mIncomeDatabase.keepSynced(true);
            mExpenseDatabase.keepSynced(true);
        } else {
            Log.e("Firebase", "User not authenticated");
        }

        //Connect floating button to layout
        fab_main_btn = myview.findViewById(R.id.fb_main_plus_btn);
        fab_income_btn = myview.findViewById(R.id.income_ft_btn);
        fab_expense_btn = myview.findViewById(R.id.expense_ft_btn);

        //Analytics Fragment
        btnViewInAnalytics = myview.findViewById(R.id.btnViewInAnalytics);
        btnViewExAnalytics = myview.findViewById(R.id.btnViewExAnalytics);

        //Connect floating text
        fab_income_txt = myview.findViewById(R.id.income_ft_text);
        fab_expense_txt = myview.findViewById(R.id.expense_ft_text);

        //Total income and expense
        totalIncomeResult = myview.findViewById(R.id.income_set_result);
        totalExpenseResult = myview.findViewById(R.id.expense_set_result);

        //Recycler
        mRecycleIncome = myview.findViewById(R.id.recycle_income);
        mRecycleExpense = myview.findViewById(R.id.recycle_expense);


        //Connect Animation
        FadeOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_open);
        FadeClose = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_close);

        btnViewInAnalytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsFragment fragment = new AnalyticsFragment(false);
                setFragment(fragment);
            }
        });

        btnViewExAnalytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsFragment fragment = new AnalyticsFragment(true);
                setFragment(fragment);
            }
        });

        fab_main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData();

                if (isOpen) {
                    fab_income_btn.startAnimation(FadeClose);
                    fab_expense_btn.startAnimation(FadeClose);
                    fab_income_btn.setClickable(false);
                    fab_expense_btn.setClickable(false);

                    fab_income_txt.startAnimation(FadeClose);
                    fab_expense_txt.startAnimation(FadeClose);
                    fab_income_txt.setClickable(false);
                    fab_expense_txt.setClickable(false);
                    isOpen = false;
                } else {
                    fab_income_btn.startAnimation(FadeOpen);
                    fab_expense_btn.startAnimation(FadeOpen);
                    fab_income_btn.setClickable(true);
                    fab_expense_btn.setClickable(true);

                    fab_income_txt.startAnimation(FadeOpen);
                    fab_expense_txt.startAnimation(FadeOpen);
                    fab_income_txt.setClickable(true);
                    fab_expense_txt.setClickable(true);
                    isOpen = true;
                }
            }
        });
 
        //Calculate total income
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalsum = 0;
                for (DataSnapshot mysnap:snapshot.getChildren()){
                    Data data = mysnap.getValue(Data.class);
                    totalsum += totalsum + data.getAmount();
                    String strResult = String.valueOf(totalsum);
                    totalIncomeResult.setText(strResult+".00");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Calculate total expense
        mExpenseDatabase.addValueEventListener((new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalsum = 0;
                for(DataSnapshot mysnap:snapshot.getChildren()){
                    Data data = mysnap.getValue(Data.class);
                    totalsum += data.getAmount();
                    String strTotalSum = String.valueOf(totalsum);
                    totalExpenseResult.setText(strTotalSum+".00");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));


        //Recycler
        LinearLayoutManager layoutManagerIncome = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        layoutManagerIncome.setStackFromEnd(true);
        layoutManagerIncome.setReverseLayout(true);
        mRecycleIncome.setHasFixedSize(true);
        mRecycleIncome.setLayoutManager(layoutManagerIncome);

        LinearLayoutManager layoutManagerExpense = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        layoutManagerExpense.setStackFromEnd(true);
        layoutManagerExpense.setReverseLayout(true);
        mRecycleExpense.setHasFixedSize(true);
        mRecycleExpense.setLayoutManager(layoutManagerExpense);

        //fix bug
        FirebaseRecyclerOptions<Data> incomeOptions =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mIncomeDatabase, Data.class)
                        .build();
        incomeAdapter = new FirebaseRecyclerAdapter<Data, IncomeViewHolder>(incomeOptions){
            @Override
            protected void onBindViewHolder(@NonNull IncomeViewHolder holder, int position, @NonNull Data model) {
                // Bind data to ViewHolder
                holder.setIncomeType(model.getType());
                holder.setIncomeAmount(model.getAmount());
                holder.setIncomeDate(model.getDate());
            }

            @NonNull
            @Override
            public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_income, parent, false);
                return new IncomeViewHolder(view);
            }
        };
        Log.d("CountIncome","CountIncome:"+ incomeAdapter.getItemCount());
        mRecycleIncome.setAdapter(incomeAdapter);

        FirebaseRecyclerOptions<Data> expenseOptions =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mExpenseDatabase, Data.class)
                        .build();
         expenseAdapter = new FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(expenseOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position, @NonNull Data model) {
                holder.setExpenseType(model.getType());
                holder.setExpenseAmount(model.getAmount());
                holder.setExpenseDate(model.getDate());
            }

            @NonNull
            @Override
            public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_expense, parent, false);
                return new ExpenseViewHolder(view);
            }
        };

        mRecycleExpense.setAdapter(expenseAdapter);

        return myview;
    }
    @Override
    public void onStart() {
        super.onStart();
        if (incomeAdapter != null) {
            incomeAdapter.startListening();
        }
        if (expenseAdapter != null) {
            expenseAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (incomeAdapter != null) {
            incomeAdapter.stopListening();
        }
        if (expenseAdapter != null) {
            expenseAdapter.stopListening();
        }
    }
    private void ftAnimation() {
        if (isOpen) {

            fab_income_btn.startAnimation(FadeClose);
            fab_expense_btn.startAnimation(FadeClose);
            fab_income_btn.setClickable(false);
            fab_expense_btn.setClickable(false);

            fab_income_txt.startAnimation(FadeClose);
            fab_expense_txt.startAnimation(FadeClose);
            fab_income_txt.setClickable(false);
            fab_expense_txt.setClickable(false);
            isOpen = false;

        } else {
            fab_income_btn.startAnimation(FadeOpen);
            fab_expense_btn.startAnimation(FadeOpen);
            fab_income_btn.setClickable(true);
            fab_expense_btn.setClickable(true);

            fab_income_txt.startAnimation(FadeOpen);
            fab_expense_txt.startAnimation(FadeOpen);
            fab_income_txt.setClickable(true);
            fab_expense_txt.setClickable(true);
            isOpen = true;

        }
    }

    private void addData() {
        //Fab button income

        fab_income_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incomeDataInsert();
            }
        });

        fab_expense_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenseDataInsert();
            }
        });


    }

    public void incomeDataInsert() {
        Log.d("Firebase", "incomeDataInsert called");
        AlertDialog.Builder mydiaglog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myview = inflater.inflate(R.layout.custom_layour_for_insertdata, null);
        mydiaglog.setView(myview);

        AlertDialog dialog = mydiaglog.create();

        dialog.setCancelable(false);

        EditText edtAmount = myview.findViewById(R.id.amount_edt);
        EditText edtType = myview.findViewById(R.id.type_edt);
        EditText edtNote = myview.findViewById(R.id.note_edt);

        Button btnSave = myview.findViewById(R.id.btnSave);
        Button btnCancel = myview.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = edtType.getText().toString().trim();
                String amount = edtAmount.getText().toString().trim();
                String note = edtNote.getText().toString().trim();

                if (TextUtils.isEmpty(type)) {
                    edtType.setError("Required");
                    return;
                }
                if (TextUtils.isEmpty(amount)) {
                    edtAmount.setError("Required");
                    return;
                }
                int ouramountint = Integer.parseInt(amount);
                if (TextUtils.isEmpty(note)) {
                    edtNote.setError("Required");
                    return;
                }

                String id = mIncomeDatabase.push().getKey();
                if (id == null) {
                    Log.e("Firebase", "Failed to create new id");
                    Toast.makeText(getActivity(), "Error creating new entry", Toast.LENGTH_SHORT).show();
                    return;
                }

                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(ouramountint, type, note, id, mDate);

                mIncomeDatabase.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Firebase", "Data Added Successfully");
                            Toast.makeText(getActivity(), "Data Added", Toast.LENGTH_SHORT).show();
                            ftAnimation();
                        } else {
                            Log.e("Firebase", "Data Adding Failed: " + task.getException().getMessage());
                        }
                    }
                });

                dialog.dismiss();

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    public void expenseDataInsert() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myview = inflater.inflate(R.layout.custom_layour_for_insertdata, null);
        mydialog.setView(myview);

        final AlertDialog dialog = mydialog.create();

        dialog.setCancelable(false);

        EditText ammount = myview.findViewById(R.id.amount_edt);
        EditText type = myview.findViewById(R.id.type_edt);
        EditText note = myview.findViewById(R.id.note_edt);

        Button btnSave = myview.findViewById(R.id.btnSave);
        Button btnCansel = myview.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmAmmount = ammount.getText().toString().trim();
                String tmtype = type.getText().toString().trim();
                String tmnote = note.getText().toString().trim();

                if (TextUtils.isEmpty(tmAmmount)) {
                    ammount.setError("Requires Fields...");
                    return;
                }

                int inamount = Integer.parseInt(tmAmmount);

                if (TextUtils.isEmpty(tmtype)) {
                    type.setError("Requires Fields...");
                    return;
                }
                if (TextUtils.isEmpty(tmnote)) {
                    note.setError("Requires Fields...");
                    return;
                }
                ftAnimation();
                String id = mExpenseDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(inamount, tmtype, tmnote, id, mDate);
                mExpenseDatabase.child(id).setValue(data);
                Toast.makeText(getActivity(), "Data added", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        btnCansel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ftAnimation();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

//    @Override
//    public void onStart(){
//        super.onStart();
//        FirebaseRecyclerOptions<Data> incomeOptions =
//                new FirebaseRecyclerOptions.Builder<Data>()
//                        .setQuery(mIncomeDatabase, Data.class)
//                        .build();
//        FirebaseRecyclerAdapter<Data, IncomeViewHolder> incomeAdapter = new FirebaseRecyclerAdapter<Data, IncomeViewHolder>(incomeOptions){
//            @Override
//            protected void onBindViewHolder(@NonNull IncomeViewHolder holder, int position, @NonNull Data model) {
//                // Bind data to ViewHolder
//                holder.setIncomeType(model.getType());
//                holder.setIncomeAmount(model.getAmount());
//                holder.setIncomeDate(model.getDate());
//            }
//
//            @NonNull
//            @Override
//            public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_income, parent, false);
//                return new IncomeViewHolder(view);
//            }
//        };
//        mRecycleIncome.setAdapter(incomeAdapter);
//
//        FirebaseRecyclerOptions<Data> expenseOptions =
//                new FirebaseRecyclerOptions.Builder<Data>()
//                        .setQuery(mExpenseDatabase, Data.class)
//                        .build();
//        FirebaseRecyclerAdapter<Data, ExpenseViewHolder> expenseAdapter = new FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(expenseOptions) {
//            @Override
//            protected void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position, @NonNull Data model) {
//                holder.setExpenseType(model.getType());
//                holder.setExpenseAmount(model.getAmount());
//                holder.setExpenseType(model.getDate());
//            }
//
//            @NonNull
//            @Override
//            public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_income, parent, false);
//                return new ExpenseViewHolder(view);
//            }
//        };
//
//        mRecycleExpense.setAdapter(expenseAdapter);
//    }

     class IncomeViewHolder extends RecyclerView.ViewHolder{
        View mIncomeView;

        public IncomeViewHolder(View itemView){
            super(itemView);
            mIncomeView = itemView;
        }

        public void setIncomeType(String type){
            TextView mtype = mIncomeView.findViewById(R.id.type_income_ds);
            mtype.setText(type);
        }

        public void setIncomeAmount(int amount){
            TextView mAmount = mIncomeView.findViewById(R.id.amount_income_ds);
            String strAmount = String.valueOf(amount);
            mAmount.setText(strAmount);
        }

        public void setIncomeDate(String date){
            TextView mDate = mIncomeView.findViewById(R.id.date_income_ds);
            mDate.setText(date);
        }
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame,fragment);
        fragmentTransaction.commit();
    }

     class ExpenseViewHolder extends RecyclerView.ViewHolder{
        View mExpenseView;

        public ExpenseViewHolder(View expenseView){
            super(expenseView);
            mExpenseView = expenseView;
        }

        public void setExpenseType(String type) {
            TextView mType = mExpenseView.findViewById(R.id.type_expense_ds);
                mType.setText(type);
        }

        public void setExpenseAmount(int amount){
            TextView mAmount = mExpenseView.findViewById(R.id.amount_expense_ds);
            String strAmount = String.valueOf(amount);
            mAmount.setText(strAmount);
        }

        public void setExpenseDate(String date){
            TextView mDate = mExpenseView.findViewById(R.id.date_expense_ds);
            mDate.setText(date);
        }
    }
}