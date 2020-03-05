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
package io.jawg.osmcontributor.ui.presenters;

import android.graphics.Bitmap;

import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.jawg.osmcontributor.BuildConfig;
import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.model.entities.Note;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiNodeRef;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.model.events.PleaseLoadPoiTypes;
import io.jawg.osmcontributor.model.events.PoiTypesLoaded;
import io.jawg.osmcontributor.model.events.PoisAndNotesDownloadedEvent;
import io.jawg.osmcontributor.model.events.RevertFinishedEvent;
import io.jawg.osmcontributor.rest.NetworkException;
import io.jawg.osmcontributor.ui.events.map.PleaseChangeValuesDetailNoteFragmentEvent;
import io.jawg.osmcontributor.ui.events.map.PleaseChangeValuesDetailPoiFragmentEvent;
import io.jawg.osmcontributor.ui.events.map.PleaseInitializeDrawer;
import io.jawg.osmcontributor.ui.fragments.MapFragment;
import io.jawg.osmcontributor.ui.managers.loadPoi.GetPois;
import io.jawg.osmcontributor.ui.managers.loadPoi.KillByRequestException;
import io.jawg.osmcontributor.ui.managers.loadPoi.PoiLoadingProgress;
import io.jawg.osmcontributor.ui.managers.loadPoi.exception.TooManyPois;
import io.jawg.osmcontributor.ui.utils.LatLngBoundsUtils;
import io.jawg.osmcontributor.ui.utils.MapMode;
import io.jawg.osmcontributor.ui.utils.views.map.marker.LocationMarkerView;
import io.jawg.osmcontributor.ui.utils.views.map.marker.LocationMarkerViewOptions;
import io.jawg.osmcontributor.utils.Box;
import io.jawg.osmcontributor.utils.ConfigManager;
import io.jawg.osmcontributor.utils.OsmAnswers;
import io.jawg.osmcontributor.utils.core.MapElement;
import rx.Subscriber;
import timber.log.Timber;

public class MapFragmentPresenter {

    /*=========================================*/
    /*------------ATTRIBUTES-------------------*/
    /*=========================================*/
    private boolean forceRefreshPoi = false;

    private boolean forceRefreshNotes = false;

    private boolean abortedTooManyPois = false;

    private List<PoiType> poiTypes = null;

    private Float previousZoom;

    private LatLngBounds triggerReloadPoiLatLngBounds;

    private MapFragment mapFragment;

    private List<Long> ids;

    private GetPoisSubscriber getPoisSubscriber;

    @Inject
    EventBus eventBus;

    @Inject
    ConfigManager configManager;

    @Inject
    GetPois getPoisAndNotes;


    /*=========================================*/
    /*------------CONSTRUCTORS-----------------*/
    /*=========================================*/
    public MapFragmentPresenter(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
        ((OsmTemplateApplication) mapFragment.getActivity().getApplication()).getOsmTemplateComponent().inject(this);
    }


    /*=========================================*/
    /*------------GETTER/SETTER----------------*/
    /*=========================================*/
    public int getNumberOfPoiTypes() {
        return poiTypes != null ? poiTypes.size() : 0;
    }

    public List<PoiType> getPoiTypes() {
        return poiTypes == null ? new ArrayList<PoiType>() : poiTypes;
    }

    public PoiType getPoiType(int id) {
        return poiTypes.get(id);
    }

    public void setForceRefreshPoi() {
        this.forceRefreshPoi = true;
    }

    public void setForceRefreshNotes() {
        this.forceRefreshNotes = true;
    }


    /*=========================================*/
    /*------------- Life cycle  ---------------*/
    /*=========================================*/

    public void onDestroy() {
        if (getPoisAndNotes != null) {
            getPoisAndNotes.unsubscribe();
        }
    }

