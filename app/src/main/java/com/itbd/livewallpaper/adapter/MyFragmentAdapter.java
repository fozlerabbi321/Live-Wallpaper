package com.itbd.livewallpaper.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.itbd.livewallpaper.Fragment.CetagoryFragment;
import com.itbd.livewallpaper.Fragment.TrendingFragment;
import com.itbd.livewallpaper.Fragment.RecentsFragment;

public class MyFragmentAdapter extends FragmentPagerAdapter
{
    private Context context;
    public MyFragmentAdapter(@NonNull FragmentManager fm,Context context) {
        super(fm);
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        if(position == 0)
            return CetagoryFragment.getInstance();

        else if(position == 1)
            return TrendingFragment.getInstance();

        else if(position == 2)
            return RecentsFragment.getInstance(context);
        else
            return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position)
        {
            case 0:
                return "Category";
            case 1:
                return "Trending";
            case 2:
                return "Recents";

        }
        return "";
    }
}
