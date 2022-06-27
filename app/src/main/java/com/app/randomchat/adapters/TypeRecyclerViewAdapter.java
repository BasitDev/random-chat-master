package com.app.randomchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.randomchat.Info.Info;
import com.app.randomchat.Pojo.Message;
import com.app.randomchat.Pojo.Super;
import com.app.randomchat.Pojo.User;
import com.app.randomchat.Pojo.UserConHistory;
import com.app.randomchat.R;
import com.app.randomchat.Utils.DownloadFileFromURL;
import com.app.randomchat.Utils.Hashing;
import com.app.randomchat.activities.ChatActivity;
import com.app.randomchat.activities.PreviewImageActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.List;


public class TypeRecyclerViewAdapter extends RecyclerView.Adapter<TypeRecyclerViewHolder> implements Info {

    private static final String TAG = "TAG";
    Context context;
    List<Super> listInstances;
    int type;

    public TypeRecyclerViewAdapter(Context context, List<Super> listInstances, int type) {
        this.context = context;
        this.listInstances = listInstances;
        this.type = type;
    }

    @NonNull
    @Override
    public TypeRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater li = LayoutInflater.from(context);
        View view;

        if (viewType == TYPE_SHOW_RIGHT)
            view = li.inflate(R.layout.item_message_sender, parent, false);
        else if (viewType == TYPE_SHOW_LEFT)
            view = li.inflate(R.layout.item_message, parent, false);
        else
            view = li.inflate(R.layout.rv_user_detail, parent, false);

        return new TypeRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TypeRecyclerViewHolder holder, int position) {

        if (type == TYPE_USER) {
            User user = (User) listInstances.get(position);
            String userName = user.getFirstName() + " " + user.getLastName();
            holder.tvUserName.setText(userName);
//            holder.ivUserProfile.setImageURI(user.getUrlToImage());
            holder.tvLastText.setVisibility(View.GONE);
            return;
        }

        if (type == TYPE_MESSAGE) {
            UserConHistory message = (UserConHistory) listInstances.get(position);
            holder.tvUserName.setVisibility(View.VISIBLE);
            holder.tvUserName.setTextColor(context.getColor(R.color.black));
            holder.tvUserName.setText(message.getUserName());
            holder.ivUserProfile.setImageURI(message.getUserImageUrl());

            holder.ivUserProfile.setOnClickListener(v -> getImageUriAge(message.getTargetUserId()));

            holder.ibTouchField.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(KEY_TARGET_USER_ID, message.getTargetUserId());
                context.startActivity(intent);
            });
            try {
                holder.tvLastText.setText(Hashing.decryptMsg(message.getLastMessage()));
            } catch (Exception e) {

                e.printStackTrace();
            }
            return;
        }

        if (type == TYPE_REC_MESSAGE) {
            Message message = (Message) listInstances.get(position);
            holder.cardView.setVisibility(View.VISIBLE);
            holder.layout.setVisibility(View.VISIBLE);
            try {
                String string = message.getFileUrl();
                URI uri = new URI(string);
                String path = uri.getPath();
                String idStr = path.substring(path.lastIndexOf('/') + 1);
                holder.fileName.setText(idStr);
                holder.cvFileLayout.setVisibility(View.VISIBLE);
                holder.cvFileLayout.setOnClickListener(v -> {
                    try {
                        DownloadFileFromURL.downloadTask(context, message.getFileUrl());
                    } catch (URISyntaxException | GeneralSecurityException | IOException e) {
                        Toast.makeText(context, "Error occurred", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
                holder.messageTextView.setVisibility(View.GONE);
                if (message.getFileUrl() == null) {
                    holder.cvFileLayout.setVisibility(View.GONE);
                    holder.cardView.setCardBackgroundColor(context.getColor(R.color.fui_transparent));
                }
                if (message.getFileUrl() != null && message.getFileUrl().isEmpty()) {
                    holder.cvFileLayout.setVisibility(View.GONE);
                    holder.cardView.setCardBackgroundColor(context.getColor(R.color.fui_transparent));
                }


            } catch (Exception e) {
                holder.cvFileLayout.setVisibility(View.GONE);
                holder.messageTextView.setVisibility(View.VISIBLE);
                try {
                    holder.messageTextView.setText(Hashing.decryptMsg(message.getText()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            holder.authorTextView.setText(message.getFromUserName());
        }
    }

    private void getImageUriAge(String userId) {
        FirebaseDatabase.getInstance().getReference(USERS).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Intent intent = new Intent(context, PreviewImageActivity.class);
                intent.putExtra(KEY_IMAGE, user.getUserImageUrl());
                intent.putExtra(KEY_AGE, user.getAge());
                context.startActivity(intent);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    @Override
    public int getItemCount() {
        return listInstances.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (type == TYPE_REC_MESSAGE) {
            Message message = (Message) listInstances.get(position);
            if (message.isShowOnRight())
                return TYPE_SHOW_RIGHT;
            else
                return TYPE_SHOW_LEFT;
        }

        return 0;
    }

}
