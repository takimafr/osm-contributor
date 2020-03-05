/**
 * Copyright (C) 2019 Takima
 *
 * This file is part of OSM Contributor.
 *
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor.ui.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import org.greenrobot.eventbus.EventBus;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.ui.utils.BitmapHandler;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.ui.events.map.NewPoiTypeSelected;

public class PoiTypePickerAdapter extends BaseAdapter implements Filterable {

    private List<PoiType> originalValues = null;
    private List<PoiType> filteredValues = null;
    private List<PoiType> lastUseValues = null;
    private LayoutInflater inflater;
    private ItemFilter filter = new ItemFilter();
    private EditText editText;
    private Context context;
    private BitmapHandler bitmapHandler;
    private boolean expertMode;


    EventBus eventBus;

    public PoiTypePickerAdapter(Context context, List<PoiType> values, EditText editText, EventBus eventBus, BitmapHandler bitmapHandler, boolean expertMode) {
        this.filteredValues = values;
        this.lastUseValues = values;
        this.originalValues = filteredValues;
        this.editText = editText;
        this.context = context;
        this.eventBus = eventBus;
        this.bitmapHandler = bitmapHandler;
        this.expertMode = expertMode;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return filteredValues.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredValues.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder = null;

        if (view != null) {
            holder = (ViewHolder) view.getTag();
            if (holder instanceof ExpertViewHolder ^ expertMode) {
                holder = null;
            }
        }
        if (holder == null) {
            if (expertMode) {
                view = inflater.inflate(R.layout.single_expert_poitype_autocomplete_layout, parent, false);
                holder = new ExpertViewHolder(view);
            } else {
                view = inflater.inflate(R.layout.single_poitype_autocomplete_layout, parent, false);
                holder = new ViewHolder(view);
            }
            view.setTag(holder);
        }


        final PoiType value = filteredValues.get(position);
        holder.onBind(value);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventBus.post(new NewPoiTypeSelected(value));
                closeKeyboard();
            }
        });
        return view;
    }

    public void addAll(Collection<PoiType> poiTypes) {
        originalValues.clear();
        originalValues.addAll(poiTypes);
        notifyDataSetChanged();
    }

    public void addAllLastUse(Collection<PoiType> poiTypes) {
        lastUseValues.clear();
        lastUseValues.addAll(poiTypes);
        notifyDataSetChanged();
    }

    public void setExpertMode(boolean expertMode) {
        this.expertMode = expertMode;
    }

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    class ViewHolder {
        @BindView(R.id.name_text)
        TextView nameView;

        @BindView(R.id.icon)
        ImageView icon;

        @BindView(R.id.info)
        ImageView info;

        public TextView getNameView() {
            return nameView;
        }

        public ImageView getIcon() {
            return icon;
        }

        public ImageView getInfo() {
            return info;
        }

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void onBind(final PoiType poiType) {
            String cap = poiType.getName().substring(0, 1).toUpperCase() + poiType.getName().substring(1);
            nameView.setText(cap);
            icon.setImageDrawable(bitmapHandler.getDrawable(poiType.getIcon()));
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPoiTypeInfo(poiType);
                }
            });
        }
    }

    class ExpertViewHolder extends ViewHolder {

        @BindView((R.id.technical_name_text))
        TextView technicalNameView;

        public TextView getTechnicalNameView() {
            return technicalNameView;
        }

        public ExpertViewHolder(View view) {
            super(view);
        }

        @Override
        public void onBind(final PoiType poiType) {
            super.onBind(poiType);
            technicalNameView.setText(poiType.getTechnicalName());
        }
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();


            FilterResults results = new FilterResults();

            final List<PoiType> list = originalValues;

            int count = list.size();
            final ArrayList<PoiType> newValuesList = new ArrayList<>(count);

            if (filterString.isEmpty()) {
                results.values = lastUseValues;
                results.count = lastUseValues.size();
                return results;
            }

            for (int i = 0; i < count; i++) {
                String filterableString = list.get(i).getKeyWords() + " " + list.get(i).getName() + " " + list.get(i).getTechnicalName();
                if (filterableString.toLowerCase().contains(filterString) || filterableString.toLowerCase().equals(filterString)) {
                    newValuesList.add(list.get(i));
                }
            }

            results.values = newValuesList;
            results.count = newValuesList.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredValues = (ArrayList<PoiType>) results.values;
            notifyDataSetChanged();
        }

    }

    private void showPoiTypeInfo(PoiType poiType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(poiType.getDescription())
                .setCancelable(true)
                .setTitle(poiType.getName())
                .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}

