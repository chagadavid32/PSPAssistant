package com.example.pspassistant.Adaptadores;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class Adaptador_Fragment extends FragmentPagerAdapter {

    private final List<Fragment> fragment = new ArrayList<>();
    private final List<String > nombres = new ArrayList<>();

    public Adaptador_Fragment(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return fragment.get(i);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return fragment.size();
    }

    public void Agrega(Fragment fragment, String nombre){
        this.fragment.add(fragment);
        this.nombres.add(nombre);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return nombres.get(position);
    }
}
