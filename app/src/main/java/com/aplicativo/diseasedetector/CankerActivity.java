package com.aplicativo.diseasedetector;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class CankerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canker);

        Float score = getIntent().getExtras().getFloat("score");
        String textoScore =  String.format("%.2f", score);

        String modelo = getIntent().getExtras().getString("modelo");

        TextView text = (TextView)findViewById(R.id.resultText);
        String diagnostico = "Infelimente o CANCRO CÍTRICO foi detectado na imagem ";
        diagnostico = diagnostico.concat(modelo);
        diagnostico = diagnostico.concat(" informada, com ".concat(textoScore).concat("% de certeza."));
        text.setText(diagnostico.concat(" A seguir são listados alguns procedimentos que devem ser seguidos."));
    }
}