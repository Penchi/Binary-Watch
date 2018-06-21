//Código creado por Aarón Angulo

package com.sigmabrio.penchi.binarywatch;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
/**
 * Created by orlan on 07/10/2016.
 */
public class Sincronizador implements DataApi.DataListener
{
    private static Sincronizador ourInstance = new Sincronizador();
    public static Sincronizador getInstance()
    {
        return ourInstance;
    }

    private GoogleApiClient mGoogleApiClient;
    private Context contexto;
    private SharedPreferences config;
    private PutDataMapRequest pdmr;

    private BinaryFace.Engine settings;

    private Sincronizador()
    {

    }

    public void IniciarGoogleApi(Context contexto, BinaryFace.Engine settings)
    {
        mGoogleApiClient = new GoogleApiClient.Builder(contexto)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                    }
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        this.contexto = contexto;
        this.settings = settings;
    }

    public void Sincronizar()
    {
        config = contexto.getSharedPreferences("config", 0);
        pdmr = PutDataMapRequest.create("/wearConfig");

        pdmr.getDataMap().putInt("horas", config.getInt("horas", -16711936));
        pdmr.getDataMap().putInt("minutos", config.getInt("minutos", -65536));
        pdmr.getDataMap().putInt("segundos", config.getInt("segundos", -16776961));
        pdmr.getDataMap().putBoolean("figura", config.getBoolean("figura", true));
        pdmr.getDataMap().putBoolean("fondo", config.getBoolean("fondo", true));
        pdmr.getDataMap().putBoolean("segundero", config.getBoolean("segundero", true));
        pdmr.getDataMap().putBoolean("formato", config.getBoolean("formato", true));
        pdmr.getDataMap().putBoolean("fecha", config.getBoolean("fecha", true));
        pdmr.getDataMap().putBoolean("ayuda", config.getBoolean("ayuda", true));
        pdmr.getDataMap().putLong("peticion", System.currentTimeMillis());

        Wearable.DataApi.putDataItem(mGoogleApiClient, pdmr.asPutDataRequest());
    }

    private ConfigActivity ca;
    public void ActividadTemporal(ConfigActivity caT)
    {
        ca = caT;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents)
    {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED)
            {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/handledConfig") == 0)
                {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                    SharedPreferences config = contexto.getSharedPreferences("config", 0);
                    SharedPreferences.Editor editor = config.edit();

                    editor.putInt("horas", dataMap.getInt("horas"));
                    editor.putInt("minutos", dataMap.getInt("minutos"));
                    editor.putInt("segundos", dataMap.getInt("segundos"));
                    editor.putBoolean("figura", dataMap.getBoolean("figura"));
                    editor.putBoolean("fondo", dataMap.getBoolean("fondo"));
                    editor.putBoolean("segundero", dataMap.getBoolean("segundero"));
                    editor.putBoolean("formato", dataMap.getBoolean("formato"));
                    editor.putBoolean("fecha", dataMap.getBoolean("fecha"));
                    editor.putBoolean("ayuda", dataMap.getBoolean("ayuda"));

                    editor.apply();
                    settings.Inicializar();
                    settings.Posiciones();
                    if(ca != null)
                        ca.Inicializar();
                }
                else
                    if (item.getUri().getPath().compareTo("/handledPeticion") == 0)
                        Sincronizar();
            }
        }
    }
}