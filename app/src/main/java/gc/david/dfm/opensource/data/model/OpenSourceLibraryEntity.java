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

package gc.david.dfm.opensource.data.model;

/**
 * Created by david on 25.01.17.
 */
public class OpenSourceLibraryEntity {

    private final String name;
    private final String author;
    private final String version;
    private final String link;
    private final String licenseCode;
    private final String licenseYear;
    private final String description;

    public OpenSourceLibraryEntity(final String name,
                                   final String author,
                                   final String version,
                                   final String link,
                                   final String licenseCode,
                                   final String licenseYear,
                                   final String description) {
        this.name = name;
        this.author = author;
        this.version = version;
        this.link = link;
        this.licenseCode = licenseCode;
        this.licenseYear = licenseYear;
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

    public String getLicenseCode() {
        return licenseCode;
    }

    public String getLicenseYear() {
        return licenseYear;
    }

    public String getDescription() {
        return description;
    }
}
