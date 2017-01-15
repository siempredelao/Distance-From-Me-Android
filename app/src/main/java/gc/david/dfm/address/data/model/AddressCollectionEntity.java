package gc.david.dfm.address.data.model;

import java.util.List;

public class AddressCollectionEntity {

    private List<Result> results;
    private String       status;

    private AddressCollectionEntity(Builder builder) {
        results = builder.results;
        status = builder.status;
    }

    public List<Result> getResults() {
        return results;
    }

    public String getStatus() {
        return status;
    }

    public static final class Builder {
        private List<Result> results;
        private String       status;

        public Builder() {
        }

        public Builder withResults(List<Result> val) {
            results = val;
            return this;
        }

        public Builder withStatus(String val) {
            status = val;
            return this;
        }

        public AddressCollectionEntity build() {
            return new AddressCollectionEntity(this);
        }
    }
}
