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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.diseasedetector.ml.Modelo;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityResultLauncher<String> galleryActivityLauncher;
    ActivityResultLauncher<Intent> cameraActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // configurar opencv
        OpenCVLoader.initDebug();

        // configurar o acesso a galeria
        galleryActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        try {
                            // ler o bitmap da imagem selecionada
                            if (result != null) {
                                Bitmap bitmapImg = MediaStore.Images.Media.getBitmap(getContentResolver(), result);
                                imageSelected(bitmapImg);
                            }
                        } catch (IOException e) {
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
        // criar imagem mat com o bitmap
        Mat originalMatImage = new Mat();
        Utils.bitmapToMat(bitmapImg, originalMatImage);

        // aplicar o blur
        Mat bluredMatImage = new Mat(originalMatImage.rows(), originalMatImage.cols(), originalMatImage.type());
        Imgproc.blur(originalMatImage, bluredMatImage, new Size(51, 51));

        // voltar de Mat pra bitmap para seguir com a análise
        Bitmap finalBitmap = Bitmap.createBitmap(originalMatImage.cols(), originalMatImage.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(bluredMatImage, finalBitmap);

//        ImageView imgView = findViewById(R.id.imageView);
//        imgView.setImageBitmap(finalBitmap);

        // usar o modelo pré-treinado para analizar a imagem selecionada
        try {
            Modelo model = Modelo.newInstance(getApplicationContext());

            // Converte a imagem selecionada de bitmap para tensor
            TensorImage image = TensorImage.fromBitmap(finalBitmap);

            // Roda a inferência do modelo e pega os resultados
            Modelo.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();

            // Listar todos os resultados
//            ArrayList<String> resultados = new ArrayList<>();
//            for(Category resultado : probability)
//            {
//                Float score = resultado.getScore() * 100;
//                String textResult = resultado.getLabel().concat(" - ").concat(score.toString()).concat("%");
//                resultados.add(textResult);
//            }
//
//            ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, resultados);
//
//            ListView list = findViewById(R.id.list);
//            list.setAdapter(listAdapter);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
        }
    }
}