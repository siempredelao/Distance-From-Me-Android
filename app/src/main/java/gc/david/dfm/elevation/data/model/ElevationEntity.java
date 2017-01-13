package gc.david.dfm.elevation.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ElevationEntity {

    @SerializedName("error_message")
    @Expose
    private String       errorMessage;
    @Expose
    private List<Result> results;
    @Expose
    private String       status;

    private ElevationEntity(Builder builder) {
        errorMessage = builder.errorMessage;
        results = builder.results;
        status = builder.status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<Result> getResults() {
        return results;
    }

    public String getStatus() {
        return status;
    }

    public static final class Builder {
        private String       errorMessage;
        private List<Result> results;
        private String       status;

        public Builder() {
        }

        public Builder withErrorMessage(String val) {
            errorMessage = val;
            return this;
        }

        public Builder withResults(List<Result> val) {
            results = val;
            return this;
        }

        public Builder withStatus(String val) {
            status = val;
            return this;
        }

        public ElevationEntity build() {
            return new ElevationEntity(this);
        }
    }
}
