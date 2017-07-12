/*
 * Copyright (C) 2017 Jakob Nixdorf
 * Copyright (C) 2015 Bruno Bierbaumer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.shadowice.flocke.andotp;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.shadowice.flocke.andotp.ItemTouchHelper.SimpleItemTouchHelperCallback;

public class MainActivity extends AppCompatActivity {
    private EntriesCardAdapter adapter;
    private FloatingActionMenu floatingActionMenu;
    private SimpleItemTouchHelperCallback touchHelperCallback;

    private Handler handler;
    private Runnable handlerTask;

    private static final int PERMISSIONS_REQUEST_WRITE_EXPORT = 42;
    private static final int PERMISSIONS_REQUEST_READ_IMPORT = 41;

    private static final int INTENT_OPEN_DOCUMENT = 24;
    private static final int INTENT_SAVE_DOCUMENT= 23;
    private static final int INTENT_SETTINGS = 22;
    private static final int INTENT_AUTHENTICATE = 21;

    private static final String DEFAULT_BACKUP_FILENAME = "otp_accounts.json";
    private static final String DEFAULT_BACKUP_MIMETYPE = "application/json";

    // QR code scanning
    private void scanQRCode(){
        new IntentIntegrator(MainActivity.this)
                .setOrientationLocked(false)
                .setBeepEnabled(false)
                .initiateScan();
    }

    // Manual data entry
    private void enterDetails() {
        ViewGroup container = (ViewGroup) findViewById(R.id.main_content);
        View inputView = getLayoutInflater().inflate(R.layout.manual_entry, container, false);

        final Spinner typeInput = (Spinner) inputView.findViewById(R.id.manual_type);
        final EditText labelInput = (EditText) inputView.findViewById(R.id.manual_label);
        final EditText secretInput = (EditText) inputView.findViewById(R.id.manual_secret);
        final EditText periodInput = (EditText) inputView.findViewById(R.id.manual_period);

        typeInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, Entry.OTPType.values()));
        periodInput.setText(Integer.toString(TOTPHelper.TOTP_DEFAULT_PERIOD));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title_manual_entry)
                .setView(inputView)
                .setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Entry.OTPType type = (Entry.OTPType) typeInput.getSelectedItem();
                        if (type == Entry.OTPType.TOTP) {
                            String label = labelInput.getText().toString();
                            String secret = secretInput.getText().toString();
                            int period = Integer.parseInt(periodInput.getText().toString());

                            Entry e = new Entry(type, secret, period, label);
                            e.setCurrentOTP(TOTPHelper.generate(e.getSecret(), e.getPeriod()));
                            adapter.addEntry(e);
                            adapter.saveEntries();
                        } else if (type == Entry.OTPType.HOTP) {
                            Toast.makeText(getBaseContext(), R.string.toast_tmp_hotp, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .create()
                .show();
    }

    // About dialog
    private void showAbout() {
        ViewGroup container = (ViewGroup) findViewById(R.id.main_content);
        View messageView = getLayoutInflater().inflate(R.layout.dialog_about, container, false);

        String versionName = "";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView versionText = (TextView) messageView.findViewById(R.id.about_version);
        versionText.setText(versionName);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
                .setIcon(R.mipmap.ic_launcher)
                .setView(messageView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .create()
                .show();
    }

    // Export to JSON
    private void doExportJSON(Uri uri) {
        if (StorageHelper.isExternalStorageWritable()) {
            boolean success = DatabaseHelper.exportAsJSON(this, uri);

            if (success)
                Toast.makeText(this, R.string.toast_export_success, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, R.string.toast_export_failed, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.toast_storage_not_accessible, Toast.LENGTH_LONG).show();
        }
    }

    private void exportJSONWithPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            exportJSONWithSelector();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXPORT);
        }
    }

    private void exportJSONWithSelector() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(DEFAULT_BACKUP_MIMETYPE);
        intent.putExtra(Intent.EXTRA_TITLE, DEFAULT_BACKUP_FILENAME);
        startActivityForResult(intent, INTENT_SAVE_DOCUMENT);
    }

    private void exportJSONWithWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.dialog_title_security_warning)
                .setMessage(R.string.dialog_msg_export_warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        exportJSONWithPermissions();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create()
                .show();
    }

    // Import from JSON
    private void doImportJSON(Uri uri) {
        if (StorageHelper.isExternalStorageReadable()) {
            boolean success = DatabaseHelper.importFromJSON(this, uri);

            if (success) {
                adapter.loadEntries();
                Toast.makeText(this, R.string.toast_import_success, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.toast_import_failed, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.toast_storage_not_accessible, Toast.LENGTH_LONG).show();
        }
    }

    private void importJSONWithSelector() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, INTENT_OPEN_DOCUMENT);
    }

    private void importJSONWithPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            importJSONWithSelector();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_IMPORT);
        }
    }

    // Permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
       if (requestCode == PERMISSIONS_REQUEST_WRITE_EXPORT) {
           if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               exportJSONWithSelector();
           } else {
               Toast.makeText(this, R.string.toast_storage_permissions, Toast.LENGTH_LONG).show();
           }
       } else if (requestCode == PERMISSIONS_REQUEST_READ_IMPORT) {
           if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               importJSONWithSelector();
           } else {
               Toast.makeText(this, R.string.toast_storage_permissions, Toast.LENGTH_LONG).show();
           }
       } else {
           super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       }
    }

    // Initialize the main application
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_launcher);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            if(sharedPref.getBoolean(getString(R.string.settings_key_auth_device), false)) {
                KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                if (km.isKeyguardSecure()) {
                    Intent authIntent = km.createConfirmDeviceCredentialIntent(getString(R.string.dialog_title_auth), getString(R.string.dialog_msg_auth));
                    startActivityForResult(authIntent, INTENT_AUTHENTICATE);
                }
            }
        }

        floatingActionMenu = new FloatingActionMenu(this, (ConstraintLayout) findViewById(R.id.fab_main_layout));
        floatingActionMenu.setFABHandler(new FloatingActionMenu.FABHandler() {
            @Override
            public void onQRFabClick() {
                scanQRCode();
            }

            @Override
            public void onManualFabClick() {
                enterDetails();
            }
        });

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        RecyclerView recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        adapter = new EntriesCardAdapter(this);
        recList.setAdapter(adapter);

        touchHelperCallback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
        touchHelper.attachToRecyclerView(recList);

        float durationScale = Settings.Global.getFloat(this.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 0);
        final long animatorDuration = (long) (1000 / durationScale);

        adapter.setMoveEventCallback(new EntriesCardAdapter.ViewHolderEventCallback() {
            @Override
            public void onMoveEventStart() {
                stopUpdater();
            }

            @Override
            public void onMoveEventStop() {
                startUpdater();
            }
        });

        handler = new Handler();
        handlerTask = new Runnable()
        {
            @Override
            public void run() {
                int progress =  (int) (TOTPHelper.TOTP_DEFAULT_PERIOD - (System.currentTimeMillis() / 1000) % TOTPHelper.TOTP_DEFAULT_PERIOD) ;
                progressBar.setProgress(progress*100);

                ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", (progress-1)*100);
                animation.setDuration(animatorDuration);
                animation.setInterpolator(new LinearInterpolator());
                animation.start();

                boolean change = false;
                for(int i =0;i < adapter.getFullItemCount(); i++){
                    boolean item_changed = adapter.getItem(i).updateOTP();
                    change = change || item_changed;
                }

                if (change)
                    adapter.notifyDataSetChanged();

                handler.postDelayed(this, 1000);
            }
        };
    }

    // Controls for the updater background task
    public void stopUpdater() {
        handler.removeCallbacks(handlerTask);
    }

    public void startUpdater() {
        handler.post(handlerTask);
    }

    @Override
    public void onResume() {
        super.onResume();
        startUpdater();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopUpdater();
    }

    // Activity results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result != null) {
            if(result.getContents() != null) {
                try {
                    Entry e = new Entry(result.getContents());
                    e.setCurrentOTP(TOTPHelper.generate(e.getSecret(), e.getPeriod()));
                    adapter.addEntry(e);
                    adapter.saveEntries();
                } catch (Exception e) {
                    Toast.makeText(this, R.string.toast_invalid_qr_code, Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == INTENT_OPEN_DOCUMENT && resultCode == Activity.RESULT_OK) {
            Uri file;
            if (intent != null) {
                file = intent.getData();
                doImportJSON(file);
            }
        } else if (requestCode == INTENT_SAVE_DOCUMENT && resultCode == Activity.RESULT_OK) {
            Uri file;
            if (intent != null) {
                file = intent.getData();
                doExportJSON(file);
            }
        } else if (requestCode == INTENT_SETTINGS && resultCode == RESULT_OK) {
            adapter.notifyDataSetChanged();
        } else if (requestCode == INTENT_AUTHENTICATE && resultCode != RESULT_OK) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            }
        }
    }

    // Options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                floatingActionMenu.hide();
                touchHelperCallback.setDragEnabled(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                floatingActionMenu.show();
                touchHelperCallback.setDragEnabled(true);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_export) {
            exportJSONWithWarning();
            return true;
        } else if (id == R.id.action_import) {
            importJSONWithPermissions();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, INTENT_SETTINGS);
        } else if (id == R.id.action_about){
            showAbout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}