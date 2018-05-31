package org.donampa.nbibik.dipl;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.ArrayMap;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.donampa.nbibik.dipl.util.Categs;
import org.donampa.nbibik.dipl.util.LocationListener;
import org.donampa.nbibik.dipl.util.Logger;
import org.donampa.nbibik.dipl.util.PrefManager;
import org.donampa.nbibik.dipl.util.StringTable;
import org.jetbrains.annotations.Contract;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, UserLoginTask.OnLoginTaskFinal {

    private final int LOGIN_ACTIVITY_REQUEST = 1;
    private final int CATLIST_ACTIVITY_REQUEST = 2;
    private final int MAKE_APEAL_ACTIVITY_REQUEST = 3;
    private final int FINE_LOCATION_PERMISSION_REQUEST = 4;
    private boolean isPermissionFineLocation = false;
    private boolean firstTime = true;
    TextView tv;
    PrefManager itsPrefManager;
    static Categs itsCategs;

    private static class actionNames {
        static final String actMake = "Подать обращение";

        static final String actShow = "Мои обращения";

        @NonNull
        @Contract(pure = true)
        static String[] getStrings() {
            return new String[]{actMake, actShow};
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (firstTime) {
            Intent intentSplash = new Intent(this, SplashActivity.class);
            startActivity(intentSplash);
            itsCategs = new Categs(this);
            LocationListener.startUp(this);
            itsPrefManager = new PrefManager(getApplicationContext());
            firstTime = false;
        }
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.caption);
        String[] actionNames = MainActivity.actionNames.getStrings();
        ArrayMap<String, Integer> itemsMap = new ArrayMap<>();
        for (String actionName : actionNames)
            itemsMap.put(actionName, 0); // ToDo: Replace zero with icon resource identifiers.

        ListView lv = findViewById(R.id.actions_list);
        IconListAdapter adapter = new IconListAdapter.Builder(this, R.id.cat_list_text, itemsMap)
                .denySortItems()
                .make();

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        loadUsernameFromPref();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
        IconListAdapter.ViewHolder viewHolder = (IconListAdapter.ViewHolder) itemClicked.getTag();
        String strText = viewHolder.text.getText().toString(); // получаем текст нажатого элемента

        if (strText.equals(actionNames.actMake)) {
            Intent makeApealIntent = new Intent(this, MakeApealActivity.class);
            startActivityForResult(makeApealIntent, MAKE_APEAL_ACTIVITY_REQUEST);
        }
    }

    private void swapVisibility() {
        ProgressBar loadingProgress = findViewById(R.id.loading_progress);
        LinearLayout mainLayout = findViewById(R.id.main_layout);
        if (loadingProgress.getVisibility() == View.VISIBLE && mainLayout.getVisibility() == View.GONE) {
            mainLayout.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.GONE);
        } else {
            mainLayout.setVisibility(View.GONE);
            loadingProgress.setVisibility(View.VISIBLE);
        }
    }

    private void loadUsernameFromPref() {
        String curiosity = itsPrefManager.Get(PrefManager.Prefs.Curiosity, (String) null);
        if (curiosity != null && !curiosity.isEmpty() && curiosity.contains(":")) {
            String[] curiosityParts = curiosity.split(":", 2);
            String username = curiosityParts[0];
            if (username == null)
                Logger.e(getClass(), "username == null");
            String pwd = curiosityParts[1];
            if (pwd == null)
                Logger.e(getClass(), "pwd == null");
            UserLoginTask loginTask = new UserLoginTask(this, username, pwd);
            loginTask.execute((Void) null);
            loginTask.setOnFinishListener(this);
            return;
        }
        Logger.w(getClass(), "Information about authorisation does not found!");
        Intent loginFormIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(loginFormIntent, LOGIN_ACTIVITY_REQUEST);
    }

    @Override
    public void onLoginTaskFinal(Errs res) {
        switch (res) {
            case NoErr:
                String username = itsPrefManager.Get(PrefManager.Prefs.Curiosity, (String) null).split(":", 2)[0];
                swapVisibility();
                Logger.i(getClass(), "Logged in as \"" + username + "\".");
                tv.setText("Здравствуйте, Ув. " + username + '!');
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LOGIN_ACTIVITY_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    String fullUserName = data.getStringExtra(StringTable.IntentDataCuriosity);
                    itsPrefManager.Set(PrefManager.Prefs.Curiosity, fullUserName);
                    swapVisibility();
                } else {
                    AlertDialog.Builder failAlert = new AlertDialog.Builder(this);
                    failAlert.setTitle("Ошибка!")
                            .setMessage("Неудачная попытка входа!")
                            .setCancelable(false)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNegativeButton("Ок", (dialog, which) -> finish());
                }
                break;
            case CATLIST_ACTIVITY_REQUEST:
                break;
        }
    }

    public boolean getLocationPermission() {
        if (!isPermissionFineLocation ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder explanationDialogBuilder = new AlertDialog.Builder(this);
                explanationDialogBuilder
                        .setTitle("Внимание!")
                        .setMessage("Для корректной работы приложения необходим доступ к геоданным.")
                        .setIcon(android.R.drawable.stat_sys_warning)
                        .setCancelable(false)
                        .setNegativeButton("Ок", (DialogInterface dialog, int id) -> dialog.cancel());
                new Thread(explanationDialogBuilder::show, "explDialog");
            }
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSION_REQUEST);
            return isPermissionFineLocation;
            // PERMISSIONS_READ_CONTACTS_REQUEST is an
            // app-defined int constant. The callback method gets the
            // result of the request.
// TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        } else return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_REQUEST:
                // If request is cancelled, the result arrays are empty.
                isPermissionFineLocation =
                        grantResults.length > 0
                                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//                    isPermissionFineLocation = true;
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    isPermissionFineLocation = false;
//                }
                break;
        }
    }

}
