/**
 * Copyright (C) 2019 Takima
 * <p>
 * This file is part of OSM Contributor.
 * <p>
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor.database.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.jawg.osmcontributor.database.helper.DatabaseHelper;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.utils.Box;

/**
 * Dao for {@link io.jawg.osmcontributor.model.entities.Poi} objects.
 * Check {@link io.jawg.osmcontributor.ui.managers.PoiManager} for methods to save and delete POIs.
 *
 * @see io.jawg.osmcontributor.ui.managers.PoiManager
 */
public class PoiDao extends RuntimeExceptionDao<Poi, Long> {

    @Inject
    public PoiDao(Dao<Poi, Long> dao) {
        super(dao);
    }

    /**
     * Query for all the POIs contained in the bounds defined by the box.
     *
     * @param box Bounds of the search in latitude and longitude coordinates.
     * @return The notes contained in the box.
     */
    public List<Poi> queryForAllInRect(final Box box) {
        return DatabaseHelper.wrapException(new Callable<List<Poi>>() {
            @Override
            public List<Poi> call() throws Exception {
                return queryBuilder()
                        .where().gt(Poi.LATITUDE, new SelectArg(box.getSouth()))
                        .and().lt(Poi.LATITUDE, new SelectArg(box.getNorth()))
                        .and().gt(Poi.LONGITUDE, new SelectArg(box.getWest()))
                        .and().lt(Poi.LONGITUDE, new SelectArg(box.getEast()))
                        .and().eq(Poi.OLD, false)
                        .query();
            }
        });
    }

    /**
     * Query for all the POIs contained in the bounds defined by the box, retrived by pages.
     * The first page will get Pois closer to the center.
     *
     * @param box Bounds of the search in latitude and longitude coordinates.
     * @return The notes contained in the box.
     */
    public List<Poi> queryForAllInRect(final Box box, final long offset, final long limit) {
        return DatabaseHelper.wrapException(new Callable<List<Poi>>() {
            @Override
            public List<Poi> call() throws Exception {
                Double latCenter = box.getNorth() - box.getSouth();
                Double lngCenter = box.getEast() - box.getWest();

                QueryBuilder<Poi, Long> poiLongQueryBuilder = queryBuilder();
                poiLongQueryBuilder
                        .where().gt(Poi.LATITUDE, new SelectArg(box.getSouth()))
                        .and().lt(Poi.LATITUDE, new SelectArg(box.getNorth()))
                        .and().gt(Poi.LONGITUDE, new SelectArg(box.getWest()))
                        .and().lt(Poi.LONGITUDE, new SelectArg(box.getEast()))
                        .and().eq(Poi.OLD, false);

                String rawSql = " (SELECT (ABS(" + latCenter + " - " + Poi.LATITUDE + ") + " + "ABS(" + lngCenter + "-" + Poi.LONGITUDE + ")))";
                poiLongQueryBuilder.distinct().offset(offset).limit(limit).orderByRaw(rawSql);
                return poiLongQueryBuilder.query();
            }
        });
    }

