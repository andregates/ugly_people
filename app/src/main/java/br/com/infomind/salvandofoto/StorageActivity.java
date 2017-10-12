package br.com.infomind.salvandofoto;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orm.SugarContext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by André Gomes on 11/10/2017.
 */

public class StorageActivity extends AppCompatActivity {
    private TextView titulo;
    private int tipo;
    private ImageView imgFoto;
    private String FILENAME = "photo_tabajara.jpg";
    private String pictureImagePath = "";
    private Bitmap img;
    private Intent cameraIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.storage_activity);

        this.titulo = (TextView) findViewById(R.id.txtTitulo);

        /* Obtenho o título exibido na activity (Interno, Externo ou BD), bem como
           a opção selecionada e coloco na variavel tipo.  1-interno  2-externo   3-bd */

        Bundle params = getIntent().getExtras();
        this.titulo.setText(params.getString("titulo"));
        this.tipo = params.getInt("tipo");

        imgFoto = (ImageView) findViewById(R.id.imgFoto);

        if(this.tipo==3){
            SugarContext.init(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(this.tipo==3) SugarContext.terminate();
    }

    public void takePhotoAndSave(View v) throws IOException {

        switch(this.tipo) {
            case 1: case 3:
                cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,tipo);
                break;
            case 2:
                if (checkStorage() == false) {
                    return;
                } else {
                    File file = createImageFile();
                    Uri outputFileUri = FileProvider.getUriForFile(StorageActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                    Log.i("Salvando", file.getAbsolutePath());
                    cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    startActivityForResult(cameraIntent, tipo);
                }
                break;
        }
    }

    public void readPhoto(View v) throws IOException {

        switch (this.tipo) {
            case 1:
                FileInputStream fin ;
                try {
                    //abre o arquivo chamado FILENAME para LEITURA
                    fin = openFileInput(FILENAME);

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                    Bitmap imagem = BitmapFactory.decodeStream(fin,null, options);

                    imgFoto.setImageBitmap(imagem);

                    fin.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                if (checkStorage() == false) {
                    Toast.makeText(this, "Storage is busy", Toast.LENGTH_SHORT).show();
                    return;
                } else {

                    File imgFile = createImageFile();

                    Log.i("Imagem", imgFile.getAbsolutePath());
                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        Log.i("Testes", "Arquivo:" + imgFile);
                        this.imgFoto.setImageBitmap(myBitmap);
                    }
                }
                break;
            case 3:
                ImageRecord imr = ImageRecord.last(ImageRecord.class);

                if(imr != null){
                    Bitmap imagem = BitmapFactory.decodeByteArray(imr.getImage(),0, imr.getImage().length);
                    this.imgFoto.setImageBitmap(imagem);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == tipo) {
            String msg="";
            switch (tipo){
                case 1:
                    if(data!=null){
                        Bundle extras = data.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");

                        FileOutputStream fOut = null;
                        try {
                            fOut = openFileOutput(FILENAME, Context.MODE_PRIVATE);;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                        try {
                            fOut.flush();
                            fOut.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    msg = "Imagem salva com sucesso no armazenamento interno.";
                    break;
                case 2:
                    msg = "Imagem salva com sucesso no armazenamento externo.";
                    break;
                case 3:
                    if(data!=null){

                        Bundle extras = data.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] photo = baos.toByteArray();

                        ImageRecord imageBanco = new ImageRecord(photo);

                        imageBanco.save();
                    }
                    msg = "Imagem salva com sucesso no banco de dados.";
                    break;
            }

            if(resultCode == RESULT_OK){
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean checkStorage(){
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            return false;
        }
        return true;
    }


    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        pictureImagePath = storageDir.getAbsolutePath()+"/"+FILENAME;
        File image = new File(pictureImagePath);
        return image;
    }
}
