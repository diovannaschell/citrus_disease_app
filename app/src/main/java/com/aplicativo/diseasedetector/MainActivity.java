package com.aplicativo.diseasedetector;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import com.aplicativo.diseasedetector.ml.GaussianBlurModel;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
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
                            //
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
        Imgproc.GaussianBlur(originalMatImage, bluredMatImage, new Size(3, 3), 0);

        // voltar de Mat pra bitmap para seguir com a análise
        Bitmap finalBitmap = Bitmap.createBitmap(originalMatImage.cols(), originalMatImage.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(bluredMatImage, finalBitmap);

        this.executeModelInference(finalBitmap);
    }

    protected void executeModelInference(Bitmap bitmapImage) {
        // usar o modelo pré-treinado para analizar a imagem selecionada
        try {
            GaussianBlurModel model = GaussianBlurModel.newInstance(getApplicationContext());

            // Converte a imagem selecionada de bitmap para tensor
            TensorImage image = TensorImage.fromBitmap(bitmapImage);

            // Roda a inferência do modelo e pega os resultados
            GaussianBlurModel.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();

            // encerra o uso do modelo
            model.close();

            // ordenar os resultados do modelo para que o que tem maior probabilidade ficar em primeiro
            probability.sort((a,b) -> {
                return a.getScore() < b.getScore() ? 1: -1;
            });

            Category first = probability.get(0);

            // se a classe com maior chance de estar na imagem tiver pontuação igual ou menor que 50% então não é um resultado válido
            if (first.getScore() <= 0.5) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("Ops!");
                dialog.setMessage("Não foi possível chegar a uma conclusão com a imagem informada. Isso pode acontecer quando a foto não aprensenta sintomas ou os sinatomas apresentados não são correspondentes as doenças abordadas nesse APP. Por favor, tente com outra imagem.");
                dialog.show();
            } else {
                this.redirectToResultScreen(first);
            }
        } catch (IOException e) {
        }
    }

    private void redirectToResultScreen(Category result) {
        String label = result.getLabel();

        // redireciona para a tela de resultado correspondente a label inferida
        switch (label) {
            case "healthy":
                Intent healtyIntent = new Intent(MainActivity.this, HealtyActivity.class);
                startActivity(healtyIntent);
                break;
            case "canker":
                Intent cankerIntent = new Intent(MainActivity.this, CankerActivity.class);
                startActivity(cankerIntent);
                break;
            case "greening":
                Intent greeningIntent = new Intent(MainActivity.this, GreeningActivity.class);
                startActivity(greeningIntent);
                break;
            case "Black spot":
                Intent blackSpotIntent = new Intent(MainActivity.this, BlackSpotActivity.class);
                startActivity(blackSpotIntent);
                break;
        }
    }
}