package krtkush.github.io.trail;

import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by kartikeykushwaha on 07/04/17.
 */

public class Trail {

    // Time from which a particular view has been started viewing.
    private long startTime = 0;

    // Time at which a particular view has been stopped viewing.
    private long endTime = 0;

    // Flag is required because 'addOnGlobalLayoutListener' is called multiple times.
    // The flag limits the action inside 'onGlobalLayout' to only once.
    private boolean firstTrackFlag = false;

    // Flag to pause tracking.
    private boolean trackingPaused = false;

    // ArrayList of view positions that have been viewed for more than the threshold time.
    private ArrayList<Integer> positionOfViewsViewed = new ArrayList<>();

    // ArrayList of TrackingData class instances.
    private ArrayList<TrackingData> trackingData = new ArrayList<>();

    // The instance of the recyclerView whose views are supposed to be tracked.
    private RecyclerView recyclerView;

    // Time interval after which data should be given to the user.
    private long dataDumpInterval;

    // The minimum time user must spend on a view item for its tracking data to be considered.
    private long minimumViewingTimeThreshold;

    // Boolean flag to inform whether to dump data at an interval or not.
    private boolean dumpDataAfterInterval;

    // The minimum amount of area of the list item that should be on
    // the screen for the tracking to start.
    private double minimumVisibleHeightThreshold;

    // Reference to the `TrailTrackingListener` interface.
    private TrailTrackingListener trailTrackingListener;

    private Timer dataDumpTimer = new Timer();

    public Trail(Builder builder) {

        this.recyclerView = builder.recyclerView;
        this.dataDumpInterval = builder.dataDumpInterval;
        this.minimumVisibleHeightThreshold = builder.minimumVisibleHeightThreshold;
        this.minimumViewingTimeThreshold = builder.minimumViewingTimeThreshold;
        this.trailTrackingListener = builder.trailTrackingListener;
        this.dumpDataAfterInterval = builder.dumpDataAfterInterval;
    }

