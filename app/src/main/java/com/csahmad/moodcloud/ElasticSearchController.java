package com.csahmad.moodcloud;

import android.os.AsyncTask;
import android.util.Log;
import com.searchly.jestdroid.DroidClientConfig;
import com.searchly.jestdroid.JestClientFactory;
import com.searchly.jestdroid.JestDroidClient;
import java.util.ArrayList;
import java.util.List;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

/**
 * Created by oahmad on 2017-03-06.
 */

public class ElasticSearchController {

    private static String url = "http://cmput301.softwareprocess.es:8080";
    private static String index = "CMPUT301W17T13";

    private static int resultSize = 25;

    /** For building and executing save commands and search queries. */
    private static JestDroidClient client;

    // AsyncTask<Params, Progress, Result>
    /**
     * For saving an object using elasticsearch.
     */
    public static class Add<T extends ElasticSearchObject> extends AsyncTask<T, Void, Void> {

        /**
         * Saves the given object(s) using elasticsearch.
         *
         * @param items the objects to save
         * @return null
         */
        @Override
        protected Void doInBackground(T... items) {

            ElasticSearchController.setClientIfNull();        // Set up client if it is null

            // Save each object
            for (T item: items) {

                Index index = new Index.Builder(item)
                        .index(ElasticSearchController.index)
                        .type(item.getTypeName())
                        .build();

                try {

                    DocumentResult result = ElasticSearchController.client.execute(index);

                    if (result.isSucceeded())
                        item.setId(result.getId());

                    else
                        Log.i("Error", "Elasticsearch was not able to add the object");
                }

                catch (Exception e) {
                    Log.i("Error", "The application failed to build and send the objects.");
                }
            }

            return null;
        }
    }

    // AsyncTask<Params, Progress, Result>
    /**
     * For searching for objects by keyword using elasticsearch.
     */
    public static class GetByKeyword<T extends ElasticSearchObject>
            extends AsyncTask<String, Void, ArrayList<T>> {

        private String fieldToSearch;
        private String typeName;
        private Class type;

        public String getFieldToSearch() {

            return this.fieldToSearch;
        }

        public void setFieldToSearch(String fieldToSearch) {

            this.fieldToSearch = fieldToSearch;
        }

        public String getTypeName() {

            return this.typeName;
        }

        public void setTypeName(String typeName) {

            this.typeName = typeName;
        }

        public Class getType() {

            return this.type;
        }

        public void setType(Class type) {

            this.type = type;
        }

        /**
         * Searches for objects using the given keywords.
         *
         * @param search_parameters the keywords to search by (currently only first used)
         * @return the objects containing the given keywords
         */
        @Override
        protected ArrayList<T> doInBackground(String... search_parameters) {

            if (this.fieldToSearch == null) {

                throw new IllegalStateException(
                        "Cannot call doInBackground without setting fieldToSearch.");
            }

            if (this.typeName == null) {

                throw new IllegalStateException(
                        "Cannot call doInBackground without setting typeName.");
            }

            if (this.type == null) {

                throw new IllegalStateException(
                        "Cannot call doInBackground without setting type.");
            }

            ElasticSearchController.setClientIfNull();        // Set up client if it is null

            // Will store results (objects with the given keywords)
            ArrayList<T> results = new ArrayList<T>();

            String fieldName = this.fieldToSearch;
            String query = "";
            String keywordString = search_parameters[0];

            // I
            if (keywordString.equals("")) {

                query = "{\n" +
                        "    \"size\" : " + ElasticSearchController.resultSize + ",\n" +
                        "    \"query\" : {\n" +
                        "        \"match_all\" : {}\n" +
                        "    }\n" +
                        "}";
            }

            // If keyword passed, make the query string (otherwise leave query as an empty string)
            //if (!keywordString.equals("")) {
            else {

                query = "{\n" +
                        "    \"size\" : " + ElasticSearchController.resultSize + ",\n" +
                        "    \"query\" : {\n" +
                        "        \"query_string\" : {\n" +
                        "            \"default_field\" : \"" + fieldName + "\",\n" +
                        "            \"query\" : \"" + keywordString + "\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}";
            }

            Search search = new Search.Builder(query)
                    .addIndex(ElasticSearchController.index)
                    .addType(this.typeName)
                    .build();

            try {

                // Get the results of the query:

                SearchResult result = client.execute(search);

                if (result.isSucceeded()) {
                    List<T> foundObjects = result.getSourceAsObjectList(this.type);
                    results.addAll(foundObjects);
                }

                else
                    Log.i("Error", "The search query failed to find any objects that matched " +
                            query);
            }

            catch (Exception e) {
                Log.i("Error", "Something went wrong when we tried to communicate with the" +
                        "elasticsearch server!");
            }

            return results;
        }
    }

    /** Set up client if it is null. */
    public static void setClientIfNull() {

        if (ElasticSearchController.client == null) {

            DroidClientConfig.Builder builder = new DroidClientConfig.Builder(
                    ElasticSearchController.url);

            DroidClientConfig config = builder.build();

            JestClientFactory factory = new JestClientFactory();
            factory.setDroidClientConfig(config);
            ElasticSearchController.client = (JestDroidClient) factory.getObject();
        }
    }
}