package top.qvisa.httpdowndemo;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final int GETPictureOK = 0;
    private static final int GETPictureERROR = 1;
    public static final int DownPictureOK = 2;
    public static final int DownPictureError = 3;
    private ImageView mImageView_Url;
    private EditText mEditText_Url;
    private RadioButton mRadioButton_save_in;
    private RadioButton mRadioButton_save_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditText_Url = findViewById(R.id.et_url);
        mImageView_Url = findViewById(R.id.iv_url);
        mRadioButton_save_in = findViewById(R.id.rb_save_in);
        mRadioButton_save_out = findViewById(R.id.rb_save_out);
    }

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case GETPictureOK:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    mImageView_Url.setImageBitmap(bitmap);
                    break;
                case GETPictureERROR:
                    Toast.makeText(MainActivity.this, "访问失败", Toast.LENGTH_SHORT).show();
                    break;
                case DownPictureOK:
                    Toast.makeText(MainActivity.this, "下载成功！", Toast.LENGTH_SHORT).show();
                    break;
                case DownPictureError:
                    Toast.makeText(MainActivity.this, "下载失败！", Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    public void bt_view_picture(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL(mEditText_Url.getText().toString().trim());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(8000);
                    urlConnection.setReadTimeout(8000);
                    int Code = urlConnection.getResponseCode();
                    if (Code == 200) {
                        InputStream inputStream = urlConnection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null) {
                            Message message = new Message();
                            message.what = GETPictureOK;
                            message.obj = bitmap;
                            mHandler.sendMessage(message);
                        } else {
                            Message message_error = new Message();
                            message_error.what = GETPictureERROR;
                            mHandler.sendMessage(message_error);
                        }
                    } else {
                        Message message_error = new Message();
                        message_error.what = GETPictureERROR;
                        mHandler.sendMessage(message_error);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void bt_save_picture(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL(mEditText_Url.getText().toString().trim());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(8000);
                    urlConnection.setReadTimeout(8000);
                    int Code = urlConnection.getResponseCode();
                    if (Code == 200) {
                        InputStream inputStream = urlConnection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        String urlPath = url.getPath();
                        String[] urlPaths = urlPath.split("/");//以'/'分割
                        String filename = urlPaths[urlPaths.length - 1];
                        //存储在内部
                        FileOutputStream fileOutput = null;
                        if (mRadioButton_save_in.isChecked()) {
                            fileOutput = openFileOutput(filename, MODE_PRIVATE);

                        } else if (mRadioButton_save_out.isChecked()) {//存储在外部
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                            String StoragePath = Environment.getExternalStorageDirectory().toString();
                            fileOutput  = new FileOutputStream(StoragePath+"/"+filename);
                        }
                        boolean result = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutput);
                        fileOutput.flush();
                        fileOutput.close();
                        if (result = true) {
                            Message messageDownOK = new Message();
                            messageDownOK.what = DownPictureOK;
                            mHandler.sendMessage(messageDownOK);
                        }
                        else {
                            Message messageDownError = new Message();
                            messageDownError.what = DownPictureError;
                            mHandler.sendMessage(messageDownError);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
