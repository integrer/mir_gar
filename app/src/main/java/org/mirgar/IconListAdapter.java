package org.mirgar;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

class IconListAdapter extends ArrayAdapter<String> {
    private Map<String, Integer> iconResIdMap;
    private final float textSize;
    private final Activity context;

    public static class Builder {
        // Обязательные поля. Вводятся в конструкторе.
        private Activity context;
        private int textViewResourceId;
        private Map<String, Integer> items;

        // Необязательные поля. Могут быть инициализированы через методы.
        private float textSize = 0;

        Builder(Activity context, int textViewResourceId, Map<String, Integer> items) {
            this.context = context;
            this.textViewResourceId = textViewResourceId;
            this.items = items;
        }

        IconListAdapter make() {
            return new IconListAdapter(context, textViewResourceId, items, textSize);
        }

        Builder setTextSize(float textSize) {
            this.textSize = textSize;
            return this;
        }
    }

    class ViewHolder {
        public AppCompatImageView icon;
        public TextView text;
    }

    private IconListAdapter(Activity context, int textViewResourceId, Map<String, Integer> items, float textSize) {
        super(context, textViewResourceId, new ArrayList<>(items.keySet()));

        this.iconResIdMap = items;
        this.context = context;
        this.textSize = textSize;
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


        String currentItem = getItem(position);
        viewHolder.text.setText(currentItem);

        if (textSize > 0)
            viewHolder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

        int iconResource = iconResIdMap.get(currentItem);
        viewHolder.icon.setImageResource(iconResource);

        return row;
    }

    public void updateDataSet(Map<String, Integer> dataSet) {
        iconResIdMap = dataSet;
        clear();
        addAll(dataSet.keySet());
        notifyDataSetChanged();
    }
}
