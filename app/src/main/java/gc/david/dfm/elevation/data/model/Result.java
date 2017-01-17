/*
 * Copyright (c) 2017 David Aguiar Gonzalez
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

public class Result {

    private final double   elevation;
    private final Location location;
    private final double   resolution;

    private Result(Builder builder) {
        elevation = builder.elevation;
        location = builder.location;
        resolution = builder.resolution;
    }

    public double getElevation() {
        return elevation;
    }

    public static final class Builder {
        private double   elevation;
        private Location location;
        private double   resolution;

        public Builder() {
        }

        public Builder withElevation(double val) {
            elevation = val;
            return this;
        }

        public Builder withLocation(Location val) {
            location = val;
            return this;
        }

        public Builder withResolution(double val) {
            resolution = val;
            return this;
        }

        public Result build() {
            return new Result(this);
        }
    }
}
