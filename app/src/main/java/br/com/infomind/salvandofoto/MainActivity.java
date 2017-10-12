package br.com.infomind.salvandofoto;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Context c =this;
    private ScrollView srcoll;
    private ImageView im;
    private String titulo;
    private int tipo;

    // StorageActivity Permissions
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        srcoll = (ScrollView) findViewById(R.id.scrollV);

        im = (ImageView) findViewById(R.id.externo);
    }

    public void onClick(View v){
        vibrar();

        ImageView img = (ImageView)v;
        switch(v.getId()){

            case R.id.interno:
                titulo = "Armazenamento Interno";
                this.tipo=1;
                break;

            case R.id.externo:
                titulo = "Armazenamento Externo";
                this.tipo=2;
                break;
            case R.id.bd:
                titulo = "Armazenamento em BD";
                this.tipo=3;
                break;
            case R.id.cloud:
                Toast.makeText(this, "Em desenvolvimento...", Toast.LENGTH_SHORT).show();
                return;
        }

        Bundle b = new Bundle();
        b.putString("titulo", this.titulo);
        b.putInt("tipo", this.tipo);
        Intent i = new Intent(MainActivity.this, StorageActivity.class);
        i.putExtras(b);
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionUtils.validate(MainActivity.this,0, PERMISSIONS_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                // Alguma permissão foi negada, agora é com você :-)
                im.setEnabled(false);
                alertAndFinish();
                return;
            }
        }
        im.setEnabled(true);
    }

    private void alertAndFinish() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name).setMessage("Para utilizar todas as funções desse aplicativo, você precisa aceitar o acesso ao armazenamento externo.");
        // Add the buttons
        builder.setNegativeButton("Fechar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //faz nada...
            }
        });
        builder.setPositiveButton("Permitir", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void vibrar()
    {
        Vibrator rr = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long milliseconds = 30;//'30' é o tempo em milissegundos, é basicamente o tempo de duração da vibração. portanto, quanto maior este numero, mais tempo de vibração você irá ter
        rr.vibrate(milliseconds);
    }
}
