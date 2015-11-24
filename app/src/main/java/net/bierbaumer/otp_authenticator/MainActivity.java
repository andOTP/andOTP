package net.bierbaumer.otp_authenticator;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements  ActionMode.Callback {

    private ArrayList<Entry> entries;
    private EntriesAdapter adapter;
    private FloatingActionButton fab;

    private Entry nextSelection = null;
    private void showNoAccount(){
        Snackbar noAccountSnackbar = Snackbar.make(fab, R.string.no_accounts, Snackbar.LENGTH_INDEFINITE)
                .setAction("Add", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scanQRCode();
                    }
                });
        noAccountSnackbar.show();
    }

    private void scanQRCode(){
        new IntentIntegrator(MainActivity.this)
                .setCaptureActivity(CaptureActivityAnyOrientation.class)
                .setOrientationLocked(false)
                .initiateScan();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.action_scan);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanQRCode();
            }
        });

        final ListView listView = (ListView) findViewById(R.id.listView);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        entries = SettingsHelper.load(this);

        adapter = new EntriesAdapter();
        adapter.setEntries(entries);

        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                nextSelection = entries.get(i);
                startActionMode(MainActivity.this);

                return true;
            }
        });

        if(entries.isEmpty()){
            showNoAccount();
        }

        int progress = ((int) (System.currentTimeMillis() / 1000) % 30);
        progressBar.setProgress((int) (1000.0 * progress / 30.0));

        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 1000);
        animation.setDuration((30 - progress) * 1000);
        animation.setInterpolator(new LinearInterpolator());
        animation.start();


        final Handler handler = new Handler();
        final Runnable handlerTask = new Runnable()
        {
            @Override
            public void run() {
                int progress =  (int) (System.currentTimeMillis() / 1000) % 30 ;

                if(progress == 0){
                    progressBar.setProgress(progress);

                    ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 1000);
                    animation.setDuration(30 * 1000);
                    animation.setInterpolator(new LinearInterpolator());
                    animation.start();
                }

                for(int i =0;i< adapter.getCount();i++){
                    if(progress == 0 || adapter.getItem(i).getCurrentOTP() == null){
                        adapter.getItem(i).setCurrentOTP(TOTPHelper.generate(adapter.getItem(i).getSecret()));
                    }
                }
                adapter.notifyDataSetChanged();

                handler.postDelayed(this, 1000);
            }
        };
        handlerTask.run();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                Entry e = new Entry(intent.getStringExtra(Intents.Scan.RESULT));
                e.setCurrentOTP(TOTPHelper.generate(e.getSecret()));
                entries.add(e);
                SettingsHelper.store(this, entries);

                adapter.notifyDataSetChanged();

                Snackbar.make(fab, "Account added", Snackbar.LENGTH_LONG).show();
            } catch (Exception e) {
                Snackbar.make(fab, "Invalid QR Code", Snackbar.LENGTH_LONG).setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);

                        if(entries.isEmpty()){
                            showNoAccount();
                        }
                    }
                }).show();

                return;
            }
        }

        if(entries.isEmpty()){
            showNoAccount();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_about){
            WebView view = (WebView) LayoutInflater.from(this).inflate(R.layout.dialog_about, null);
            view.loadUrl("file:///android_res/raw/about.html");
            new AlertDialog.Builder(this).setView(view).show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_edit, menu);

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        adapter.setCurrentSelection(nextSelection);
        adapter.notifyDataSetChanged();
        actionMode.setTitle(adapter.getCurrentSelection().getLabel());

        return true;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.action_delete) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Remove " + adapter.getCurrentSelection().getLabel() + "?");
            alert.setMessage("Are you sure you want do remove this account?");

            alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    entries.remove(adapter.getCurrentSelection());

                    Snackbar.make(fab, "Account removed", Snackbar.LENGTH_LONG).setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            super.onDismissed(snackbar, event);

                            if (entries.isEmpty()) {
                                showNoAccount();
                            }
                        }
                    }).show();

                    actionMode.finish();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                    actionMode.finish();
                }
            });

            alert.show();

            return true;
        }
        else if (id == R.id.action_edit) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Rename");

            final EditText input = new EditText(this);
            input.setText(adapter.getCurrentSelection().getLabel());
            alert.setView(input);

            alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    adapter.getCurrentSelection().setLabel(input.getEditableText().toString());
                    actionMode.finish();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                    actionMode.finish();
                }
            });

            alert.show();

            return true;
        }

        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        adapter.setCurrentSelection(null);
        adapter.notifyDataSetChanged();

        SettingsHelper.store(this, entries);
    }
}