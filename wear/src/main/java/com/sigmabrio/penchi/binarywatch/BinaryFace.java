//Código creado por Aarón Angulo

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sigmabrio.penchi.binarywatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;

import java.lang.ref.WeakReference;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import android.content.SharedPreferences;

import java.text.DateFormatSymbols;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class BinaryFace extends CanvasWatchFaceService
{
    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<BinaryFace.Engine> mWeakReference;

        public EngineHandler(BinaryFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            BinaryFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    public class Engine extends CanvasWatchFaceService.Engine
    {
        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;
        Paint mBackgroundPaint;
        Paint mHandPaint;
        boolean mAmbient;
        Time mTime;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        private SharedPreferences config;
        private Rect pantalla;

        @Override
        public void onCreate(SurfaceHolder holder)
        {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(BinaryFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            mBackgroundPaint = new Paint();

            mHandPaint = new Paint();
            mHandPaint.setAntiAlias(true);
            mHandPaint.setStrokeCap(Paint.Cap.ROUND);

            mTime = new Time();

            config = getSharedPreferences("config", 0);
            minuto = mTime.minute;
            segundo = mTime.second;

            Sincronizador.getInstance().IniciarGoogleApi(getApplicationContext(), this);

            PreCalendario();
            Inicializar();
        }

        private boolean figura;
        private boolean fondo;
        private boolean formato;
        private boolean segundero;
        private boolean fecha;
        private boolean ayuda;

        private Paint colorHoras = new Paint();
        private Paint colorMinutos = new Paint();
        private Paint colorSegundos = new Paint();
        private Paint colorTema = new Paint();
        private Paint texto = new Paint();

        public void Inicializar()
        {
            figura = config.getBoolean("figura", true);
            fondo = config.getBoolean("fondo", true);
            formato = config.getBoolean("formato", true);
            segundero = config.getBoolean("segundero", true);
            fecha = config.getBoolean("fecha", true);
            ayuda = config.getBoolean("ayuda", true);

            if(figura)
            {
                colorHoras.setAntiAlias(false);
                colorMinutos.setAntiAlias(false);
                colorSegundos.setAntiAlias(false);
                colorTema.setAntiAlias(false);
            }
            else
            {
                colorHoras.setAntiAlias(true);
                colorMinutos.setAntiAlias(true);
                colorSegundos.setAntiAlias(true);
                colorTema.setAntiAlias(true);
            }

            if(fondo)
            {
                mBackgroundPaint.setColor(Color.BLACK);
                colorTema.setColor(Color.DKGRAY);
                texto.setColor(Color.GRAY);
            }
            else
            {
                mBackgroundPaint.setColor(Color.WHITE);
                colorTema.setColor(Color.LTGRAY);
                texto.setColor(Color.BLACK);
            }

            colorHoras.setColor(config.getInt("horas", -16711936));
            colorHoras.setStyle(Paint.Style.FILL);

            colorMinutos.setColor(config.getInt("minutos", -65536));
            colorMinutos.setStyle(Paint.Style.FILL);

            colorSegundos.setColor(config.getInt("segundos", -16776961));
            colorSegundos.setStyle(Paint.Style.FILL);


            colorTema.setStyle(Paint.Style.FILL);

            texto.setStyle(Paint.Style.FILL);
            texto.setAntiAlias(true);
            texto.setElegantTextHeight(true);
            texto.setTextSize(22);
            CalcularHora();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode)
        {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode)
            {
                segundero = false;
                colorTema.setColor(Color.DKGRAY);
                colorHoras.setColor(Color.WHITE);
                colorMinutos.setColor(Color.WHITE);

                Posiciones();
                mAmbient = inAmbientMode;
                if (mLowBitAmbient)
                {
                    mHandPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }
            if(!inAmbientMode)
            {
                Inicializar();
                Posiciones();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private boolean inicializado = true;
        private int segundo;
        private int minuto;
        private String hora;
        private String mes;

        @Override
        public void onDraw(Canvas canvas, Rect bounds)
        {
            mTime.setToNow();
            // Draw the background.
            if (isInAmbientMode())
                canvas.drawColor(Color.BLACK);
            else
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);

            if(inicializado)
            {
                inicializado = false;
                pantalla = bounds;
                Posiciones();
            }

            if(minuto != mTime.minute)
                CalcularHora();

            if(segundero)
                if(segundo != mTime.second)
                    CalcularSegundos();

            if(figura)
                DibujarCuadros(canvas);
            else
                DibujarCirculos(canvas);

            if(fecha)
                Fecha(canvas);

            if(ayuda)
                Ayuda(canvas);

            if(isVisible() && !mAmbient)
                invalidate();
        }

        void PreCalendario()
        {
            mes = new DateFormatSymbols().getMonths()[mTime.month].substring(0, 3) + " " + mTime.monthDay;
        }

        int Digitos(int n)
        {
            if(n < 10)
                return 1;
            else
                return 2;
        }

        private int[] horas = new int[6];

        void CalcularHora()
        {
            minuto = mTime.minute;
            int n = mTime.hour;

            if(!formato)
                if (n > 12)
                    n = mTime.hour - 12;

            if(Digitos(n) == 1)
            {
                horas[0] = 0;
                horas[1] = n;
            }
            else
            {
                horas[0] = n / 10;
                horas[1] = n % 10;
            }

            n = mTime.minute;

            if(Digitos(n) == 1)
            {
                horas[2] = 0;
                horas[3] = n;
            }
            else
            {
                horas[2] = n / 10;
                horas[3] = n % 10;
            }
        }

        void CalcularSegundos()
        {
            segundo = mTime.second;
            int n = mTime.second;

            if(Digitos(n) == 1)
            {
                horas[4] = 0;
                horas[5] = n;
            }
            else
            {
                horas[4] = n / 10;
                horas[5] = n % 10;
            }
        }
        //indices pares son inicios de figura, impares son fin de figura
        private int[] posX = new int[12];
        private int[] posY = new int[8];
        private int posXFecha;
        private int posYFecha;
        private int[] posXAyuda = new int[6];
        private int posYAyuda;
        /*
            Se calculan las posiciones de las figuras.
            Se le llama cuando inicia la aplicación y cada vez que se termina de configurar
         */
        void Posiciones()
        {
            int medida;
            int ancho = pantalla.width();
            int alto = pantalla.height();

            if(fecha)
            {
                posXFecha = (int) (pantalla.width() / 2.5);
                posYFecha = (int) (pantalla.height() / 1.15);
            }

            if(segundero)
            {
                medida = ancho / 9;

                posX[0] = ancho / 8;
                posX[1] = posX[0] + medida;
                for (int i = 2; i < 11; i++) {
                    posX[i] = posX[i - 2] + medida + medida / 5;
                    i++;
                    posX[i] = posX[i - 1] + medida;
                }

                posY[0] = (int) (alto / 1.5f);
                posY[1] = posY[0] + medida;
                for (int i = 2; i < 7; i++) {
                    posY[i] = posY[i - 2] - medida - medida / 5;
                    i++;
                    posY[i] = posY[i - 1] + medida;
                }
            }

            else
            {
                medida = ancho / 7;

                posX[0] = ancho / 6;
                posX[1] = posX[0] + medida;
                for (int i = 2; i < 11; i++) {
                    posX[i] = posX[i - 2] + medida + medida / 5;
                    i++;
                    posX[i] = posX[i - 1] + medida;
                }

                posY[0] = (int) (alto / 1.5f);
                posY[1] = posY[0] + medida;
                for (int i = 2; i < 7; i++) {
                    posY[i] = posY[i - 2] - medida - medida / 5;
                    i++;
                    posY[i] = posY[i - 1] + medida;
                }
            }

            if(ayuda)
            {
                if(segundero)
                    posYAyuda = posY[7] - medida - (medida / 4);
                else
                    posYAyuda = posY[7] - medida- (medida / 6);

                for (int i = 0; i < 6; i++)
                    posXAyuda[i] = posX[i * 2] + (medida / 2) - 6;
            }
        }

        //Dibuja el texto que representa la fecha
        void Fecha(Canvas canvas)
        {
            canvas.drawText(mes, posXFecha, posYFecha, texto);
        }

        //Dibuja el texto para ayudar a leer el reloj
        void Ayuda(Canvas canvas)
        {
            for (int i = 0; i < 4; i++)
                canvas.drawText("" + horas[i], posXAyuda[i], posYAyuda, texto);

            if(segundero)
            {
                canvas.drawText("" + horas[4], posXAyuda[4], posYAyuda, texto);
                canvas.drawText("" + horas[5], posXAyuda[5], posYAyuda, texto);
            }
        }
        //Cuando figura == false, se dibujan circulos para representar el reloj, se ejecuta cada frame
        void DibujarCirculos(Canvas canvas)
        {
            if(formato)
                switch (horas[0])
                {
                    case 0:
                        canvas.drawOval(posX[0], posY[0], posX[1], posY[1], colorTema);
                        canvas.drawOval(posX[0], posY[2], posX[1], posY[3], colorTema);
                        break;
                    case 1:
                        canvas.drawOval(posX[0], posY[0], posX[1], posY[1], colorHoras);
                        canvas.drawOval(posX[0], posY[2], posX[1], posY[3], colorTema);
                        break;
                    case 2:
                        canvas.drawOval(posX[0], posY[0], posX[1], posY[1], colorTema);
                        canvas.drawOval(posX[0], posY[2], posX[1], posY[3], colorHoras);
                        break;
                }
            else
                switch (horas[0])
                {
                    case 0:
                        canvas.drawOval(posX[0], posY[0], posX[1], posY[1], colorTema);
                        break;
                    case 1:
                        canvas.drawOval(posX[0], posY[0], posX[1], posY[1], colorHoras);
                        break;
                    case 2:
                        canvas.drawOval(posX[0], posY[0], posX[1], posY[1], colorTema);
                        break;
                }

            switch(horas[1])
            {
                case 0:
                    canvas.drawOval(posX[2], posY[0], posX[3], posY[1], colorTema);
                    canvas.drawOval(posX[2], posY[2], posX[3], posY[3], colorTema);
                    canvas.drawOval(posX[2], posY[4], posX[3], posY[5], colorTema);
                    canvas.drawOval(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 1:
                    canvas.drawOval(posX[2], posY[0], posX[3], posY[1], colorHoras);
                    canvas.drawOval(posX[2], posY[2], posX[3], posY[3], colorTema);
                    canvas.drawOval(posX[2], posY[4], posX[3], posY[5], colorTema);
                    canvas.drawOval(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 2:
                    canvas.drawOval(posX[2], posY[0], posX[3], posY[1], colorTema);
                    canvas.drawOval(posX[2], posY[2], posX[3], posY[3], colorHoras);
                    canvas.drawOval(posX[2], posY[4], posX[3], posY[5], colorTema);
                    canvas.drawOval(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 3:
                    canvas.drawOval(posX[2], posY[0], posX[3], posY[1], colorHoras);
                    canvas.drawOval(posX[2], posY[2], posX[3], posY[3], colorHoras);
                    canvas.drawOval(posX[2], posY[4], posX[3], posY[5], colorTema);
                    canvas.drawOval(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 4:
                    canvas.drawOval(posX[2], posY[0], posX[3], posY[1], colorTema);
                    canvas.drawOval(posX[2], posY[2], posX[3], posY[3], colorTema);
                    canvas.drawOval(posX[2], posY[4], posX[3], posY[5], colorHoras);
                    canvas.drawOval(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 5:
                    canvas.drawOval(posX[2], posY[0], posX[3], posY[1], colorHoras);
                    canvas.drawOval(posX[2], posY[2], posX[3], posY[3], colorTema);
                    canvas.drawOval(posX[2], posY[4], posX[3], posY[5], colorHoras);
                    canvas.drawOval(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 6:
                    canvas.drawOval(posX[2], posY[0], posX[3], posY[1], colorTema);
                    canvas.drawOval(posX[2], posY[2], posX[3], posY[3], colorHoras);
                    canvas.drawOval(posX[2], posY[4], posX[3], posY[5], colorHoras);
                    canvas.drawOval(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 7:
                    canvas.drawOval(posX[2], posY[0], posX[3], posY[1], colorHoras);
                    canvas.drawOval(posX[2], posY[2], posX[3], posY[3], colorHoras);
                    canvas.drawOval(posX[2], posY[4], posX[3], posY[5], colorHoras);
                    canvas.drawOval(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 8:
                    canvas.drawOval(posX[2], posY[0], posX[3], posY[1], colorTema);
                    canvas.drawOval(posX[2], posY[2], posX[3], posY[3], colorTema);
                    canvas.drawOval(posX[2], posY[4], posX[3], posY[5], colorTema);
                    canvas.drawOval(posX[2], posY[6], posX[3], posY[7], colorHoras);
                    break;
                case 9:
                    canvas.drawOval(posX[2], posY[0], posX[3], posY[1], colorHoras);
                    canvas.drawOval(posX[2], posY[2], posX[3], posY[3], colorTema);
                    canvas.drawOval(posX[2], posY[4], posX[3], posY[5], colorTema);
                    canvas.drawOval(posX[2], posY[6], posX[3], posY[7], colorHoras);
                    break;
            }

            switch(horas[2])
            {
                case 0:
                    canvas.drawOval(posX[4], posY[0], posX[5], posY[1], colorTema);
                    canvas.drawOval(posX[4], posY[2], posX[5], posY[3], colorTema);
                    canvas.drawOval(posX[4], posY[4], posX[5], posY[5], colorTema);
                    break;
                case 1:
                    canvas.drawOval(posX[4], posY[0], posX[5], posY[1], colorMinutos);
                    canvas.drawOval(posX[4], posY[2], posX[5], posY[3], colorTema);
                    canvas.drawOval(posX[4], posY[4], posX[5], posY[5], colorTema);
                    break;
                case 2:
                    canvas.drawOval(posX[4], posY[0], posX[5], posY[1], colorTema);
                    canvas.drawOval(posX[4], posY[2], posX[5], posY[3], colorMinutos);
                    canvas.drawOval(posX[4], posY[4], posX[5], posY[5], colorTema);
                    break;
                case 3:
                    canvas.drawOval(posX[4], posY[0], posX[5], posY[1], colorMinutos);
                    canvas.drawOval(posX[4], posY[2], posX[5], posY[3], colorMinutos);
                    canvas.drawOval(posX[4], posY[4], posX[5], posY[5], colorTema);
                    break;
                case 4:
                    canvas.drawOval(posX[4], posY[0], posX[5], posY[1], colorTema);
                    canvas.drawOval(posX[4], posY[2], posX[5], posY[3], colorTema);
                    canvas.drawOval(posX[4], posY[4], posX[5], posY[5], colorMinutos);
                    break;
                case 5:
                    canvas.drawOval(posX[4], posY[0], posX[5], posY[1], colorMinutos);
                    canvas.drawOval(posX[4], posY[2], posX[5], posY[3], colorTema);
                    canvas.drawOval(posX[4], posY[4], posX[5], posY[5], colorMinutos);
                    break;
            }

            switch(horas[3])
            {
                case 0:
                    canvas.drawOval(posX[6], posY[0], posX[7], posY[1], colorTema);
                    canvas.drawOval(posX[6], posY[2], posX[7], posY[3], colorTema);
                    canvas.drawOval(posX[6], posY[4], posX[7], posY[5], colorTema);
                    canvas.drawOval(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 1:
                    canvas.drawOval(posX[6], posY[0], posX[7], posY[1], colorMinutos);
                    canvas.drawOval(posX[6], posY[2], posX[7], posY[3], colorTema);
                    canvas.drawOval(posX[6], posY[4], posX[7], posY[5], colorTema);
                    canvas.drawOval(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 2:
                    canvas.drawOval(posX[6], posY[0], posX[7], posY[1], colorTema);
                    canvas.drawOval(posX[6], posY[2], posX[7], posY[3], colorMinutos);
                    canvas.drawOval(posX[6], posY[4], posX[7], posY[5], colorTema);
                    canvas.drawOval(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 3:
                    canvas.drawOval(posX[6], posY[0], posX[7], posY[1], colorMinutos);
                    canvas.drawOval(posX[6], posY[2], posX[7], posY[3], colorMinutos);
                    canvas.drawOval(posX[6], posY[4], posX[7], posY[5], colorTema);
                    canvas.drawOval(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 4:
                    canvas.drawOval(posX[6], posY[0], posX[7], posY[1], colorTema);
                    canvas.drawOval(posX[6], posY[2], posX[7], posY[3], colorTema);
                    canvas.drawOval(posX[6], posY[4], posX[7], posY[5], colorMinutos);
                    canvas.drawOval(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 5:
                    canvas.drawOval(posX[6], posY[0], posX[7], posY[1], colorMinutos);
                    canvas.drawOval(posX[6], posY[2], posX[7], posY[3], colorTema);
                    canvas.drawOval(posX[6], posY[4], posX[7], posY[5], colorMinutos);
                    canvas.drawOval(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 6:
                    canvas.drawOval(posX[6], posY[0], posX[7], posY[1], colorTema);
                    canvas.drawOval(posX[6], posY[2], posX[7], posY[3], colorMinutos);
                    canvas.drawOval(posX[6], posY[4], posX[7], posY[5], colorMinutos);
                    canvas.drawOval(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 7:
                    canvas.drawOval(posX[6], posY[0], posX[7], posY[1], colorMinutos);
                    canvas.drawOval(posX[6], posY[2], posX[7], posY[3], colorMinutos);
                    canvas.drawOval(posX[6], posY[4], posX[7], posY[5], colorMinutos);
                    canvas.drawOval(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 8:
                    canvas.drawOval(posX[6], posY[0], posX[7], posY[1], colorTema);
                    canvas.drawOval(posX[6], posY[2], posX[7], posY[3], colorTema);
                    canvas.drawOval(posX[6], posY[4], posX[7], posY[5], colorTema);
                    canvas.drawOval(posX[6], posY[6], posX[7], posY[7], colorMinutos);
                    break;
                case 9:
                    canvas.drawOval(posX[6], posY[0], posX[7], posY[1], colorMinutos);
                    canvas.drawOval(posX[6], posY[2], posX[7], posY[3], colorTema);
                    canvas.drawOval(posX[6], posY[4], posX[7], posY[5], colorTema);
                    canvas.drawOval(posX[6], posY[6], posX[7], posY[7], colorMinutos);
                    break;
            }

            if(segundero)
            {
                switch(horas[4])
                {
                    case 0:
                        canvas.drawOval(posX[8], posY[0], posX[9], posY[1], colorTema);
                        canvas.drawOval(posX[8], posY[2], posX[9], posY[3], colorTema);
                        canvas.drawOval(posX[8], posY[4], posX[9], posY[5], colorTema);
                        break;
                    case 1:
                        canvas.drawOval(posX[8], posY[0], posX[9], posY[1], colorSegundos);
                        canvas.drawOval(posX[8], posY[2], posX[9], posY[3], colorTema);
                        canvas.drawOval(posX[8], posY[4], posX[9], posY[5], colorTema);
                        break;
                    case 2:
                        canvas.drawOval(posX[8], posY[0], posX[9], posY[1], colorTema);
                        canvas.drawOval(posX[8], posY[2], posX[9], posY[3], colorSegundos);
                        canvas.drawOval(posX[8], posY[4], posX[9], posY[5], colorTema);
                        break;
                    case 3:
                        canvas.drawOval(posX[8], posY[0], posX[9], posY[1], colorSegundos);
                        canvas.drawOval(posX[8], posY[2], posX[9], posY[3], colorSegundos);
                        canvas.drawOval(posX[8], posY[4], posX[9], posY[5], colorTema);
                        break;
                    case 4:
                        canvas.drawOval(posX[8], posY[0], posX[9], posY[1], colorTema);
                        canvas.drawOval(posX[8], posY[2], posX[9], posY[3], colorTema);
                        canvas.drawOval(posX[8], posY[4], posX[9], posY[5], colorSegundos);
                        break;
                    case 5:
                        canvas.drawOval(posX[8], posY[0], posX[9], posY[1], colorSegundos);
                        canvas.drawOval(posX[8], posY[2], posX[9], posY[3], colorTema);
                        canvas.drawOval(posX[8], posY[4], posX[9], posY[5], colorSegundos);
                        break;
                }

                switch(horas[5])
                {
                    case 0:
                        canvas.drawOval(posX[10], posY[0], posX[11], posY[1], colorTema);
                        canvas.drawOval(posX[10], posY[2], posX[11], posY[3], colorTema);
                        canvas.drawOval(posX[10], posY[4], posX[11], posY[5], colorTema);
                        canvas.drawOval(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 1:
                        canvas.drawOval(posX[10], posY[0], posX[11], posY[1], colorSegundos);
                        canvas.drawOval(posX[10], posY[2], posX[11], posY[3], colorTema);
                        canvas.drawOval(posX[10], posY[4], posX[11], posY[5], colorTema);
                        canvas.drawOval(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 2:
                        canvas.drawOval(posX[10], posY[0], posX[11], posY[1], colorTema);
                        canvas.drawOval(posX[10], posY[2], posX[11], posY[3], colorSegundos);
                        canvas.drawOval(posX[10], posY[4], posX[11], posY[5], colorTema);
                        canvas.drawOval(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 3:
                        canvas.drawOval(posX[10], posY[0], posX[11], posY[1], colorSegundos);
                        canvas.drawOval(posX[10], posY[2], posX[11], posY[3], colorSegundos);
                        canvas.drawOval(posX[10], posY[4], posX[11], posY[5], colorTema);
                        canvas.drawOval(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 4:
                        canvas.drawOval(posX[10], posY[0], posX[11], posY[1], colorTema);
                        canvas.drawOval(posX[10], posY[2], posX[11], posY[3], colorTema);
                        canvas.drawOval(posX[10], posY[4], posX[11], posY[5], colorSegundos);
                        canvas.drawOval(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 5:
                        canvas.drawOval(posX[10], posY[0], posX[11], posY[1], colorSegundos);
                        canvas.drawOval(posX[10], posY[2], posX[11], posY[3], colorTema);
                        canvas.drawOval(posX[10], posY[4], posX[11], posY[5], colorSegundos);
                        canvas.drawOval(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 6:
                        canvas.drawOval(posX[10], posY[0], posX[11], posY[1], colorTema);
                        canvas.drawOval(posX[10], posY[2], posX[11], posY[3], colorSegundos);
                        canvas.drawOval(posX[10], posY[4], posX[11], posY[5], colorSegundos);
                        canvas.drawOval(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 7:
                        canvas.drawOval(posX[10], posY[0], posX[11], posY[1], colorSegundos);
                        canvas.drawOval(posX[10], posY[2], posX[11], posY[3], colorSegundos);
                        canvas.drawOval(posX[10], posY[4], posX[11], posY[5], colorSegundos);
                        canvas.drawOval(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 8:
                        canvas.drawOval(posX[10], posY[0], posX[11], posY[1], colorTema);
                        canvas.drawOval(posX[10], posY[2], posX[11], posY[3], colorTema);
                        canvas.drawOval(posX[10], posY[4], posX[11], posY[5], colorTema);
                        canvas.drawOval(posX[10], posY[6], posX[11], posY[7], colorSegundos);
                        break;
                    case 9:
                        canvas.drawOval(posX[10], posY[0], posX[11], posY[1], colorSegundos);
                        canvas.drawOval(posX[10], posY[2], posX[11], posY[3], colorTema);
                        canvas.drawOval(posX[10], posY[4], posX[11], posY[5], colorTema);
                        canvas.drawOval(posX[10], posY[6], posX[11], posY[7], colorSegundos);
                        break;
                }
            }
        }
        //Cuando figura == true, se dibujan cuadros para representar el reloj, se ejecuta cada frame
        void DibujarCuadros(Canvas canvas)
        {
            if(formato)
                switch (horas[0])
                {
                    case 0:
                        canvas.drawRect(posX[0], posY[0], posX[1], posY[1], colorTema);
                        canvas.drawRect(posX[0], posY[2], posX[1], posY[3], colorTema);
                        break;
                    case 1:
                        canvas.drawRect(posX[0], posY[0], posX[1], posY[1], colorHoras);
                        canvas.drawRect(posX[0], posY[2], posX[1], posY[3], colorTema);
                        break;
                    case 2:
                        canvas.drawRect(posX[0], posY[0], posX[1], posY[1], colorTema);
                        canvas.drawRect(posX[0], posY[2], posX[1], posY[3], colorHoras);
                        break;
                }
            else
                switch (horas[0])
                {
                    case 0:
                        canvas.drawRect(posX[0], posY[0], posX[1], posY[1], colorTema);
                        break;
                    case 1:
                        canvas.drawRect(posX[0], posY[0], posX[1], posY[1], colorHoras);
                        break;
                    case 2:
                        canvas.drawRect(posX[0], posY[0], posX[1], posY[1], colorTema);
                        break;
                }
            switch(horas[1])
            {
                case 0:
                    canvas.drawRect(posX[2], posY[0], posX[3], posY[1], colorTema);
                    canvas.drawRect(posX[2], posY[2], posX[3], posY[3], colorTema);
                    canvas.drawRect(posX[2], posY[4], posX[3], posY[5], colorTema);
                    canvas.drawRect(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 1:
                    canvas.drawRect(posX[2], posY[0], posX[3], posY[1], colorHoras);
                    canvas.drawRect(posX[2], posY[2], posX[3], posY[3], colorTema);
                    canvas.drawRect(posX[2], posY[4], posX[3], posY[5], colorTema);
                    canvas.drawRect(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 2:
                    canvas.drawRect(posX[2], posY[0], posX[3], posY[1], colorTema);
                    canvas.drawRect(posX[2], posY[2], posX[3], posY[3], colorHoras);
                    canvas.drawRect(posX[2], posY[4], posX[3], posY[5], colorTema);
                    canvas.drawRect(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 3:
                    canvas.drawRect(posX[2], posY[0], posX[3], posY[1], colorHoras);
                    canvas.drawRect(posX[2], posY[2], posX[3], posY[3], colorHoras);
                    canvas.drawRect(posX[2], posY[4], posX[3], posY[5], colorTema);
                    canvas.drawRect(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 4:
                    canvas.drawRect(posX[2], posY[0], posX[3], posY[1], colorTema);
                    canvas.drawRect(posX[2], posY[2], posX[3], posY[3], colorTema);
                    canvas.drawRect(posX[2], posY[4], posX[3], posY[5], colorHoras);
                    canvas.drawRect(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 5:
                    canvas.drawRect(posX[2], posY[0], posX[3], posY[1], colorHoras);
                    canvas.drawRect(posX[2], posY[2], posX[3], posY[3], colorTema);
                    canvas.drawRect(posX[2], posY[4], posX[3], posY[5], colorHoras);
                    canvas.drawRect(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 6:
                    canvas.drawRect(posX[2], posY[0], posX[3], posY[1], colorTema);
                    canvas.drawRect(posX[2], posY[2], posX[3], posY[3], colorHoras);
                    canvas.drawRect(posX[2], posY[4], posX[3], posY[5], colorHoras);
                    canvas.drawRect(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 7:
                    canvas.drawRect(posX[2], posY[0], posX[3], posY[1], colorHoras);
                    canvas.drawRect(posX[2], posY[2], posX[3], posY[3], colorHoras);
                    canvas.drawRect(posX[2], posY[4], posX[3], posY[5], colorHoras);
                    canvas.drawRect(posX[2], posY[6], posX[3], posY[7], colorTema);
                    break;
                case 8:
                    canvas.drawRect(posX[2], posY[0], posX[3], posY[1], colorTema);
                    canvas.drawRect(posX[2], posY[2], posX[3], posY[3], colorTema);
                    canvas.drawRect(posX[2], posY[4], posX[3], posY[5], colorTema);
                    canvas.drawRect(posX[2], posY[6], posX[3], posY[7], colorHoras);
                    break;
                case 9:
                    canvas.drawRect(posX[2], posY[0], posX[3], posY[1], colorHoras);
                    canvas.drawRect(posX[2], posY[2], posX[3], posY[3], colorTema);
                    canvas.drawRect(posX[2], posY[4], posX[3], posY[5], colorTema);
                    canvas.drawRect(posX[2], posY[6], posX[3], posY[7], colorHoras);
                    break;
            }

            switch(horas[2])
            {
                case 0:
                    canvas.drawRect(posX[4], posY[0], posX[5], posY[1], colorTema);
                    canvas.drawRect(posX[4], posY[2], posX[5], posY[3], colorTema);
                    canvas.drawRect(posX[4], posY[4], posX[5], posY[5], colorTema);
                    break;
                case 1:
                    canvas.drawRect(posX[4], posY[0], posX[5], posY[1], colorMinutos);
                    canvas.drawRect(posX[4], posY[2], posX[5], posY[3], colorTema);
                    canvas.drawRect(posX[4], posY[4], posX[5], posY[5], colorTema);
                    break;
                case 2:
                    canvas.drawRect(posX[4], posY[0], posX[5], posY[1], colorTema);
                    canvas.drawRect(posX[4], posY[2], posX[5], posY[3], colorMinutos);
                    canvas.drawRect(posX[4], posY[4], posX[5], posY[5], colorTema);
                    break;
                case 3:
                    canvas.drawRect(posX[4], posY[0], posX[5], posY[1], colorMinutos);
                    canvas.drawRect(posX[4], posY[2], posX[5], posY[3], colorMinutos);
                    canvas.drawRect(posX[4], posY[4], posX[5], posY[5], colorTema);
                    break;
                case 4:
                    canvas.drawRect(posX[4], posY[0], posX[5], posY[1], colorTema);
                    canvas.drawRect(posX[4], posY[2], posX[5], posY[3], colorTema);
                    canvas.drawRect(posX[4], posY[4], posX[5], posY[5], colorMinutos);
                    break;
                case 5:
                    canvas.drawRect(posX[4], posY[0], posX[5], posY[1], colorMinutos);
                    canvas.drawRect(posX[4], posY[2], posX[5], posY[3], colorTema);
                    canvas.drawRect(posX[4], posY[4], posX[5], posY[5], colorMinutos);
                    break;
            }

            switch(horas[3])
            {
                case 0:
                    canvas.drawRect(posX[6], posY[0], posX[7], posY[1], colorTema);
                    canvas.drawRect(posX[6], posY[2], posX[7], posY[3], colorTema);
                    canvas.drawRect(posX[6], posY[4], posX[7], posY[5], colorTema);
                    canvas.drawRect(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 1:
                    canvas.drawRect(posX[6], posY[0], posX[7], posY[1], colorMinutos);
                    canvas.drawRect(posX[6], posY[2], posX[7], posY[3], colorTema);
                    canvas.drawRect(posX[6], posY[4], posX[7], posY[5], colorTema);
                    canvas.drawRect(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 2:
                    canvas.drawRect(posX[6], posY[0], posX[7], posY[1], colorTema);
                    canvas.drawRect(posX[6], posY[2], posX[7], posY[3], colorMinutos);
                    canvas.drawRect(posX[6], posY[4], posX[7], posY[5], colorTema);
                    canvas.drawRect(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 3:
                    canvas.drawRect(posX[6], posY[0], posX[7], posY[1], colorMinutos);
                    canvas.drawRect(posX[6], posY[2], posX[7], posY[3], colorMinutos);
                    canvas.drawRect(posX[6], posY[4], posX[7], posY[5], colorTema);
                    canvas.drawRect(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 4:
                    canvas.drawRect(posX[6], posY[0], posX[7], posY[1], colorTema);
                    canvas.drawRect(posX[6], posY[2], posX[7], posY[3], colorTema);
                    canvas.drawRect(posX[6], posY[4], posX[7], posY[5], colorMinutos);
                    canvas.drawRect(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 5:
                    canvas.drawRect(posX[6], posY[0], posX[7], posY[1], colorMinutos);
                    canvas.drawRect(posX[6], posY[2], posX[7], posY[3], colorTema);
                    canvas.drawRect(posX[6], posY[4], posX[7], posY[5], colorMinutos);
                    canvas.drawRect(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 6:
                    canvas.drawRect(posX[6], posY[0], posX[7], posY[1], colorTema);
                    canvas.drawRect(posX[6], posY[2], posX[7], posY[3], colorMinutos);
                    canvas.drawRect(posX[6], posY[4], posX[7], posY[5], colorMinutos);
                    canvas.drawRect(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 7:
                    canvas.drawRect(posX[6], posY[0], posX[7], posY[1], colorMinutos);
                    canvas.drawRect(posX[6], posY[2], posX[7], posY[3], colorMinutos);
                    canvas.drawRect(posX[6], posY[4], posX[7], posY[5], colorMinutos);
                    canvas.drawRect(posX[6], posY[6], posX[7], posY[7], colorTema);
                    break;
                case 8:
                    canvas.drawRect(posX[6], posY[0], posX[7], posY[1], colorTema);
                    canvas.drawRect(posX[6], posY[2], posX[7], posY[3], colorTema);
                    canvas.drawRect(posX[6], posY[4], posX[7], posY[5], colorTema);
                    canvas.drawRect(posX[6], posY[6], posX[7], posY[7], colorMinutos);
                    break;
                case 9:
                    canvas.drawRect(posX[6], posY[0], posX[7], posY[1], colorMinutos);
                    canvas.drawRect(posX[6], posY[2], posX[7], posY[3], colorTema);
                    canvas.drawRect(posX[6], posY[4], posX[7], posY[5], colorTema);
                    canvas.drawRect(posX[6], posY[6], posX[7], posY[7], colorMinutos);
                    break;
            }

            if(segundero)
            {
                switch(horas[4])
                {
                    case 0:
                        canvas.drawRect(posX[8], posY[0], posX[9], posY[1], colorTema);
                        canvas.drawRect(posX[8], posY[2], posX[9], posY[3], colorTema);
                        canvas.drawRect(posX[8], posY[4], posX[9], posY[5], colorTema);
                        break;
                    case 1:
                        canvas.drawRect(posX[8], posY[0], posX[9], posY[1], colorSegundos);
                        canvas.drawRect(posX[8], posY[2], posX[9], posY[3], colorTema);
                        canvas.drawRect(posX[8], posY[4], posX[9], posY[5], colorTema);
                        break;
                    case 2:
                        canvas.drawRect(posX[8], posY[0], posX[9], posY[1], colorTema);
                        canvas.drawRect(posX[8], posY[2], posX[9], posY[3], colorSegundos);
                        canvas.drawRect(posX[8], posY[4], posX[9], posY[5], colorTema);
                        break;
                    case 3:
                        canvas.drawRect(posX[8], posY[0], posX[9], posY[1], colorSegundos);
                        canvas.drawRect(posX[8], posY[2], posX[9], posY[3], colorSegundos);
                        canvas.drawRect(posX[8], posY[4], posX[9], posY[5], colorTema);
                        break;
                    case 4:
                        canvas.drawRect(posX[8], posY[0], posX[9], posY[1], colorTema);
                        canvas.drawRect(posX[8], posY[2], posX[9], posY[3], colorTema);
                        canvas.drawRect(posX[8], posY[4], posX[9], posY[5], colorSegundos);
                        break;
                    case 5:
                        canvas.drawRect(posX[8], posY[0], posX[9], posY[1], colorSegundos);
                        canvas.drawRect(posX[8], posY[2], posX[9], posY[3], colorTema);
                        canvas.drawRect(posX[8], posY[4], posX[9], posY[5], colorSegundos);
                        break;
                }

                switch(horas[5])
                {
                    case 0:
                        canvas.drawRect(posX[10], posY[0], posX[11], posY[1], colorTema);
                        canvas.drawRect(posX[10], posY[2], posX[11], posY[3], colorTema);
                        canvas.drawRect(posX[10], posY[4], posX[11], posY[5], colorTema);
                        canvas.drawRect(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 1:
                        canvas.drawRect(posX[10], posY[0], posX[11], posY[1], colorSegundos);
                        canvas.drawRect(posX[10], posY[2], posX[11], posY[3], colorTema);
                        canvas.drawRect(posX[10], posY[4], posX[11], posY[5], colorTema);
                        canvas.drawRect(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 2:
                        canvas.drawRect(posX[10], posY[0], posX[11], posY[1], colorTema);
                        canvas.drawRect(posX[10], posY[2], posX[11], posY[3], colorSegundos);
                        canvas.drawRect(posX[10], posY[4], posX[11], posY[5], colorTema);
                        canvas.drawRect(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 3:
                        canvas.drawRect(posX[10], posY[0], posX[11], posY[1], colorSegundos);
                        canvas.drawRect(posX[10], posY[2], posX[11], posY[3], colorSegundos);
                        canvas.drawRect(posX[10], posY[4], posX[11], posY[5], colorTema);
                        canvas.drawRect(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 4:
                        canvas.drawRect(posX[10], posY[0], posX[11], posY[1], colorTema);
                        canvas.drawRect(posX[10], posY[2], posX[11], posY[3], colorTema);
                        canvas.drawRect(posX[10], posY[4], posX[11], posY[5], colorSegundos);
                        canvas.drawRect(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 5:
                        canvas.drawRect(posX[10], posY[0], posX[11], posY[1], colorSegundos);
                        canvas.drawRect(posX[10], posY[2], posX[11], posY[3], colorTema);
                        canvas.drawRect(posX[10], posY[4], posX[11], posY[5], colorSegundos);
                        canvas.drawRect(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 6:
                        canvas.drawRect(posX[10], posY[0], posX[11], posY[1], colorTema);
                        canvas.drawRect(posX[10], posY[2], posX[11], posY[3], colorSegundos);
                        canvas.drawRect(posX[10], posY[4], posX[11], posY[5], colorSegundos);
                        canvas.drawRect(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 7:
                        canvas.drawRect(posX[10], posY[0], posX[11], posY[1], colorSegundos);
                        canvas.drawRect(posX[10], posY[2], posX[11], posY[3], colorSegundos);
                        canvas.drawRect(posX[10], posY[4], posX[11], posY[5], colorSegundos);
                        canvas.drawRect(posX[10], posY[6], posX[11], posY[7], colorTema);
                        break;
                    case 8:
                        canvas.drawRect(posX[10], posY[0], posX[11], posY[1], colorTema);
                        canvas.drawRect(posX[10], posY[2], posX[11], posY[3], colorTema);
                        canvas.drawRect(posX[10], posY[4], posX[11], posY[5], colorTema);
                        canvas.drawRect(posX[10], posY[6], posX[11], posY[7], colorSegundos);
                        break;
                    case 9:
                        canvas.drawRect(posX[10], posY[0], posX[11], posY[1], colorSegundos);
                        canvas.drawRect(posX[10], posY[2], posX[11], posY[3], colorTema);
                        canvas.drawRect(posX[10], posY[4], posX[11], posY[5], colorTema);
                        canvas.drawRect(posX[10], posY[6], posX[11], posY[7], colorSegundos);
                        break;
                }
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible)
        {
            super.onVisibilityChanged(visible);

            if (visible)
            {
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
                registerReceiver();
                Inicializar();
                Posiciones();
                CalcularHora();
                PreCalendario();
            }
            else
                unregisterReceiver();
            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            BinaryFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            BinaryFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

    }
}
