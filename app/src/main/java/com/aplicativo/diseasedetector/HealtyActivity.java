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
        String diagnostico = "A folha da imagem informada está saudável com ";
        diagnostico = diagnostico.concat(textoScore.concat("% de certeza."));
        text.setText(diagnostico.concat(" A seguir são listados alguns procedimentos que devem ser seguidos."));
    }
}