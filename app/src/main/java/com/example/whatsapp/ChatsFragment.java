package com.example.whatsapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View privateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;



    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        chatsList=(RecyclerView) privateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);


        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatViewHolder chatViewHolder, int i, @NonNull Contacts contacts)
                    {
                        final String usersIDs = getRef(i).getKey();
                        final String[] retImage = {"default_image"};

                        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists())
                                {
                                    if (dataSnapshot.hasChild("image"))
                                    {
                                        retImage[0] =dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(chatViewHolder.profileImage);
                                    }


                                    final String retName =dataSnapshot.child("name").getValue().toString();
                                    String retStatus =dataSnapshot.child("status").getValue().toString();

                                    chatViewHolder.userName.setText(retName);
                                    chatViewHolder.userStatus.setText("Last Seen: " + "\n" + "Date " + "Time ");


                                    if (dataSnapshot.child("userState").hasChild("state"))
                                    {
                                        String state =dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date =dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time =dataSnapshot.child("userState").child("time").getValue().toString();

                                        if (state.equals("online"))
                                        {
                                            chatViewHolder.userStatus.setText("online");
                                        }else if (state.equals("offline"))
                                        {
                                            chatViewHolder.userStatus.setText("Last Seen: " + date + " " + time);

                                        }

                                    }else {
                                        chatViewHolder.userStatus.setText("offline");

                                    }


                                    chatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            Intent chatIntent =new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id",usersIDs);
                                            chatIntent.putExtra("visit_user_name",retName);
                                            chatIntent.putExtra("visit_image", retImage[0]);

                                            startActivity(chatIntent);

                                        }
                                    });
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        ChatViewHolder chatViewHolder=new ChatViewHolder(view);
                        return chatViewHolder ;
                    }
                };
        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    private static class ChatViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);        }
    }
}
