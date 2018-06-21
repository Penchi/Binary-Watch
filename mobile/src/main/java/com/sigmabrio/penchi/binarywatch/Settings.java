//Código creado por Aarón Angulo

package com.sigmabrio.penchi.binarywatch;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.ToggleButton;

import java.util.Random;

public class Settings extends AppCompatActivity
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
        setContentView(R.layout.activity_settings);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        rbCuadrado = ((RadioButton) findViewById(R.id.cuadradoRb));
        rbCirculo = ((RadioButton) findViewById(R.id.circuloRb));
        rbNegro = ((RadioButton) findViewById(R.id.negroRb));
        rbBlanco = ((RadioButton) findViewById(R.id.blancoRb));
        tbSegundero = ((ToggleButton) findViewById(R.id.segunderoTbtn));
        tbFormato = ((ToggleButton) findViewById(R.id.formatoTbtn));
        tbFecha = ((ToggleButton) findViewById(R.id.fechaTbtn));
        tbAyuda = ((ToggleButton) findViewById(R.id.ayudaTbtn));

        Sincronizador.getInstance().IniciarGoogleApi(getApplicationContext(), this);
        Inicializar();
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

    /*
                -16711936 = verde
                -65536    = rojo
                -16776961 = azul
                -256      = amarillo
                -16711681 = cyan
                -65281    = magenta
        */
    public void CambiarColor(View v)
    {
        SharedPreferences config = getSharedPreferences("config", 0);
        SharedPreferences.Editor editor = config.edit();

        TextoEmergente et = new TextoEmergente(getApplicationContext());

        CheckBox cbHoras = (CheckBox) findViewById(R.id.cbHoras);
        CheckBox cbMinutos = (CheckBox) findViewById(R.id.cbMinutos);
        CheckBox cbSegundos = (CheckBox) findViewById(R.id.cbSegundos);

        if (cbHoras.isChecked() || cbMinutos.isChecked() || cbSegundos.isChecked()) {
            et.SetText(getString(R.string.toastColoresExito));
            switch (v.getId()) {
                case R.id.verdeBtn:
                    if (cbHoras.isChecked()) editor.putInt("horas", -16711936);
                    if (cbMinutos.isChecked()) editor.putInt("minutos", -16711936);
                    if (cbSegundos.isChecked()) editor.putInt("segundos", -16711936);
                    break;
                case R.id.rojoBtn:
                    if (cbHoras.isChecked()) editor.putInt("horas", -65536);
                    if (cbMinutos.isChecked()) editor.putInt("minutos", -65536);
                    if (cbSegundos.isChecked()) editor.putInt("segundos", -65536);
                    break;
                case R.id.azulBtn:
                    if (cbHoras.isChecked()) editor.putInt("horas", -16776961);
                    if (cbMinutos.isChecked()) editor.putInt("minutos", -16776961);
                    if (cbSegundos.isChecked()) editor.putInt("segundos", -16776961);
                    break;
                case R.id.amarilloBtn:
                    if (cbHoras.isChecked()) editor.putInt("horas", -256);
                    if (cbMinutos.isChecked()) editor.putInt("minutos", -256);
                    if (cbSegundos.isChecked()) editor.putInt("segundos", -256);
                    break;
                case R.id.cyanBtn:
                    if (cbHoras.isChecked()) editor.putInt("horas", -16711681);
                    if (cbMinutos.isChecked()) editor.putInt("minutos", -16711681);
                    if (cbSegundos.isChecked()) editor.putInt("segundos", -16711681);
                    break;
                case R.id.magentaBtn:
                    if (cbHoras.isChecked()) editor.putInt("horas", -65281);
                    if (cbMinutos.isChecked()) editor.putInt("minutos", -65281);
                    if (cbSegundos.isChecked()) editor.putInt("segundos", -65281);
                    break;
                case R.id.randomBtn:
                    Random random = new Random();
                    if (cbHoras.isChecked()) editor.putInt("horas", 0 - random.nextInt(2147483647));
                    if (cbMinutos.isChecked())
                        editor.putInt("minutos", 0 - random.nextInt(2147483647));
                    if (cbSegundos.isChecked())
                        editor.putInt("segundos", 0 - random.nextInt(2147483647));
                    break;
            }
            cbHoras.setChecked(false);
            cbMinutos.setChecked(false);
            cbSegundos.setChecked(false);

            editor.apply();
            Sincronizador.getInstance().Sincronizar();
        } else
            et.SetText(getString(R.string.toastColoresError));

        et.MostrarTexto();
    }

    public void Figuras(View v)
    {
        SharedPreferences config = getSharedPreferences("config", 0);
        SharedPreferences.Editor editor = config.edit();

        //0 = Cuadrado 1 = Circulo
        if (v.getId() == R.id.cuadradoRb)
            editor.putBoolean("figura", true);
        else if (v.getId() == R.id.circuloRb)
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

        if (((ToggleButton) findViewById(R.id.segunderoTbtn)).isChecked())
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

        if (((ToggleButton) findViewById(R.id.formatoTbtn)).isChecked())
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

        if (((ToggleButton) findViewById(R.id.fechaTbtn)).isChecked())
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

        if (((ToggleButton) findViewById(R.id.ayudaTbtn)).isChecked())
            editor.putBoolean("ayuda", true);
        else
            editor.putBoolean("ayuda", false);

        editor.apply();
        Sincronizador.getInstance().Sincronizar();
    }
}