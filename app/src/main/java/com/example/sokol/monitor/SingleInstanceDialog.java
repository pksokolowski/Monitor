package com.example.sokol.monitor;

import android.app.FragmentManager;

/**
 * a simple contract for dialogs that could potentially be opened multiple times
 * one instance on top of another. Besides the traditional, required, way of showing
 * these dialogs, the interface requires them to have a method which only shows
 * a new instance if no previous instance exists as far as the provided FragmentManager
 * is aware.
 */
public interface SingleInstanceDialog {
    void showIfNotVisibleAlready(FragmentManager fragmentManager);
}
