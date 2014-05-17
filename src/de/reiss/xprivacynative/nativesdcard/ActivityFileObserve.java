package de.reiss.xprivacynative.nativesdcard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import biz.bokhorst.xprivacy.ActivityBase;
import biz.bokhorst.xprivacy.PrivacyService;
import biz.bokhorst.xprivacy.R;
import de.reiss.xprivacynative.Global;

import java.io.File;
import java.util.ArrayList;

public class ActivityFileObserve extends ActivityBase {


    private static final int REQUEST_OBSERVE_FILE = 165455654;


    EditText filewatch_ed_chosenfile;
    ListView filewatch_lv_runningwatchers;
    Button filewatch_btn_choosefile;
    Button filewatch_btn_startwatching;

    FileObserveTaskListAdapter listAdapter;

    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check privacy service client
        if (!PrivacyService.checkClient())
            return;

        // Set layout

        mActivity = this;

        setContentView(R.layout.filewatch);

        filewatch_ed_chosenfile = (EditText) findViewById(R.id.filewatch_ed_chosenfile);
        filewatch_lv_runningwatchers = (ListView) findViewById(R.id.filewatch_lv_runningwatchers);

        if (Global.fileObserveTasks == null) {
            Global.fileObserveTasks = new ArrayList<FileObserveTask>();
        }
        listAdapter = new FileObserveTaskListAdapter(this, Global.fileObserveTasks);
        filewatch_lv_runningwatchers.setAdapter(listAdapter);

        initButtons();

        // Set title
        setTitle(String.format("%s - %s", getString(R.string.app_name), getString(R.string.title_fileobserve)));


        // Up navigation
        getActionBar().setDisplayHomeAsUpEnabled(true);


    }


    private void initButtons() {

        filewatch_btn_choosefile = (Button) findViewById(R.id.filewatch_btn_choosefile);
        filewatch_btn_choosefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                startFileChooser();
            }
        });


        filewatch_btn_startwatching = (Button) findViewById(R.id.filewatch_btn_startwatching);
        filewatch_btn_startwatching.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                final String chosenFile = getChosenFile();
                if (!TextUtils.isEmpty(chosenFile)) {


                    // create a new async Task with a unique id. yes I know it's ugly
                    FileObserveTask task = new FileObserveTask(mActivity, chosenFile);
                    while (Global.fileObserveTasks.contains(task)) {
                        task = new FileObserveTask(mActivity, chosenFile);
                    }
                    listAdapter.add(task);
                    listAdapter.notifyDataSetChanged();

                    task.startWatching();

                    final String toastText = "Started file watcher for file '" + chosenFile + "'";
                    Toast.makeText(mActivity, toastText, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getChosenFile() {
        if (filewatch_ed_chosenfile != null) {
            return filewatch_ed_chosenfile.getText().toString();
        }
        return "";
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_OBSERVE_FILE:
                    if (data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
                        // Get the file path
                        final File f = new File(data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH));
                        final String filepath = f.getPath();
                        if (filewatch_ed_chosenfile != null) {
                            filewatch_ed_chosenfile.setText(filepath);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }


    private void startFileChooser() {

        // Create a new Intent for the file picker activity
        Intent intent = new Intent(this, FilePickerActivity.class);

        // Set the initial directory to be the sdcard
        intent.putExtra(FilePickerActivity.EXTRA_FILE_PATH,
                Environment.getExternalStorageDirectory().toString());

        // Show hidden files
        intent.putExtra(FilePickerActivity.EXTRA_SHOW_HIDDEN_FILES, true);

        // Only make .xml files visible
//        ArrayList<String> extensions = new ArrayList<String>();
//        extensions.add(".xml");
//        intent.putExtra(FilePickerActivity.EXTRA_ACCEPTED_FILE_EXTENSIONS, extensions);

        // Start the activity
        startActivityForResult(intent, REQUEST_OBSERVE_FILE);

    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (inflater != null && PrivacyService.checkClient()) {
            inflater.inflate(R.menu.usage, menu);
            return true;
        } else
            return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (upIntent != null)
                    if (NavUtils.shouldUpRecreateTask(this, upIntent))
                        TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                    else
                        NavUtils.navigateUpTo(this, upIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
