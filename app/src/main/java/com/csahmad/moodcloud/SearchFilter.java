package com.csahmad.moodcloud;

import java.util.ArrayList;

/**
 * Defines restrictions on elasticsearch queries.
 *
 * @see ElasticSearch
 */
public class SearchFilter {

    private ArrayList<String> keywords;
    private ArrayList<String> keywordFields;

    private ArrayList<FieldValue> fieldValues;

    private Integer maxTimeUnitsAgo;
    private String timeUnits = "w";
    private String dateField = "date";

    private Double maxDistance;
    private SimpleLocation location;
    private String distanceUnits = "km";
    private String locationField = "location";

    // Results must not have an empty list for any of these fields
    private ArrayList<String> nonEmptyFields;

    public boolean hasRestrictions() {

        return !NullTools.allNullOrEmpty(this.keywords, this.fieldValues, this.maxTimeUnitsAgo,
                this.maxDistance, nonEmptyFields);
    }

    public boolean hasNonEmptyFields() {

        return !NullTools.allNullOrEmpty(this.nonEmptyFields);
    }

    public boolean hasKeywords() {

        return !NullTools.allNullOrEmpty(this.keywords);
    }

    public boolean hasTimeUnitsAgo() {

        return !NullTools.allNullOrEmpty(this.maxTimeUnitsAgo);
    }

    public boolean hasMaxDistance() {

        return !NullTools.allNullOrEmpty(this.maxDistance);
    }

    public ArrayList<String> getNonEmptyFields() {

        return this.nonEmptyFields;
    }

    public SearchFilter setNonEmptyFields(ArrayList<String> fields) {

        this.nonEmptyFields = fields;
        return this;
    }

    public ArrayList<String> getKeywords() {
        
        return this.keywords;
    }

    public SearchFilter setKeywords(ArrayList<String> keywords) {
        
        this.keywords = keywords;
        return this;
    }

    public SearchFilter addFieldValue(FieldValue fieldValue) {

        if (this.fieldValues == null) {
            this.fieldValues = new ArrayList<FieldValue>();
        }

        this.fieldValues.add(fieldValue);
        return this;
    }

    public SearchFilter addNonEmptyField(String field) {

        if (this.nonEmptyFields == null) {
            this.nonEmptyFields = new ArrayList<String>();
        }

        this.nonEmptyFields.add(field);
        return this;
    }

    public ArrayList<FieldValue> getFieldValues() {

        return this.fieldValues;
    }

    public SearchFilter setFieldValues(ArrayList<FieldValue> fieldValues) {

        this.fieldValues = fieldValues;
        return this;
    }

    public boolean hasFieldValues() {

        return this.fieldValues != null;
    }

    public ArrayList<String> getKeywordFields() {
        
        return this.keywordFields;
    }

    public SearchFilter setKeywordFields(ArrayList<String> keywordFields) {
        
        this.keywordFields = keywordFields;
        return this;
    }

    public Integer getMaxTimeUnitsAgo() {
        
        return this.maxTimeUnitsAgo;
    }

    public SearchFilter setMaxTimeUnitsAgo(Integer maxTimeUnitsAgo) {
        
        this.maxTimeUnitsAgo = maxTimeUnitsAgo;
        return this;
    }

    public String getTimeUnits() {

        return this.timeUnits;
    }

    public SearchFilter setTimeUnits(String timeUnits) {

        this.timeUnits = timeUnits;
        return this;
    }

    public String getDateField() {
        
        return this.dateField;
    }

    public SearchFilter setDateField(String dateField) {
        
        this.dateField = dateField;
        return this;
    }

    public Double getMaxDistance() {
        
        return this.maxDistance;
    }

    public SearchFilter setMaxDistance(Double maxDistance) {
        
        this.maxDistance = maxDistance;
        return this;
    }

    public SimpleLocation getLocation() {
        
        return this.location;
    }

    public SearchFilter setLocation(SimpleLocation location) {
        
        this.location = location;
        return this;
    }

    public String getDistanceUnits() {

        return this.distanceUnits;
    }

    public void setDistanceUnits(String distanceUnits) {

        this.distanceUnits = distanceUnits;
    }

    public String getLocationField() {
        
        return this.locationField;
    }

    public SearchFilter setLocationField(String locationField) {
        
        this.locationField = locationField;
        return this;
    }
}