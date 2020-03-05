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
package io.jawg.osmcontributor.model.events;

import java.util.List;

import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.relation_save.RelationEdition;
import io.jawg.osmcontributor.utils.edition.PoiChanges;

public class PleaseCreatePoiEvent {

    private final Poi poi;
    private final PoiChanges poiChanges;
    private final List<RelationEdition> relationEditions;

    public PleaseCreatePoiEvent(Poi poi, PoiChanges poiChanges, List<RelationEdition> relationEditions) {
        this.poi = poi;
        this.poiChanges = poiChanges;
        this.relationEditions = relationEditions;
    }

    public Poi getPoi() {
        return poi;
    }

    public PoiChanges getPoiChanges() {
        return poiChanges;
    }

    public List<RelationEdition> getRelationEditions() {
        return relationEditions;
    }
}
