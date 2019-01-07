package com.link.dheyaa.textme.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.link.dheyaa.textme.activities.MessagingPage;
import com.link.dheyaa.textme.R;
import com.link.dheyaa.textme.activities.Search;
import com.link.dheyaa.textme.adapters.FriendAdapter;
import com.link.dheyaa.textme.itemDecorators.friendsItemDecorator;
import com.link.dheyaa.textme.utils.Sorting;
import com.link.dheyaa.textme.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class FriendsFragment extends Fragment {

    @Nullable
    private RecyclerView listView;
    private FirebaseAuth mAuth;
    private DatabaseReference DBref;
    private FriendAdapter adapter;
    private ArrayList<User> friends = new ArrayList<User>();
    private ConstraintLayout noFriends;
    private ProgressBar loading;
    private boolean itemCLicked;
    private boolean sortingAcending;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.friends_tab, container, false);
        listView = (RecyclerView) root.findViewById(R.id.friends_list);
        sortingAcending = true;
        noFriends = (ConstraintLayout) root.findViewById(R.id.nofriends);
        loading = (ProgressBar) root.findViewById(R.id.progressBar);
        AppCompatButton searchBtn = (AppCompatButton) root.findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Search = new Intent(getContext(), com.link.dheyaa.textme.activities.Search.class);
                startActivity(Search);
            }
        });
        SetViews(false, false);

        mAuth = FirebaseAuth.getInstance();
        DBref = FirebaseDatabase.getInstance().getReference("Users");
        DBref.child(mAuth.getCurrentUser().getUid()).child("friends").orderByValue().equalTo(1).addChildEventListener(FriendsChildEventListner);

        DBref.child(mAuth.getCurrentUser().getUid()).child("friends").orderByValue().equalTo(1).addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue () == null){
                    SetViews(false, false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        final ToggleButton switchSorting = (ToggleButton) root.findViewById(R.id.switchSorting);
        switchSorting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchSorting.getText().equals("A-Z")) {
                    sortingAcending = true;
                    adapter.sortFirends(sortingAcending);
                } else {
                    sortingAcending = false;
                    adapter.sortFirends(sortingAcending);
                }
            }
        });

        adapter = new FriendAdapter(getContext(), R.layout.friends_list_item, friends);

        listView.setHasFixedSize(true);
        listView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(layoutManager);

        listView.addItemDecoration(new friendsItemDecorator(0));

        return root;

    }
    ChildEventListener FriendsChildEventListner =  new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            System.out.println("child->>>>Added : key->>> "+dataSnapshot.getKey());
            if(dataSnapshot.getValue(Integer.class) == 1){
                addFriendData(dataSnapshot.getKey());
                SetViews(true, false);
            }else{
                adapter.removeOldbyID(dataSnapshot.getKey());
                System.out.println ("child->>removeById->>1->"+dataSnapshot.getKey());

            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            System.out.println ("child->cahnges->important");

            if(dataSnapshot.getValue(Integer.class) == 1){
                addFriendData(dataSnapshot.getKey());
                SetViews(true, false);
            }else{
                System.out.println ("child->>removeById->>2->"+dataSnapshot.getKey());
                adapter.removeOldbyID(dataSnapshot.getKey());
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            adapter.removeOldbyID(dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    public void addFriendData(final String friendId){
        DBref.child(friendId).orderByKey().addValueEventListener(new ValueEventListener() {
            String userId = friendId;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setId(userId);
                        user.setFriends(null);
                       // friends.add (user);
                        System.out.println("userAdded ->> user ->>" + user);

                        adapter.addFreind(user, sortingAcending);
                        adapter.notifyDataSetChanged();
                    }
                }catch (Exception err){
                    System.out.println ("err123 ->>> "+err.toString ());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                SetViews(false, false);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        itemCLicked = false;

    }

    public void SetViews(boolean hasFriends, boolean isLoading) {
        if (isLoading) {
            listView.setVisibility(View.INVISIBLE);
            noFriends.setVisibility(View.INVISIBLE);
            loading.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(hasFriends ? View.VISIBLE : View.INVISIBLE);
            noFriends.setVisibility(hasFriends ? View.INVISIBLE : View.VISIBLE);
            loading.setVisibility(View.INVISIBLE);
        }
    }




}
