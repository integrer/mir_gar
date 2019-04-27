package org.mirgar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.mirgar.util.Cats;
import org.mirgar.util.Logger;
import org.mirgar.util.LoginErrs;
import org.mirgar.util.PrefManager;
import org.mirgar.util.tasks.UserLoginTask;

import java.util.Map;

public class MainActivity extends GeneralActivity implements AdapterView.OnItemClickListener, UserLoginTask.OnLoginTaskFinal {

    private static final String MENU_ID = "MENU_ID";

    private static final int MENU_ROOT = 0;
    private static final String ACTION_MAKE = "Подать обращение";
    private static final String ACTION_WATCH = "Мои обращения";
    private static final String ACTION_HELP = "Помощь";

    private static final int MENU_HELP = 1;
    private static final String ACTION_FAQ = "Часто задаваемые вопросы";
    private static final String ACTION_USING_TERMS = "Правила пользования";
    private static final String ACTION_ABOUT_PROJECT = "О проекте";
    private static final String ACTION_ABOUT = "О приложении";

    private static final int MENU_USING_TERMS = 2;
    private static final String ACTION_MODERATION_RULES = "Единые правила модерации";
    private static final String ACTION_PROCESSING_RULES = "Регламент обработки";
    private static final String ACTION_USER_AGREEMENT = "Пользовательское соглашение";

    private static final String GET_ARTICLES_URL = "http://mirgar.ga/modules/mod_mobile_app_support/getArticle.php?id=";

    private static final byte ARTICLE_MODERATION_RULES = 0;
    private static final byte ARTICLE_PROCESSING_RULES = 1;
    private static final byte ARTICLE_FAQ = 2;
    private static final byte ARTICLE_USER_AGREEMENT = 3;
    private static final byte ARTICLE_ABOUT_PROJECT = 4;

    private final int LOGIN_ACTIVITY_REQUEST = 1;
    private final int MAKE_APEAL_ACTIVITY_REQUEST = 3;
    TextView tv;

    IconListAdapter adapter;

    MenuCollection menuCollection;
    Menu currentMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        makeMenu();
        if (savedInstanceState == null) {
            Intent intentSplash = new Intent(this, SplashActivity.class);
            startActivity(intentSplash);
            Cats.init(this);
            loadUsernameFromPref();
            Logger.i("Running.");
            currentMenu = menuCollection.get(MENU_ROOT);

//            try {
//                File newFile = new File(getCacheDir(), "example.mht");
//                if (!newFile.exists())
//                    newFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        } else {
            int menuId = savedInstanceState.getInt(MENU_ID);
            currentMenu = menuCollection.get(menuId);
        }
        setContentView(R.layout.activity_main);

//        if(savedInstanceState == null)
//            LocationListener.init(this);
        tv = findViewById(R.id.caption);

