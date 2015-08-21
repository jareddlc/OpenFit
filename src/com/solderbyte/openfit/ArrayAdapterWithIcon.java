package com.solderbyte.openfit;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ArrayAdapterWithIcon extends ArrayAdapter<String> {
    private List<Drawable> icons;

    public ArrayAdapterWithIcon(Context context, List<String> items, List<Drawable> images) {
        super(context, android.R.layout.select_dialog_item, android.R.id.text1, items);
        this.icons = images;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        //textView.setCompoundDrawablesWithIntrinsicBounds(icons.get(position), null, null, null);
        textView.setCompoundDrawables(icons.get(position), null, null, null);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics());
        textView.setCompoundDrawablePadding(margin);

        return view;
    }
}