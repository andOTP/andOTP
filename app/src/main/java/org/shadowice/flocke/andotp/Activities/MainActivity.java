/*
 * Copyright (C) 2017-2020 Jakob Nixdorf
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
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.EncryptionHelper;
import org.shadowice.flocke.andotp.Utilities.KeyStoreHelper;
import org.shadowice.flocke.andotp.Utilities.NotificationHelper;
import org.shadowice.flocke.andotp.Utilities.ScanQRCodeFromFile;
import org.shadowice.flocke.andotp.Utilities.TokenCalculator;
import org.shadowice.flocke.andotp.View.EntriesCardAdapter;
import org.shadowice.flocke.andotp.View.ItemTouchHelper.SimpleItemTouchHelperCallback;
import org.shadowice.flocke.andotp.Dialogs.ManualEntryDialog;
import org.shadowice.flocke.andotp.View.TagsAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.SecretKey;

import static org.shadowice.flocke.andotp.Utilities.Constants.AuthMethod;
import static org.shadowice.flocke.andotp.Utilities.Constants.EncryptionType;
import static org.shadowice.flocke.andotp.Utilities.Constants.SortMode;

public class MainActivity extends BaseActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static long animatorDuration = 1000;

    private static final String INTENT_SCAN_QR = "org.shadowice.flocke.andotp.intent.SCAN_QR";
    private static final String INTENT_IMPORT_QR = "org.shadowice.flocke.andotp.intent.IMPORT_QR";
    private static final String INTENT_ENTER_DETAILS = "org.shadowice.flocke.andotp.intent.ENTER_DETAILS";

    private EntriesCardAdapter adapter;
    private SpeedDialView speedDial;
    private MenuItem sortMenu;
    private SimpleItemTouchHelperCallback touchHelperCallback;

    private EncryptionType encryptionType = EncryptionType.KEYSTORE;
    private boolean requireAuthentication = false;

    private boolean recreateActivity = false;
    private boolean cacheEncKey = false;

    private Handler handler;
    private Runnable handlerTask;

    private DrawerLayout tagsDrawerLayout;
    private ListView tagsDrawerListView;
    private TagsAdapter tagsDrawerAdapter;
    private ActionBarDrawerToggle tagsToggle;
    private String filterString;

    private CountDownTimer countDownTimer;

    // QR code scanning
    private void scanQRCode(){
        new IntentIntegrator(MainActivity.this)
                .setOrientationLocked(false)
                .setBeepEnabled(false)
                .setCaptureActivity(SecureCaptureActivity.class)
                .initiateScan();
    }

    private void showFirstTimeWarning() {
        Intent introIntent = new Intent(this, IntroScreenActivity.class);
        startActivityForResult(introIntent, Constants.INTENT_MAIN_INTRO);
    }

    public void authenticate(int messageId) {
        AuthMethod authMethod = settings.getAuthMethod();

        if (authMethod == AuthMethod.DEVICE) {
            KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if (km.isKeyguardSecure()) {
                Intent authIntent = km.createConfirmDeviceCredentialIntent(getString(R.string.dialog_title_auth), getString(R.string.dialog_msg_auth));
                startActivityForResult(authIntent, Constants.INTENT_MAIN_AUTHENTICATE);
            }
        } else if (authMethod == AuthMethod.PASSWORD || authMethod == AuthMethod.PIN) {
            Intent authIntent = new Intent(this, AuthenticateActivity.class);
            authIntent.putExtra(Constants.EXTRA_AUTH_MESSAGE, messageId);
            startActivityForResult(authIntent, Constants.INTENT_MAIN_AUTHENTICATE);
        }
    }

    private void restoreSortMode() {
        if (settings != null && adapter != null && touchHelperCallback != null) {
            SortMode mode = settings.getSortMode();
            adapter.setSortMode(mode);

            if (mode == SortMode.UNSORTED)
                touchHelperCallback.setDragEnabled(true);
            else
                touchHelperCallback.setDragEnabled(false);
        }
    }

    private void saveSortMode(SortMode mode) {
        if (settings != null)
            settings.setSortMode(mode);
    }

    private void populateAdapter() {
        adapter.loadEntries();
        tagsDrawerAdapter.setTags(TagsAdapter.createTagsMap(adapter.getEntries(), settings));
        adapter.filterByTags(tagsDrawerAdapter.getActiveTags());
    }

    // Initialize the main application
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.app_name);

        if (! settings.getScreenshotsEnabled())
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        settings.registerPreferenceChangeListener(this);

        encryptionType = settings.getEncryption();

        if (settings.getAuthMethod() != AuthMethod.NONE && savedInstanceState == null)
            requireAuthentication = true;

        setBroadcastCallback(new BroadcastReceivedCallback() {
            @Override
            public void onReceivedScreenOff() {
                if (settings.getRelockOnScreenOff() && settings.getAuthMethod() != AuthMethod.NONE)
                    requireAuthentication = true;
            }
        });

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new ProcessLifecycleObserver());

        if (! settings.getFirstTimeWarningShown()) {
           showFirstTimeWarning();
        }

        speedDial = findViewById(R.id.speedDial);
        speedDial.inflate(R.menu.menu_fab);

        speedDial.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem speedDialActionItem) {
                switch (speedDialActionItem.getId()) {
                    case R.id.fabScanQR:
                        scanQRCode();
                        return false;
                    case R.id.fabEnterDetails:
                        ManualEntryDialog.show(MainActivity.this, settings, adapter);
                        return false;
                    case R.id.fabScanQRFromImage:
                        showOpenFileSelector(Constants.INTENT_MAIN_QR_OPEN_IMAGE);
                        return false;
                    default:
                        return false;
                }
            }
        });

        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(settings.isHideGlobalTimeoutEnabled() ? View.GONE : View.VISIBLE);

        RecyclerView recList = findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        tagsDrawerAdapter = new TagsAdapter(this, new HashMap<String, Boolean>());
        adapter = new EntriesCardAdapter(this, tagsDrawerAdapter);

        if (savedInstanceState != null) {
            byte[] encKey = savedInstanceState.getByteArray("encKey");
            if (encKey != null) {
                adapter.setEncryptionKey(EncryptionHelper.generateSymmetricKey(encKey));
                requireAuthentication = false;
            }
        }

        recList.setAdapter(adapter);

        touchHelperCallback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
        touchHelper.attachToRecyclerView(recList);

        NotificationHelper.initializeNotificationChannels(this);
        restoreSortMode();

        float durationScale = android.provider.Settings.Global.getFloat(this.getContentResolver(), android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 0);
        if (durationScale == 0)
            durationScale = 1;

        animatorDuration = (long) (1000 / durationScale);

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
                if (!settings.isHideGlobalTimeoutEnabled()) {
                    int progress = (int) (TokenCalculator.TOTP_DEFAULT_PERIOD - (System.currentTimeMillis() / 1000) % TokenCalculator.TOTP_DEFAULT_PERIOD);
                    progressBar.setProgress(progress * 100);

                    ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", (progress - 1) * 100);
                    animation.setDuration(animatorDuration);
                    animation.setInterpolator(new LinearInterpolator());
                    animation.start();
                }

                adapter.updateTimeBasedTokens();

                handler.postDelayed(this, 1000);
            }
        };

        setupDrawer();

        if (savedInstanceState != null) {
            setFilterString(savedInstanceState.getString("filterString", ""));
        }
    }

    private void checkIntent() {
        Intent callingIntent = getIntent();
        if (callingIntent != null && callingIntent.getAction() != null) {
            // Cache and reset the action to prevent the same intent from being evaluated multiple times
            String intentAction = callingIntent.getAction();
            callingIntent.setAction(null);

            if (intentAction.equals(INTENT_SCAN_QR)) {
                scanQRCode();
            } else if (intentAction.equals(INTENT_IMPORT_QR)) {
                showOpenFileSelector(Constants.INTENT_MAIN_QR_OPEN_IMAGE);
            } else if (intentAction.equals(INTENT_ENTER_DETAILS)) {
                ManualEntryDialog.show(MainActivity.this, settings, adapter);
            } else if (intentAction.equals(Intent.ACTION_VIEW)) {
                try {
                    Entry entry = new Entry(callingIntent.getDataString());
                    entry.updateOTP();
                    entry.setLastUsed(System.currentTimeMillis());
                    adapter.addEntry(entry);
                    Toast.makeText(this, R.string.toast_intent_creation_succeeded, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(this, R.string.toast_intent_creation_failed, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        tagsToggle.syncState();
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
            if (settings.getAuthMethod() != AuthMethod.NONE) {
                requireAuthentication = false;
                authenticate(R.string.auth_msg_authenticate);
            }
        } else {
            if (settings.getFirstTimeWarningShown()) {
                if (adapter.getEncryptionKey() == null) {
                    updateEncryption(null);
                } else {
                    populateAdapter();
                }
                checkIntent();
            }

            if (setCountDownTimerNow())
                countDownTimer.start();
        }

        if (filterString != null) {
            // ensure the current filter string is applied after a resume
            setFilterString(this.filterString);
        }

        View cardList = findViewById(R.id.cardList);
        if(cardList.getVisibility() == View.INVISIBLE)
            cardList.setVisibility(View.VISIBLE);

        startUpdater();
    }

    @Override
    public void onPause() {
        if(settings.getAuthMethod() == AuthMethod.DEVICE)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.cardList).setVisibility(View.INVISIBLE);
                }
            });
        super.onPause();
        stopUpdater();

        if (countDownTimer != null)
            countDownTimer.cancel();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("filterString", filterString);

        if (cacheEncKey) {
            outState.putByteArray("encKey", adapter.getEncryptionKey().getEncoded());
            cacheEncKey = false;
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(getString(R.string.settings_key_label_size)) ||
                key.equals(getString(R.string.settings_key_label_scroll)) ||
                key.equals(getString(R.string.settings_key_split_group_size)) ||
                key.equals(getString(R.string.settings_key_thumbnail_size))) {
            adapter.notifyDataSetChanged();
        } else if (key.equals(getString(R.string.settings_key_search_includes))) {
            adapter.clearFilter();
        } else if (key.equals(getString(R.string.settings_key_tap_single)) ||
                key.equals(getString(R.string.settings_key_tap_double)) ||
                key.equals(getString(R.string.settings_key_theme)) ||
                key.equals(getString(R.string.settings_key_locale)) ||
                key.equals(getString(R.string.settings_key_enable_screenshot)) ||
                key.equals(getString(R.string.settings_key_tag_functionality)) ||
                key.equals(getString(R.string.settings_key_label_highlight_token)) ||
                key.equals(getString(R.string.settings_key_card_layout)) ||
                key.equals(getString(R.string.settings_key_hide_global_timeout))) {
            recreateActivity = true;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        tagsToggle.onConfigurationChanged(newConfig);
    }

    // Activity results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result != null) {
            if(result.getContents() != null) {
                addQRCode(result.getContents());
            }
        } else if (requestCode == Constants.INTENT_MAIN_BACKUP && resultCode == RESULT_OK) {
            if (intent.getBooleanExtra("reload", false)) {
                adapter.loadEntries();
                refreshTags();
            }
        } else if (requestCode == Constants.INTENT_MAIN_SETTINGS && resultCode == RESULT_OK) {
            boolean encryptionChanged = intent.getBooleanExtra(Constants.EXTRA_SETTINGS_ENCRYPTION_CHANGED, false);
            byte[] newKey = intent.getByteArrayExtra(Constants.EXTRA_SETTINGS_ENCRYPTION_KEY);

            if (encryptionChanged)
                updateEncryption(newKey);

            if (recreateActivity) {
                cacheEncKey = true;
                recreate();
            }
        } else if (requestCode == Constants.INTENT_MAIN_AUTHENTICATE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(getBaseContext(), R.string.toast_auth_failed_fatal, Toast.LENGTH_LONG).show();
                finishAndRemoveTask();
            } else {
                requireAuthentication = false;

                byte[] authKey = null;

                if (intent != null)
                    authKey = intent.getByteArrayExtra(Constants.EXTRA_AUTH_PASSWORD_KEY);

                updateEncryption(authKey);
            }
        } else if (requestCode == Constants.INTENT_MAIN_QR_OPEN_IMAGE && resultCode == RESULT_OK) {
            if (intent != null) {
                addQRCode(ScanQRCodeFromFile.scanQRImage(this, intent.getData()));
            }
        }
    }

    private void updateEncryption(byte[] newKey) {
        SecretKey encryptionKey = null;

        encryptionType = settings.getEncryption();

        if (encryptionType == EncryptionType.KEYSTORE) {
            encryptionKey = KeyStoreHelper.loadEncryptionKeyFromKeyStore(this, false);
        } else if (encryptionType == EncryptionType.PASSWORD) {
            if (newKey != null && newKey.length > 0) {
                encryptionKey = EncryptionHelper.generateSymmetricKey(newKey);
            } else {
                authenticate(R.string.auth_msg_confirm_encryption);
            }
        }

        if (encryptionKey != null)
            adapter.setEncryptionKey(encryptionKey);

        populateAdapter();
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
            } else if (mode == SortMode.ISSUER) {
                sortMenu.setIcon(R.drawable.ic_sort_inverted_label_white);
                menu.findItem(R.id.menu_sort_issuer).setChecked(true);
            } else if (mode == SortMode.LABEL) {
                sortMenu.setIcon(R.drawable.ic_sort_inverted_label_white);
                menu.findItem(R.id.menu_sort_label).setChecked(true);
            } else if (mode == SortMode.LAST_USED) {
                sortMenu.setIcon(R.drawable.ic_sort_inverted_time_white);
                menu.findItem(R.id.menu_sort_last_used).setChecked(true);
            } else if (mode == SortMode.MOST_USED) {
                sortMenu.setIcon(R.drawable.ic_sort_inverted_time_white);
                menu.findItem(R.id.menu_sort_most_used).setChecked(true);
            }
        }

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                setFilterString(newText);
                return false;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                speedDial.setVisibility(View.GONE);
                touchHelperCallback.setDragEnabled(false);
                if (sortMenu != null)
                    sortMenu.setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                speedDial.setVisibility(View.VISIBLE);

                if (adapter == null || adapter.getSortMode() == SortMode.UNSORTED)
                    touchHelperCallback.setDragEnabled(true);

                if (sortMenu != null)
                    sortMenu.setVisible(true);

                return true;
            }
        });

        return true;
    }

    private void setFilterString(String newText) {
        if (newText.isEmpty())
            adapter.filterByTags(tagsDrawerAdapter.getActiveTags());
        else
            adapter.getFilter().filter(newText);

        this.filterString = newText;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_backup) {
            Intent backupIntent = new Intent(this, BackupActivity.class);
            backupIntent.putExtra(Constants.EXTRA_BACKUP_ENCRYPTION_KEY, adapter.getEncryptionKey().getEncoded());
            startActivityForResult(backupIntent, Constants.INTENT_MAIN_BACKUP);
        } else if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            if (adapter.getEncryptionKey() != null)
                settingsIntent.putExtra(Constants.EXTRA_SETTINGS_ENCRYPTION_KEY, adapter.getEncryptionKey().getEncoded());
            startActivityForResult(settingsIntent, Constants.INTENT_MAIN_SETTINGS);
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
        } else if (id == R.id.menu_sort_issuer) {
            item.setChecked(true);
            sortMenu.setIcon(R.drawable.ic_sort_inverted_label_white);
            saveSortMode(SortMode.ISSUER);
            if(adapter != null) {
                adapter.setSortMode(SortMode.ISSUER);
                touchHelperCallback.setDragEnabled(false);
            }
        } else if (id == R.id.menu_sort_label) {
            item.setChecked(true);
            sortMenu.setIcon(R.drawable.ic_sort_inverted_label_white);
            saveSortMode(SortMode.LABEL);
            if (adapter != null) {
                adapter.setSortMode(SortMode.LABEL);
                touchHelperCallback.setDragEnabled(false);
            }
        } else if (id == R.id.menu_sort_last_used) {
            item.setChecked(true);
            sortMenu.setIcon(R.drawable.ic_sort_inverted_time_white);
            saveSortMode(SortMode.LAST_USED);
            if (adapter != null) {
                adapter.setSortMode(SortMode.LAST_USED);
                touchHelperCallback.setDragEnabled(false);
            }
            if (! settings.getUsedTokensDialogShown())
                showUsedTokensDialog();
        } else if (id == R.id.menu_sort_most_used) {
            item.setChecked(true);
            sortMenu.setIcon(R.drawable.ic_sort_inverted_time_white);
            saveSortMode(SortMode.MOST_USED);
            if (adapter != null) {
                adapter.setSortMode(SortMode.MOST_USED);
                touchHelperCallback.setDragEnabled(false);
            }
            if (! settings.getUsedTokensDialogShown())
                showUsedTokensDialog();
        } else if (tagsToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showUsedTokensDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title_used_tokens)
                .setMessage(R.string.dialog_msg_used_tokens)
                .setPositiveButton(android.R.string.ok, (DialogInterface dialogInterface, int i) -> settings.setUsedTokensDialogShown(true))
                .create()
                .show();
    }

    private void setupDrawer() {
        tagsDrawerListView = findViewById(R.id.tags_list_in_drawer);
        tagsDrawerLayout = findViewById(R.id.drawer_layout);

        tagsToggle = new ActionBarDrawerToggle(this, tagsDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(R.string.label_tags);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(R.string.app_name);
                invalidateOptionsMenu();
            }
        };

        tagsToggle.setDrawerIndicatorEnabled(true);
        tagsDrawerLayout.addDrawerListener(tagsToggle);

        final CheckedTextView noTagsButton = findViewById(R.id.no_tags_entries);
        final CheckedTextView allTagsButton = findViewById(R.id.all_tags_in_drawer);

        allTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckedTextView checkedTextView = ((CheckedTextView)view);
                checkedTextView.setChecked(!checkedTextView.isChecked());

                settings.setAllTagsToggle(checkedTextView.isChecked());

                for(int i = 0; i < tagsDrawerListView.getChildCount(); i++) {
                    CheckedTextView childCheckBox = (CheckedTextView) tagsDrawerListView.getChildAt(i);
                    childCheckBox.setChecked(checkedTextView.isChecked());
                }

                for (String tag: tagsDrawerAdapter.getTags()) {
                    tagsDrawerAdapter.setTagState(tag, checkedTextView.isChecked());
                    settings.setTagToggle(tag, checkedTextView.isChecked());
                }

                if(checkedTextView.isChecked()) {
                    adapter.filterByTags(tagsDrawerAdapter.getActiveTags());
                } else {
                    adapter.filterByTags(new ArrayList<String>());
                }
            }
        });
        allTagsButton.setChecked(settings.getAllTagsToggle());

        noTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckedTextView checkedTextView = ((CheckedTextView)view);
                checkedTextView.setChecked(!checkedTextView.isChecked());

                if(settings.getTagFunctionality() == Constants.TagFunctionality.SINGLE) {
                    checkedTextView.setChecked(true);
                    allTagsButton.setChecked(false);
                    settings.setAllTagsToggle(false);

                    for (String tag: tagsDrawerAdapter.getTags()) {
                        settings.setTagToggle(tag, false);
                        tagsDrawerAdapter.setTagState(tag, false);
                    }
                }

                settings.setNoTagsToggle(checkedTextView.isChecked());
                adapter.filterByTags(tagsDrawerAdapter.getActiveTags());
            }
        });
        noTagsButton.setChecked(settings.getNoTagsToggle());

        tagsDrawerListView.setAdapter(tagsDrawerAdapter);
        tagsDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checkedTextView = ((CheckedTextView)view);

                if(settings.getTagFunctionality() == Constants.TagFunctionality.SINGLE) {
                    allTagsButton.setChecked(false);
                    settings.setAllTagsToggle(false);
                    noTagsButton.setChecked(false);
                    settings.setNoTagsToggle(false);

                    for (String tag: tagsDrawerAdapter.getTags()) {
                        settings.setTagToggle(tag, false);
                        tagsDrawerAdapter.setTagState(tag, false);
                    }
                    checkedTextView.setChecked(true);
                }else {
                    checkedTextView.setChecked(!checkedTextView.isChecked());
                }

                settings.setTagToggle(checkedTextView.getText().toString(), checkedTextView.isChecked());
                tagsDrawerAdapter.setTagState(checkedTextView.getText().toString(), checkedTextView.isChecked());

                if (! checkedTextView.isChecked()) {
                    allTagsButton.setChecked(false);
                    settings.setAllTagsToggle(false);
                }

                if (tagsDrawerAdapter.allTagsActive()) {
                    allTagsButton.setChecked(true);
                    settings.setAllTagsToggle(true);
                }

                adapter.filterByTags(tagsDrawerAdapter.getActiveTags());
            }
        });

        adapter.filterByTags(tagsDrawerAdapter.getActiveTags());
    }

    public void refreshTags() {
        HashMap<String, Boolean> tagsHashMap = new HashMap<>();
        for(String tag: tagsDrawerAdapter.getTags()) {
            tagsHashMap.put(tag, false);
        }
        for(String tag: tagsDrawerAdapter.getActiveTags()) {
            tagsHashMap.put(tag, true);
        }
        for(String tag: adapter.getTags()) {
            if(!tagsHashMap.containsKey(tag))
                tagsHashMap.put(tag, true);
        }
        tagsDrawerAdapter.setTags(tagsHashMap);
        adapter.filterByTags(tagsDrawerAdapter.getActiveTags());
    }

    @Override
    public void onUserInteraction(){
        super.onUserInteraction();

        // Refresh Blackout Timer
        if (countDownTimer != null)
            countDownTimer.cancel();

        if (setCountDownTimerNow())
            countDownTimer.start();
    }

    private boolean setCountDownTimerNow() {
        int secondsToBlackout = 1000 * settings.getAuthInactivityDelay();

        if (settings.getAuthMethod() == AuthMethod.NONE || !settings.getAuthInactivity() || secondsToBlackout == 0)
            return false;

        countDownTimer = new CountDownTimer(secondsToBlackout, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                authenticate(R.string.auth_msg_authenticate);
                this.cancel();
            }
        };

        return true;
    }

    private void showOpenFileSelector(int intentId){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, intentId);
    }
    
    private void addQRCode(String result){
        if(!TextUtils.isEmpty(result)) {
            try {
                Entry e = new Entry(result);
                e.updateOTP();
                e.setLastUsed(System.currentTimeMillis());
                adapter.addEntry(e);
                refreshTags();
            } catch (Exception e) {
                Toast.makeText(this, R.string.toast_invalid_qr_code, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class ProcessLifecycleObserver implements DefaultLifecycleObserver {
        @Override
        public void onStop(LifecycleOwner owner) {
            if (MainActivity.this.settings.getRelockOnBackground())
                MainActivity.this.requireAuthentication = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (speedDial.isOpen()) {
                speedDial.close();
                return true;
            }

            if (tagsDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                tagsDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            return super.onKeyDown(keyCode, event);
        }

        return super.onKeyDown(keyCode, event);
    }
}