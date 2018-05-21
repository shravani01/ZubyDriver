package com.zuby.zubydriverdemo.view.DocumentUpload.View;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zuby.zubydriverdemo.view.DocumentUpload.Presenter.DocumentUploadPresenter;
import com.zuby.zubydriverdemo.Presenter.interfaces.ResultInterface;
import com.zuby.zubydriverdemo.R;
import com.zuby.zubydriverdemo.Utils.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import static com.zuby.zubydriverdemo.Utils.Utilities.encodeTobase64;

/**
 * Created by citymapper-pc5 on 20/5/18.
 */

public class DocumentPreviewActivity extends Activity
{
    private Button mRetry,mSave_image,mTake_photo;
    private TextView mInstructions,document_name;
    private static final int CAMERA_REQUEST = 1;
    private static final int PICK_FROM_GALLERY = 2;
    private Bitmap mBitmap;
    private ImageView mImgPreview;
    private LinearLayout mTwo_buttons;
    private PreferenceManager mPreferencemanager;
    private HashMap<String,String>map;
    private String mDriverid,mTokenid,mDocumentid,mDocument_name;
    private Bundle mBundle;


    @Override
    public void onCreate( Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_preview);
        mRetry=findViewById(R.id.retry);
        mSave_image=findViewById(R.id.save_image);
        mTake_photo=findViewById(R.id.take_photo);
        mInstructions=findViewById(R.id.instructions);
        mImgPreview=findViewById(R.id.imgPreview);
        mTwo_buttons=findViewById(R.id.two_buttons);
        document_name=findViewById(R.id.document_name);

        mPreferencemanager =  new PreferenceManager(DocumentPreviewActivity.this);
        map=mPreferencemanager.getLoginDetails();
        mDriverid=map.get("user_id");

        mBundle=getIntent().getExtras();

        if(mBundle!=null)
        {
            mTokenid=mBundle.getString("tokenid");
            mDocumentid=mBundle.getString("document_id");
            mDocument_name=mBundle.getString("documnet_name");

//            document_name.setText(mDocument_name);

            Log.e("Em","tokenid in preview"+" "+mTokenid);
        }


        mTake_photo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                captureImage();
            }
        });


        mSave_image.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                new DocumentUploadPresenter().show(new ResultInterface()
                {
                    @Override
                    public void onSuccess(String object)
                    {

                    }

                    @Override
                    public void onSuccess(Object object)
                    {

                        Intent intent = new Intent(DocumentPreviewActivity.this,DocumentUploadActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("tokenid",mTokenid);
                        bundle.putString("document_id",mDocumentid);
                        bundle.putString("document_name",mDocument_name);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        Toast.makeText(DocumentPreviewActivity.this,"success",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailed(Object string)
                    {
                        Intent intent = new Intent(DocumentPreviewActivity.this,DocumentUploadActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("tokenid",mTokenid);
                        bundle.putString("document_id",mDocumentid);
                        bundle.putString("document_name",mDocument_name);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        Toast.makeText(DocumentPreviewActivity.this,"failure",Toast.LENGTH_LONG).show();
                    }
                },DocumentPreviewActivity.this,mDriverid,"123","2019-06-19",encodeTobase64(mBitmap),mTokenid);
            }
        });



    }


    private void captureImage()
    {
        final String[] option = new String[] { "Take from Camera",
                "Select from Gallery" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_item, option);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Option");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Log.e("Selected Item", String.valueOf(which));
                if (which == 0) {
                    callCamera();
                }
                if (which == 1) {
                    callGallery();
                }

            }
        });
        final AlertDialog dialog = builder.create();

        dialog.show();

    }


    public void callCamera()
    {
        Intent cameraIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra("crop", "true");
        cameraIntent.putExtra("aspectX", 0);
        cameraIntent.putExtra("aspectY", 0);
        cameraIntent.putExtra("outputX", 200);
        cameraIntent.putExtra("outputY", 150);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);

    }

    public void callGallery()
    {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, PICK_FROM_GALLERY);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode)
        {
            case CAMERA_REQUEST:

                Bundle extras = data.getExtras();

                if (extras != null)
                {

                    Bitmap tobecompressedbitmap = extras.getParcelable("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    tobecompressedbitmap.compress(Bitmap.CompressFormat.JPEG,50,stream);
                    byte[] byteArray = stream.toByteArray();
                    mBitmap  = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);


                    mImgPreview.setImageBitmap(mBitmap);

                    Log.e("Em","::::bitmap::::"+" "+mBitmap);

                    mInstructions.setVisibility(View.GONE);
                    mTake_photo.setVisibility(View.GONE);
                    mTwo_buttons.setVisibility(View.VISIBLE);


                }
                break;
            case PICK_FROM_GALLERY:



                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                Cursor cursor = this.getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                Log.e("Em","picturePath"+" "+picturePath);

                Bitmap compressedBitmap = BitmapFactory.decodeFile(picturePath);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG,50,stream);
                byte[] byteArray = stream.toByteArray();
                mBitmap  = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);

                mImgPreview.setImageBitmap(mBitmap);

                mInstructions.setVisibility(View.GONE);
                mTake_photo.setVisibility(View.GONE);
                mTwo_buttons.setVisibility(View.VISIBLE);



                break;
        }
    }





}