    /*=========================================*/
    /*----------------EVENTS-------------------*/
    /*=========================================*/
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPoiTypesLoaded(PoiTypesLoaded event) {
        Timber.d("Received event PoiTypesLoaded");
        poiTypes = event.getPoiTypes();
        setForceRefreshPoi();
        setForceRefreshNotes();
        if (BuildConfig.WITH_FILTER) {
            eventBus.post(new PleaseInitializeDrawer(poiTypes, mapFragment.getPoiTypeHidden()));
        }
        mapFragment.loadPoiTypeSpinner();
        mapFragment.loadPoiTypeFloatingBtn();
        eventBus.removeStickyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRevertFinishedEvent(RevertFinishedEvent event) {
        Timber.d("Received event RevertFinishedEvent");
        Object object = event.getRelatedObject();

        if (object == null) {
            mapFragment.removeAllPoiMarkers();
            setForceRefreshPoi();
            loadPoisIfNeeded();
        } else if (object instanceof Poi) {
            Poi poi = (Poi) object;
            LocationMarkerViewOptions markerOptions = mapFragment.getMarkerOptions(event.getMarkerType(), poi.getId());
            if (markerOptions == null) {
                markerOptions = new LocationMarkerViewOptions<Poi>().position(poi.getPosition()).relatedObject(poi);
                setIcon(markerOptions, poi, false);
                mapFragment.addPoi(markerOptions);
            } else {
                markerOptions.position(poi.getPosition()).relatedObject(poi);
            }
            setIcon(markerOptions, poi, false);
            OsmAnswers.localPoiAction(poi.getType().getTechnicalName(), "cancel");
        } else if (object instanceof PoiNodeRef) {
            mapFragment.switchMode(MapMode.WAY_EDITION);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPoisAndNotesDownloadedEvent(PoisAndNotesDownloadedEvent event) {
        mapFragment.showProgressBar(false);
        forceRefreshPoi = true;
        loadPoisIfNeeded();
    }

    public void register() {
        eventBus.register(this);
    }

    public void unregister() {
        eventBus.unregister(this);
    }


    /*=========================================*/
    /*------------CODE-------------------------*/
    /*=========================================*/
    public void loadPoisIfNeeded(boolean forceRefresh) {
        loadPoi(false, forceRefresh);
    }

    public void loadPoisIfNeeded() {
        loadPoi(false, false);
    }

    private void loadPoi(boolean refreshData, boolean forceRefresh) {
        if (poiTypes == null) {
            Timber.v("PleaseLoadPoiTypes");
            eventBus.post(new PleaseLoadPoiTypes());
        }


        LatLngBounds viewLatLngBounds = mapFragment.getViewLatLngBounds();
        if (viewLatLngBounds != null) {
            if (mapFragment.getZoomLevel() > BuildConfig.ZOOM_MARKER_MIN) {
                if (shouldReload(viewLatLngBounds) || refreshData || forceRefresh) {
                    previousZoom = mapFragment.getZoomLevel();
                    triggerReloadPoiLatLngBounds = LatLngBoundsUtils.enlarge(viewLatLngBounds, 1.2);
                    LatLngBounds latLngToLoad = LatLngBoundsUtils.enlarge(viewLatLngBounds, 1.2);
                    getPoisAndNotes.unsubscribe();
                    Timber.e("Unsubscribe current loading");

                    if (getPoisSubscriber != null) {
                        getPoisSubscriber.unsubscribe();
                    }
                    getPoisSubscriber = new GetPoisSubscriber();
                    getPoisAndNotes.init(Box.convertFromLatLngBounds(latLngToLoad), refreshData).execute(getPoisSubscriber);
                    mapFragment.displayZoomTooLargeError(false);
                }
            } else {
                forceRefreshPoi = true;
                cleanAllZoomTooLarge();
            }
        }
    }

    public void downloadAreaPoisAndNotes() {
        loadPoi(true, true);
    }


    /*=========================================*/
    /*------------PRIVATE CODE-----------------*/
    /*=========================================*/
    private boolean shouldReload(LatLngBounds viewLatLngBounds) {
        if (forceRefreshPoi || forceRefreshNotes) {
            forceRefreshPoi = false;
            forceRefreshNotes = false;
            return true;
        }
        if (previousZoom != null && previousZoom < BuildConfig.ZOOM_MARKER_MIN) {
            return true;
        }
        return triggerReloadPoiLatLngBounds == null
                || !triggerReloadPoiLatLngBounds.union(viewLatLngBounds).equals(triggerReloadPoiLatLngBounds) || abortedTooManyPois;
    }

    private void setIcon(LocationMarkerViewOptions markerOptions, Object relatedObject, boolean selected) {
        Bitmap bitmap;
        if (relatedObject instanceof Poi) {
            Poi poi = (Poi) relatedObject;
            bitmap = mapFragment.getBitmapHandler().getMarkerBitmap(poi.getType(), Poi.computeState(selected, false, poi.getUpdated()));
        } else {
            Note note = (Note) relatedObject;
            bitmap = mapFragment.getBitmapHandler().getNoteBitmap(Note.computeState(note, selected, false));
        }

        if (bitmap != null) {
            markerOptions.icon(IconFactory.getInstance(mapFragment.getActivity()).fromBitmap(bitmap));
        }
    }

    private void startLoading() {
        ids = new ArrayList<>();
    }

    private void loadingFinished() {
        mapFragment.showProgressBar(false);
    }

    private void onLoaded(List<MapElement> mapElements, LocationMarkerView.MarkerType markerType) {
        LocationMarkerView markerSelected = mapFragment.getMarkerSelected();

        for (MapElement mapElement : mapElements) {
            ids.add(mapElement.getId());
            LocationMarkerViewOptions markerOptions = mapFragment.getMarkerOptions(markerType, mapElement.getId());
            boolean selected = false;

            if (markerOptions == null) {
                markerOptions = new LocationMarkerViewOptions<>().relatedObject(mapElement).position(mapElement.getPosition());
                if (mapFragment.getSelectedMarkerType().equals(markerType) && mapElement.getId().equals(mapFragment.getMarkerSelectedId())) {
                    selected = true;
                    mapFragment.setMarkerSelected(markerOptions.getMarker());
                } else if (mapFragment.getSelectedMarkerType().equals(LocationMarkerView.MarkerType.POI) && markerSelected != null && mapElement.getId().equals(((Poi) markerSelected.getRelatedObject()).getId())) {
                    selected = true;
                }

                //the poi in edition should be hidden
                if (!(markerSelected != null && mapFragment.getMapMode() == MapMode.POI_POSITION_EDITION && markerSelected.equals(markerOptions.getMarker())) && (mapElement instanceof Poi && !((Poi) mapElement).getToDelete())) {
                    setIcon(markerOptions, mapElement, selected);
                    mapFragment.addPoi(markerOptions);
                }

                if (markerType == LocationMarkerView.MarkerType.NOTE) {
                    if (mapFragment.getSelectedMarkerType().equals(LocationMarkerView.MarkerType.NOTE) && mapElement.getId().equals(mapFragment.getMarkerSelectedId())) {
                        mapFragment.setMarkerSelected(markerOptions.getMarker());
                    }
                    setIcon(markerOptions, mapElement, false);
                    mapFragment.addNote(markerOptions);
                }
            } else {
                if (markerType == LocationMarkerView.MarkerType.POI) {
                    Poi poi = (Poi) mapElement;
                    Poi oldPoi = (Poi) markerOptions.getMarker().getRelatedObject();
                    oldPoi.setName(poi.getName());
                    oldPoi.setDetailsUpdated(poi.getDetailsUpdated());
                    selected = false;
                    if (mapFragment.getSelectedMarkerType().equals(LocationMarkerView.MarkerType.POI)
                            && (mapElement.getId().equals(mapFragment.getMarkerSelectedId())
                            || markerSelected != null
                            && mapElement.getId().equals(((Poi) markerSelected.getRelatedObject()).getId()))) {
                        selected = true;
                    }
                    setIcon(markerOptions, oldPoi, selected);
                } else {
                    if (mapFragment.getSelectedMarkerType().equals(LocationMarkerView.MarkerType.NOTE)
                            && markerSelected != null
                            && mapElement.getId().equals(((Note) markerSelected.getRelatedObject()).getId())) {
                        selected = true;
                    }
                    setIcon(markerOptions, mapElement, selected);
                }

                //update the detail banner data
                if (selected) {
                    if (mapFragment.getMapMode() == MapMode.DETAIL_NOTE) {
                        eventBus.post(new PleaseChangeValuesDetailNoteFragmentEvent((Note) mapElement));
                    } else {
                        Poi poi = (Poi) mapElement;
                        eventBus.post(new PleaseChangeValuesDetailPoiFragmentEvent(poi));
                    }
                }
            }
        }

        if ((mapFragment.getMapMode() == MapMode.DEFAULT || mapFragment.getMapMode() == MapMode.POI_CREATION)) {
            mapFragment.reselectMarker();
        }

        if (mapFragment.getSelectedMarkerType().equals(markerType) && markerSelected == null) {
            mapFragment.setMarkerSelectedId(-1L);
        }
    }

    private void impacteLoadedPoi(List<Poi> pois) {
        List<MapElement> mapElements = new ArrayList<>(pois.size());
        for (Poi poi : pois) {
            mapElements.add(poi);
        }
        onLoaded(mapElements, LocationMarkerView.MarkerType.POI);
    }

    private void impacteLoadedNotes(List<Note> notes) {
        List<MapElement> mapElements = new ArrayList<>(notes.size());
        for (Note note : notes) {
            mapElements.add(note);
        }
        onLoaded(mapElements, LocationMarkerView.MarkerType.NOTE);
    }

    public void refreshAreaConfirmed() {
        loadPoi(true, false);
    }

    public void cleanAllZoomTooLarge() {
        mapFragment.showProgressBar(false);
        getPoisAndNotes.unsubscribe();
        mapFragment.displayNetworkError(false);
        mapFragment.displayZoomTooLargeError(true);
        mapFragment.displayTooManyPois(false, 0L, 0);
        if (ids != null) {
            ids.clear();
        }
        mapFragment.removeNoteMarkersNotIn(new ArrayList<Long>());
        mapFragment.removePoiMarkersNotIn(new ArrayList<Long>());
    }


    /*=========================================*/
    /*------------  SUBSCRIBER  ---------------*/
    /*=========================================*/

    private final class GetPoisSubscriber extends Subscriber<PoiLoadingProgress> {
        boolean hasEncounterNetworkError;
        private Long poisCount;
        long loadedElements = 0L;
        long toLoadElements = 0L;

        public GetPoisSubscriber() {
            abortedTooManyPois = false;
            hasEncounterNetworkError = false;
        }

        @Override
        public void onStart() {
            super.onStart();
            startLoading();
            request(1);
        }

        @Override
        public void onCompleted() {
            loadingFinished();
            mapFragment.displayNetworkError(hasEncounterNetworkError);
            mapFragment.displayTooManyPois(abortedTooManyPois, poisCount, BuildConfig.MAX_POIS_ON_MAP);
            if (abortedTooManyPois || hasEncounterNetworkError) {
                forceRefreshPoi = true;
            }
        }

        @Override
        public void onError(Throwable e) {
            mapFragment.showProgressBar(false);
            if ((e instanceof NetworkException)) {
                hasEncounterNetworkError = true;
                onCompleted();
            } else if (e instanceof TooManyPois) {
                poisCount = ((TooManyPois) e).getCount();
                abortedTooManyPois = true;
                onCompleted();
            } else if (!(e instanceof KillByRequestException)) {
                Timber.e(e, "Error while loading Pois");
            } else {
                Timber.e("request killed");
            }
        }

        @Override
        public void onNext(PoiLoadingProgress poiLoadingProgress) {
            switch (poiLoadingProgress.getLoadingStatus()) {
                case POI_LOADING:
                    impacteLoadedPoi(poiLoadingProgress.getPois());
                    break;
                case NOTE_LOADING:
                    impacteLoadedNotes(poiLoadingProgress.getNotes());
                    break;
                case LOADING_FROM_SERVER:
                    displayProgress(poiLoadingProgress);
                    break;
                case OUT_DATED_DATA:
                    mapFragment.showNeedToRefreshData(poiLoadingProgress.getLastUpdateDate());
                    break;
                case MAPPING_POIS:
                    displayProgress(poiLoadingProgress);
                    break;
                case FINISH:
                    Timber.i("done loading " + ids.size());
                    mapFragment.removeNoteMarkersNotIn(ids);
                    mapFragment.removePoiMarkersNotIn(ids);
                    mapFragment.showProgressBar(false);
                    break;
                case AREA_NEEDED:
                    break;
            }
            request(1);
        }

        private void displayProgress(PoiLoadingProgress poiLoadingProgress) {
            mapFragment.showProgressBar(true);
            loadedElements += poiLoadingProgress.getLoadedElements();
            toLoadElements += poiLoadingProgress.getTotalsElements();
            mapFragment.displayAreaProgress(loadedElements, toLoadElements);
        }
    }
}