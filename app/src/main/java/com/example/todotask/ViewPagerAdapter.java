package com.example.todotask;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.todotask.fragments.ActiveFragment;
import com.example.todotask.fragments.AllFragment;
import com.example.todotask.fragments.CompletedFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new BaseFragment();
            case 1:
                return new ActiveFragment();
            case 2:
                return new CompletedFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "All";
            case 1:
                return "Active";
            case 2:
                return "Completed";
            default:
                return null;
        }
    }
}
