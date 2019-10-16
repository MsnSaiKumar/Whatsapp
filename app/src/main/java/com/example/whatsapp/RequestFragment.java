package com.example.whatsapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
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


public class RequestFragment extends Fragment {


    private View RequestFragmentView;
    private RecyclerView myrequestsList;

    private DatabaseReference ChatRequestsRef,UserRef,ContactsRef;
    private FirebaseAuth mauth;
    private String CurrentUserId;

    public RequestFragment() {

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RequestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        ChatRequestsRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        UserRef=FirebaseDatabase.getInstance().getReference().child("Users");
        mauth=FirebaseAuth.getInstance();
        CurrentUserId=mauth.getCurrentUser().getUid();
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");

        myrequestsList=(RecyclerView) RequestFragmentView.findViewById(R.id.chat_request_list);
        myrequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequestFragmentView;
    }
    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options =new
                FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestsRef.child(CurrentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,RequestsViewHolder>  adapter = new
                FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model)
                    {
                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                        final String list_user_id =getRef(position).getKey(); //list_user_id stores the id of the reciever//
                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    String type =dataSnapshot.getValue().toString();

                                    if (type.equals("Recieved"))
                                    {
                                        UserRef.child(list_user_id).addValueEventListener(new ValueEventListener()
                                        {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                            {
                                                if (dataSnapshot.hasChild("image"))
                                                {


                                                    final String requestUserImage = dataSnapshot.child("image").getValue().toString();



                                                    Picasso.with(getContext()).load(requestUserImage).into(holder.profileImage);

                                                }


                                                final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText(requestUserStatus);


                                                holder.itemView.setOnClickListener(new View.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(View view)
                                                    {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"
                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUserName +  " Chat Request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener()
                                                        {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i)
                                                            {
                                                                if (i==0)  // 'i' is the position & this 'i==0' means accept button option//
                                                                {
                                                                    ContactsRef.child(CurrentUserId).child(list_user_id)
                                                                            .child("Contacts").setValue("Saved")
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                            {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        ContactsRef.child(list_user_id).child(CurrentUserId)
                                                                                                .child("Contacts").setValue("Saved")
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                    {
                                                                                                        if (task.isSuccessful())
                                                                                                        {
                                                                                                            ChatRequestsRef.child(CurrentUserId)
                                                                                                                    .child(list_user_id)
                                                                                                                    .removeValue()
                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                                                    {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                                        {
                                                                                                                            ChatRequestsRef.child(list_user_id)
                                                                                                                                    .child(CurrentUserId)
                                                                                                                                    .removeValue()
                                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                                                                    {
                                                                                                                                        @Override
                                                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                                                        {
                                                                                                                                            if (task.isSuccessful())
                                                                                                                                            {
                                                                                                                                                Toast.makeText(getContext(), "New contact Saved", Toast.LENGTH_SHORT).show();
                                                                                                                                            }

                                                                                                                                        }
                                                                                                                                    });

                                                                                                                        }
                                                                                                                    });
                                                                                                        }

                                                                                                    }
                                                                                                });

                                                                                    }

                                                                                }
                                                                            });

                                                                }
                                                                if (i==1) // 'i==1' means cancel button option present above/
                                                                {
                                                                    ChatRequestsRef.child(CurrentUserId)
                                                                            .child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                            {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    ChatRequestsRef.child(list_user_id)
                                                                                            .child(CurrentUserId)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                            {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                                {
                                                                                                    if (task.isSuccessful())
                                                                                                    {
                                                                                                        Toast.makeText(getContext(), "Contact deleted", Toast.LENGTH_SHORT).show();
                                                                                                    }

                                                                                                }
                                                                                            });

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
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view =LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout,viewGroup,false);
                        RequestsViewHolder holder = new RequestsViewHolder(view);
                        return holder;
                    }
                };
        myrequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        Button AcceptButton,cancelbutton;


        public RequestsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName=itemView.findViewById(R.id.all_users_profile_full_name);
            userStatus=itemView.findViewById(R.id.all_users_status);
            profileImage=itemView.findViewById(R.id.all_users_profile_image);
            AcceptButton=itemView.findViewById(R.id.request_accept_btn);
            cancelbutton=itemView.findViewById(R.id.request_cancel_btn);
        }
    }

}
