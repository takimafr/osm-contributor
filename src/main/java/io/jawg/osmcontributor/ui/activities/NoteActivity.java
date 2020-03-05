/**
 * Copyright (C) 2019 Takima
 *
 * This file is part of OSM Contributor.
 *
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.Comment;
import io.jawg.osmcontributor.model.entities.Note;
import io.jawg.osmcontributor.model.events.NoteLoadedEvent;
import io.jawg.osmcontributor.model.events.NoteSavedEvent;
import io.jawg.osmcontributor.model.events.PleaseLoadNoteEvent;
import io.jawg.osmcontributor.rest.events.SyncFinishUploadNote;
import io.jawg.osmcontributor.rest.events.error.SyncConflictingNoteErrorEvent;
import io.jawg.osmcontributor.rest.events.error.SyncUnauthorizedEvent;
import io.jawg.osmcontributor.ui.adapters.CommentAdapter;
import io.jawg.osmcontributor.ui.events.map.PleaseApplyNewComment;
import io.jawg.osmcontributor.ui.events.note.ApplyNewCommentFailedEvent;


public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_ID = "NOTE_ID";

    private Note note;
    private CommentAdapter adapter;
    private List<Comment> comments = new ArrayList<>();
    private long noteId = -1;
    private ActionBar actionBar;

    public static final String CLOSE = "Close";
    public static final String REOPEN = "re-Opened";
    public static final String COMMENT = "Comment";

    @Inject
    EventBus eventBus;

    @BindView(R.id.comments)
    ListView commentsListView;

    @BindView(R.id.send_comment)
    Button addCommentButton;

    @BindView(R.id.comment_edit_text)
    EditText newCommentEditText;

    @BindView(R.id.action_spinner)
    Spinner newCommentActionSpinner;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @OnClick(R.id.send_comment)
    void addCommentOnClick() {
        if (newCommentEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.comment_text_cant_be_null, Toast.LENGTH_SHORT).show();
        } else {
            if (note.getStatus().equals(Note.STATUS_SYNC)) {
                Toast.makeText(this, R.string.comment_synchronizing, Toast.LENGTH_SHORT).show();
            } else {
                eventBus.post(new PleaseApplyNewComment(note, newCommentActionSpinner.getSelectedItem().toString(), newCommentEditText.getText().toString()));
                newCommentEditText.getText().clear();
                closeKeyboard();
            }
        }
    }


    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_HIDDEN, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();

        noteId = intent.getLongExtra(NOTE_ID, -1);

        if (savedInstanceState != null) {
            noteId = savedInstanceState.getLong(NOTE_ID);
        }

        adapter = new CommentAdapter(this, comments);
        commentsListView.setAdapter(adapter);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == android.R.id.home) {
            if (!newCommentEditText.getText().toString().isEmpty()) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                alertDialog.setTitle(R.string.quit_note_title);
                alertDialog.setMessage(R.string.quit_note_message);
                alertDialog.setPositiveButton(R.string.quit_note_positive_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alertDialog.setNegativeButton(R.string.quit_note_negative_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
            } else {
                finish();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
        if (noteId != -1) {
            eventBus.post(new PleaseLoadNoteEvent(noteId));
        }

    }

    @Override
    protected void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(NOTE_ID, noteId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoteLoadedEvent(NoteLoadedEvent event) {
        note = event.getNote();
        if (note.getComments() != null) {
            adapter.addAll(note.getComments());
        }

        initSpinner();

        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_note, note.getStatus()));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncFinishUploadNote(SyncFinishUploadNote event) {
        refreshView(event.getNote());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApplyNewCommentFailedEvent(ApplyNewCommentFailedEvent event) {
        Toast.makeText(this, getString(R.string.failed_apply_comment), Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncConflictingNoteErrorEvent(SyncConflictingNoteErrorEvent event) {
        if (event.getNote().getBackendId().equals(note.getBackendId())) {
            Toast.makeText(this, getResources().getString(R.string.note_already_closed), Toast.LENGTH_SHORT).show();
            refreshView(event.getNote());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncUnauthorizedEvent(SyncUnauthorizedEvent event) {
        Toast.makeText(this, R.string.couldnt_connect_retrofit, Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoteSavedEvent(NoteSavedEvent event) {
        refreshView(event.getNote());
    }

    private void refreshView(Note note) {
        if (note.getBackendId().equals(this.note.getBackendId())) {
            this.note = note;

            //update all comments
            adapter.addAll(note.getComments());

            //update spinner data
            initSpinner();

            //go to the end of the listView
            commentsListView.setSelection(adapter.getCount() - 1);

            //set title
            if (actionBar != null) {
                actionBar.setTitle(getString(R.string.title_note, note.getStatus()));
            }
        }
    }

    private void initSpinner() {
        ArrayList<String> spinnerArray = new ArrayList<>();

        switch (note.getStatus()) {
            case Note.STATUS_OPEN:
                spinnerArray.add(COMMENT);
                spinnerArray.add(CLOSE);
                break;

            case Note.STATUS_CLOSE:
                spinnerArray.add(REOPEN);
                break;

        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newCommentActionSpinner.setAdapter(spinnerArrayAdapter);
    }
}
