package hack.suggesttag;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PHOTO = 1;
    private ImageView imageView;
    private TextView textView;
    private ArrayList<String> topTags;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);
        textView.setText("");

        findViewById(R.id.btn_pickimage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });
        findViewById(R.id.btn_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, SELECT_PHOTO);
            }
        });


        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            showImageUri(imageUri);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    showImageUri(selectedImage);
                }
        }
    }

    private void showImageUri(Uri selectedImage) {
        mImageUri = selectedImage;
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
        imageView.setImageBitmap(yourSelectedImage);
        textView.setText("Recognizing...");
        topTags=null;
        ImageTags.getTags(getRealPathFromURI(selectedImage), new ImageTags.Callback(){
            @Override
            public void onResult(List<RecognitionResult> results) {
                StringBuilder sb = new StringBuilder();
                sb.append("tags:\n");
                int k=0;
                topTags = new ArrayList<>();
                for(Tag t: results.get(0).getTags()) {
                    sb.append(t.getName()).append(" ");
                    topTags.add(t.getName());
                    if(++k>=3) {
                        break;
                    }
                }
                textView.setText(sb.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            if (topTags != null) {
                Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
                i.putExtra(Intent.EXTRA_STREAM, mImageUri);
                i.putExtra(Intent.EXTRA_TEXT, buildHashTags(topTags));
                i.setType("*/*");
                startActivity(i);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String buildHashTags(ArrayList<String> topTags) {
        StringBuilder sb = new StringBuilder();
        for(String s: topTags) {
            sb.append("#").append(s.replace(" ", "")).append(" ");
        }
        return sb.toString();
    }


    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
