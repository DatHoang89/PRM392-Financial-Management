package com.example.financialmanagement;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

import org.checkerframework.checker.units.qual.A;

import java.text.DateFormat;
import java.util.Date;

public class ExpenseFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDatabase;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Data, ExpenseFragment.MyViewHolder> adapter;
    private TextView expenseTotalSum;

    private EditText edtAmount;
    private EditText edtType;
    private EditText edtNote;

    //button for update and delete;
    private Button btnUpdate;
    private Button btnDelete;

    //Data item value

    private String type;
    private String note;
    private int amount;
    private String post_key;

    private String databaseUrl = "https://expense-manager-6ccad-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_expense, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();
        mExpenseDatabase = FirebaseDatabase.getInstance(databaseUrl).getReference().child("ExpenseData").child(uid);
        recyclerView = myView.findViewById(R.id.rvExpense);
        expenseTotalSum = myView.findViewById(R.id.txtExpense);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int totalValue = 0;
                for (DataSnapshot mysnapshot : dataSnapshot.getChildren()) {
                    Data data = mysnapshot.getValue(Data.class);
                    totalValue += data.getAmount();
                }
                String stTotalValue = String.valueOf(totalValue);
                expenseTotalSum.setText(stTotalValue+".00");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mExpenseDatabase, Data.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Data, ExpenseFragment.MyViewHolder>(options) {
            @NonNull
            @Override
            public ExpenseFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_recycler_data, parent, false);
                return new ExpenseFragment.MyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ExpenseFragment.MyViewHolder holder, int position, @NonNull Data model) {
                holder.setAmmount(model.getAmount());
                holder.setType(model.getType());
                holder.setNote(model.getNote());
                holder.setDate(model.getDate());

                holder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        post_key = getRef(holder.getAdapterPosition()).getKey();

                        type = model.getType();
                        note = model.getNote();
                        amount = model.getAmount();

                        UpdateDataItem();
                    }
                });
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
            TextView mType = myView.findViewById(R.id.txtTypeExpense);
            mType.setText(type);
        }

        private void setNote(String category) {
            TextView mNote = myView.findViewById(R.id.txtNoteExpense);
            mNote.setText(category);
        }

        private void setDate(String date) {
            TextView mDate = myView.findViewById(R.id.txtDateExpense);
            mDate.setText(date);
        }

        private void setAmmount(int amount) {
            TextView mAmount = myView.findViewById(R.id.txtAmmountExpense);
            String stAmount = String.valueOf(amount);
            mAmount.setText(stAmount);
        }
    }

    private void UpdateDataItem(){
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myView = inflater.inflate(R.layout.update_data_item, null);

        mydialog.setView(myView);
        edtAmount = myView.findViewById(R.id.amount_edt);
        edtType = myView.findViewById(R.id.type_edt);
        edtNote = myView.findViewById(R.id.note_edt);

        edtAmount = myView.findViewById(R.id.amount_edt);
        edtType = myView.findViewById(R.id.type_edt);
        edtNote = myView.findViewById(R.id.note_edt);

        edtType.setText(type);
        edtType.setSelection(type.length());
        edtNote.setText(note);
        edtNote.setSelection(note.length());
        edtAmount.setText(String.valueOf(amount));
        edtAmount.setSelection(String.valueOf(amount).length());

        btnUpdate = myView.findViewById(R.id.btnUp_Update);
        btnDelete = myView.findViewById(R.id.btnUp_Delete);

        AlertDialog dialog = mydialog.create();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = edtType.getText().toString().trim();
                note = edtNote.getText().toString().trim();

                String mdamount = String.valueOf(amount);
                mdamount = edtAmount.getText().toString().trim();

                int myAmount = Integer.parseInt(mdamount);

                String mDate = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(myAmount,type,post_key,note,mDate);

                mExpenseDatabase.child(post_key).setValue(data);
                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpenseDatabase.child(post_key).removeValue();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}