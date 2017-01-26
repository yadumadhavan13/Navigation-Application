package ashishrpa.navig;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class ShowActivity extends AppCompatActivity {
    File mFile;// = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/temproute.png");
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        imageView = (ImageView)findViewById(R.id.imageview_show);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MapsActivity.EXTRA_MESSAGE);
        showImageFromSD(message);
    }

    private void showImageFromSD(String imageUrl) {
        mFile = new File(imageUrl);
        if(mFile.exists()){
            imageView.setImageBitmap(BitmapFactory.decodeFile(mFile.getAbsolutePath()));
        }else {
            Log.e("showImg ","FILE NOT EXIST");
            Toast.makeText(getApplicationContext(),"File Not Exist",Toast.LENGTH_LONG).show();
        }

    }
}
