package com.example.notes.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notes.R;
import com.example.notes.database.NoteDatabase;
import com.example.notes.entities.Note;
import com.example.notes.utils.RealPathUtil;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateNotesActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST_CODE = 1000;
    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteText;
    private TextView textDateTime;
    private View viewSubtitleIndicator;
    private String selectedNoteColor;
    private ImageView imageNote;
    private TextView textWebUrl;
    private LinearLayout layoutWebUrl;
    private static final int REQUEST_CODE_PERMISSION = 1;
    private String selectedImagePath;
    private AlertDialog AddUrlDialog;
    private AlertDialog dialogDeleteNote;
    private Note aleardyAvailableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_creat_notes);
        ImageView imageBack = findViewById(R.id.imageBack);
        viewSubtitleIndicator = findViewById(R.id.subtitleIndicator);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteText = findViewById(R.id.inputNote);
        textDateTime = findViewById(R.id.textDateTime);
        imageNote = findViewById(R.id.imageNoteCreate);
        textWebUrl = findViewById(R.id.textWebURL);
        layoutWebUrl = findViewById(R.id.layoutWebURL);

        textDateTime.setText(
                new SimpleDateFormat("EEEE,dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date())
        );
        ImageView imageSave = findViewById(R.id.imageSave);
        imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });
        selectedNoteColor = "#333333";
        selectedImagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            aleardyAvailableNote =  getIntent().getParcelableExtra("note");
            setViewOrUpdateNote();
        }

            findViewById(R.id.imageRemoveWebURL).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    textWebUrl.setText(null);
                    layoutWebUrl.setVisibility(View.GONE);

                }
            });
        initMiscellaneous();
        setSubtitleIndicatorColor();
    }
    private  void setViewOrUpdateNote(){

            inputNoteTitle.setText(aleardyAvailableNote.getTitle());
            inputNoteSubtitle.setText(aleardyAvailableNote.getSubtitle());
            inputNoteText.setText(aleardyAvailableNote.getNoteText());
            textDateTime.setText(aleardyAvailableNote.getDateTime());
            if(aleardyAvailableNote.getImagePath() != null && !aleardyAvailableNote.getImagePath().trim().isEmpty()){
                imageNote.setImageURI(Uri.parse(aleardyAvailableNote.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
                selectedImagePath = aleardyAvailableNote.getImagePath();
          }
            if (aleardyAvailableNote.getWebLink() != null && !aleardyAvailableNote.getWebLink().trim().isEmpty()){
                textWebUrl.setText(aleardyAvailableNote.getWebLink());
                textWebUrl.setVisibility(View.VISIBLE);
            }
        Log.d("Debug", "Image Path: " + aleardyAvailableNote.getImagePath());
        Log.d("Debug", "Web Link: " + aleardyAvailableNote.getWebLink());

    }

    private void saveNote()
    {
        if (inputNoteTitle.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this,"Note title can't be empty!",Toast.LENGTH_LONG).show();
            return;
        }
        if (inputNoteSubtitle.getText().toString().trim().isEmpty() && inputNoteText.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this,"Note can't be empty!",Toast.LENGTH_LONG).show();
            return;
        }
        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setNoteText(inputNoteText.getText().toString());
        note.setDateTime(textDateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImagePath);

        if (layoutWebUrl.getVisibility() == View.VISIBLE){
            note.setWebLink(textWebUrl.getText().toString());
        }
        if (aleardyAvailableNote != null){
            note.setId(aleardyAvailableNote.getId());
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {

            @Override
            public void run() {
                NoteDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                });
            }
        });

    }
    private void initMiscellaneous(){
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);                }
            }
        });
        //********************************** SELECT COLOR ****************************************//
        //********************************** SELECT COLOR ****************************************//
        final ImageView imageColor1 = layoutMiscellaneous.findViewById(R.id.imageColor1);
        final ImageView imageColor2 = layoutMiscellaneous.findViewById(R.id.imageColor2);
        final ImageView imageColor3 = layoutMiscellaneous.findViewById(R.id.imageColor3);
        final ImageView imageColor4 = layoutMiscellaneous.findViewById(R.id.imageColor4);
        final ImageView imageColor5 = layoutMiscellaneous.findViewById(R.id.imageColor5);

        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#333333";
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#FDBE3B";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#FF4842";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#3A52FC";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#000000";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_done);
                setSubtitleIndicatorColor();
            }
        });
        if (aleardyAvailableNote != null) {

            String color = aleardyAvailableNote.getColor();
        if (color != null && color.trim().isEmpty()) {
            switch (aleardyAvailableNote.getColor()) {
                case "#FDBE3B":
                    layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3A52FC":
                    layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                    break;

            }
        }
        }
        //********************************** ADD IMAGE ****************************************//
        //********************************** ADD IMAGE ****************************************//
        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                    selectImage();

            }
        });
        layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog();
            }
        });
        if (aleardyAvailableNote != null){
            layoutMiscellaneous.findViewById(R.id.layoutDelete).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layoutDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }

    }

    private void showDeleteNoteDialog(){
        if (dialogDeleteNote == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNotesActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    (ViewGroup) findViewById(R.id.layoutDeleteContainer)
            );
            builder.setView(view);
            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null){
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());
                    executorService.execute(new Runnable() {

                        @Override
                        public void run() {
                            NoteDatabase.getDatabase(getApplicationContext()).noteDao().deleteNote(aleardyAvailableNote);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent();
                                    intent.putExtra("isNoteDeleted", true);
                                    setResult(RESULT_OK,intent);
                                    finish();
                                }
                            });
                        }
                    });
                }
            });
            view.findViewById(R.id.textCancelNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDeleteNote.dismiss();
                }
            });
        }
        dialogDeleteNote.show();
    } // END OF ShowDeleteDialog() METHOD


    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }



    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,PICK_IMAGE_REQUEST_CODE);

    }
    ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
           result ->{
                if (result.getResultCode() == RESULT_OK){
                    if (result.getData() != null){
                        String realPath;
                        // SDK < API11
                        if (Build.VERSION.SDK_INT < 11)
                            realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, result.getData().getData());

                            // SDK >= 11 && SDK < 19
                        else if (Build.VERSION.SDK_INT < 19)
                            realPath = RealPathUtil.getRealPathFromURI_API11to18(this,result.getData().getData());

                            // SDK > 19 (Android 4.4)
                        else
                            realPath = RealPathUtil.getRealPathFromURI_API19(this, result.getData().getData());

                         try {
                             assert realPath != null;

                             imageNote.setImageResource(Integer.parseInt(realPath));
                             imageNote.setVisibility(View.VISIBLE);
                             Log.v("URI","image uri:"+ realPath);
                             selectedImagePath = realPath;
                             Log.v("URI", "selected image uri"+ selectedImagePath);
                         }catch (Exception e){
                             e.printStackTrace();
                         }
                         }
                }
            }
    );

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String realPath;
            realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());
                    try {
                        assert realPath != null;
                        InputStream inputStream = getContentResolver().openInputStream(Uri.parse(realPath));
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        Log.v("URI","image uri:"+ realPath);
                        selectedImagePath = realPath;
                        Log.v("URI", "selected image uri"+ selectedImagePath);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }

            // Now you have the image URI, you can use it to display or process the image
            // ...
        }



    private void showAddURLDialog(){
        if (AddUrlDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNotesActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer)
            );

            builder.setView(view);
            AddUrlDialog = builder.create();
            if (AddUrlDialog.getWindow() != null){
                AddUrlDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.inputUrl);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inputURL.getText().toString().trim().isEmpty()){
                        Toast.makeText(CreateNotesActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    }else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()){
                        Toast.makeText(CreateNotesActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    }else {
                        textWebUrl.setText(inputURL.getText().toString());
                        layoutWebUrl.setVisibility(View.VISIBLE);
                        AddUrlDialog.dismiss();
                    }
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AddUrlDialog.dismiss();
                }
            });
        }

        AddUrlDialog.show();

    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                // Permission denied, handle this case (e.g., display a message)
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



}