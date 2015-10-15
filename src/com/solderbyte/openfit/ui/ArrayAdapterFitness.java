package com.solderbyte.openfit.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ArrayAdapterFitness extends ArrayAdapter<String> {

    private List<String> items;
    private List<String> subitems;
    private List<Drawable> icons;

    public ArrayAdapterFitness(Context context, List<String> items, List<String> subitems, ArrayList<Drawable> icons) {
        super(context, android.R.layout.simple_list_item_2, android.R.id.text1, items);
        this.items = items;
        this.subitems = subitems;
        this.icons = icons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        TextView text2 = (TextView) view.findViewById(android.R.id.text2);

        text1.setCompoundDrawables(icons.get(0), null, null, null);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics());
        text1.setCompoundDrawablePadding(margin);

        text1.setText(items.get(position));
        text1.setTypeface(null, Typeface.BOLD);

        text2.setText(subitems.get(position));
        return view;
    }
}
