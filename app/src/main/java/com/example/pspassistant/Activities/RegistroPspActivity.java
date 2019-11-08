package com.example.pspassistant.Activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.example.pspassistant.Adaptadores.Adaptador_Fragment;
import com.example.pspassistant.Fragments.FragmentDefectos;
import com.example.pspassistant.Fragments.FragmentTiempos;
import com.example.pspassistant.Modelos.Proyecto;
import com.example.pspassistant.R;

import io.realm.Realm;

public class RegistroPspActivity extends AppCompatActivity {

    private int id;
    Toolbar toolbar;
    TabLayout tabLayout;
    public static ViewPager viewPager;
    Realm config;
    Proyecto proyecto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_psp);

        Realm.init(this);
        config = Realm.getDefaultInstance();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            id = bundle.getInt("id");

            proyecto = config.where(Proyecto.class).equalTo("Id_Proyecto", id).findFirst();

            toolbar = (Toolbar) findViewById(R.id.barra_registro);

            toolbar.setTitle(proyecto.getNombre_Proyecto());
        }

        tabLayout = (TabLayout) findViewById(R.id.Tab_layout);
        viewPager = (ViewPager) findViewById(R.id.Pager);

        tabLayout.setupWithViewPager(viewPager);

        Adaptador_Fragment adaptadorFragment = new Adaptador_Fragment(getSupportFragmentManager());
        adaptadorFragment.Agrega(new FragmentTiempos(), "Tiempos");
        adaptadorFragment.Agrega(new FragmentDefectos(), "Defectos");

        viewPager.setAdapter(adaptadorFragment);

    }

}