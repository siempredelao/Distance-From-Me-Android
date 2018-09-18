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

package gc.david.dfm.model;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END

/**
 * Entity mapped to table "POSITION".
 */
@Entity
public class Position {

    @Id(autoincrement = true)
    private Long id;
    private double latitude;
    private double longitude;
    private long distanceId;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public Position() {
    }

    public Position(Long id) {
        this.id = id;
    }

    @Generated
    public Position(Long id, double latitude, double longitude, long distanceId) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceId = distanceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getDistanceId() {
        return distanceId;
    }

    public void setDistanceId(long distanceId) {
        this.distanceId = distanceId;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
