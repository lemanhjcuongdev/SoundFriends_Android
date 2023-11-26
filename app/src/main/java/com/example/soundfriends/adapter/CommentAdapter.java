package com.example.soundfriends.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.soundfriends.R;
import com.example.soundfriends.fragments.Model.Comment;
import com.google.firebase.database.DatabaseReference;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.util.Locale;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private Context context;

    private DatabaseReference commentReferences;

    public static final String ACTION_REPLY_BUTTON_CLICK = "action_reply_button_click";

    public CommentAdapter(Context context,List<Comment> comments, DatabaseReference commentReferences) {
        this.context = context;
        this.comments = comments;
        this.commentReferences = commentReferences; // Thêm dòng này
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent,false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);

        //bind item click event
        handleItemClickHolder(holder, comment);

        String commentTime = comment.getTimestamp();

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        LocalDateTime dateTime = LocalDateTime.parse(commentTime, inputFormatter);

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy 'lúc' HH:mm");
        String formattedTime = dateTime.format(outputFormatter);

        if(comment == null) return;

        holder.tvAccount.setText(comment.getUsername());
        holder.tvBody.setText(comment.getBody());
        holder.tvTime.setText(formattedTime);
        Glide.with(context).load(Uri.parse(comment.getAvatarUrl())).placeholder(R.drawable.empty_avatar).into(holder.avatarComment);
        holder.tvTextLike.setText(String.valueOf(comment.getLikeCount()));

        // Đặt trạng thái nút thích dựa trên trạng thái đã thích của comment
        int likeDrawableResId = comment.isLiked() ? R.drawable.ic_like_selected : R.drawable.ic_like_unselected;

        // Gọi đúng với chỉ một đối số
        holder.updateLikeButton(likeDrawableResId, comment.getLikeCount());
    }

    @Override
    public int getItemCount() {
        if(comments != null){
            return comments.size();
        }
        return 0;
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatarComment, btnLikeComment;
        public TextView tvAccount, tvTime, tvBody, tvTextLike;
        public Button btnReplyComment;

        public CommentViewHolder(View itemView) {
            super(itemView);
            avatarComment = itemView.findViewById(R.id.avatarComment);
            tvAccount = itemView.findViewById(R.id.accountComment);
            tvTime = itemView.findViewById(R.id.timeComment);
            tvBody = itemView.findViewById(R.id.bodyComment);
            tvTextLike = itemView.findViewById(R.id.textLikeComment);
            btnLikeComment = itemView.findViewById(R.id.likeComment);
            btnReplyComment = itemView.findViewById(R.id.replyComment);

            btnLikeComment.setOnClickListener(v -> onLikeButtonClick(comments.get(getAdapterPosition())));

        }


        private void onLikeButtonClick(Comment comment) {
            int likeCount = comment.getLikeCount();
            boolean isLiked = comment.isLiked();
            if (isLiked==false){
                isLiked=true;
            }

            if (isLiked) {
                likeCount++;
            }

            // Cập nhật trực tiếp vào đối tượng Comment
            comment.setLiked(isLiked);
            comment.setLikeCount(likeCount);

            // Cập nhật giao diện người dùng
            updateLikeButton(isLiked ? R.drawable.ic_like_selected : R.drawable.ic_like_unselected, likeCount);

            // Cập nhật dữ liệu thích trên Firebase
            updateLikeDataInFirebase(comment);
        }

        private void updateLikeButton(int likeDrawableResId, int likeCount) {
            btnLikeComment.setImageResource(likeDrawableResId);
            tvTextLike.setText(String.valueOf(likeCount));
        }


        private void updateLikeDataInFirebase(Comment comment) {
            // Assuming commentReferences is a DatabaseReference
            String commentKey = comment.getCommentId(); // Assuming you have the key

            // Tạo một DatabaseReference đến nút cần thay thế
            DatabaseReference commentRef = commentReferences.child(commentKey);

            // Đặt giá trị mới cho các trường trong nút cụ thể
            commentRef.child("likeCount").setValue(comment.getLikeCount());
            commentRef.child("liked").setValue(comment.isLiked());
        }
    }

    private void handleItemClickHolder(CommentViewHolder holder, Comment model){
        holder.btnReplyComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent replyIntent = new Intent(ACTION_REPLY_BUTTON_CLICK);
                replyIntent.putExtra("data", model.getUsername());
                LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(replyIntent);
            }
        });
    }
}
