package krtkush.github.io.trail;

/**
 * Created by kartikeykushwaha on 07/04/17.
 */

public class TrackingData {

    // Duration for which the view has been viewed.
    private long viewDuration;

    // Unique Id for the view that was viewed.
    private String viewId;

    // Percentage of height visible
    private double percentageHeightVisible;

    public double getPercentageHeightVisible() {
        return percentageHeightVisible;
    }

    public void setPercentageHeightVisible(double percentageHeightVisible) {
        this.percentageHeightVisible = percentageHeightVisible;
    }

    public long getViewDuration() {
        return viewDuration;
    }

    public void setViewDuration(long viewDuration) {
        this.viewDuration = viewDuration;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }
}
