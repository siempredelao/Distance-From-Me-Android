package gc.david.dfm.elevation.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ElevationEntity {

    @SerializedName("error_message")
    private final String       errorMessage;
    private final List<Result> results;
    private final String       status;

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
