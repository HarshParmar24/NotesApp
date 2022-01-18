package com.example.firenotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firenotes.authentication.Login;
import com.example.firenotes.authentication.Register;
import com.example.firenotes.model.Adapter;
import com.example.firenotes.model.Note;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerlayout;
    NavigationView nav_view;
    ActionBarDrawerToggle toggle;
    RecyclerView noteLists;
    Adapter adapter;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter<Note,NoteViewHolder> noteAdapter,noteAdapter2;
    FirebaseAuth fAuth;
    FirebaseUser user;
    //ImageView searchNotes;
    //EditText searchIext;
    SearchView searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getActionBar().setTitle("NotesApp");

        //searchNotes=findViewById(R.id.searchNotes);

        noteLists=findViewById(R.id.notelist);

        //To create navigation side
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fStore=FirebaseFirestore.getInstance();
        fAuth=FirebaseAuth.getInstance();
        user=FirebaseAuth.getInstance().getCurrentUser();
        //String uid = (user != null) ? user.getUid() : "";

        Query query=fStore.collection("notes")
                            .document(user.getUid())
                                .collection("userNotes")
                                .orderBy("title",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Note> allNotes= new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query,Note.class)
                .build();


        noteAdapter=new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {
                @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull final Note note) {
                noteViewHolder.noteTitle.setText(note.getTitle());
                noteViewHolder.noteContent.setText(note.getContent());
                final int code=getRandomColor();
                noteViewHolder.mCardView.setCardBackgroundColor(noteViewHolder.view.getResources().getColor(code,null));

                String docId=noteAdapter.getSnapshots().getSnapshot(i).getId();

                noteViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i=new Intent(view.getContext(), NoteDetails.class);
                        i.putExtra("content",note.getContent());
                        i.putExtra("title",note.getTitle());
                        i.putExtra("code",code);
                        i.putExtra("noteId",docId);
                        view.getContext().startActivity(i);
                    }
                });

                ImageView menuIcon=noteViewHolder.view.findViewById(R.id.menuIcon);
                menuIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String docId=noteAdapter.getSnapshots().getSnapshot(i).getId();
                        PopupMenu menu=new PopupMenu(view.getContext(),view);
                        menu.setGravity(Gravity.END);

                        menu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                Intent i=new Intent(view.getContext(),EditNote.class);
                                i.putExtra("title",note.getTitle());
                                i.putExtra("content",note.getContent());
                                i.putExtra("noteId",docId);
                                startActivity(i);
                                return false;
                            }
                        });

                        menu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                DocumentReference docRef=fStore.collection("notes").document(user.getUid()).collection("userNotes").document(docId);
                                docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this, "Note Deleted Successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Some error in deleting the notes..", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });

                        menu.show();
                    }
                });
            }


            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout,parent,false);
                return new NoteViewHolder(view);
            }
        };



        searchText=findViewById(R.id.searchText);
        searchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                processSearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                processSearch(s);
                return false;
            }
        });



        searchText.setOnCloseListener(new SearchView.OnCloseListener() {

            @Override
            public boolean onClose() {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
                return false;
            }
        });


        /*
        searchIext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        searchNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Query query=fStore.collection("notes")
                        .document(user.getUid())
                        .collection("userNotes")
                        .orderBy("content",Query.Direction.DESCENDING).startAt(searchIext.getText().toString()).endAt(searchIext.getText().toString());
                FirestoreRecyclerOptions<Note> allNotes= new FirestoreRecyclerOptions.Builder<Note>()
                        .setQuery(query,Note.class)
                        .build();

                noteAdapter=new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {
                    @Override
                    protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull final Note note) {
                        noteViewHolder.noteTitle.setText(note.getTitle());
                        noteViewHolder.noteContent.setText(note.getContent());
                        final int code=getRandomColor();
                        noteViewHolder.mCardView.setCardBackgroundColor(noteViewHolder.view.getResources().getColor(code,null));

                        String docId=noteAdapter.getSnapshots().getSnapshot(i).getId();

                        noteViewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i=new Intent(view.getContext(), NoteDetails.class);
                                i.putExtra("content",note.getContent());
                                i.putExtra("title",note.getTitle());
                                i.putExtra("code",code);
                                i.putExtra("noteId",docId);
                                view.getContext().startActivity(i);
                            }
                        });

                        ImageView menuIcon=noteViewHolder.view.findViewById(R.id.menuIcon);
                        menuIcon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final String docId=noteAdapter.getSnapshots().getSnapshot(i).getId();
                                PopupMenu menu=new PopupMenu(view.getContext(),view);
                                menu.setGravity(Gravity.END);

                                menu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        Intent i=new Intent(view.getContext(),EditNote.class);
                                        i.putExtra("title",note.getTitle());
                                        i.putExtra("content",note.getContent());
                                        i.putExtra("noteId",docId);
                                        startActivity(i);
                                        return false;
                                    }
                                });

                                menu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        DocumentReference docRef=fStore.collection("notes").document(user.getUid()).collection("userNotes").document(docId);
                                        docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(MainActivity.this, "Note Deleted Successfully.", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, "Some error in deleting the notes..", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        return false;
                                    }
                                });

                                menu.show();
                            }
                        });
                    }


                    @NonNull
                    @Override
                    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout,parent,false);
                        return new NoteViewHolder(view);
                    }
                };

                noteLists.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
                noteLists.setAdapter(noteAdapter);

                noteAdapter.startListening();
            }
        });

        */


        drawerlayout=findViewById(R.id.drawer);
        nav_view=findViewById(R.id.nav_view);

        nav_view.setNavigationItemSelectedListener(this);

        toggle=new ActionBarDrawerToggle(this,drawerlayout,toolbar,R.string.open,R.string.close);
        drawerlayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

