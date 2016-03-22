package com.bonitasoft.custompage.longboard.qualification;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class QualificationGraphDisplay {

    public final static String loggerName = "com.bonitasoft.custompage.longboard.qualification";

    public static String getSampleBarChart(String title, List<QualificationPlatform.Sample> listSamples) {
        Logger logger = Logger.getLogger(loggerName);

        /**
         * structure
         * "rows": [
         * {
         * c: [
         * { "v": "January" },"
         * { "v": 19,"f": "42 items" },
         * { "v": 12,"f": "Ony 12 items" },
         * ]
         * },
         * {
         * c: [
         * { "v": "January" },"
         * { "v": 19,"f": "42 items" },
         * { "v": 12,"f": "Ony 12 items" },
         * ]
         * },
         */
        String resultValue = "";
        String resultLabel = "";
        resultLabel += "{ \"id\": \"perf\", \"label\": \"Perf\", \"type\": \"string\" }, "
                + "{ \"id\": \"perfbase\", \"label\": \"ValueBase\", \"type\": \"number\" },"
                + "{ \"id\": \"perfvalue\", \"label\": \"Value\", \"type\": \"number\" },";

        for (int i = 0; i < listSamples.size(); i++)
        {
            resultValue += "{ \"c\": [ { \"v\": \"" + listSamples.get(i).label + "\" },"
                    + " { \"v\": " + listSamples.get(i).baseInMs + " }, "
                    + " { \"v\": " + listSamples.get(i).timeInMs + " } "
                    + "] },";
        }
        resultValue = resultValue.substring(0, resultValue.length() - 1);
        resultLabel = resultLabel.substring(0, resultLabel.length() - 1);

        String valueChart = "	{"
                + "\"type\": \"BarChart\", "
                + "\"displayed\": true, "
                + "\"data\": {"
                + "\"cols\": [" + resultLabel + "], "
                + "\"rows\": [" + resultValue + "]}, "
                + "\"options\": { "
                + "\"bars\": \"horizontal\","
                + "\"title\": \"" + title + "\", \"fill\": 20, \"displayExactValues\": true,"
                + "\"vAxis\": { \"title\": \"ms\", \"gridlines\": { \"count\": 100 } }"
                + "}"
                + "}";
        // 				+"\"isStacked\": \"true\","

        //		    +"\"displayExactValues\": true,"
        //		    
        //		    +"\"hAxis\": { \"title\": \"Date\" }"
        //		    +"},"
        // logger.info("Value1 >"+valueChart+"<");

        return valueChart;
    }

    /*
     * valueChart ="	{"
     * +"\"type\": \"ColumnChart\", \"displayed\": true,"
     * +"\"data\": {"
     * +"\"cols\": ["
     * + "{      \"id\": \"month\",      \"label\": \"Month\",   \"type\": \"string\",    \"p\": {}      },"
     * + "{      \"id\": \"laptop-id\",   \"label\": \"Laptop\",      \"type\": \"number\",      \"p\": {}     },"
     * + "{   \"id\": \"desktop-id\", \"label\": \"Desktop\",\"type\": \"number\", \"p\": {}			      },"
     * + "{   \"id\": \"server-id\",  \"label\": \"Server\", \"type\": \"number\", \"p\": {}   },"
     * + "{   \"id\": \"cost-id\",    \"label\": \"Shipping\", \"type\": \"number\" }"
     * +"],"
     * +"\"rows\": ["
     * +"{  \"c\": [ {"
     * +"\"v\": \"January\""
     * +"},"
     * +"{"
     * +"							            \"v\": 19,"
     * +"\"f\": \"42 items\""
     * +"},"
     * +"{"
     * +" \"v\": 12,"
     * +"\"f\": \"Ony 12 items\""
     * +"},"
     * +"{"
     * +"\"v\": 7,"
     * +"\"f\": \"7 servers\""
     * +"},"
     * +"{"
     * +"\"v\": 4"
     * +"}"
     * +"]"
     * +"},"
     * +"{"
     * +"\"c\": ["
     * +"{"
     * +"\"v\": \"February\""
     * +"},"
     * +"{"
     * +"\"v\": 13"
     * +"},"
     * +"{"
     * +"\"v\": 1,"
     * +"\"f\": \"1 unit (Out of stock this month)\""
     * +"},"
     * +"{"
     * +"\"v\": 12"
     * +"},"
     * +"{"
     * +"\"v\": 2"
     * +"}"
     * +"]"
     * +"},"
     * +"{"
     * +"\"c\": ["
     * +"{"
     * +"\"v\": \"March\""
     * +"},"
     * +"{"
     * +"\"v\": 24"
     * +"},"
     * +"{"
     * +"\"v\": 5"
     * +"},"
     * +"{"
     * +"\"v\": 11"
     * +"},"
     * +"{"
     * +"\"v\": 6"
     * +"}"
     * +"  ]"
     * +"}"
     * +"]"
     * +"},"
     * +"\"options\": {"
     * +"\"title\": \"Sales per month\","
     * +"\"isStacked\": \"true\","
     * +"\"fill\": 20,"
     * +"\"displayExactValues\": true,"
     * +"\"vAxis\": {"
     * +"\"title\": \"Sales unit\","
     * +"\"gridlines\": {"
     * +"\"count\": 10"
     * +"      }"
     * +"},"
     * +"\"hAxis\": {"
     * +"\"title\": \"Date\""
     * +"				    }"
     * +"},"
     * +"  \"formatters\": {}"
     * +"}";
     */
    // result in a chart
    /*
     * String valueChart = "	{"
     * +"\"type\": \"ColumnChart\","
     * +"							  \"displayed\": true,"
     * +"\"data\": {"
     * +"\"cols\": ["
     * +"{      \"id\": \"month\",      \"label\": \"Month\",   \"type\": \"string\",    \"p\": {}      },"
     * +"{      \"id\": \"laptop-id\",   \"label\": \"Laptop\",      \"type\": \"number\",      \"p\": {}     },"
     * +"{"
     * +"\"id\": \"desktop-id\","
     * +"\"label\": \"Desktop\","
     * +"\"type\": \"number\","
     * +"\"p\": {}"
     * +"							      },"
     * +"{"
     * +"\"id\": \"server-id\","
     * +"\"label\": \"Server\","
     * +"\"type\": \"number\","
     * +"\"p\": {}"
     * +"},"
     * +"{"
     * +"\"id\": \"cost-id\","
     * +"\"label\": \"Shipping\","
     * +"\"type\": \"number\""
     * +"}"
     * +"],"
     * +"\"rows\": ["
     * +"{"
     * +"\"c\": ["
     * +"{"
     * +"\"v\": \"January\""
     * +"},"
     * +"{"
     * +"							            \"v\": 19,"
     * +"\"f\": \"42 items\""
     * +"},"
     * +"{"
     * +" \"v\": 12,"
     * +"\"f\": \"Ony 12 items\""
     * +"},"
     * +"{"
     * +"\"v\": 7,"
     * +"\"f\": \"7 servers\""
     * +"},"
     * +"{"
     * +"\"v\": 4"
     * +"}"
     * +"]"
     * +"},"
     * +"{"
     * +"\"c\": ["
     * +"{"
     * +"\"v\": \"February\""
     * +"},"
     * +"{"
     * +"\"v\": 13"
     * +"},"
     * +"{"
     * +"\"v\": 1,"
     * +"\"f\": \"1 unit (Out of stock this month)\""
     * +"},"
     * +"{"
     * +"\"v\": 12"
     * +"},"
     * +"{"
     * +"\"v\": 2"
     * +"}"
     * +"]"
     * +"},"
     * +"{"
     * +"\"c\": ["
     * +"{"
     * +"\"v\": \"March\""
     * +"},"
     * +"{"
     * +"\"v\": 24"
     * +"},"
     * +"{"
     * +"\"v\": 5"
     * +"},"
     * +"{"
     * +"\"v\": 11"
     * +"},"
     * +"{"
     * +"\"v\": 6"
     * +"}"
     * +"  ]"
     * +"}"
     * +"]"
     * +"},"
     * +"\"options\": {"
     * +"\"title\": \"Sales per month\","
     * +"\"isStacked\": \"true\","
     * +"\"fill\": 20,"
     * +"\"displayExactValues\": true,"
     * +"\"vAxis\": {"
     * +"\"title\": \"Sales unit\","
     * +"\"gridlines\": {"
     * +"\"count\": 10"
     * +"      }"
     * +"},"
     * +"\"hAxis\": {"
     * +"\"title\": \"Date\""
     * +"				    }"
     * +"},"
     * +"  \"formatters\": {}"
     * +"}";
     */

    /*
     * "type": "AreaChart",
     * "displayed": true,
     * "data": {
     * "cols": [
     * {
     * "id": "month",
     * "label": "Month",
     * "type": "string",
     * "p": {}
     * },
     * {
     * "id": "laptop-id",
     * "label": "Laptop",
     * "type": "number",
     * "p": {}
     * },
     * {
     * "id": "desktop-id",
     * "label": "Desktop",
     * "type": "number",
     * "p": {}
     * },
     * {
     * "id": "server-id",
     * "label": "Server",
     * "type": "number",
     * "p": {}
     * },
     * {
     * "id": "cost-id",
     * "label": "Shipping",
     * "type": "number"
     * }
     * ],
     * "rows": [
     * {
     * "c": [
     * {
     * "v": "January"
     * },
     * {
     * "v": 19,
     * "f": "42 items"
     * },
     * {
     * "v": 12,
     * "f": "Ony 12 items"
     * },
     * {
     * "v": 7,
     * "f": "7 servers"
     * },
     * {
     * "v": 4
     * }
     * ]
     * },
     * {
     * "c": [
     * {
     * "v": "February"
     * },
     * {
     * "v": 13
     * },
     * {
     * "v": 1,
     * "f": "1 unit (Out of stock this month)"
     * },
     * {
     * "v": 12
     * },
     * {
     * "v": 2
     * }
     * ]
     * },
     * {
     * "c": [
     * {
     * "v": "March"
     * },
     * {
     * "v": 24
     * },
     * {
     * "v": 5
     * },
     * {
     * "v": 11
     * },
     * {
     * "v": 6
     * }
     * ]
     * }
     * ]
     * },
     * "options": {
     * "title": "Sales per month",
     * "isStacked": "true",
     * "fill": 20,
     * "displayExactValues": true,
     * "vAxis": {
     * "title": "Sales unit",
     * "gridlines": {
     * "count": 10
     * }
     * },
     * "hAxis": {
     * "title": "Date"
     * }
     * },
     * "formatters": {}
     * }
     */

}
