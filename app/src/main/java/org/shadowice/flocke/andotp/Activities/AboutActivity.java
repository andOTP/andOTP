/*
 * Copyright (C) 2017-2020 Jakob Nixdorf
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

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewStub;

import com.mikepenz.aboutlibraries.LibsBuilder;

import org.shadowice.flocke.andotp.Fragments.AboutFragment;
import org.shadowice.flocke.andotp.R;

public class AboutActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.about_activity_title);
        setContentView(R.layout.activity_container);

        Toolbar toolbar = findViewById(R.id.container_toolbar);
        setSupportActionBar(toolbar);

        ViewStub stub = findViewById(R.id.container_stub);
        stub.setLayoutResource(R.layout.component_about);

        View v = stub.inflate();

        ViewPager viewPager = v.findViewById(R.id.viewPager);
        TabLayout tabLayout = v.findViewById(R.id.tabLayout);
        AboutPageAdapter aboutPageAdapter = new AboutPageAdapter(getSupportFragmentManager());

        viewPager.setAdapter(aboutPageAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    // Go back to the main activity
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private class AboutPageAdapter extends FragmentPagerAdapter {
        AboutPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {
                case 0:
                    return new AboutFragment();
                case 1:
                    return new LibsBuilder()
                            .withFields(R.string.class.getFields())
                            .withLicenseShown(true)
                            .withVersionShown(true)
                            .supportFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public String getPageTitle(int pos) {
            switch(pos) {
                case 0:
                    return getString(R.string.about_tab_about);
                case 1:
                    return getString(R.string.about_tab_libraries);
                default:
                    return null;
            }
        }
    }
}
