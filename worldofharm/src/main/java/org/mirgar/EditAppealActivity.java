package org.mirgar;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.AppCompatImageView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualLinkedHashBidiMap;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.TestOnly;
import org.json.JSONException;
import org.json.JSONObject;
import org.mirgar.model.Appeal;
import org.mirgar.model.Photo;
import org.mirgar.util.BitmapResizer;
import org.mirgar.util.Cats;
import org.mirgar.util.Logger;
import org.mirgar.util.PrefManager;
import org.mirgar.util.db.DataContext;
import org.mirgar.util.db.DbProvider;
import org.mirgar.util.exceptions.NoCameraException;
import org.mirgar.util.exceptions.NoLocationException;
import org.mirgar.util.tasks.GetAddressesTask;
import org.mirgar.util.tasks.SendAppealTask;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class EditAppealActivity extends GeneralActivity implements Cats.OnFinishLoadCategsListener, AdapterView.OnItemClickListener {
    private final static int CATLIST_ACTIVITY_REQUEST = 2;
    private final static int CAMERA_ACTIVITY_REQUEST = 3;
    //private static final String LOCAL_ID_FIELD = "local id";
    private static final String APPEAL_ID_FIELD = "appeal_id";
    private static final String NATIVE_SYSTEM_LNG_ADDR_MAP_KEYS_FIELD = "NSLAM keys";
    private static final String NATIVE_SYSTEM_LNG_ADDR_MAP_VALS_FIELD = "NSLAM vals";

    // Ожидание завершения ассинхронной операции загрузки категорий.
    private boolean waiting = false;

    private ProgressBar progressView;
    private EditText titleEdtTxt;
    BitmapResizer.Rect greedItemParams;
    private GridLayout mainLayout;
    private Spinner addressSpinner;
    private IconGreedAdapter adapter;
    private Cats itsCats;

    private Appeal itsAppeal;
    private boolean isAppealChanged = false;

    private File photoFile;
    private File tempPhoto;
    private BidiMap<String, String> nativeSystemLngAddrMap;
    private EditText descEdtTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            itsCats = Cats.getInstance();

            if(nativeSystemLngAddrMap == null)nativeSystemLngAddrMap = new DualLinkedHashBidiMap<>();

            if (savedInstanceState == null) itsAppeal = new Appeal();

            setContentView(R.layout.activity_edit_appeal);
            greedItemParams = calcGreedItemParams();
            List<Bitmap> bmps = new LinkedList<>();
            Bitmap bitmap = BitmapResizer.makeBitmapFromDrawable(getResources(), R.drawable.ic_add_photo_24dp, getTheme());
            if (bitmap != null) bmps.add(bitmap);
            else throw new NullPointerException("Unable to decode resource to bitmap.");

            adapter = new IconGreedAdapter(this, bmps);
            ActionBar actionBar = getActionBar();
            if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

            progressView = findViewById(R.id.loading_progress);
            mainLayout = findViewById(R.id.main_layout);

            addressSpinner = findViewById(R.id.address_spinner);
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line);
            addressSpinner.setAdapter(spinnerAdapter);
            addressSpinner.setVisibility(View.GONE);


            addressSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                 @Override
                 public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                     if (view instanceof TextView) {
                         TextView textView = ((TextView) view);
                         itsAppeal.address = nativeSystemLngAddrMap.get(textView.getText().toString());
                     }
                 }

                 @Override
                 public void onNothingSelected(AdapterView<?> parent) {}
             });

            TextWatcher watcher = new EditWatcher();

            titleEdtTxt = findViewById(R.id.title_text_edit);
            descEdtTxt = findViewById(R.id.description);
            titleEdtTxt.addTextChangedListener(watcher);
            descEdtTxt.addTextChangedListener(watcher);

            GridView gridView = findViewById(R.id.preview_icon_grid);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(this);
        } catch (NullPointerException ex) {
            Logger.e(ex);
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
        switch (id) {
            case R.id.set_cat_btn:
                if (!itsCats.isTaskRunning())
                    startCatlistActivity();
                else {
                    itsCats.addListener(this);
                    waiting = true;
                    swapVisibility(mainLayout, progressView);
                }
                break;
            case R.id.submit:
                EditText headerTextEdit = findViewById(R.id.title_text_edit);
                Button setCatBtn = findViewById(R.id.set_cat_btn);
                String header = headerTextEdit.getText().toString();
                boolean ok = true;
                if (header.length() < 1) {
                    headerTextEdit.setError("Поле заголовка не может быть пустым.");
                    ok = false;
                } else if (header.length() < 5) {
                    headerTextEdit.setError("Заголовок обращения слишком короткий.");
                    ok = false;
                }

                if (itsAppeal.category == null) {
                    setCatBtn.setError("Пожалуйста, выберите категорию.");
                    ok = false;
                }
                if (ok) {
                    itsAppeal.desc = ((EditText)findViewById(R.id.description)).getText().toString();
                    itsAppeal.title = header;
                    int userId = PrefManager.getInstance().Get(PrefManager.Prefs.USER_ID, 0);
                    double latitude = 0;
                    double longitude = 0;

                    List<Photo> photos = itsAppeal.photos();

                    for(Photo photo: photos) {
                        Location loc = photo.getLocation();
                        latitude += loc.getLatitude();
                        longitude += loc.getLongitude();
                    }

                    latitude /= photos.size() * 1.0;
                    longitude /= photos.size() * 1.0;

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject
                                .put("user_id", userId)
                                .put("id_cat", itsAppeal.category.globalId)
                                .put("description", itsAppeal.desc)
                                .put("title", itsAppeal.title)
                                .put("address", itsAppeal.address)
                                .put("latitude", latitude)
                                .put("longitude", longitude);
//                                .put("filename1", "")
//                                .put("filename2", "")
//                                .put("filename3", "");
                        Set<String> output = new LinkedHashSet<>();
                        output.add(jsonObject.toString());
                        for(Photo photo: photos)
                            output.add(photo.getFile().getCanonicalPath());
                        new SendAppealTask(getApplicationContext()).execute(output.toArray(new String [0]));
                    } catch (JSONException|IOException ignored) {}
                }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, @SuppressWarnings("unused") View view, int position, @SuppressWarnings("unused") long id) {
        if (position == parent.getCount() - 1) {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                try {
                    if(adapter.getCount() > 3)
                        return;
                    swapVisibility(mainLayout, progressView);

                    if(!checkLocationWorks()) {
                        swapVisibility(mainLayout, progressView);
                        return;
                    }

                    startCameraActivity();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoCameraException e) {
                    e.printStackTrace();
                    showMessage("В вашем устойстве нет приложения Камера.");
                    swapVisibility(mainLayout, progressView);
                }
            } else
                showMessage("В вашем устойстве нет приложения Камера.");
            Logger.e("No camera feature.");
        } else {
            showPhotoDialog(position);
        }
    }

    private void showPhotoDialog(int position) {
        CopyOnWriteArrayList<Bitmap> pics = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Photo> photos = new CopyOnWriteArrayList<>(itsAppeal.photos());
        View popupView = getLayoutInflater().inflate(R.layout.activity_edit_appeal_popup, null);

        AppCompatImageView pictureView = new AppCompatImageView(this);
        ImageButton deleteBtn = popupView.findViewById(R.id.b_delete);
        ImageButton cropBtn = popupView.findViewById(R.id.b_crop);
        String picturePath = "";
        try {
            picturePath = photos.get(position).getFile().getCanonicalPath();
        } catch (IOException ignored) {}
        Bitmap pictureBitmap = BitmapResizer.decodeFile(picturePath);
        pics.add(position, pictureBitmap);

        deleteBtn.setOnClickListener(
                (View view1)-> {
                    AtomicBoolean cancel = new AtomicBoolean();

                    new AlertDialog.Builder(this)
                            .setOnCancelListener(dialog -> cancel.set(true))
                            .setTitle("Удаление")
                            .setMessage("Данное действие нельзя будет отменить.")
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                cancel.set(false);
                                dialog.cancel();
                            })
                            .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                                cancel.set(true);
                                dialog.cancel();
                            })
                            .show();

                    try {
                        cancel.wait();
                    } catch (InterruptedException e) {
                        return;
                    }

                    if (!cancel.get()) {
                        // ToDo: place deleting code here
                    }
                });

        pictureView.setImageBitmap(pictureBitmap);

        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
        popupBuilder
                .setTitle("Фотография")
                .setCancelable(true)
                .setOnCancelListener(dialog -> pics.clear())
                .setPositiveButton(android.R.string.cancel, (dialog, which) -> {
                    pics.clear();
                    dialog.cancel();
                })
                .setView(popupView)
                .show();
    }

    @Override
    @TestOnly
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CATLIST_ACTIVITY_REQUEST:
                if (resultCode == RESULT_OK) {
                    //itsAppeal.rootCatId = data.getIntExtra(CatListActivity.ROOT_CAT_ID_FIELD, 0);
                    long catId = data.getLongExtra(CatListActivity.CAT_ID_FIELD, 0);
                    if (catId == 0){
                        Logger.wtf("Unable to get cat id");
                        return;
                    }

                    itsAppeal.category = DataContext.getCategory(catId);
                    itsAppeal.save();

                    String catName = itsAppeal.category.name;
                    String rootCatName = itsAppeal.category.parent.name;

                    Button btn = findViewById(R.id.set_cat_btn);
                    String captionBtnStart = btn.getText().toString().split(":", 2)[0] + ":\n";
                    btn.setText(String.format("%s%s >> %s", captionBtnStart, rootCatName, catName));
                }
                break;
            case CAMERA_ACTIVITY_REQUEST:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap loaded;
                        if(data == null) {
                            Location loc = null;
                            final short maxAttempts = 32;
                            short counter = 0;
                            while(loc == null && counter++ < maxAttempts) {
                                loc = getLocation();
                            }
                            if(loc == null) throw new NoLocationException();

                            itsAppeal.save();

                            Photo photo = new Photo();
                            photo.appeal = itsAppeal;
                            photo.setLocation(loc);
                            photo.setFile(photoFile);
                            photo.save();

                            itsAppeal = DataContext.getAppeal(itsAppeal.getId());

                            double avgLat = 0;
                            double avgLnt = 0;
                            List<Photo> photos = itsAppeal.photos();
                            if (photos.size() > 1) {
                                for (Photo _photo : photos) {
                                    loc = _photo.getLocation();
                                    avgLat += loc.getLatitude();
                                    avgLnt += loc.getLongitude();
                                }
                                avgLat = avgLat / photos.size();
                                avgLnt = avgLnt / photos.size();
                            } else {
                                avgLat = loc.getLatitude();
                                avgLnt = loc.getLongitude();
                            }


                            try {
                                if (!(addressSpinner.getAdapter() instanceof ArrayAdapter<?>))
                                    addressSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line));

                                new GetAddressesTask((ArrayAdapter) addressSpinner.getAdapter(),
                                        nativeSystemLngAddrMap,
                                        () -> addressSpinner.setVisibility(View.VISIBLE))
                                        .execute(avgLat, avgLnt);
                            } catch (ClassCastException ignored) {
                            }

                            isAppealChanged = true;

                            FileUtils.copyFile(tempPhoto, photoFile);
                            if (!photoFile.exists()) throw new IOException("New file in internal dir does not exists!");
                            if (!FileUtils.contentEquals(photoFile, tempPhoto)) throw new IOException("Content of files does not equals!");

                            File min = new File(getCacheDir(), "min_" + photoFile.getName());
                            loaded = BitmapResizer.loadMinBitmap(photoFile, min, adapter.itemParams);
                            adapter.add(loaded);
                        }
                        else {
                            loaded = data.getParcelableExtra("data");
                            if (loaded == null)
                                Logger.e("Bitmap in extra data does not exist!");
                            adapter.add(loaded);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        photoFile.delete();
                    } catch (NoLocationException e) {
                        new AlertDialog.Builder(this)
                                .setTitle("Ошибка")
                                .setMessage("Геолокация не доступна!")
                                .setCancelable(false)
                                .setNegativeButton(android.R.string.ok, (dialog, which) -> dialog.cancel())
                                .show();
                        e.printStackTrace();
                        photoFile.delete();
                    } finally {
                        tempPhoto.delete();
                    }
                }
                swapVisibility(mainLayout, progressView);
        }
    }

    private void startCatlistActivity() {
        Intent intentCatList = new Intent(this, CatListActivity.class);
        startActivityForResult(intentCatList, CATLIST_ACTIVITY_REQUEST);
    }

    private void startCameraActivity() throws IOException, NoCameraException {
        final int currentAPIVersion = android.os.Build.VERSION.SDK_INT;
        final String deviceManufacturer = android.os.Build.MANUFACTURER;

        Intent cameraIntent;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M && deviceManufacturer.compareTo("samsung") != 0)
            cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        else
            cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(cameraIntent.resolveActivity(getPackageManager()) != null) {
            makePhotoFiles();

            Uri photoTmpPath;
            if (currentAPIVersion >= 24)
                photoTmpPath = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".file_provider", tempPhoto);
            else
                photoTmpPath = Uri.fromFile(tempPhoto);

            cameraIntent.putExtra("output", photoTmpPath);
            startActivityForResult(cameraIntent, CAMERA_ACTIVITY_REQUEST);
        } else throw new NoCameraException();
    }

    private void makePhotoFiles() throws IOException {
        Date date = new Date();
        String imageFileName = "mirgar_" + date.getTime() + PrefManager.getInstance().Get(PrefManager.Prefs.USER_ID, 0);
        photoFile = new File(getApplicationContext().getFilesDir(), imageFileName);
        if(!photoFile.createNewFile()) throw new IOException("Unsuccessful creating of file.");
        File tempDir = new File(Environment.getExternalStorageDirectory(), "cache");
        if(!tempDir.exists() || !tempDir.isDirectory())
            if(!tempDir.mkdir()) throw new IOException("Unable to create cache dir " + tempDir.getAbsolutePath() + ".");
        tempPhoto = File.createTempFile("tmp_", "", tempDir);
    }

    @NonNull
    private BitmapResizer.Rect calcGreedItemParams() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float size = Math.max(metrics.heightPixels, metrics.widthPixels);
        size *= 0.9;
        size /= 3;
        return new BitmapResizer.Rect(size, size);
    }

    @Override
    public void onFinishLoadCategs(boolean successfully) {
        if (!waiting)
            return;
        else {
            if (successfully) {
                waiting = false;
                swapVisibility(mainLayout, progressView);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(nativeSystemLngAddrMap != null) {
            Logger.v(String.format(Locale.getDefault(), "method \"onSaveInstanceState\" - nativeSystemLngAddrMap.size(): %d", nativeSystemLngAddrMap.size()));
            outState.putStringArray(NATIVE_SYSTEM_LNG_ADDR_MAP_KEYS_FIELD, nativeSystemLngAddrMap.keySet().toArray(new String[0]));
            outState.putStringArray(NATIVE_SYSTEM_LNG_ADDR_MAP_VALS_FIELD, nativeSystemLngAddrMap.values().toArray(new String[0]));
        }

        itsAppeal.save();

        outState.putLong(APPEAL_ID_FIELD, itsAppeal.getId());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {

            String[] keyAddrArray = savedInstanceState.getStringArray(NATIVE_SYSTEM_LNG_ADDR_MAP_KEYS_FIELD);
            String[] valAddrArray = savedInstanceState.getStringArray(NATIVE_SYSTEM_LNG_ADDR_MAP_KEYS_FIELD);
            if (keyAddrArray != null && valAddrArray != null) {
                int mapSize = Math.max(keyAddrArray.length, valAddrArray.length);

                for (int i = 0; i < mapSize; i++) {
                    if (keyAddrArray[i] != null || valAddrArray[i] != null) {
                        if (keyAddrArray[i] != null && valAddrArray[i] != null)
                            nativeSystemLngAddrMap.put(keyAddrArray[i], valAddrArray[i]);
                        else if (valAddrArray[i] == null)
                            nativeSystemLngAddrMap.put(keyAddrArray[i], keyAddrArray[i]);
                        else
                            nativeSystemLngAddrMap.put(valAddrArray[i], valAddrArray[i]);
                    }
                }

                Logger.v(String.format(Locale.getDefault(), "nativeSystemLngAddrMap.size(): %d", nativeSystemLngAddrMap.size()));
            }
            long appealId = savedInstanceState.getLong(APPEAL_ID_FIELD, -1);
            if (appealId != -1) {
                itsAppeal = DataContext.getAppeal(appealId);
            }
        } else if (itsAppeal == null) itsAppeal = new Appeal();
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            offerSaveAsDraft(this::finish);
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK
//                && event.isTracking() && !event.isCanceled()
//                && event.getEventTime() - lastKey <= 3000) {
//                offerSaveAsDraft(this::finishAffinity);
//                return true;
//            }
//        return super.onKeyUp(keyCode, event);
//    }
//
//
//    private void offerSaveAsDraft(Runnable postRunnable) {
//        if (itsAppeal.photos.size() > 0 || itsAppeal.catId != 0
//                || !itsAppeal.title.isEmpty() || !itsAppeal.descr.isEmpty()
//                || isAppealChanged) {
//            Logger.v("onDestroy()");
//
//            Runnable saveAppeal = () -> {
//                DbProvider dbProvider = DbProvider.getInstance();
//
//                if (dbProvider == null) {
//                    DbProvider.init(this);
//                    dbProvider = DbProvider.getInstance();
//                }
//
//                if (itsAppeal.localId == 0)
//                    dbProvider.insertAppeal(itsAppeal, true);
//                else dbProvider.updateAppeal(itsAppeal, true);
//
//                isAppealChanged = false;
//            };
//
//            new AlertDialog.Builder(this)
//                    .setTitle("Внимание!")
//                    .setMessage("Ваше обращение не сохранено. Сохранить в черновик?")
//                    .setPositiveButton(android.R.string.yes,
//                                       (dialog, which) -> {
//                                            saveAppeal.run();
//                                            postRunnable.run();
//                                       })
//                    .setNegativeButton(android.R.string.no,
//                                       (dialog, which) -> {
//                                            postRunnable.run();
//                                        })
//                    .setCancelable(false)
//                    .show();
//        } else postRunnable.run();
//    }

    class EditWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.hashCode() == titleEdtTxt.getText().hashCode()) {
                itsAppeal.title = s.toString();
                itsAppeal.save();
            }
            if (s.hashCode() == descEdtTxt.getText().hashCode()) {
                itsAppeal.desc = s.toString();
                itsAppeal.save();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    class IconGreedAdapter extends BaseAdapter {
        private List<Bitmap> bmps;
        private GridView.LayoutParams layoutParams = new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        private final Context itsContext;

        private BitmapResizer.Rect itemParams;

        IconGreedAdapter(@NonNull Context context, List<Bitmap> bmps) {
            super();

            itsContext = context;
            itemParams = new BitmapResizer.Rect();
            this.bmps = bmps;
        }

        public int add(Bitmap newItem) {
            int position = getCount() - 1;
            bmps.add(getCount() - 1, newItem);
            notifyDataSetChanged();
            return position;
        }

        @Override
        public int getCount() { return bmps.size(); }

        @Override
        public Object getItem(int position) { return bmps.get(position); }

        @Override
        public long getItemId(int position) { return 0; }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ImageView icon;
            if (convertView == null) {
                icon = new ImageView(itsContext);
                icon.setLayoutParams(layoutParams);
                icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                icon.setPaddingRelative(8, 8, 8, 8);
            } else icon = (ImageView) convertView;

            icon.setImageBitmap(bmps.get(position));

            itemParams.set(icon.getMaxWidth(), icon.getMaxHeight());

            return icon;
        }
    }
}
