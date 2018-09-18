/*
 * Copyright (c) 2018 David Aguiar Gonzalez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
