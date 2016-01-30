package com.solderbyte.openfit.ui;

import java.util.ArrayList;
import java.util.List;

import com.solderbyte.openfit.R;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ArrayAdapterFitness extends ArrayAdapter<String> {

    private List<String> items;
    private List<String> subitems;
    private List<Drawable> icons;

    public ArrayAdapterFitness(Context context, List<String> items, List<String> subitems, ArrayList<Drawable> icons) {
        super(context, R.layout.fitness, R.id.text_1, items);
        this.items = items;
        this.subitems = subitems;
        this.icons = icons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView text1 = (TextView) view.findViewById(R.id.text_1);
        TextView text2 = (TextView) view.findViewById(R.id.text_2);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);

        icon.setImageDrawable(icons.get(position));
        text1.setText(items.get(position));
        text1.setTypeface(null, Typeface.BOLD);
        text2.setText(subitems.get(position));

        return view;
    }
}
