package com.gcscout.trackerdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.gcscout.trackerdemo.App.OnInitializationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class HomeActivity extends FragmentActivity implements OnInitializationListener {
    private static String currentFragmentTag;

    private boolean isCreated = false;

    private void attachFragment(String fragmentClassName) {
        if (currentFragmentTag != null) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
            if (currentFragment != null && !currentFragment.isDetached())
                if (currentFragmentTag.equals(fragmentClassName))
                    return;
                else
                    getSupportFragmentManager().beginTransaction().detach(currentFragment).commit();
        }

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentClassName);
        if (fragment != null)
            getSupportFragmentManager().beginTransaction().attach(fragment).commit();
        else {
            fragment = Fragment.instantiate(HomeActivity.this, fragmentClassName);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment, fragmentClassName)
                    .commit();
        }

        currentFragmentTag = fragmentClassName;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        findViewById(R.id.home_tracker_tab).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                trySetTabsState(TrackerFragment.class.getName());
            }
        });

        findViewById(R.id.home_settings_tab).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                trySetTabsState(SettingsFragment.class.getName());
            }
        });

        isCreated = true;
        App.waitForInitialization(this);
    }

    @Override
    protected void onDestroy() {
        isCreated = false;
        super.onDestroy();
    }

    private void trySetTabsState(final String fragmentClassName) {
        if (!fragmentClassName.equals(SettingsFragment.class.getName()) && SettingsFragment.haveChanges()) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setCancelable(false);
            dlg.setTitle(getResources().getString(R.string.settings_label));
            dlg.setMessage(getResources().getString(R.string.settings_alert_question));
            dlg.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    setTabsState(fragmentClassName);
                }
            });
            dlg.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlg.show();
        } else
            setTabsState(fragmentClassName);
    }

    private void setTabsState(String fragmentClassName) {
        SettingsFragment.cancelChanges();
        findViewById(R.id.home_tracker_tab).setSelected(fragmentClassName.equals(TrackerFragment.class.getName()));
        findViewById(R.id.home_settings_tab).setSelected(fragmentClassName.equals(SettingsFragment.class.getName()));

        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        }

        attachFragment(fragmentClassName);
    }

    public void onInitialized() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            Toast.makeText(this, R.string.main_gpservices_not_available_alert, Toast.LENGTH_LONG).show();
            finish();
        }

        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        findViewById(R.id.home_tab_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.home_progress).setVisibility(View.GONE);
        if (isCreated)
            trySetTabsState(currentFragmentTag == null ? TrackerFragment.class.getName() : currentFragmentTag);
    }
}