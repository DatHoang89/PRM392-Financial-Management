package com.example.financialmanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialmanagement.Models.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class IncomeFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Data, MyViewHolder> adapter;
    private TextView incomeTotalSum;

    private String type;
    private String category;
    private double amount;

    private String post_key;
    private String databaseUrl = "https://expense-manager-6ccad-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_income, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();
        mIncomeDatabase = FirebaseDatabase.getInstance(databaseUrl).getReference().child("IncomeData").child(uid);
        recyclerView = myView.findViewById(R.id.rvIncome);
        incomeTotalSum = myView.findViewById(R.id.txtIncome);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int totalValue = 0;
                for (DataSnapshot mysnapshot : dataSnapshot.getChildren()) {
                    Data data = mysnapshot.getValue(Data.class);
                    totalValue += data.getAmount();
                }
                String stTotalValue = String.valueOf(totalValue);
                incomeTotalSum.setText(stTotalValue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mIncomeDatabase, Data.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.income_recycler_data, parent, false);
                return new MyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Data model) {
                holder.setAmmount(model.getAmount());
                holder.setType(model.getType());
                holder.setNote(model.getNote());
                holder.setDate(model.getDate());
            }
        };

        recyclerView.setAdapter(adapter);
        return myView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        View myView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myView = itemView;
        }

        private void setType(String type) {
            TextView mType = myView.findViewById(R.id.txtTypeIncome);
            mType.setText(type);
        }

        private void setNote(String category) {
            TextView mNote = myView.findViewById(R.id.txtNoteIncome);
            mNote.setText(category);
        }

        private void setDate(String date) {
            TextView mDate = myView.findViewById(R.id.txtDateIncome);
            mDate.setText(date);
        }

        private void setAmmount(int amount) {
            TextView mAmount = myView.findViewById(R.id.txtAmmountIncome);
            String stAmount = String.valueOf(amount);
            mAmount.setText(stAmount);
        }
    }
}