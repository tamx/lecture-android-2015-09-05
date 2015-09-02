package com.example.tam.cardboardtest;

import android.os.Bundle;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;

public class MainActivity extends CardboardActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CardboardView view = new CardboardView(this);
        view.setRenderer(new GameRenderer());
        setContentView(view);
    }
}