//        List<String> title=new ArrayList<>();
//        List<String> content=new ArrayList<>();
//
//        title.add("First Note title");
//        content.add("First note content sample.");
//
//        title.add("Second Note title");
//        content.add("Second note content sample.Second note content sample.Second note content sample.");
//
//        title.add("Third Note title");
//        content.add("Third note content sample.");

//        adapter=new Adapter(title,content);

        noteLists.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteLists.setAdapter(noteAdapter);

        //Here index is '0' because we have onlu only one header view which is in the navigation view.
        //If we have more than one header view then we have to give index number according to that
        View headerView=nav_view.getHeaderView(0);
        TextView userEmail=headerView.findViewById(R.id.userDisplayEmail);
        TextView userName=headerView.findViewById(R.id.userDisplayName);

        if(user.isAnonymous())
        {
            userEmail.setVisibility(View.GONE);
            userName.setText("Temporary User");
        }
        else {
            userEmail.setText(user.getEmail());
            userName.setText(user.getDisplayName());
        }



        FloatingActionButton fab = findViewById(R.id.addNoteFloat);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(),AddNote.class));
            }
        });
    }


    private void processSearch(String s) {
        Query query=fStore.collection("notes")
                .document(user.getUid())
                .collection("userNotes")
                .orderBy("title",Query.Direction.DESCENDING).startAt(s).endAt(s);
        FirestoreRecyclerOptions<Note> allNotes= new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query,Note.class)
                .build();

        noteAdapter2=new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull final Note note) {
                noteViewHolder.noteTitle.setText(note.getTitle());
                noteViewHolder.noteContent.setText(note.getContent());
                final int code=getRandomColor();
                noteViewHolder.mCardView.setCardBackgroundColor(noteViewHolder.view.getResources().getColor(code,null));

                String docId=noteAdapter.getSnapshots().getSnapshot(i).getId();

                noteViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i=new Intent(view.getContext(), NoteDetails.class);
                        i.putExtra("content",note.getContent());
                        i.putExtra("title",note.getTitle());
                        i.putExtra("code",code);
                        i.putExtra("noteId",docId);
                        view.getContext().startActivity(i);
                    }
                });

                ImageView menuIcon=noteViewHolder.view.findViewById(R.id.menuIcon);
                menuIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String docId=noteAdapter.getSnapshots().getSnapshot(i).getId();
                        PopupMenu menu=new PopupMenu(view.getContext(),view);
                        menu.setGravity(Gravity.END);

                        menu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                Intent i=new Intent(view.getContext(),EditNote.class);
                                i.putExtra("title",note.getTitle());
                                i.putExtra("content",note.getContent());
                                i.putExtra("noteId",docId);
                                startActivity(i);
                                return false;
                            }
                        });

                        menu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                DocumentReference docRef=fStore.collection("notes").document(user.getUid()).collection("userNotes").document(docId);
                                docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this, "Note Deleted Successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Some error in deleting the notes..", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });

                        menu.show();
                    }
                });
            }


            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout,parent,false);
                return new NoteViewHolder(view);
            }
        };

        noteAdapter2.startListening();
        //noteLists.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteLists.setAdapter(noteAdapter2);

    }



    private void searchNote(CharSequence charSequence) {

    }

    // On selecting NavigationMenu items
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerlayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId())
        {
            case R.id.addNote:
                startActivity(new Intent(this,AddNote.class));
                break;

            case R.id.sync:
                if(user.isAnonymous()) {
                    startActivity(new Intent(this, Login.class));
                }
                else {
                    Toast.makeText(this, "Already Synced.", Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.shareapp:
                //shareYourApp();
                Toast.makeText(this, "App is sharing.", Toast.LENGTH_SHORT).show();
                break;

            case R.id.txtRecognition:
                startActivity(new Intent(this,ImageToText.class));
                break;

            case R.id.screenWriting:
                startActivity(new Intent(this,ScreenWriting.class));
                break;

            case R.id.logout:
                checkUser();
                break;

            default:
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
                break;

        }
        return false;
    }

    private void shareYourApp() {
        ApplicationInfo apinfo=getApplicationContext().getApplicationInfo();
        String apkpath = apinfo.sourceDir;
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.android.package-archive");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(apkpath)));
        startActivity(Intent.createChooser(intent,"ShareVia"));

    }

    private void checkUser() {
        if(user.isAnonymous())
        {
            displayAlertDialogBox();
        }
        else{
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(),Splash.class));
            finish();
        }
    }

    private void displayAlertDialogBox() {
        AlertDialog.Builder warning=new AlertDialog.Builder(this)
                .setTitle("Are you sure ?")
                .setMessage("Hey User, you are logged in with Temporary account. Logging out will delete all the Notes.")
                .setPositiveButton("Sync Note", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(getApplicationContext(), Register.class));
                        finish();
                    }
                }).setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //To Delete all the notes Created by Annonymous User
                        //To delete Annoymous User

                        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startActivity(new Intent(getApplicationContext(),Splash.class));
                                finish();
                            }
                        });
                    }
                });
        warning.show();
    }

    // To create option Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    // On selecting optionMenu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.settings)
        {
            Toast.makeText(this, "Setting Menu is clicked.", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle,noteContent;
        View view;
        CardView mCardView;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle=itemView.findViewById(R.id.titles);
            noteContent=itemView.findViewById(R.id.content);
            view=itemView;
            mCardView=itemView.findViewById(R.id.noteCard);
        }
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

    //Whenever we are reopening app or are in any activity,
    // we should accept the change in data ,
    // which is changing or editing data in firestore or app


    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(noteAdapter!=null)
        {
            noteAdapter.stopListening();
        }
    }
}
