package com.example.whatsapp;


import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
public class RequestsFragment extends Fragment
{

     private View RequestsFragmentview;
     private RecyclerView myRecyclerList;

     ///
     private DatabaseReference ChatRequestRef ,UsersRer, ContactRef ;
     private FirebaseAuth mAuth;
     private String currentUserID;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         RequestsFragmentview= inflater.inflate(R.layout.fragment_requests, container, false);

        myRecyclerList =(RecyclerView)RequestsFragmentview.findViewById(R.id.chat_requests_lis);
        myRecyclerList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();

        UsersRer= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactRef=FirebaseDatabase.getInstance().getReference().child("Contacts");



        return RequestsFragmentview;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options =new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestRef.child(currentUserID),Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,RequestsViewHolder>adapter
                =new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder requestsViewHolder, int i, @NonNull Contacts contacts)
            {
                requestsViewHolder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                requestsViewHolder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                final String list_user_id=getRef(i).getKey();
                DatabaseReference getTypeRef=getRef(i).child("request_type").getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists())
                        {
                            String type =dataSnapshot.getValue().toString();
                            if (type.equals("received"))
                            {
                                UsersRer.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild("image")) {

                                            final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(requestsViewHolder.profileImage);
                                        }

                                        final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                        final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                        requestsViewHolder.userName.setText(requestUserName);
                                        requestsViewHolder.userStatus.setText(requestUserStatus);


                                        requestsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                CharSequence options []=new CharSequence[]
                                                        {
                                                          "Acceppt","Cacel"
                                                        };
                                                AlertDialog.Builder builder =new AlertDialog.Builder(getContext());

                                                builder.setTitle(requestUserName+"  Chat Request")
                                                        .setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which)
                                                            {
                                                                if (which==0)
                                                                {
                                                                    ContactRef.child(currentUserID).child(list_user_id).child("Contacts")
                                                                            .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                ContactRef.child(list_user_id).child(currentUserID).child("Contacts")
                                                                                        .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        if (task.isSuccessful())
                                                                                        {

                                                                                            ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                                {
                                                                                                    if (task.isSuccessful())
                                                                                                    {
                                                                                                        ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                                            {
                                                                                                                if (task.isSuccessful())
                                                                                                                {
                                                                                                                    Toast.makeText(getContext(),"New Contacts saved",Toast.LENGTH_SHORT).show();

                                                                                                                }

                                                                                                            }
                                                                                                        });
                                                                                                    }

                                                                                                }
                                                                                            });

                                                                                        }

                                                                                    }
                                                                                });


                                                                            }

                                                                        }
                                                                    });

                                                                }
                                                                if (which==1)
                                                                {
                                                                    ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        if (task.isSuccessful())
                                                                                        {
                                                                                            Toast.makeText(getContext(),"Contact Deleted",Toast.LENGTH_SHORT).show();

                                                                                        }

                                                                                    }
                                                                                });
                                                                            }

                                                                        }
                                                                    });

                                                                }

                                                            }
                                                        });

                                                builder.show();
                                            }
                                        });




                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }else if (type.equals("sent"))
                            {
                                Button request_sent_btn=requestsViewHolder.itemView.findViewById(R.id.request_accept_btn);
                                request_sent_btn.setText("Req sent");
                                requestsViewHolder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);

                                UsersRer.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild("image")) {

                                            final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(requestsViewHolder.profileImage);
                                        }

                                        final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                        final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                        requestsViewHolder.userName.setText(requestUserName);
                                        requestsViewHolder.userStatus.setText("yo have sent request to "+requestUserStatus);


                                        requestsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                CharSequence options []=new CharSequence[]
                                                        {
                                                                "Cancel chat Request"
                                                        };
                                                AlertDialog.Builder builder =new AlertDialog.Builder(getContext());

                                                builder.setTitle("Already Sent Request")
                                                        .setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which)
                                                            {

                                                                if (which==0)
                                                                {
                                                                    ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        if (task.isSuccessful())
                                                                                        {
                                                                                            Toast.makeText(getContext(),"you have cancelled the chat request..",Toast.LENGTH_SHORT).show();

                                                                                        }

                                                                                    }
                                                                                });
                                                                            }

                                                                        }
                                                                    });

                                                                }

                                                            }
                                                        });

                                                builder.show();
                                            }
                                        });




                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });





            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                 View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);

                RequestsViewHolder holder=new RequestsViewHolder(view);

                return holder;
            }
        } ;

        myRecyclerList.setAdapter(adapter);
        adapter.startListening();


    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {

        TextView userName,userStatus;
        CircleImageView profileImage;
        Button AcceptButton,CancelButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            AcceptButton = itemView.findViewById(R.id.request_accept_btn);
            CancelButton = itemView.findViewById(R.id.request_cancel_btn);

        }
    }

}
