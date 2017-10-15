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

package org.shadowice.flocke.andotp.Activities;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
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
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.View.EntriesCardAdapter;
import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.View.FloatingActionMenu;
import org.shadowice.flocke.andotp.View.ItemTouchHelper.SimpleItemTouchHelperCallback;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.TokenCalculator;

import java.util.Locale;

import static org.shadowice.flocke.andotp.Utilities.Settings.SortMode;

public class MainActivity extends BaseActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int INTENT_INTERNAL_AUTHENTICATE   = 100;
    private static final int INTENT_INTERNAL_SETTINGS       = 101;
    private static final int INTENT_INTERNAL_BACKUP         = 102;

    private EntriesCardAdapter adapter;
    private FloatingActionMenu floatingActionMenu;
    private SearchView searchView;
    private MenuItem sortMenu;
    private SimpleItemTouchHelperCallback touchHelperCallback;

    private boolean requireAuthentication = false;

    private Handler handler;
    private Runnable handlerTask;

    // QR code scanning
    private void scanQRCode(){
        new IntentIntegrator(MainActivity.this)
                .setOrientationLocked(false)
                .setBeepEnabled(false)
                .initiateScan();
    }

    // Manual data entry
    private void enterDetails() {
        ViewGroup container = findViewById(R.id.main_content);
        View inputView = getLayoutInflater().inflate(R.layout.dialog_manual_entry, container, false);

        final Spinner typeInput = inputView.findViewById(R.id.manual_type);
        final EditText labelInput = inputView.findViewById(R.id.manual_label);
        final EditText secretInput = inputView.findViewById(R.id.manual_secret);
        final EditText periodInput = inputView.findViewById(R.id.manual_period);
        final EditText digitsInput = inputView.findViewById(R.id.manual_digits);
        final Spinner algorithmInput = inputView.findViewById(R.id.manual_algorithm);

        typeInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, Entry.OTPType.values()));
        algorithmInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, TokenCalculator.HashAlgorithm.values()));
        periodInput.setText(String.format(Locale.US, "%d", TokenCalculator.TOTP_DEFAULT_PERIOD));
        digitsInput.setText(String.format(Locale.US, "%d", TokenCalculator.TOTP_DEFAULT_DIGITS));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title_manual_entry)
                .setView(inputView)
                .setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Entry.OTPType type = (Entry.OTPType) typeInput.getSelectedItem();
                        TokenCalculator.HashAlgorithm algorithm = (TokenCalculator.HashAlgorithm) algorithmInput.getSelectedItem();

                        if (type == Entry.OTPType.TOTP) {
                            String label = labelInput.getText().toString();
                            String secret = secretInput.getText().toString();
                            int period = Integer.parseInt(periodInput.getText().toString());
                            int digits = Integer.parseInt(digitsInput.getText().toString());

                            Entry e = new Entry(type, secret, period, digits, label, algorithm);
                            e.updateOTP();
                            adapter.addEntry(e);
                            adapter.saveEntries();
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

    private void showFirstTimeWarning() {
        ViewGroup container = findViewById(R.id.main_content);
        View msgView = getLayoutInflater().inflate(R.layout.dialog_security_backup, container, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title_security_backup)
                .setView(msgView)
                .setPositiveButton(R.string.button_warned, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        settings.setFirstTimeWarningShown(true);
                    }
                })
                .create()
                .show();
    }

    public void authenticate() {
        Settings.AuthMethod authMethod = settings.getAuthMethod();

        if (authMethod == Settings.AuthMethod.DEVICE) {
            KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && km.isKeyguardSecure()) {
                Intent authIntent = km.createConfirmDeviceCredentialIntent(getString(R.string.dialog_title_auth), getString(R.string.dialog_msg_auth));
                startActivityForResult(authIntent, INTENT_INTERNAL_AUTHENTICATE);
            }
        } else if (authMethod == Settings.AuthMethod.PASSWORD || authMethod == Settings.AuthMethod.PIN) {
            Intent authIntent = new Intent(this, AuthenticateActivity.class);
            startActivityForResult(authIntent, INTENT_INTERNAL_AUTHENTICATE);
        }
    }

    private void restoreSortMode() {
        if (settings != null && adapter != null && touchHelperCallback != null) {
            SortMode mode = settings.getSortMode();
            adapter.setSortMode(mode);

            if (mode == SortMode.LABEL)
                touchHelperCallback.setDragEnabled(false);
            else
                touchHelperCallback.setDragEnabled(true);
        }
    }

    private void saveSortMode(SortMode mode) {
        if (settings != null)
            settings.setSortMode(mode);
    }

    // Initialize the main application
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.app_name);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        settings.registerPreferenceChangeListener(this);

        if (savedInstanceState == null)
            requireAuthentication = true;

        setBroadcastCallback(new BroadcastReceivedCallback() {
            @Override
            public void onReceivedScreenOff() {
                requireAuthentication = true;
            }
        });

        if (! settings.getFirstTimeWarningShown()) {
           showFirstTimeWarning();
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

        final ProgressBar progressBar = findViewById(R.id.progressBar);

        RecyclerView recList = findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        adapter = new EntriesCardAdapter(this);
        recList.setAdapter(adapter);

        recList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    floatingActionMenu.hide();
                } else {
                    if (searchView == null || searchView.isIconified())
                        floatingActionMenu.show();
                }
            }
        });

        touchHelperCallback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
        touchHelper.attachToRecyclerView(recList);

        restoreSortMode();

        float durationScale = android.provider.Settings.Global.getFloat(this.getContentResolver(), android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 0);
        if (durationScale == 0)
            durationScale = 1;

        final long animatorDuration = (long) (1000 / durationScale);

        adapter.setCallback(new EntriesCardAdapter.Callback() {
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
                int progress =  (int) (TokenCalculator.TOTP_DEFAULT_PERIOD - (System.currentTimeMillis() / 1000) % TokenCalculator.TOTP_DEFAULT_PERIOD) ;
                progressBar.setProgress(progress*100);

                ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", (progress-1)*100);
                animation.setDuration(animatorDuration);
                animation.setInterpolator(new LinearInterpolator());
                animation.start();

                adapter.updateTokens();

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

        if (requireAuthentication) {
            requireAuthentication = false;
            authenticate();
        }

        startUpdater();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopUpdater();
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(getString(R.string.settings_key_label_size)) ||
                key.equals(getString(R.string.settings_key_tap_to_reveal))) {
            adapter.notifyDataSetChanged();
        } else if (key.equals(getString(R.string.settings_key_theme))) {
            recreate();
        }
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
                    e.updateOTP();
                    adapter.addEntry(e);
                    adapter.saveEntries();
                } catch (Exception e) {
                    Toast.makeText(this, R.string.toast_invalid_qr_code, Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == INTENT_INTERNAL_BACKUP && resultCode == RESULT_OK) {
            if (intent.getBooleanExtra("reload", false))
                adapter.loadEntries();
        } else if (requestCode == INTENT_INTERNAL_AUTHENTICATE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(getBaseContext(), R.string.toast_auth_failed, Toast.LENGTH_LONG).show();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }
            } else {
                requireAuthentication = false;
            }
        }
    }

    // Options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        sortMenu = menu.findItem(R.id.menu_sort);

        if (adapter != null) {
            SortMode mode = adapter.getSortMode();

            if (mode == SortMode.UNSORTED) {
                sortMenu.setIcon(R.drawable.ic_sort_inverted_white);
                menu.findItem(R.id.menu_sort_none).setChecked(true);
            } else if (mode == SortMode.LABEL) {
                sortMenu.setIcon(R.drawable.ic_sort_inverted_label_white);
                menu.findItem(R.id.menu_sort_label).setChecked(true);
            }
        }

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) searchItem.getActionView();
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

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                floatingActionMenu.hide();
                touchHelperCallback.setDragEnabled(false);
                if (sortMenu != null)
                    sortMenu.setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                floatingActionMenu.show();
                if (adapter == null || adapter.getSortMode() == SortMode.UNSORTED)
                    touchHelperCallback.setDragEnabled(true);
                if (sortMenu != null)
                    sortMenu.setVisible(true);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_backup) {
            Intent backupIntent = new Intent(this, BackupActivity.class);
            startActivityForResult(backupIntent, INTENT_INTERNAL_BACKUP);
        } else if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(settingsIntent, INTENT_INTERNAL_SETTINGS);
        } else if (id == R.id.action_about){
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        } else if (id == R.id.menu_sort_none) {
            item.setChecked(true);
            sortMenu.setIcon(R.drawable.ic_sort_inverted_white);
            saveSortMode(SortMode.UNSORTED);
            if (adapter != null) {
                adapter.setSortMode(SortMode.UNSORTED);
                touchHelperCallback.setDragEnabled(true);
            }
        } else if (id == R.id.menu_sort_label) {
            item.setChecked(true);
            sortMenu.setIcon(R.drawable.ic_sort_inverted_label_white);
            saveSortMode(SortMode.LABEL);
            if (adapter != null) {
                adapter.setSortMode(SortMode.LABEL);
                touchHelperCallback.setDragEnabled(false);
            }
        }

        return super.onOptionsItemSelected(item);
    }
}