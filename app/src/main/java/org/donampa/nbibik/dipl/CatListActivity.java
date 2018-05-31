package org.donampa.nbibik.dipl;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.util.ArrayMap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.donampa.nbibik.dipl.util.Categs;
import org.donampa.nbibik.dipl.util.Logger;

public class CatListActivity extends ListActivity {

    //public static final String KEY_PAR_ITEM_LIST = "kfddlfgf";
    private Categs categs;
    private int parentCatId;
    private String lastSelection;
    private final int CHILD_ACTIVITY_REQUEST = 0;
    private int itsCatId;
    public static final String ROOT_CAT_ID_FIELD = "ROOT_CAT_ID";
    public static final String ROOT_CAT_NAME_FIELD = "ROOT_CAT_NAME";
    public static final String CAT_ID_FIELD = "CAT_ID";
    public static final String CAT_NAME_FIELD = "CAT_NAME";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categs = MainActivity.itsCategs;

        final @IdRes int textViewId = R.id.cat_list_text;
        final @LayoutRes int itsHeaderLayout = R.layout.activity_cat_list_header;

        Intent itsIntent = getIntent();
        parentCatId = itsIntent.getIntExtra("Parent_Id", 0);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        View header = getLayoutInflater().inflate(itsHeaderLayout, null);
        ListView listView = getListView();
        listView.addHeaderView(header);

        TextView itsCaption = header.findViewById(R.id.caption);
        ImageView itsIcon = header.findViewById(R.id.caption_icon);


        if (parentCatId == 0) {
            itsCaption.setText("Выберите категорию");
            Logger.d(getClass(), "5");
        } else {
            itsCaption.setText("Выберите рубрику");
            itsIcon.setImageResource(categs.getIconId(parentCatId));
        }


        String[] catNames = categs.getCats(parentCatId);
        ArrayMap<String, Integer> items = new ArrayMap<>();
        for (String currentItem : catNames) {
            items.put(currentItem, categs.getIconId(currentItem));
        }
        IconListAdapter.Builder adapterBuilder = new IconListAdapter.Builder(this, textViewId, items);

        if (parentCatId != 0)
            adapterBuilder.setTextSize(20);

        setListAdapter(adapterBuilder.make());

        Logger.v(getClass(), "Adapter Loaded.");
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (position != 0) {
            IconListAdapter.ViewHolder holder = (IconListAdapter.ViewHolder) v.getTag();
            String selection = holder.text.getText().toString();
            if (parentCatId == 0) {
                itsCatId = categs.getRootCatId(selection);
                lastSelection = selection;
                Intent childActivityIntent = new Intent(this, CatListActivity.class);
                childActivityIntent.putExtra("Parent_Id", itsCatId);
                startActivityForResult(childActivityIntent, CHILD_ACTIVITY_REQUEST);
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(CAT_ID_FIELD, categs.getCatId(parentCatId, selection));
                resultIntent.putExtra(CAT_NAME_FIELD, selection);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == CHILD_ACTIVITY_REQUEST) && (resultCode == RESULT_OK)) {
            Logger.d(getClass(), "Get result!");
            data.putExtra(ROOT_CAT_ID_FIELD, itsCatId);
            data.putExtra(ROOT_CAT_NAME_FIELD, lastSelection);
            setResult(RESULT_OK, data);
            finish();
        }
    }
}