package com.example.financialmanagement;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExpenseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExpenseFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDatabase;

    //Recyclerview..

    private RecyclerView recyclerView;


    private TextView expenseSumResult;
    public ExpenseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExpenseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExpenseFragment newInstance(String param1, String param2) {
        ExpenseFragment fragment = new ExpenseFragment();
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
        View myView= inflater.inflate(R.layout.fragment_expense, container, false);
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser=mAuth.getCurrentUser();
        String uid=mUser.getUid();
        mExpenseDatabase= FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid);
        expenseSumResult=myView.findViewById(R.id.txtExpense);
        recyclerView=myView.findViewById(R.id.recycler_id_expense);

        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int expenseSum=0;
                for (DataSnapshot mySnapshot:dataSnapshot.getChildren()){
                    Data data=mySnapshot.getValue(Data.class);
                    expenseSum+=data.getAmount();
                    String strExpenseSum=String.valueOf(expenseSum);
                    expenseSumResult.setText(strExpenseSum);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return  myView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mExpenseDatabase, Data.class)
                .build();
        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, ExpenseFragment.MyViewHolder>(options) {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_recycler_data, parent, false);
                return new MyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ExpenseFragment.MyViewHolder viewHolder, int position, @NonNull Data model) {
                viewHolder.setDate(model.getDate());
                viewHolder.setType(model.getType());
                viewHolder.setAmmount(model.getAmount());
            }
        };
        recyclerView.setAdapter(adapter);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        View myView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myView = itemView;
        }

        private void setType(String type) {
            TextView mType = myView.findViewById(R.id.txtTypeExpense);
            mType.setText(type);
        }



        private void setDate(String date) {
            TextView mDate = myView.findViewById(R.id.txtDateExpense);
            mDate.setText(date);
        }

        private void setAmmount(int ammount) {
            TextView mAmmount = myView.findViewById(R.id.txtAmmountExpense);
            String stAmmount = String.valueOf(ammount);
            mAmmount.setText(stAmmount);
        }
    }

}