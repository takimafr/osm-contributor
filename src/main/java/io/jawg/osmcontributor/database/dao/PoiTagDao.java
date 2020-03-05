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
package io.jawg.osmcontributor.database.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.jawg.osmcontributor.database.helper.DatabaseHelper;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiTag;

/**
 * Dao for {@link io.jawg.osmcontributor.model.entities.PoiTag} objects.
 */
public class PoiTagDao extends RuntimeExceptionDao<PoiTag, Long> {

    private final PoiDao poiTypeDao;

    @Inject
    public PoiTagDao(Dao<PoiTag, Long> dao, PoiDao poiDao) {
        super(dao);
        this.poiTypeDao = poiDao;
    }

    /**
     * Query for all the existing values of a given PoiTag.
     *
     * @param key The key of the PoiTag.
     * @return The list of values.
     */
    public List<String> existingValuesForTag(final String key, final Long poiTypeId) {
        return DatabaseHelper.wrapException(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                QueryBuilder<Poi, Long> checkPoiTypeQueryBuilder = poiTypeDao.queryBuilder();
                checkPoiTypeQueryBuilder.where().eq(Poi.POI_TYPE_ID, poiTypeId);
                String statement = queryBuilder()
                        .selectColumns(PoiTag.VALUE).distinct()
                        .orderBy(PoiTag.VALUE, true)
                        .join(checkPoiTypeQueryBuilder)
                        .where().eq(PoiTag.KEY, new SelectArg())
                        .prepare().getStatement();
                return queryRaw(statement, new RawRowMapper<String>() {
                    @Override
                    public String mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
                        return resultColumns[0];
                    }
                }, key).getResults();
            }
        });
    }

    /**
     * Query for all the PoiTag of a given Poi.
     *
     * @param poiId The id of the Poi.
     * @return The list of PoiTag.
     */
    public List<PoiTag> queryByPoiId(Long poiId) {
        if (poiId == null) {
            return new ArrayList<>();
        }
        return queryForEq(PoiTag.POI_ID, poiId);
    }

    /**
     * Delete all POI tags with the given POI ids.
     *
     * @param poiIds The ids of the POIs.
     * @return The number of Poi tags deleted.
     */
    public Integer deleteByPoiIds(final Collection<Long> poiIds) {
        return DatabaseHelper.wrapException(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                DeleteBuilder<PoiTag, Long> builder = deleteBuilder();
                builder.where().in(PoiTag.POI_ID, poiIds);
                return builder.delete();
            }
        });
    }

    /**
     * Delete all the POIs in the database.
     */
    public void deleteAll() {
        DatabaseHelper.wrapException(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                deleteBuilder().delete();
                return null;
            }
        });
    }
}
