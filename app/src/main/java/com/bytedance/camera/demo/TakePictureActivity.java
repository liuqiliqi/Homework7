package com.bytedance.camera.demo;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bytedance.camera.demo.utils.Utils;

import java.io.File;
import java.security.Permission;
import java.security.Permissions;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_IMAGE;

public class TakePictureActivity extends AppCompatActivity {

    private ImageView imageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 101;
    private File imageFile;
    String[] permissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);
        imageView = findViewById(R.id.img);
        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            if (Utils.isPermissionsReady(TakePictureActivity.this,permissions)) {

                Log.i("Permission", "hello");
                try{
                    takePicture();
                }catch (SecurityException e){
                    Utils.reuqestPermissions(TakePictureActivity.this,permissions,REQUEST_EXTERNAL_STORAGE);
                }

            } else {
                //todo 在这里申请相机、存储的权限

                Utils.reuqestPermissions(TakePictureActivity.this,permissions,REQUEST_EXTERNAL_STORAGE);
            }
        });

    }

    private void takePicture() {
        //todo 打开相机
        Intent takePictureIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFile = Utils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
        Log.i("sssssss","123");
        if(imageFile!=null) {
            Uri fileUri = FileProvider.getUriForFile(this,"com.bytedance.camera.demo",imageFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
            startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
        //todo 处理返回数据
            //todo 根据imageView裁剪


            //todo 根据缩放比例读取文件，生成Bitmap
            int targetW =imageView.getWidth();
            int targetH =imageView.getHeight();

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageFile.getAbsolutePath(),bmOptions);

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scalefactor = Math.min(photoW/targetW,photoH/targetH);

            bmOptions.inJustDecodeBounds=false;
            bmOptions.inSampleSize = scalefactor;
            bmOptions.inPurgeable=true;

            Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(),bmOptions);
            Log.i("pppp","aaa"+data);

            try{
                rotateImage(imageBitmap,imageFile.getAbsolutePath());
            }catch (Exception e){

            }
            imageView.setImageBitmap(imageBitmap);

        }
    }

    public static Bitmap rotateImage(Bitmap bitmap,String path)throws Exception{
        ExifInterface srcExif = new ExifInterface(path);
        Matrix matrix = new Matrix();
        int angle = 0;
        int orientation = srcExif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
        switch (orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                angle = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                angle= 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                angle = 270;
                break;
            default:
                break;


        }
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }


    private void setPic() {

        //todo 如果存在预览方向改变，进行图片旋转
        //todo 显示图片
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                //todo 判断权限是否已经授予
                if (Utils.isPermissionsReady(TakePictureActivity.this,permissions)){
                    takePicture();
                }
                break;
            }
        }
    }
}
