package org.donampa.nbibik.dipl;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.kosalgeek.android.photoutil.CameraPhoto;

import org.donampa.nbibik.dipl.util.Categs;
import org.donampa.nbibik.dipl.util.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MakeApealActivity extends Activity implements Categs.OnFinishLoadCategsListener {
    private final static int CATLIST_ACTIVITY_REQUEST = 2;
    private final static int CAMERA_ACTIVITY_REQUEST = 3;

    // Ожидание завершения ассинхронной операции загрузки категорий.
    private boolean waiting = false;

    private ProgressBar progressView;
    private LinearLayout linearLayout;
    private IconGreedAdapter adapter;
    private Camera camera;
    private CameraPhoto cam;
    Categs itsCategs;

    private int rootCatId;
    private int catId;
    private File photoFile;
    private MediaRecorder mediaRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            itsCategs = MainActivity.itsCategs;

            cam = new CameraPhoto(getApplicationContext());

            setContentView(R.layout.activity_make_apeal);
            List<Bitmap> bmps = new LinkedList<>();
            bmps.add(
                    BitmapFactory.decodeResource(getResources(), R.drawable.cat_icon4)
            );

            adapter = new IconGreedAdapter(this, bmps);
            ActionBar actionBar = getActionBar();
            if (actionBar != null) getActionBar().setDisplayHomeAsUpEnabled(true);
            progressView = findViewById(R.id.loading_progress);
            linearLayout = findViewById(R.id.main_layout);
            GridView gridView = findViewById(R.id.preview_icon_grid);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(this::onGridItemClick);
        } catch (NullPointerException ex) {
            Logger.e(getClass(), ex);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle(R.string.prompt_err)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNegativeButton("Ок", (dialog, which) -> finish())
                    .show();
        }
    }

    public void onButtonClick(View view) {
        int id = view.getId();
        if (id == R.id.select_cat_btn)
            if (!itsCategs.isTaskRunning())
                startCatlistActivity();
            else {
                itsCategs.addListener(this);
                waiting = true;
                swapVisibility();
            }
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        if (camera != null)
            camera.release();
        camera = null;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    public void onPictureTaken(byte[] data, Camera camera) {

        File pictureFile = null;
        try {
            pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (pictureFile == null) {
            Logger.d(getClass(), "Error creating media file, check storage permissions.");
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Logger.d(getClass(), "File not found: " + e.getMessage());
        } catch (IOException e) {
            Logger.d(getClass(), "Error accessing file: " + e.getMessage());
        }
    }

    public void onGridItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == parent.getCount() - 1) {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                try {
                    startActivityForResult(cam.takePhotoIntent(), CAMERA_ACTIVITY_REQUEST);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                Logger.e(getClass(), "No camera feature.");
        }
    }

    private File getOutputMediaFile(int mediaType) throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).toString();
        String imageFileName = timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                new File(Environment.DIRECTORY_PICTURES, "mirgar/")
        );
        String mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void swapVisibility() {
        if (progressView.getVisibility() == View.VISIBLE || linearLayout.getVisibility() == View.GONE) {
            linearLayout.setVisibility(View.GONE);
            progressView.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.VISIBLE);
            progressView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CATLIST_ACTIVITY_REQUEST:
                if (resultCode == RESULT_OK) {
                    int rootCatId = data.getIntExtra(CatListActivity.ROOT_CAT_ID_FIELD, 0);
                    int catId = data.getIntExtra(CatListActivity.CAT_ID_FIELD, 0);
                    String rootCatName = data.getStringExtra(CatListActivity.ROOT_CAT_NAME_FIELD);
                    String catName = data.getStringExtra(CatListActivity.CAT_NAME_FIELD);
                    if ((rootCatId == 0) || (catId == 0) ||
                            (rootCatName == null) || (catName == null))
                        return;
                    this.rootCatId = rootCatId;
                    this.catId = catId;
                    Button btn = findViewById(R.id.select_cat_btn);
                    String captionBtnStart = btn.getText().toString().split(":", 2)[0] + ":\n";
                    btn.setText(String.format("%s%s - %s", captionBtnStart, rootCatName, catName));
                }
                break;
            case CAMERA_ACTIVITY_REQUEST:
                if (resultCode == RESULT_OK) {
                    Bitmap loaded = (Bitmap) data.getExtras().get("data");
                    adapter.add(loaded);
                }
        }
    }

    private void startCatlistActivity() {
        Intent intentCatList = new Intent(this, CatListActivity.class);
        startActivityForResult(intentCatList, CATLIST_ACTIVITY_REQUEST);
    }

    private void runCameraActivity() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_ACTIVITY_REQUEST);
    }

    @Override
    public void onFinishLoadCategs(boolean successfully) {
        if (!waiting)
            return;
        else {
            if (successfully) {
                waiting = false;
                swapVisibility();
                startCatlistActivity();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Внимание!")
                        .setMessage("Возможно, отсутствует подключение интернет! Проверьте ваше подключение к сети.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNegativeButton("Ок", (DialogInterface dialog, int id) -> dialog.cancel())
                        .create()
                        .show();
            }
        }
    }

    private class IconGreedAdapter extends BaseAdapter {
        private List<Bitmap> bmps;
        private GridView.LayoutParams layoutParams = new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        private final Context itsContext;

        IconGreedAdapter(@NonNull Context context, List<Bitmap> bmps) {
            super();

            itsContext = context;
            Logger.i(getClass(), "Size of bitmap list - " + bmps.size());
            this.bmps = bmps;
        }

        public void add(Bitmap newItem) {
            bmps.add(getCount() - 2, newItem);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return bmps.size();
        }

        @Override
        public Object getItem(int position) {
            return bmps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ImageView icon;
            if (convertView == null) {
                icon = new ImageView(itsContext);
                icon.setLayoutParams(layoutParams);
                icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                icon.setPaddingRelative(8, 8, 8, 8);
            } else icon = (ImageView) convertView;

            icon.setImageBitmap(bmps.get(position));
            return icon;
        }
    }
}
