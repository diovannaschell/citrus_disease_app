package com.example.diseasedetector;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    ActivityResultLauncher<String> galleryActivityLauncher;
    ActivityResultLauncher<Intent> cameraActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // configurar o acesso a galeria
        galleryActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        try {
                            // ler o bitmap da imagem selecionada
                            Bitmap bitmapImg = MediaStore.Images.Media.getBitmap(getContentResolver(), result);
                            imageSelected(bitmapImg);
                        } catch (IOException e) {
                            // TODO: tratar quando o usuário não selecionar nenhuma imagem
                            e.printStackTrace();
                        }
                    }
                }
        );

        // configurar o acesso a câmera
        cameraActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bundle bundle = result.getData().getExtras();
                            Bitmap bitmapImg = (Bitmap) bundle.get("data");
                            imageSelected(bitmapImg);
                        } else {
                            // TODO: tratar quando o usuário sair sem selecionar foto
                        }
                    }
                }
        );

        // adicionar comportamento no botão da galeria
        Button galeria = findViewById(R.id.galleryButton);
        galeria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryActivityLauncher.launch("image/*");
            }
        });

        // adicionar comportamento no botão que abre a câmera
        Button camera = findViewById(R.id.cameraButton);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    cameraActivityLauncher.launch(intent);
                }
            }
        });
    }

    protected void imageSelected(Bitmap bitmapImg) {
        ImageView imgView = findViewById(R.id.imageView);
        imgView.setImageBitmap(bitmapImg);
    }
}