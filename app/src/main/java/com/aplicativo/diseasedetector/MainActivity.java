package com.aplicativo.diseasedetector;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import com.aplicativo.diseasedetector.ml.GaussianGrayscaleModel;
import com.aplicativo.diseasedetector.ml.GaussianModelRotate;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.File;
import java.io.FileOutputStream;
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
        // redimensionar a imagem capturada para as dimensões esperadas pelo modelo
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmapImg, 224, 224, false);

        // criar imagem mat com o bitmap
        Mat originalMatImage = new Mat();
        Utils.bitmapToMat(resizedBitmap, originalMatImage);

        // aplicar o blur na imagem em cores
        Mat bluredMatImage = new Mat(originalMatImage.rows(), originalMatImage.cols(), originalMatImage.type());
        Imgproc.GaussianBlur(originalMatImage, bluredMatImage, new Size(3, 3), 0);

        // voltar de Mat pra bitmap para seguir com a análise
        Bitmap finalBitmap = Bitmap.createBitmap(originalMatImage.cols(), originalMatImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(bluredMatImage, finalBitmap);

        // salvar a imagem que será utilizada pelo modelo
        this.saveImage(finalBitmap);

        // executar o modelo
        Category result = this.executeModelInference(finalBitmap);

        // se o resultado com a imagem colorida for menor que 60% tenta converter para cinza para avaliar
        if (result.getScore() < 0.6) {
            // converter a imagem já com blur para tons de cinza
            Mat bluredGrayMatImage = new Mat(originalMatImage.rows(), originalMatImage.cols(), originalMatImage.type());
            Imgproc.cvtColor(bluredMatImage, bluredGrayMatImage, Imgproc.COLOR_RGB2GRAY);

            // voltar de Mat pra bitmap para seguir com a análise
            Bitmap finalGrayBitmap = Bitmap.createBitmap(originalMatImage.cols(), originalMatImage.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(bluredGrayMatImage, finalGrayBitmap);

            // salvar a imagem que será utilizada pelo modelo
            this.saveImage(finalGrayBitmap);

            // executar o modelo treinado em tons de cinza
            Category resultGray = this.executeGrayModelInference(finalGrayBitmap);

            // se não atingir 60% de pontuação com nenhum modelo, mostra a mensagem de erro
            if (resultGray.getScore() < 0.6) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("Ops!");
                dialog.setMessage("Não foi possível chegar a uma conclusão com a imagem informada. Isso pode acontecer quando a foto não aprensenta sintomas ou os sintomas apresentados não são correspondentes as doenças abordadas nesse APP. Por favor, tente com outra imagem.");
                dialog.show();
            } else {
                this.redirectToResultScreen(resultGray, "cinza");
            }
        } else {
            this.redirectToResultScreen(result, "colorida");
        }
    }

    protected Category executeModelInference(Bitmap bitmapImage) {
        Category first = null;

        // usar o modelo pré-treinado para analizar a imagem selecionada
        try {
            // executar o modelo treinado com as imagens coloridas
            GaussianModelRotate model = GaussianModelRotate.newInstance(getApplicationContext());

            // Converte a imagem selecionada de bitmap para tensor
            TensorImage image = TensorImage.fromBitmap(bitmapImage);

            // Roda a inferência do modelo e pega os resultados
            GaussianModelRotate.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();

            // encerra o uso do modelo
            model.close();

            // ordenar os resultados do modelo para que o que tem maior probabilidade ficar em primeiro
            probability.sort((a,b) -> {
                return a.getScore() < b.getScore() ? 1: -1;
            });

            first = probability.get(0);

        } catch (IOException e) {
        }

        return first;
    }

    protected Category executeGrayModelInference(Bitmap grayBitmapImage) {
        // usar o modelo pré-treinado para analizar a imagem selecionada
        Category firstGray = null;

        try {
            // executar o modelo treinado em escala de cinza
            GaussianGrayscaleModel modelGray = GaussianGrayscaleModel.newInstance(getApplicationContext());

            // converte a imagem selecionada em tensor
            TensorImage imageGray = TensorImage.fromBitmap(grayBitmapImage);

            // executa o modelo
            GaussianGrayscaleModel.Outputs outputsGray = modelGray.process(imageGray);
            List<Category> probabilityGray = outputsGray.getProbabilityAsCategoryList();

            // encerra o uso do modelo
            modelGray.close();

            // ordenar os resultados do modelo para que o que tem maior probabilidade ficar em primeiro
            probabilityGray.sort((a,b) -> {
                return a.getScore() < b.getScore() ? 1: -1;
            });

            firstGray = probabilityGray.get(0);
        } catch (IOException e) {
        }

        return firstGray;
    }

    private void redirectToResultScreen(Category result, String modelo) {
        String label = result.getLabel();
        Float score = (result.getScore()) * 100;

        // redireciona para a tela de resultado correspondente a label inferida
        switch (label) {
            case "healthy":
                Intent healtyIntent = new Intent(MainActivity.this, HealtyActivity.class);
                healtyIntent.putExtra("score", score);
                healtyIntent.putExtra("modelo", modelo);
                startActivity(healtyIntent);
                break;
            case "canker":
                Intent cankerIntent = new Intent(MainActivity.this, CankerActivity.class);
                cankerIntent.putExtra("score", score);
                cankerIntent.putExtra("modelo", modelo);
                startActivity(cankerIntent);
                break;
            case "greening":
                Intent greeningIntent = new Intent(MainActivity.this, GreeningActivity.class);
                greeningIntent.putExtra("score", score);
                greeningIntent.putExtra("modelo", modelo);
                startActivity(greeningIntent);
                break;
            case "Black spot":
                Intent blackSpotIntent = new Intent(MainActivity.this, BlackSpotActivity.class);
                blackSpotIntent.putExtra("score", score);
                blackSpotIntent.putExtra("modelo", modelo);
                startActivity(blackSpotIntent);
                break;
        }
    }

    private void saveImage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // Create imageDir
        File mypath=new File(directory,"image.png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //return directory.getAbsolutePath();
    }
}