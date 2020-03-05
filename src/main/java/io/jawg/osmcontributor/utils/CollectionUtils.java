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
package io.jawg.osmcontributor.utils;


import java.util.ArrayList;
import java.util.List;

public final class CollectionUtils {

    /**
     * Apply a {@link io.jawg.osmcontributor.utils.Function} to every objects of an {@link java.lang.Iterable}
     * and put the results in a {@link java.util.List}. If the result of {@link io.jawg.osmcontributor.utils.Function#apply(Object)}
     * is null, it's not put into the list.
     *
     * @param iterable The iterable.
     * @param function The function to apply.
     * @param <T>      The return type of the function.
     * @param <U>      The type of the iterable.
     * @return The list of the results of the function.
     */
    public static <T, U> List<T> map(Iterable<U> iterable, Function<U, T> function) {
        ArrayList<T> result = new ArrayList<>();
        T t;
        for (U arg : iterable) {
            if ((t = function.apply(arg)) != null) {
                result.add(t);
            }
        }
        return result;
    }


    /**
     * Transform a list of ids into a string where ids are separated from each over by a ",".
     *
     * @param ids The list of ids to transform.
     * @return The formatted list.
     */
    public static String formatIdList(List<Long> ids) {
        String idsStr = "";
        int i = 1;
        for (Long id : ids) {
            if (id != null) {
                idsStr += id;
                if (i < ids.size()) {
                    idsStr += ",";
                }
            }
            i++;
        }
        return idsStr;
    }
}
