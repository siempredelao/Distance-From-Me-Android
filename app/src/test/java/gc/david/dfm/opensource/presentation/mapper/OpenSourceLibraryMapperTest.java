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

package gc.david.dfm.opensource.presentation.mapper;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity;
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryModel;

import static org.junit.Assert.assertEquals;

/**
 * Created by david on 26.01.17.
 */
public class OpenSourceLibraryMapperTest {
    @Test
    public void transformsOpenSourceLibraryEntityToOpenSourceLibraryModel() {
        // Given
        final String libraryName = "fake name";
        final String libraryAuthor = "fake author";
        final String libraryVersion = "fake version";
        final String libraryLink = "fake link";
        final String libraryLicense = "fake license";
        final String libraryYear = "fake year";
        final String libraryDescription = "fake description";
        OpenSourceLibraryEntity openSourceLibraryEntity = new OpenSourceLibraryEntity(libraryName,
                                                                                      libraryAuthor,
                                                                                      libraryVersion,
                                                                                      libraryLink,
                                                                                      libraryLicense,
                                                                                      libraryYear,
                                                                                      libraryDescription);

        List<OpenSourceLibraryEntity> openSourceLibraryEntityList = new ArrayList<>();
        openSourceLibraryEntityList.add(openSourceLibraryEntity);

        // When
        List<OpenSourceLibraryModel> openSourceLibraryModelList = new OpenSourceLibraryMapper().transform(
                openSourceLibraryEntityList);

        // Then
        assertEquals(1, openSourceLibraryModelList.size());
        final OpenSourceLibraryModel openSourceLibraryModel = openSourceLibraryModelList.get(0);
        assertEquals(libraryName, openSourceLibraryModel.getName());
        assertEquals(libraryAuthor, openSourceLibraryModel.getAuthor());
        assertEquals(libraryVersion, openSourceLibraryModel.getVersion());
        assertEquals(libraryLink, openSourceLibraryModel.getLink());
        assertEquals(libraryLicense, openSourceLibraryModel.getLicense());
        assertEquals(libraryYear, openSourceLibraryModel.getYear());
        assertEquals(libraryDescription, openSourceLibraryModel.getDescription());
    }

}