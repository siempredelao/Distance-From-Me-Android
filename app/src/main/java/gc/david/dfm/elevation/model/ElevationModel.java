package gc.david.dfm.elevation.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ElevationModel {

    @SerializedName("error_message")
    @Expose
    private String       errorMessage;
    @Expose
    private List<Result> results;
    @Expose
    private String       status;

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<Result> getResults() {
        return results;
    }

    public String getStatus() {
        return status;
    }
}
