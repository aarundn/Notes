package com.example.notes.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.notes.R;
import com.example.notes.entities.Note;
import com.example.notes.liseners.NoteListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>{

    private List<Note> noteList;
    private Context context;
    private NoteListener noteListener;
    private Timer timer;
    private List<Note> notesSource;
    public NoteAdapter(List<Note> noteList, NoteListener noteListener, Context context){
        this.noteList = noteList;
        this.noteListener = noteListener;
        this.notesSource = noteList;
        this.context = context;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note
                ,parent
                ,false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.setNote(noteList.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteListener.onNoteClicked(noteList.get(position),holder.getLayoutPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder{

             TextView textTitle, textSubTitle, textDateTime;
             LinearLayout layoutNote;
             RoundedImageView imageNote;
            public NoteViewHolder(@NonNull View itemView) {
                super(itemView);

                textTitle = itemView.findViewById(R.id.textTitle);
                textSubTitle = itemView.findViewById(R.id.textSubTitle);
                textDateTime = itemView.findViewById(R.id.textDateTime);
                layoutNote = itemView.findViewById(R.id.layoutNote);
                imageNote = itemView.findViewById(R.id.imageNoteTv);

            }
             @SuppressLint("CheckResult")
             void setNote(Note note){
                textTitle.setText(note.getTitle());
                if (note.getSubtitle().trim().isEmpty()){
                    textSubTitle.setVisibility(View.GONE);
                }else{
                    textSubTitle.setText(note.getSubtitle());
                }
                textDateTime.setText(note.getDateTime());
                     if (note.imagePath != null && !note.imagePath.isEmpty()) {
                         Glide.with(itemView)
                                 .load(Uri.parse(note.getImagePath())) // Load the image from the file path
                                 .into(imageNote);
                        imageNote.setVisibility(View.VISIBLE);
                     } else {
                         imageNote.setVisibility(View.GONE);
                     }
                    GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
                     if (note.getColor() != null){
                         gradientDrawable.setColor(Color.parseColor(note.getColor()));
                     } else {
                         gradientDrawable.setColor(Color.parseColor("#333333"));
                     }

                 }
            }

        android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
        public void searchNotes(final String searchKeyword){
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (searchKeyword.trim().isEmpty()){
                            noteList = notesSource;
                        }else{
                            ArrayList<Note> temp = new ArrayList<>();
                            for (Note note: notesSource){
                                if (note.getTitle().toLowerCase().contains(searchKeyword)||
                                    note.getSubtitle().toLowerCase().contains(searchKeyword)||
                                    note.getNoteText().toLowerCase().contains(searchKeyword)){
                                    temp.add(note);
                                }
                            }
                            noteList = temp;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }
                },500);
        }
        public void cancelTimer(){
            if (timer != null){
                timer.cancel();
            }
        }

}
