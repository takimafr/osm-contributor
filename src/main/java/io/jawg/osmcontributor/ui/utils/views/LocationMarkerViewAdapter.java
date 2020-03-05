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
package io.jawg.osmcontributor.ui.utils.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mapbox.mapboxsdk.maps.MapboxMap;

import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.ui.utils.views.map.marker.LocationMarkerView;

/**
 * MarkerViewAdapter used for LocationMarkerView class to adapt a LocationMarkerView to an ImageView
 * @author Tommy Buonomo on 16/06/16.
 */
public class LocationMarkerViewAdapter extends MapboxMap.MarkerViewAdapter<LocationMarkerView> {
    private static final String TAG = "LocationMarkerVA";
    private LayoutInflater inflater;

    public LocationMarkerViewAdapter(Context context) {
        super(context);
        inflater = LayoutInflater.from(context);
    }

    @Nullable
    @Override
    public View getView(@NonNull LocationMarkerView marker, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.marker_layout, parent, false);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.marker_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.imageView.setImageBitmap(marker.getIcon().getBitmap());
        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
    }
}
