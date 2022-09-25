package com.aplicativo.diseasedetector;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class HealtyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healty);

        Float score = getIntent().getExtras().getFloat("score");
        String textoScore =  String.format("%.2f", score);

        TextView text = (TextView)findViewById(R.id.resultText);
        String diagnostico = "A folha da imagem está saudável com ".concat(textoScore).concat("% de certeza");
        text.setText(diagnostico.concat(" , porém é importante manter alguns cuidados da mesma forma."));
    }
}