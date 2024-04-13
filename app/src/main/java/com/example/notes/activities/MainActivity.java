package com.example.notes.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.notes.R;
import com.example.notes.adapters.NoteAdapter;
import com.example.notes.database.NoteDatabase;
import com.example.notes.entities.Note;
import com.example.notes.liseners.NoteListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NoteListener {
    private RecyclerView noteRecycler;
    private ArrayList<Note> noteList;
    private NoteAdapter noteAdapter;
    private static final int ACTION_SHOW_NOTE = 3;
    private static final int ACTION_CREATE_NOTE = 1;
    private static final int ACTION_UPDATE_NOTE = 2;
    private int onClickedPosition = -1;

    private final ActivityResultLauncher<Intent> createNoteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        getNotes(ACTION_CREATE_NOTE, false);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> updateNoteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                final Intent data = new Intent();
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        boolean isNoteDeleted = result.getData().getBooleanExtra("isNoteDeleted", false);

                        getNotes(ACTION_UPDATE_NOTE,isNoteDeleted);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noteList = new ArrayList<>();
        ImageView imageAddNoteMain = findViewById(R.id.imageAddMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNoteLauncher.launch(new Intent(getApplicationContext(), CreateNotesActivity.class));
            }
        });

        noteRecycler = findViewById(R.id.recyclerNote);
        noteRecycler.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteAdapter = new NoteAdapter(noteList, this,this);
        noteRecycler.setAdapter(noteAdapter);

        getNotes(ACTION_SHOW_NOTE, false);

        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (noteList.size() != 0){
                    noteAdapter.searchNotes(editable.toString());
                }
            }
        });
    }

    @Override
    public void onNoteClicked(Note note,int position) {

        Intent intent = new Intent(getApplicationContext(), CreateNotesActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note",  note);
        updateNoteLauncher.launch(intent);
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<Note> notes = NoteDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleNotes(notes, requestCode, isNoteDeleted);
                        noteAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void handleNotes(List<Note> notes, int requestCode, boolean isNoteDeleted) {
        if (requestCode == ACTION_SHOW_NOTE) {
            noteList.clear();
            noteList.addAll(notes);
        } else if (requestCode == ACTION_CREATE_NOTE) {
            noteList.add(0, notes.get(0));
            noteAdapter.notifyItemInserted(0);
            noteRecycler.smoothScrollToPosition(0);
        } else if (requestCode == ACTION_UPDATE_NOTE) {
            if (onClickedPosition >= 0 && onClickedPosition < noteList.size()) {
                Log.d("MyApp", "Updating note at position: " + onClickedPosition);
                noteList.remove(onClickedPosition);
                if (isNoteDeleted) {
                    Log.d("MyApp", "Note deleted at position: " + onClickedPosition);
                    noteAdapter.notifyItemRemoved(onClickedPosition);
                } else {
                    Log.d("MyApp", "Note updated at position: " + onClickedPosition);
                        noteList.add(onClickedPosition, notes.get(onClickedPosition));
                        noteAdapter.notifyItemChanged(onClickedPosition);
                    }
                }
            } else {
            Log.d("MyApp", "Note updated at position: " + onClickedPosition);
                onClickedPosition = -1;
            }
        }

    }

