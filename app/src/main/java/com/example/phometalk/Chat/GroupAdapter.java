package com.example.phometalk.Chat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.phometalk.Model.ChatModel;
import com.example.phometalk.Model.ChatRoomModel;
import com.example.phometalk.Model.UserModel;
import com.example.phometalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private static final String TAG = "GroupAdapter";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private ArrayList<ChatModel> chatModels = new ArrayList<>();
    private ArrayList<ChatRoomModel> chatRoomModels = new ArrayList<>();
    private ArrayList<UserModel> userModels = new ArrayList<>();
    private ArrayList<String> user = new ArrayList<>(); //상대방 id
    private String roomID;

    SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
    String wDate = dateFormat1.format(Calendar.getInstance().getTime());

    SimpleDateFormat writeTimeFormat = new SimpleDateFormat("a hh:mm");

    //생성자에서 데이터 리스트 객체를 전달받음
    public GroupAdapter(String id,ArrayList<ChatModel>list){
        this.roomID=id;
        this.chatModels = list;

        /*
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ChatModel c = dataSnapshot.getValue(ChatModel.class);
                String commentKey = dataSnapshot.getKey();

                Map<String,Object> read = new HashMap<>();
                read.put(currentUser.getUid(),true);
                database.getReference("Message").child(roomID).child(commentKey).child("readUsers").updateChildren(read);

                chatModels.add(c);
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        database.getReference("Message").child(roomID).addChildEventListener(childEventListener);

         */


        //채팅방 id로 상대방 정보 가져오기
        database.getReference("ChatRoom").child(roomID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ChatRoomModel crm = dataSnapshot.getValue(ChatRoomModel.class);
                chatRoomModels.add(crm);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });


    }

    //아이템 뷰를 저장하는 뷰홀더 클래스
    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView userImage;
        public TextView userName;
        public TextView textView;
        public TextView timestamp;
        public ImageView sendPhoto;
        public TextView readNum;

        ViewHolder(View view){
            super(view);
            userImage = (ImageView)view.findViewById(R.id.user_image);
            userName = (TextView)view.findViewById(R.id.user_name);
            textView = (TextView)view.findViewById(R.id.tvChat);
            timestamp = (TextView)view.findViewById(R.id.timestamp);
            sendPhoto = (ImageView)view.findViewById(R.id.ivChat);
            readNum = (TextView)view.findViewById(R.id.read_number);

        }

    }

    //상대방이 보낸 메세지인지 구분
    @Override
    public int getItemViewType(int position) {
        if(chatModels.get(position).getUID().equals(currentUser.getUid())){
            switch (chatModels.get(position).getMsgType()){
                case "0": return 1; //내가 보낸 텍스트
                case "1": return 2; //내가 보낸 사진
                default: return 1; //예외는 그냥 텍스트
            }
        }else {
            switch (chatModels.get(position).getMsgType()){
                case "0": return 3; //상대방이 보낸 텍스트
                case "1": return 4; //상대방이 보낸 사진
                default: return 3; // 예외는 텍스트로
            }
        }
    }

    //아이템 뷰를 위한 뷰홀더 객체를 생성하여 리턴
    @NonNull
    @Override
    public GroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == 1){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatbubble_right,parent,false);
        }else if(viewType ==2){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_right,parent,false);
        }else if(viewType == 4){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_left,parent,false);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatbubble_left,parent,false);
        }

        GroupAdapter.ViewHolder vh = new GroupAdapter.ViewHolder(view);

        return vh;

    }


    //position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull final GroupAdapter.ViewHolder holder, final int position) {
        //시간 포맷
        long unixTime = (long) chatModels.get(position).getTimestamp();
        Date date = new Date(unixTime);
        writeTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String time = writeTimeFormat.format(date);
        //시간 출력
        holder.timestamp.setText(time);

        if(chatModels.get(position).getMsgType().equals("0")){ //메시지 타입이 0이면 텍스트
            holder.textView.setText(chatModels.get(position).getMsg());
        }else{//아니면 이미지뷰
            Glide.with(holder.sendPhoto.getContext()).load(chatModels.get(position).getMsg()).into(holder.sendPhoto);
        }

        ReaderCounter(position,holder.readNum);//읽음 표시

        //상대방 이름, 프로필 출력
        if(!chatModels.get(position).getUID().equals(currentUser.getUid())){
            holder.userName.setText(chatModels.get(position).getUserName());//상대방 이름

            database.getReference("userInfo").child(chatModels.get(position).getUID()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserModel um = dataSnapshot.getValue(UserModel.class);
                    if(!um.getProfile().equals("")){
                        Glide.with(holder.userImage.getContext())
                                .load(um.getProfile())
                                .apply(new RequestOptions().circleCrop())
                                .into(holder.userImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

        }


    }

    //개수만큼 아이템 생성
    @Override
    public int getItemCount() {
        return chatModels.size();
    }

    public void ReaderCounter(final int position, final TextView readNumber){

        database.getReference("ChatRoom").child(roomID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ChatRoomModel cm = dataSnapshot.getValue(ChatRoomModel.class);
                int count = cm.getUsers().size()-chatModels.get(position).getReadUsers().size();
                Log.d(TAG, "onDataChange: count="+count);
                if(count>0){
                    readNumber.setVisibility(View.VISIBLE);
                    readNumber.setText(String.valueOf(count));
                }else{
                    readNumber.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

}