    public Long countForAllInRect(final Box box) {
        return DatabaseHelper.wrapException(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                QueryBuilder<Poi, Long> poiLongQueryBuilder = queryBuilder();
                poiLongQueryBuilder
                        .where().gt(Poi.LATITUDE, new SelectArg(box.getSouth()))
                        .and().lt(Poi.LATITUDE, new SelectArg(box.getNorth()))
                        .and().gt(Poi.LONGITUDE, new SelectArg(box.getWest()))
                        .and().lt(Poi.LONGITUDE, new SelectArg(box.getEast()))
                        .and().eq(Poi.OLD, false);

                return poiLongQueryBuilder.countOf();
            }
        });
    }

    /**
     * Query for all the new POIs, meaning all the POIs without a backend id.
     *
     * @return The new POIs.
     */
    public List<Poi> queryForAllNew() {
        return DatabaseHelper.wrapException(new Callable<List<Poi>>() {
            @Override
            public List<Poi> call() throws Exception {
                return queryBuilder()
                        .where().isNull(Poi.BACKEND_ID)
                        .query();
            }
        });
    }

    /**
     * Query for all updated and NOT new POIs.
     *
     * @return The list of updated POIs.
     */
    public List<Poi> queryForAllUpdated() {
        return DatabaseHelper.wrapException(() -> {
            Where<Poi, Long> where = queryBuilder().where();
            return where.and(
                    where.isNotNull(Poi.BACKEND_ID),
                    where.or(where.eq(Poi.UPDATED, true), where.eq(Poi.RELATION_UPDATED, true)),
                    where.eq(Poi.TO_DELETE, false))
                    .query();
        });
    }

    /**
     * Query for all updated pois and NOT new POIs.
     *
     * @return The list of updated POIs.
     */
    public List<Poi> queryForAllUpdatedPois() {
        return DatabaseHelper.wrapException(() -> queryBuilder()
                .where().isNotNull(Poi.BACKEND_ID)
                .and().eq(Poi.UPDATED, true)
                .and().eq(Poi.TO_DELETE, false)
                .query());
    }

 /**
     * Query for all updated relations of pois and NOT new POIs.
     *
     * @return The list of updated POIs.
     */
    public List<Poi> queryForAllUpdatedPoisRelations() {
        return DatabaseHelper.wrapException(() -> queryBuilder()
                .where().isNotNull(Poi.BACKEND_ID)
                .and().eq(Poi.RELATION_UPDATED, true)
                .and().eq(Poi.TO_DELETE, false)
                .query());
    }

    /**
     * Query for all Poi changes.
     *
     * @return The list of changed POIs.
     */
    public List<Poi> queryForAllChanges() {
        return DatabaseHelper.wrapException(new Callable<List<Poi>>() {
            @Override
            public List<Poi> call() throws Exception {
                return queryBuilder()
                        .where()
                        .eq(Poi.UPDATED, true)
                        .or().eq(Poi.TO_DELETE, true)
                        .query();
            }
        });
    }

    /**
     * Count for all Poi changes.
     *
     * @return The count of changed POIs.
     */
    public Long countForAllChanges() {
        return DatabaseHelper.wrapException(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return queryBuilder()
                        .where()
                        .eq(Poi.UPDATED, true)
                        .or().eq(Poi.RELATION_UPDATED, true)
                        .or().eq(Poi.TO_DELETE, true)
                        .countOf();
            }
        });
    }

    /**
     * Query for all POIs who are ways.
     *
     * @return The list of POIs who are ways.
     */
    public List<Poi> queryForAllWays() {
        return DatabaseHelper.wrapException(new Callable<List<Poi>>() {
            @Override
            public List<Poi> call() throws Exception {
                return queryBuilder()
                        .where().isNotNull(Poi.BACKEND_ID)
                        .and().eq(Poi.WAY, true)
                        .and().eq(Poi.TO_DELETE, false)
                        .and().eq(Poi.OLD, false)
                        .query();
            }
        });
    }

    /**
     * Query for all POIs who are ways and don't have a PoiType.
     *
     * @return The list of POIs.
     */
    public List<Poi> queryForAllWaysNoType() {
        return DatabaseHelper.wrapException(new Callable<List<Poi>>() {
            @Override
            public List<Poi> call() throws Exception {
                return queryBuilder()
                        .where().isNotNull(Poi.BACKEND_ID)
                        .and().eq(Poi.WAY, true)
                        .and().eq(Poi.TO_DELETE, false)
                        .and().isNull(Poi.POI_TYPE_ID)
                        .and().eq(Poi.OLD, false)
                        .query();
            }
        });
    }

    /**
     * Query for all the POIs to delete.
     *
     * @return The List of POIs to delete.
     */
    public List<Poi> queryToDelete() {
        return DatabaseHelper.wrapException(new Callable<List<Poi>>() {
            @Override
            public List<Poi> call() throws Exception {
                return queryBuilder()
                        .where().isNotNull(Poi.BACKEND_ID)
                        .and().eq(Poi.TO_DELETE, true)
                        .query();
            }
        });
    }

    /**
     * Query for POIs by their backend Ids.
     *
     * @param backendIds The backend ids.
     * @return The List of POIs.
     */
    public List<Poi> queryForBackendIds(final Collection<String> backendIds) {
        return DatabaseHelper.wrapException(new Callable<List<Poi>>() {
            @Override
            public List<Poi> call() throws Exception {
                return queryBuilder()
                        .where().in(Poi.BACKEND_ID, backendIds)
                        .query();
            }
        });
    }

    /**
     * Query for POIs by their Ids.
     *
     * @param ids The ids.
     * @return The List of POIs.
     */
    public List<Poi> queryForIds(final Collection<Long> ids) {
        return DatabaseHelper.wrapException(new Callable<List<Poi>>() {
            @Override
            public List<Poi> call() throws Exception {
                return queryBuilder()
                        .where().in(Poi.ID, ids)
                        .query();
            }
        });
    }

    /**
     * Query for POIs with a given backend Id.
     *
     * @param backendId The backend id.
     * @return The List of POIs.
     */
    public List<Poi> queryForBackendId(final String backendId) {
        return DatabaseHelper.wrapException(new Callable<List<Poi>>() {
            @Override
            public List<Poi> call() throws Exception {
                return queryBuilder()
                        .where().eq(Poi.BACKEND_ID, backendId)
                        .query();
            }
        });
    }

    /**
     * Count for POIs with the same backend Id.
     *
     * @param backendId The backend id.
     * @return The count of pois.
     */
    public Long countForBackendId(final String backendId) {
        return DatabaseHelper.wrapException(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return queryBuilder()
                        .where().eq(Poi.BACKEND_ID, backendId)
                        .countOf();
            }
        });
    }

    /**
     * Query for the date of the last change.
     *
     * @return The date of the last change.
     */
    public DateTime queryForMostRecentChangeDate() {
        Poi poi = DatabaseHelper.wrapException(new Callable<Poi>() {
            @Override
            public Poi call() throws Exception {
                return queryBuilder()
                        .orderBy(Poi.UPDATE_DATE, false)
                        .queryForFirst();
            }
        });
        return poi != null ? poi.getUpdateDate() : null;
    }

    /**
     * Query all POI ids for the given POI type id.
     *
     * @param poiTypeId The id of the POI type
     * @return A list of POI ids
     */
    public List<Long> queryAllIdsByPoiTypeId(final Long poiTypeId) {
        return DatabaseHelper.wrapException(new Callable<List<Long>>() {
            @Override
            public List<Long> call() throws Exception {
                String statement = queryBuilder()
                        .selectColumns(Poi.ID).distinct()
                        .where().eq(Poi.POI_TYPE_ID, poiTypeId)
                        .prepare().getStatement();
                return queryRaw(statement, new RawRowMapper<Long>() {
                    @Override
                    public Long mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
                        return Long.parseLong(resultColumns[0]);
                    }
                }).getResults();
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
