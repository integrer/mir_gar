package org.mirgar;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.mirgar.util.Cats;
import org.mirgar.util.Logger;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CatListActivity extends GeneralActivity implements ListView.OnItemClickListener {

    //public static final String KEY_PAR_ITEM_LIST = "kfddlfgf";
    private Cats cats;
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

        cats = Cats.getInstance();

        final @IdRes int textViewId = R.id.cat_list_text;
        final @LayoutRes int itsHeaderLayout = R.layout.activity_cat_list_header;

        Intent itsIntent = getIntent();
        parentCatId = itsIntent.getIntExtra("Parent_Id", 0);

        ActionBar bar = getActionBar();
        if(bar != null)
            bar.setDisplayHomeAsUpEnabled(true);

        setContentView(android.R.layout.list_content);

        ListView listView = findViewById(android.R.id.list);
        View header = getLayoutInflater().inflate(itsHeaderLayout, null);
        listView.addHeaderView(header);

        TextView itsCaption = header.findViewById(R.id.caption);
        ImageView itsIcon = header.findViewById(R.id.caption_icon);

        if (parentCatId == 0) {
            itsCaption.setText("Выберите категорию");
            Logger.d("5");
        } else {
            itsCaption.setText("Выберите рубрику");
            itsIcon.setImageResource(cats.getIconId(parentCatId));
        }

        Set<String> catNames = cats.getCats(parentCatId);
        Map<String, Integer> items = new TreeMap<>();
        for (String currentItem : catNames) {
            items.put(currentItem, cats.getIconId(currentItem));
        }
        IconListAdapter.Builder adapterBuilder = new IconListAdapter.Builder(this, textViewId, items);

        if (parentCatId != 0)
            adapterBuilder.setTextSize(20);

        listView.setAdapter(adapterBuilder.make());
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if (position != 0) {
            IconListAdapter.ViewHolder holder = (IconListAdapter.ViewHolder) v.getTag();
            String selection = holder.text.getText().toString();
            if (parentCatId == 0) {
                itsCatId = cats.getRootCatId(selection);
                lastSelection = selection;
                Intent childActivityIntent = new Intent(this, CatListActivity.class);
                childActivityIntent.putExtra("Parent_Id", itsCatId);
                startActivityForResult(childActivityIntent, CHILD_ACTIVITY_REQUEST);
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(CAT_ID_FIELD, cats.getCatId(parentCatId, selection));
                resultIntent.putExtra(CAT_NAME_FIELD, selection);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHILD_ACTIVITY_REQUEST && resultCode == RESULT_OK) {
            Logger.d("Get result!");
            data.putExtra(ROOT_CAT_ID_FIELD, itsCatId);
            data.putExtra(ROOT_CAT_NAME_FIELD, lastSelection);
            setResult(RESULT_OK, data);
            finish();
        }
    }
}