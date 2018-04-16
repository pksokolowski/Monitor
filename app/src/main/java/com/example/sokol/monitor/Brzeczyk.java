package com.example.sokol.monitor;

import android.content.Context;
import android.os.Vibrator;

public class Brzeczyk {

    public static void brzęcz(Context context) {
        Vibrator myVib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (myVib == null) return;
        myVib.vibrate(20);
    }
}
