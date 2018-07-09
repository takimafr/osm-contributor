/**
 * Copyright (C) 2016 eBusiness Information
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
package io.jawg.osmcontributor.rest.events;


public class SyncFinishUploadPoiEvent {
    private int successfullyAddedPoisCount;
    private int successfullyUpdatedPoisCount;
    private int successfullyDeletedPoisCount;
    private int successfullyUpdatedRelationsCount;

    public SyncFinishUploadPoiEvent(int successfullyAddedPoisCount, int successfullyUpdatedPoisCount, int successfullyDeletedPoisCount, int successfullyUpdatedRelationsCount) {
        this.successfullyAddedPoisCount = successfullyAddedPoisCount;
        this.successfullyUpdatedPoisCount = successfullyUpdatedPoisCount;
        this.successfullyDeletedPoisCount = successfullyDeletedPoisCount;
        this.successfullyUpdatedRelationsCount = successfullyUpdatedRelationsCount;
    }

    public int getSuccessfullyAddedPoisCount() {
        return successfullyAddedPoisCount;
    }

    public int getSuccessfullyUpdatedPoisCount() {
        return successfullyUpdatedPoisCount;
    }

    public int getSuccessfullyDeletedPoisCount() {
        return successfullyDeletedPoisCount;
    }

    public int getSuccessfullyUpdatedRelationsCount() {
        return successfullyUpdatedRelationsCount;
    }
}
