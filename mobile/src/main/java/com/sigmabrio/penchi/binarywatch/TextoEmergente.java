//Código creado por Aarón Angulo

package com.sigmabrio.penchi.binarywatch;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by orlan on 18/09/2016.
 */
public class TextoEmergente
{
    private Context context;
    private Toast toast;

    public TextoEmergente(Context context)
    {
        this.context = context;
    }

    public TextoEmergente(Context context, String texto)
    {
        this.context = context;
        toast = Toast.makeText(context, texto, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void MostrarTexto()
    {
        toast.show();
    }

    public void SetText(String texto)
    {
        toast = Toast.makeText(context, texto, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, -20);
    }
}
