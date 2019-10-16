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

import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment
{
    private View privateChatView;
    private RecyclerView ChatList;


    private DatabaseReference ChatsRef,UsersRef;
    private FirebaseAuth mauth;

    private String CurrentUserID;




    public ChatsFragment()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

       privateChatView= inflater.inflate(R.layout.fragment_chats, container, false);

       ChatList =(RecyclerView)privateChatView.findViewById(R.id.chat_fragment_list);
       ChatList.setLayoutManager(new LinearLayoutManager(getContext()));

       mauth=FirebaseAuth.getInstance();
       CurrentUserID=mauth.getCurrentUser().getUid();
       ChatsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(CurrentUserID);
       UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");

       return privateChatView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        // will give querry to Database//
        FirebaseRecyclerOptions<Contacts> options = new
                FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef,Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,chatsViewHolder> adapter = new
                FirebaseRecyclerAdapter<Contacts, chatsViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull final chatsViewHolder holder, int position, @NonNull Contacts model)
                    {
                        // getting the key id of the users who are friend to the currentUser//
                        final String usersIDs =getRef(position).getKey();

                        final String[] retImage = {"default_image"};


                        //retrieving the name,status,pic of  the users who are friend to the currentUser from Database "Users'//
                        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    if (dataSnapshot.hasChild("image"))
                                    {
                                        retImage[0] =dataSnapshot.child("image").getValue().toString();

                                        Picasso.with(getContext()).load(retImage[0]).into(holder.profileImage);
                                    }


                                    final String retName = dataSnapshot.child("name").getValue().toString();
                                    final String retStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(retName);

                                    holder.userStatus.setText("Last Seen : " + "\n" + "Date " + " Time");


                                    //when user clicks on any of the user profile in chatsfragment//

                                    holder.itemView.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("visit_users_id",usersIDs);

                                            chatIntent.putExtra("visit_users_name",retName);
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
                    public chatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        // it acesses the 'all_users_display_layout' //

                       View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout,viewGroup,false);

                       //creating object of  class 'chatsViewHolder'//
                        chatsViewHolder obj=new chatsViewHolder(view);
                        return obj;
                    }
                };
        ChatList.setAdapter(adapter);
        adapter.startListening();


    }


    public static  class chatsViewHolder extends RecyclerView.ViewHolder
    {
        // initialize fields of all_users_display_layout//
        TextView userName,userStatus;
        CircleImageView profileImage;


        public chatsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName=itemView.findViewById(R.id.all_users_profile_full_name);
            userStatus=itemView.findViewById(R.id.all_users_status);
            profileImage=itemView.findViewById(R.id.all_users_profile_image);
        }

    }

}