    /**
     * Start the tracking process.
     */
    public void startTracking() {

        // Track the views when the data is loaded into recycler view for the first time.
        recyclerView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        if(!firstTrackFlag) {

                            startTime = System.currentTimeMillis();

                            int firstVisibleItemPosition = ((LinearLayoutManager)
                                    recyclerView.getLayoutManager())
                                    .findFirstVisibleItemPosition();

                            int lastVisibleItemPosition = ((LinearLayoutManager)
                                    recyclerView.getLayoutManager())
                                    .findLastVisibleItemPosition();

                            analyzeAndAddViewData(firstVisibleItemPosition,
                                    lastVisibleItemPosition);

                            firstTrackFlag = true;
                        }
                    }
                });

        // Track the views when user scrolls through the recyclerview.
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(!trackingPaused) {

                    // User is scrolling, calculate and store the tracking data of the views
                    // that were being viewed before the scroll.
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {

                        endTime = System.currentTimeMillis();

                        for (int trackedViewsCount = 0;
                             trackedViewsCount < positionOfViewsViewed.size();
                             trackedViewsCount++ ) {

                            long duration = endTime - startTime;

                            if (duration > minimumViewingTimeThreshold) {

                                View itemView = recyclerView.getLayoutManager()
                                        .findViewByPosition(positionOfViewsViewed
                                                .get(trackedViewsCount));

                                trackingData.add(prepareTrackingData(String
                                                .valueOf(positionOfViewsViewed
                                                        .get(trackedViewsCount)),
                                        duration, getVisibleHeightPercentage(itemView)));
                            }
                        }

                        positionOfViewsViewed.clear();
                    }

                    // Scrolling has ended, start the tracking process by assigning a start time
                    // and maintaining a list of views being viewed.
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                        startTime = System.currentTimeMillis();

                        int firstVisibleItemPosition = ((LinearLayoutManager)
                                recyclerView.getLayoutManager())
                                .findFirstVisibleItemPosition();

                        int lastVisibleItemPosition = ((LinearLayoutManager)
                                recyclerView.getLayoutManager())
                                .findLastVisibleItemPosition();

                        analyzeAndAddViewData(firstVisibleItemPosition, lastVisibleItemPosition);
                    }
                }
            }
        });

        if(dumpDataAfterInterval)
            dumpDataAfterSpecifiedInterval();
    }

    /**
     * Track the views currently visible and then stop the tracking process.
     */
    public void stopTracking() {

        if(!trackingPaused) {

            endTime = System.currentTimeMillis();

            int firstVisibleItemPosition = ((LinearLayoutManager)
                    recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

            int lastVisibleItemPosition = ((LinearLayoutManager)
                    recyclerView.getLayoutManager()).findLastVisibleItemPosition();

            analyzeAndAddViewData(firstVisibleItemPosition, lastVisibleItemPosition);

            for (int trackedViewsCount = 0; trackedViewsCount < positionOfViewsViewed.size();
                 trackedViewsCount++ ) {

                long duration = endTime - startTime;

                if (duration > minimumViewingTimeThreshold) {

                    View itemView = recyclerView.getLayoutManager()
                            .findViewByPosition(positionOfViewsViewed.get(trackedViewsCount));

                    trackingData.add(prepareTrackingData(String.valueOf(
                            positionOfViewsViewed.get(trackedViewsCount)),
                            duration,
                            getVisibleHeightPercentage(itemView)));
                }
            }

            dataDumpTimer.cancel();
            positionOfViewsViewed.clear();
        }
    }

    /**
     * Method to pause tracking.
     */
    public void pauseTracking() {

        trackingPaused = true;
    }

    /**
     * Method to resume the tracking.
     */
    public void resumeTracking() {

        trackingPaused = false;
    }

    /**
     * Returns the status of tracking
     * @return 1 - Paused. 2 - Not paused.
     */
    public int trackingStatus() {

        if(trackingPaused) {
            // Paused
            return 0;
        } else {
            // Running.
            return 1;
        }
    }

    /**
     * Method to get all the tracking data.
     * @param stopTracking if true, the tracking will stop and then all the data will be
     *                     handed over.
     * @return
     */
    public ArrayList<TrackingData> getTrackingData(boolean stopTracking) {

        if(stopTracking)
            stopTracking();

        return trackingData;
    }

    /**
     * Method to clear all the tracking data.
     * @return Flag to inform whether the tracking data has been successfully cleared or not.
     */
    public boolean clearAllTrackingData() {

        try {

            trackingData.clear();
            trackingData = null;
            return true;
        } catch (Exception ex) {

            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Method to dump data to the user and then purge the collected data from the library side.
     */
    private void dumpDataAfterSpecifiedInterval() {

        dataDumpTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                trailTrackingListener.trailDataDump(trackingData);
                trackingData.clear();
                dumpDataAfterSpecifiedInterval();
            }
        }, dataDumpInterval);
    }

    /**
     * Method to analyse if the view is as much visible as the defined height threshold
     * or not and if it falls, start tracking that particular view.
     *
     * @param firstVisibleItemPosition
     * @param lastVisibleItemPosition
     */
    private void analyzeAndAddViewData(int firstVisibleItemPosition, int lastVisibleItemPosition) {

        // Analyze all the views
        for (int viewPosition = firstVisibleItemPosition;
             viewPosition <= lastVisibleItemPosition; viewPosition++) {

            Log.i("View being considered", String.valueOf(viewPosition));

            // Get the view from its position.
            View itemView = recyclerView.getLayoutManager()
                    .findViewByPosition(viewPosition);

            // Check if the visibility of the view is more than or equal
            // to the threshold provided. If it falls under the desired limit, add it to the
            // tracking data.
            if (getVisibleHeightPercentage(itemView) >= minimumVisibleHeightThreshold) {

                positionOfViewsViewed.add(viewPosition);
            }
        }
    }

    /**
     * Method to calculate how much of the view is visible (i.e. within the screen)
     * wrt the view height.
     *
     * @param view
     * @return Percentage of the height visible.
     */
    private double getVisibleHeightPercentage(View view) {

        Rect itemRect = new Rect();
        view.getLocalVisibleRect(itemRect);

        double visibleHeight = itemRect.height();
        double totalHeightOfTheView = view.getMeasuredHeight();

        return ((visibleHeight/totalHeightOfTheView) * 100);
    }

    /**
     * Method to store the tracking data in an instance of "TrackingData" and
     * then returning that instance.
     * @param viewId
     * @param viewDuration in seconds.
     * @return
     */
    private TrackingData prepareTrackingData(String viewId,
                                             long viewDuration,
                                             double percentageHeightVisible) {

        TrackingData trackingData = new TrackingData();

        trackingData.setViewId(viewId);
        trackingData.setViewDuration(viewDuration);
        trackingData.setPercentageHeightVisible(percentageHeightVisible);

        return trackingData;
    }

    public interface TrailTrackingListener {

        /**
         * Method to dump data all the tracking data.
         */
        void trailDataDump(ArrayList<TrackingData> data);
    }

    /**
     * Class for builder pattern.
     */
    public static class Builder {

        private RecyclerView recyclerView;
        private long dataDumpInterval = 60000; // Default to 1 minute.
        private double minimumVisibleHeightThreshold = 60; // Default to 60 percent.
        private long minimumViewingTimeThreshold = 3000; // Default to 3 seconds.
        private boolean dumpDataAfterInterval = false;
        private TrailTrackingListener trailTrackingListener = null;

        /**
         * Interval after which the data should be handed over to the user.
         * @param dataDumpInterval
         * @return
         */
        public Builder setDataDumpInterval(long dataDumpInterval) {
            this.dataDumpInterval = dataDumpInterval;
            return this;
        }

        /**
         * @param recyclerView RecyclerView whose items are supposed to be tracked.
         * @return
         */
        public Builder setRecyclerView(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
            return this;
        }

        /**
         * The minimum amount of height of the list item that should be on the screen
         * for the tracking to start.
         * @param minimumVisibleHeightThreshold Value in percentage (should be between 0 - 100).
         * @return
         */
        public Builder setMinimumVisibleHeightThreshold(double minimumVisibleHeightThreshold) {
            this.minimumVisibleHeightThreshold = minimumVisibleHeightThreshold;
            return this;
        }

        /**
         * The minimum time user must spend on a view item for its tracking data to be considered.
         * @param minimumViewingTimeThreshold
         * @return
         */
        public Builder setMinimumViewingTimeThreshold(long minimumViewingTimeThreshold) {
            this.minimumViewingTimeThreshold = minimumViewingTimeThreshold;
            return this;
        }

        /**
         * Reference of the class implementing the TrailTrackingListener interface.
         * @param trailTrackingListener
         * @return
         */
        public Builder setTrailTrackingListener(TrailTrackingListener trailTrackingListener) {
            this.trailTrackingListener = trailTrackingListener;
            return this;
        }

        public Builder dumpDataAfterInterval(Boolean dumpDataAfterInterval) {
            this.dumpDataAfterInterval = dumpDataAfterInterval;
            return this;
        }

        public Trail build() {
            return new Trail(this);
        }
    }
}
