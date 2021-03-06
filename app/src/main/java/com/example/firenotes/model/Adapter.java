package com.example.firenotes.model;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firenotes.NoteDetails;
import com.example.firenotes.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    List<String> title;
    List<String> content;

    public Adapter(List<String> title,List<String> content)
    {
        this.title=title;
        this.content=content;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.noteTitle.setText(title.get(position));
        holder.noteContent.setText(content.get(position));
        final int code=getRandomColor();
        holder.mCardView.setCardBackgroundColor(holder.view.getResources().getColor(code,null));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(view.getContext(), NoteDetails.class);
                i.putExtra("content",content.get(position));
                i.putExtra("title",title.get(position));
                i.putExtra("code",code);
                view.getContext().startActivity(i);
            }
        });
    }

    private int getRandomColor() {
        List<Integer> colorCode=new ArrayList<>();
        colorCode.add(R.color.blue);
        colorCode.add(R.color.yellow);
        colorCode.add(R.color.skyblue);
        colorCode.add(R.color.lightPurple);
        colorCode.add(R.color.lightGreen);
        colorCode.add(R.color.pink);
        colorCode.add(R.color.red);
        colorCode.add(R.color.greenlight);
        colorCode.add(R.color.notgreen);

        Random randomColor=new Random();
        int number=randomColor.nextInt(colorCode.size());
        return colorCode.get(number);
    }

    @Override
    public int getItemCount() {
        return title.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle,noteContent;
        View view;
        CardView mCardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle=itemView.findViewById(R.id.titles);
            noteContent=itemView.findViewById(R.id.content);
            view=itemView;
            mCardView=itemView.findViewById(R.id.noteCard);
        }
    }
}
