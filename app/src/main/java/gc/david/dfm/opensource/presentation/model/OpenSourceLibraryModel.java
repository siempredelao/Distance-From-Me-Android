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

package gc.david.dfm.opensource.presentation.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by david on 25.01.17.
 */
public class OpenSourceLibraryModel implements Parcelable {

    private final String name;
    private final String author;
    private final String version;
    private final String link;
    private final String license;
    private final String year;
    private final String description;

    public OpenSourceLibraryModel(final String name,
                                  final String author,
                                  final String version,
                                  final String link,
                                  final String license,
                                  final String year,
                                  final String description) {
        this.name = name;
        this.author = author;
        this.version = version;
        this.link = link;
        this.license = license;
        this.year = year;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public String getLink() {
        return link;
    }

    public String getLicense() {
        return license;
    }

    public String getYear() {
        return year;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.author);
        dest.writeString(this.version);
        dest.writeString(this.link);
        dest.writeString(this.license);
        dest.writeString(this.year);
        dest.writeString(this.description);
    }

    protected OpenSourceLibraryModel(Parcel in) {
        this.name = in.readString();
        this.author = in.readString();
        this.version = in.readString();
        this.link = in.readString();
        this.license = in.readString();
        this.year = in.readString();
        this.description = in.readString();
    }

    public static final Parcelable.Creator<OpenSourceLibraryModel> CREATOR = new Parcelable.Creator<OpenSourceLibraryModel>() {
        @Override
        public OpenSourceLibraryModel createFromParcel(Parcel source) {
            return new OpenSourceLibraryModel(source);
        }

        @Override
        public OpenSourceLibraryModel[] newArray(int size) {
            return new OpenSourceLibraryModel[size];
        }
    };
}
