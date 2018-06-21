//Código creado por Aarón Angulo

package com.sigmabrio.penchi.binarywatch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.RadioButton;
import android.widget.ToggleButton;

public class ConfigActivity extends Activity
{
    private RadioButton rbCuadrado;
    private RadioButton rbCirculo;
    private RadioButton rbNegro;
    private RadioButton rbBlanco;
    private ToggleButton tbSegundero;
    private ToggleButton tbFormato;
    private ToggleButton tbFecha;
    private ToggleButton tbAyuda;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener()
        {
            @Override
            public void onLayoutInflated(WatchViewStub stub)
            {
                rbCuadrado = ((RadioButton) stub.findViewById(R.id.cuadradoRb));
                rbCirculo = ((RadioButton) stub.findViewById(R.id.circuloRb));
                rbNegro = ((RadioButton) findViewById(R.id.negroRb));
                rbBlanco = ((RadioButton) findViewById(R.id.blancoRb));
                tbSegundero = ((ToggleButton) stub.findViewById(R.id.segunderoTbtn));
                tbFormato = ((ToggleButton) stub.findViewById(R.id.formatoTbtn));
                tbFecha = ((ToggleButton) stub.findViewById(R.id.fechaTbtn));
                tbAyuda = ((ToggleButton) findViewById(R.id.ayudaTbtn));

                SharedPreferences config = getSharedPreferences("config", 0);
                if (config.getBoolean("figura", true))
                {
                    rbCuadrado.setChecked(true);
                    rbCirculo.setChecked(false);
                }
                else
                {
                    rbCuadrado.setChecked(false);
                    rbCirculo.setChecked(true);
                }

                if (config.getBoolean("fondo", true))
                {
                    rbNegro.setChecked(true);
                    rbBlanco.setChecked(false);
                }
                else
                {
                    rbNegro.setChecked(false);
                    rbBlanco.setChecked(true);
                }
                tbSegundero.setChecked(config.getBoolean("segundero", true));
                tbFormato.setChecked(config.getBoolean("formato", true));
                tbFecha.setChecked(config.getBoolean("fecha", true));
                tbAyuda.setChecked(config.getBoolean("ayuda", true));
            }
        });
        Sincronizador.getInstance().ActividadTemporal(this);
    }

    public void Inicializar()
    {
        SharedPreferences config = getSharedPreferences("config", 0);
        if (config.getBoolean("figura", true))
        {
            rbCuadrado.setChecked(true);
            rbCirculo.setChecked(false);
        }
        else
        {
            rbCuadrado.setChecked(false);
            rbCirculo.setChecked(true);
        }

        if (config.getBoolean("fondo", true))
        {
            rbNegro.setChecked(true);
            rbBlanco.setChecked(false);
        }
        else
        {
            rbNegro.setChecked(false);
            rbBlanco.setChecked(true);
        }
        tbSegundero.setChecked(config.getBoolean("segundero", true));
        tbFormato.setChecked(config.getBoolean("formato", true));
        tbFecha.setChecked(config.getBoolean("fecha", true));
        tbAyuda.setChecked(config.getBoolean("ayuda", true));
    }

    public void MostrarColores(View v)
    {
        Intent i = new Intent(getBaseContext(), ColoresActivity.class);
        startActivity(i);
    }

    public void Figuras(View v)
    {
        SharedPreferences config = getSharedPreferences("config", 0);
        SharedPreferences.Editor editor = config.edit();

        //true = Negro false = Blanco
        if(v.getId() == R.id.cuadradoRb)
            editor.putBoolean("figura", true);
        else
            if(v.getId() == R.id.circuloRb)
                editor.putBoolean("figura", false);

        editor.apply();
        Sincronizador.getInstance().Sincronizar();
    }

    public void Fondo(View v)
    {
        SharedPreferences config = getSharedPreferences("config", 0);
        SharedPreferences.Editor editor = config.edit();

        //true = Negro false = Blanco
        if (v.getId() == R.id.negroRb)
            editor.putBoolean("fondo", true);
        else if (v.getId() == R.id.blancoRb)
            editor.putBoolean("fondo", false);

        editor.apply();
        Sincronizador.getInstance().Sincronizar();
    }

    public void Segundero(View v)
    {
        SharedPreferences config = getSharedPreferences("config", 0);
        SharedPreferences.Editor editor = config.edit();

        if(((ToggleButton)findViewById(R.id.segunderoTbtn)).isChecked())
            editor.putBoolean("segundero", true);
        else
            editor.putBoolean("segundero", false);

        editor.apply();
        Sincronizador.getInstance().Sincronizar();
    }

    //formato == true entonces es de 24 horas else es de 12 horas
    public void Formato(View v)
    {
        SharedPreferences config = getSharedPreferences("config", 0);
        SharedPreferences.Editor editor = config.edit();

        if(((ToggleButton)findViewById(R.id.formatoTbtn)).isChecked())
            editor.putBoolean("formato", true);
        else
            editor.putBoolean("formato", false);

        editor.apply();
        Sincronizador.getInstance().Sincronizar();
    }

    public void Fecha(View v)
    {
        SharedPreferences config = getSharedPreferences("config", 0);
        SharedPreferences.Editor editor = config.edit();

        if(((ToggleButton)findViewById(R.id.fechaTbtn)).isChecked())
            editor.putBoolean("fecha", true);
        else
            editor.putBoolean("fecha", false);

        editor.apply();
        Sincronizador.getInstance().Sincronizar();
    }

    public void Ayuda(View v)
    {
        SharedPreferences config = getSharedPreferences("config", 0);
        SharedPreferences.Editor editor = config.edit();

        if(((ToggleButton)findViewById(R.id.ayudaTbtn)).isChecked())
            editor.putBoolean("ayuda", true);
        else
            editor.putBoolean("ayuda", false);

        editor.apply();
        Sincronizador.getInstance().Sincronizar();
    }

    public void AcercaDe(View v)
    {
        Intent i = new Intent(getBaseContext(), CreditosActivity.class);
        startActivity(i);
    }
}
