/*
 * Copyright [2011-2016] "Neo Technology"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.neo4j.cineasts.movieimport;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;


public class MovieDbLocalStorage {

    protected ObjectMapper mapper;
    private String storagePath;

    public MovieDbLocalStorage(String storagePath) {
        this.storagePath = storagePath;
        ensureStorageDirectoryExists();
        mapper = new ObjectMapper();
    }

    private void ensureStorageDirectoryExists() {
        File storageDirectory = new File(storagePath);
        if (!storageDirectory.isDirectory()) {
            if (!storageDirectory.mkdirs()) {
                throw new RuntimeException("Failed to create storage dir");
            }
        }
    }

    public boolean hasMovie(String movieId) {
        return fileForMovie(movieId).exists();
    }

    private File fileForMovie(String movieId) {
        return new File(storagePath, String.format("movie_%s.json", movieId));
    }

    public Map loadMovie(String movieId) {
        File storageFile = fileForMovie(movieId);
        return loadJsonValue(storageFile);
    }

    private Map loadJsonValue(File storageFile) {
        try {
            final Object value = mapper.readValue(storageFile, Object.class);
            if (value instanceof List) {
                List list = (List) value;
                if (list.isEmpty() || list.get(0).equals("Nothing found.")) {
                    return Collections.singletonMap("not_found", System.currentTimeMillis());
                }
                return asMap(list.get(0));
            }
            return asMap(value);
        } catch (Exception e) {
            throw new MovieDbException("Failed to load JSON from storage for file " + storageFile.getPath(), e);
        }
    }

    private Map asMap(Object value) {
        if (value instanceof Map) {
            return (Map) value;
        }
        final String typeInformation = value == null ? "null" : value.getClass().getSimpleName();
        throw new MovieDbException("Wrong movie data format, expected Map/JSON-Object but was " + typeInformation);
    }

    public void storeMovie(String movieId, Object movieData) {
        File storageFile = fileForMovie(movieId);
        storeJsonValue(movieData, storageFile);
    }

    private void storeJsonValue(Object jsonData, File storageFile) {
        try {
            mapper.writeValue(storageFile, jsonData);
        } catch (Exception e) {
            throw new MovieDbException("Failed to store JSON to storage for file " + storageFile.getPath(), e);
        }
    }

    public boolean hasPerson(String personId) {
        return fileForPerson(personId).exists();
    }

    private File fileForPerson(String personId) {
        return new File(storagePath, String.format("person_%s.json", personId));
    }

    public Map loadPerson(String personId) {
        File storageFile = fileForPerson(personId);
        return loadJsonValue(storageFile);
    }

    public void storePerson(String personId, Object personJson) {
        File storageFile = fileForPerson(personId);
        storeJsonValue(personJson, storageFile);
    }
}
