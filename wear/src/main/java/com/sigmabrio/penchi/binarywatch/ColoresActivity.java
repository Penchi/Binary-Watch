//Código creado por Aarón Angulo

package com.sigmabrio.penchi.binarywatch;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Random;

public class ColoresActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colores);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                TextView mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
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

        CheckBox cbHoras = (CheckBox)findViewById(R.id.cbHoras);
        CheckBox cbMinutos = (CheckBox)findViewById(R.id.cbMinutos);
        CheckBox cbSegundos = (CheckBox)findViewById(R.id.cbSegundos);

        if(cbHoras.isChecked() || cbMinutos.isChecked() || cbSegundos.isChecked())
        {
            et.SetText(getString(R.string.toastColoresExito));
            switch (v.getId())
            {
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
                    if (cbMinutos.isChecked()) editor.putInt("minutos", 0 - random.nextInt(2147483647));
                    if (cbSegundos.isChecked()) editor.putInt("segundos", 0 - random.nextInt(2147483647));
                    break;
            }
            cbHoras.setChecked(false);
            cbMinutos.setChecked(false);
            cbSegundos.setChecked(false);

            editor.apply();
            Sincronizador.getInstance().Sincronizar();
        }
        else
            et.SetText(getString(R.string.toastColoresError));

        et.MostrarTexto();
    }

    @Override
    public void onPause()
    {
        finish();
        super.onPause();
    }
}
