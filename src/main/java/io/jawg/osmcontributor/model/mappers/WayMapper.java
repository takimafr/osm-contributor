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
package io.jawg.osmcontributor.model.mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiNodeRef;
import io.jawg.osmcontributor.model.entities.Way;

public class WayMapper {

    public static Set<Way> poisToWays(List<Poi> pois) {
        Set<Way> ways = new HashSet<>();
        if (pois != null && !pois.isEmpty()) {
            for (Poi poi : pois) {
                Way way = new Way(poi);
                for (PoiNodeRef nodeRef : poi.getNodeRefs()) {
                    // Properties of a node in way edition
                    way.add(nodeRef);
                }
                ways.add(way);
            }
        }
        return ways;
    }
}
