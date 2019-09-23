package com.bonitasoft.custompage.longboard.connectortracker;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.bonitasoft.custompage.longboard.connectortracker.TrackerAccess.StatsTracker;

// see http://bouil.github.io/angular-google-chart/#/generic/ColumnChart

public class TrackRangeGraph {

    /**
     * @param listLabels
     * @param listValues
     * @return
     */

    public static String getGraphRange(final String title, boolean showDuration, boolean showMaximum, final List<StatsTracker> listRange) {

        /**
         * structure "rows": [ { c: [ { "v": "January" }," { "v": 19,"f": "42
         * items" }, { "v": 12,"f": "Ony 12 items" }, ] }, { c: [ { "v":
         * "January" }," { "v": 19,"f": "42 items" }, { "v": 12,"f": "Ony 12
         * items" }, ] },
         */
        if (!showDuration && !showMaximum)
            showDuration = true;

        String resultValue = "";

        for (int i = 0; i < listRange.size(); i++) {
            resultValue += "{\"c\":[{\"v\":\"" + listRange.get(i).titleGraph + "\"}";
            if (showDuration)
                resultValue += ", {\"v\": " + listRange.get(i).getAvgDuration() + "}";
            if (showMaximum)
                resultValue += ", {\"v\": " + listRange.get(i).maxDurationMs + "} ";
            resultValue += "]},";
        }
        if (resultValue.length() > 0) {
            resultValue = resultValue.substring(0, resultValue.length() - 1);
        }

        String resultLabel = "{ \"type\": \"string\", \"id\": \"whattime\", \"label\":\"whattime\" }";
        if (showDuration)
            resultLabel += ", { \"type\": \"number\", \"id\": \"value\", \"label\":\"Duration\" }";
        if (showMaximum)
            resultLabel += ", { \"type\": \"number\", \"id\": \"value\", \"label\":\"Maximum\" }";

        final String valueChart = "	{" + "\"type\": \"ColumnChart\", " + "\"displayed\": true, " + "\"data\": {" + "\"cols\": [" + resultLabel + "], " + "\"rows\": [" + resultValue + "] "
        /*
         * + "\"options\": { " + "\"bars\": \"horizontal\"," + "\"title\": \""
         * +title+"\", \"fill\": 20, \"displayExactValues\": true," +
         * "\"vAxis\": { \"title\": \"ms\", \"gridlines\": { \"count\": 100 } }"
         */
                + "}" + "}";
        // +"\"isStacked\": \"true\","

        // +"\"displayExactValues\": true,"
        //
        // +"\"hAxis\": { \"title\": \"Date\" }"
        // +"},"
        // logger.info("TrackRangeChart >>"+ valueChart+"<<");
        // String valueChartBar="{\"type\": \"BarChart\", \"displayed\": true,
        // \"data\": {\"cols\": [{ \"id\": \"perf\", \"label\": \"Perf\",
        // \"type\": \"string\" }, { \"id\": \"perfbase\", \"label\":
        // \"ValueBase\", \"type\": \"number\" },{ \"id\": \"perfvalue\",
        // \"label\": \"Value\", \"type\": \"number\" }], \"rows\": [{ \"c\": [
        // { \"v\": \"Write BonitaHome\" }, { \"v\": 550 }, { \"v\": 615 } ] },{
        // \"c\": [ { \"v\": \"Read BonitaHome\" }, { \"v\": 200 }, { \"v\": 246
        // } ] },{ \"c\": [ { \"v\": \"Read Medata\" }, { \"v\": 370 }, { \"v\":
        // 436 } ] },{ \"c\": [ { \"v\": \"Sql Request\" }, { \"v\": 190 }, {
        // \"v\": 213 } ] },{ \"c\": [ { \"v\": \"Deploy process\" }, { \"v\":
        // 40 }, { \"v\": 107 } ] },{ \"c\": [ { \"v\": \"Create 100 cases\" },
        // { \"v\": 3600 }, { \"v\": 16382 } ] },{ \"c\": [ { \"v\": \"Process
        // 100 cases\" }, { \"v\": 3700 }, { \"v\": 16469 } ] }]}, \"options\":
        // { \"bars\": \"horizontal\",\"title\": \"Performance Measure\",
        // \"fill\": 20, \"displayExactValues\": true,\"vAxis\": { \"title\":
        // \"ms\", \"gridlines\": { \"count\": 100 } }}}";

        return valueChart;
    }

    /**
     * ----------------------------------------------------------------
     * getGraphRepartition
     *
     * @return
     */
    public static String getGraphRepartition(final String title, final HashMap<String, StatsTracker> listRange) {
        final Logger logger = Logger.getLogger("org.bonitasoft");

        /**
         * structure "rows": [ { c: [ { "v": "January" }," { "v": 19,"f": "42
         * items" }, { "v": 12,"f": "Ony 12 items" }, ] }, { c: [ { "v":
         * "January" }," { "v": 19,"f": "42 items" }, { "v": 12,"f": "Ony 12
         * items" }, ] },
         */
        String resultValue = "";
        // logger.info("Graph["+title+"] ListRange.size() ["+listRange.size()+"]
        // value="+listRange);

        for (final StatsTracker statsTracker : listRange.values()) {
            resultValue += "{\"c\":[{\"v\":\"" + statsTracker.titleGraph + "\"},{\"v\": " + statsTracker.getAvgDuration();
            if (statsTracker.getLabel() != null) {
                resultValue += ",\"f\":\"" + statsTracker.getLabel() + "\"";
            }
            resultValue += "} ]},";

        }
        if (resultValue.length() > 0) {
            resultValue = resultValue.substring(0, resultValue.length() - 1);
        }

        final String resultLabel = "{ \"type\": \"string\", \"id\": \"connector\", \"label\":\"connector\" }," + "{ \"type\": \"number\", \"id\": \"value\", \"label\":\"Duration\" }";

        final String valueChart = "	{" + "\"type\": \"PieChart\", " + "\"displayed\": true, " + "\"data\": {" + "\"cols\": [" + resultLabel + "], " + "\"rows\": [" + resultValue + "] " + "}, " + "\"options\": { " + "\"pieHole\": \"0.4\"," + "\"title\": \"" + title + "\"," + "\"is3D\": \"true\" "
                + "}" + "} ";
        // +"\"isStacked\": \"true\","

        // +"\"displayExactValues\": true,"
        //
        // +"\"hAxis\": { \"title\": \"Date\" }"
        // +"},"
        // logger.info("TrackRangeChart >>"+ valueChart+"<<");

        return valueChart;
    }

}
