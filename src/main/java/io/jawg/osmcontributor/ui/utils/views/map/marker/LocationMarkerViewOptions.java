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
package io.jawg.osmcontributor.ui.utils.views.map.marker;


import android.os.Parcel;
import android.os.Parcelable;

import com.mapbox.mapboxsdk.annotations.BaseMarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.geometry.LatLng;

import io.jawg.osmcontributor.model.entities.Note;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiNodeRef;

/**
 * Contains options for LocationMarkerView.
 * @param <T> Type of related object
 */
public class LocationMarkerViewOptions<T> extends BaseMarkerViewOptions<LocationMarkerView<T>, LocationMarkerViewOptions<T>> {
    // Must be declared because BaseMarkerOptions implements Parceable. DO NOT REMOVE !
    private Parcelable.Creator CREATOR;

    private T relatedObject;

    private LocationMarkerView.MarkerType markerType;

    private LocationMarkerView<T> marker;


    public LocationMarkerViewOptions<T> relatedObject(T relatedObject) {
        if (marker == null) {
            this.relatedObject = relatedObject;
        } else {
            marker.setRelatedObject(relatedObject);
        }
        setMarkerType();
        return this;
    }

    @Override
    public LocationMarkerViewOptions<T> getThis() {
        return this;
    }

    @Override
    public LocationMarkerView<T> getMarker() {
        if (marker == null) {
            marker = new LocationMarkerView<>(this);
            marker.setRelatedObject(relatedObject);
            marker.setType(markerType);
            marker.setPosition(position);
            marker.setSnippet(snippet);
            marker.setTitle(title);
            marker.setIcon(icon);
            marker.setFlat(flat);
            marker.setAnchor(0.5f, 1f);
            marker.setInfoWindowAnchor(infoWindowAnchorU, infoWindowAnchorV);
            marker.setRotation(rotation);
            marker.setVisible(visible);
        }
        return marker;
    }

    @Override
    public LocationMarkerViewOptions<T> icon(Icon icon) {
        if (marker == null) {
            this.icon = icon;
        } else {
            marker.setIcon(icon);
        }
        return this;
    }

    @Override
    public LocationMarkerViewOptions<T> position(LatLng position) {
        if (marker == null) {
            this.position = position;
        } else {
            marker.setPosition(position);
        }
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    /**
     * Set marker type depending on object type.
     */
    private void setMarkerType() {
        if (relatedObject instanceof Poi) {
            markerType = LocationMarkerView.MarkerType.POI;
        } else if (relatedObject instanceof Note) {
            markerType = LocationMarkerView.MarkerType.NOTE;
        } else if (relatedObject instanceof PoiNodeRef) {
            markerType = LocationMarkerView.MarkerType.NODE_REF;
        } else {
            markerType = LocationMarkerView.MarkerType.NONE;
        }
    }
}