        ListView lv = findViewById(R.id.actions_list);
        adapter = new IconListAdapter.Builder(this, R.id.cat_list_text, currentMenu.getItems())
                .make();

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
    }

    private void makeMenu() {
        menuCollection = new MenuCollection();

        Map<String, Integer> itemsMap = new ArrayMap<>();
        itemsMap.put(ACTION_MAKE, R.drawable.ic_make);
        itemsMap.put(ACTION_WATCH, R.drawable.ic_watch_appeals);
        itemsMap.put(ACTION_HELP, R.drawable.ic_help);

        menuCollection.add(new Menu(itemsMap, MENU_ROOT, 0));

        itemsMap = new ArrayMap<>();
        itemsMap.put(ACTION_FAQ, 0);
        itemsMap.put(ACTION_USING_TERMS, 0);
        itemsMap.put(ACTION_ABOUT_PROJECT, 0);
        itemsMap.put(ACTION_ABOUT, 0);

        menuCollection.add(new Menu(itemsMap, MENU_HELP, MENU_ROOT));

        itemsMap = new ArrayMap<>();
        itemsMap.put(ACTION_MODERATION_RULES, 0);
        itemsMap.put(ACTION_PROCESSING_RULES, 0);
        itemsMap.put(ACTION_USER_AGREEMENT, 0);

        menuCollection.add(new Menu(itemsMap, MENU_USING_TERMS, MENU_HELP));
    }

    private class Menu {
        private final Map<String, Integer> items;
        public final int id;
        public final int parent;

        public Map<String, Integer> getItems() {
            return items;
        }

        public Menu(Map<String, Integer> items, int id, int parent) {
            this.items = items;
            this.id = id;
            this.parent = parent;
        }
    }

    private class MenuCollection {
        private final SparseArray<Menu> menuMap;

        public Menu get(int id) { return menuMap.get(id); }

        public void add(Menu menu) { menuMap.put(menu.id, menu); }

        public MenuCollection() { menuMap = new SparseArray<>(); }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
        IconListAdapter.ViewHolder viewHolder = (IconListAdapter.ViewHolder) itemClicked.getTag();
        String strText = viewHolder.text.getText().toString(); // получаем текст нажатого элемента

        label:
        switch (currentMenu.id) {
            case MENU_ROOT:
                if (strText.equals(ACTION_MAKE)) {
                    Intent makeAppealIntent = new Intent(this, EditAppealActivity.class);
                    startActivityForResult(makeAppealIntent, MAKE_APEAL_ACTIVITY_REQUEST);
                } else if (strText.equals(ACTION_HELP)) {
                    changeMenu(MENU_HELP);
                }
                break;
            case MENU_HELP:
                byte articleId;
                String wndTitle;
                switch (strText) {
                    case ACTION_FAQ:
                        articleId = ARTICLE_FAQ;
                        wndTitle = ACTION_FAQ;
                        break;
                    case ACTION_USING_TERMS:
                        changeMenu(MENU_USING_TERMS);
                        break label;
                    case ACTION_ABOUT_PROJECT:
                        articleId = ARTICLE_ABOUT_PROJECT;
                        wndTitle = ACTION_ABOUT_PROJECT;
                        break;
                    default:
                        break label;
                }

                Intent textViewActivityIntent = new Intent(this, TextViewActivity.class);
                textViewActivityIntent.putExtra(TextViewActivity.FIELD_URL, GET_ARTICLES_URL + articleId);
                textViewActivityIntent.putExtra(TextViewActivity.FIELD_TITLE, wndTitle);
                startActivityForResult(textViewActivityIntent, 0);

                break;
            case MENU_USING_TERMS:
                switch (strText) {
                    case ACTION_MODERATION_RULES:
                        articleId = ARTICLE_MODERATION_RULES;
                        wndTitle = ACTION_MODERATION_RULES;
                        break;
                    case ACTION_PROCESSING_RULES:
                        articleId = ARTICLE_PROCESSING_RULES;
                        wndTitle = ACTION_PROCESSING_RULES;
                        break;
                    case ACTION_USER_AGREEMENT:
                        articleId = ARTICLE_USER_AGREEMENT;
                        wndTitle = ACTION_USER_AGREEMENT;
                        break;
                    default:
                        break label;
                }

                textViewActivityIntent = new Intent(this, TextViewActivity.class);
                textViewActivityIntent.putExtra(TextViewActivity.FIELD_URL, GET_ARTICLES_URL + articleId);
                textViewActivityIntent.putExtra(TextViewActivity.FIELD_TITLE, wndTitle);
                startActivityForResult(textViewActivityIntent, 0);

        }
    }

    private void changeMenu(int menuId) {
        currentMenu = menuCollection.get(menuId);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(currentMenu.id != MENU_ROOT);

        adapter.updateDataSet(currentMenu.getItems());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && currentMenu.id != MENU_ROOT) {
            changeMenu(currentMenu.parent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUsernameFromPref() {
        PrefManager itsPrefManager = PrefManager.getInstance();

        String curiosity = itsPrefManager.Get(PrefManager.Prefs.CURIOSITY, (String) null);
        if (curiosity != null && !curiosity.isEmpty() && curiosity.contains(":")) {
            String[] curiosityParts = curiosity.split(":", 2);
            String username = curiosityParts[0];
            if (username == null)
                Logger.e("username == null");
            String pwd = curiosityParts[1];
            if (pwd == null)
                Logger.e("pwd == null");
            UserLoginTask loginTask = new UserLoginTask(this, username, pwd);
            loginTask.execute((Void) null);
            loginTask.setOnFinishListener(this);
            return;
        }

        Logger.w("Information about authorisation does not found!");
        Intent loginFormIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(loginFormIntent, LOGIN_ACTIVITY_REQUEST);
    }

    @Override
    public void onLoginTaskFinal(LoginErrs res, int userId) {
        switch (res) {
            case NoErr:
                PrefManager itsPrefManager = PrefManager.getInstance();

                String username = itsPrefManager
                        .Get(PrefManager.Prefs.CURIOSITY, (String) null)
                        .split(":", 2)[0];
                View mainLayout = findViewById(R.id.main_layout);
                View loadingProgress = findViewById(R.id.loading_progress);
                swapVisibility(mainLayout, loadingProgress);
                Logger.i("Logged in as \"" + username + "\".");
                tv.setText("Здравствуйте, Ув. " + username + '!');
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LOGIN_ACTIVITY_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    PrefManager itsPrefManager = PrefManager.getInstance();
                    String fullUserName = data.getStringExtra(UserLoginTask.INTENT_DATA_CURIOSITY);
                    int userId = data.getIntExtra(UserLoginTask.INTENT_DATA_USER_ID, 0);
                    itsPrefManager.Set(PrefManager.Prefs.CURIOSITY, fullUserName);
                    itsPrefManager.Set(PrefManager.Prefs.USER_ID, userId);
                    swapVisibility(findViewById(R.id.main_layout), findViewById(R.id.loading_progress));
                } else {
                    AlertDialog.Builder failAlert = new AlertDialog.Builder(this);
                    failAlert.setTitle("Ошибка!")
                            .setMessage("Неудачная попытка входа!")
                            .setCancelable(false)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNegativeButton("Ок", (dialog, which) -> finish());
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(MENU_ID, currentMenu != null ? currentMenu.id : 0);
    }
}
