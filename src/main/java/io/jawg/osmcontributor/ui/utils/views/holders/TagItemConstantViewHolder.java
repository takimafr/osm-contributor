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
package io.jawg.osmcontributor.ui.utils.views.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.R;

public class TagItemConstantViewHolder extends RecyclerView.ViewHolder {
    public View poiTagLayout;

    @BindView(R.id.poi_key)
    TextView textViewKey;

    @BindView(R.id.poi_value)
    TextView textViewValue;

    @BindView(R.id.content_layout)
    RelativeLayout content;


    public TagItemConstantViewHolder(View v) {
        super(v);
        poiTagLayout = v;
        ButterKnife.bind(this, v);
    }

    public View getPoiTagLayout() {
        return poiTagLayout;
    }

    public TextView getTextViewKey() {
        return textViewKey;
    }

    public TextView getTextViewValue() {
        return textViewValue;
    }

    public RelativeLayout getContent() {
        return content;
    }
}
