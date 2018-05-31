package org.donampa.nbibik.dipl;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

class IconListAdapter extends ArrayAdapter<String> {
    private static String[] items;
    private final ArrayMap<String, Integer> iconResIdMap;
    private final float textSize;
    private final Activity context;

    static class Builder {
        // Обязательные поля. Вводятся в конструкторе.
        private Activity context;
        private int textViewResourceId;
        private ArrayMap<String, Integer> items;

        // Необязательные поля. Могут быть инициализированы через методы.
        private float textSize = 0;
        private boolean sortItems = true;

        Builder(Activity context, int textViewResourceId, ArrayMap<String, Integer> items) {
            this.context = context;
            this.textViewResourceId = textViewResourceId;
            this.items = items;
        }

        IconListAdapter make() {
            return new IconListAdapter(context, textViewResourceId, items, textSize, sortItems);
        }

        Builder setTextSize(float textSize) {
            this.textSize = textSize;
            return this;
        }

        Builder denySortItems() {
            this.sortItems = false;
            return this;
        }
    }

    class ViewHolder {
        public ImageView icon;
        public TextView text;
    }

    private IconListAdapter(Activity context, int textViewResourceId, ArrayMap<String, Integer> items, float textSize, boolean allowSortItems) {
        super(context, textViewResourceId, fetchStringArray(items, allowSortItems));

        this.iconResIdMap = items;
        this.context = context;
        this.textSize = textSize;
    }

    private static String[] fetchStringArray(ArrayMap<String, Integer> _items, boolean allowSortItems) {
        items = _items.keySet().toArray(new String[0]);
        if (allowSortItems)
            Arrays.sort(items);
        return items;
    }

    private ViewHolder bindViewHolder(View row) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.icon = row.findViewById(R.id.cat_list_icon);
        viewHolder.text = row.findViewById(R.id.cat_list_text);
        row.setTag(viewHolder);
        return viewHolder;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View row, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (row == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            row = inflater.inflate(R.layout.icon_list_item, parent, false);

            viewHolder = bindViewHolder(row);
        } else
            viewHolder = (ViewHolder) row.getTag();


        String currentItem = items[position];
        viewHolder.text.setText(currentItem);

        if (textSize > 0)
            viewHolder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

        int iconResource = iconResIdMap.get(currentItem);
        if (iconResource != 0)
            viewHolder.icon.setImageResource(iconResource);
        else
            viewHolder.icon.setImageResource(android.R.drawable.checkbox_off_background);

        return row;
    }
}
