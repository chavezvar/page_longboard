package com.bonitasoft.custompage.longboard.connectortracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bonitasoft.engine.tracking.FlushEvent;
import org.bonitasoft.engine.tracking.FlushEventListener;
import org.bonitasoft.engine.tracking.FlushResult;
import org.bonitasoft.engine.tracking.Record;

public class ConnectorTrackRecordEvents implements FlushEventListener {

    /**
     * ATTENTION ATTENTION ATTENTION
     * Each custom page has its onw class loader. So, we must be sure to register a STATIC object, else one object load from a custom page
     * WILL NOT be accessible by the another custom page. This is the goal of the factory
     * * @see ConnectorTrackFactory
     */
    protected ConnectorTrackRecordEvents()
    {
    };

    // format is date, listOfRecord. Date is "20140432" in order to remove it.
    public HashMap<String, ArrayList<Record>> collectors = new HashMap<String, ArrayList<Record>>();

    public FlushResult flush(final FlushEvent flushEvent) throws Exception {
        final Logger logger = Logger.getLogger("org.bonitasoft");

        if (flushEvent.getRecords().size() == 0) {
            return new FlushResult(flushEvent);
        }

        logger.info("ConnectorTrackRecordEvents.TrackerAccess : _____________________________________" + this + " Receive a FlushEvent ["
                + flushEvent.getRecords().size() + "]");
        // keep all theses new event
        final List<Record> records = flushEvent.getRecords();

        for (final Record record : records)
        {
            logger.info("  record[" + record.getName() + "] duration [" + record.getDuration() + "]");
        }

        final Calendar currentDate = Calendar.getInstance();
        final String key = String.valueOf(currentDate.get(Calendar.YEAR)) + String.valueOf(currentDate.get(Calendar.DAY_OF_YEAR));
        ArrayList<Record> listOfDay = collectors.get(key);
        if (listOfDay == null) {
            listOfDay = new ArrayList<Record>();
        }
        listOfDay.addAll(records);
        collectors.put(key, listOfDay);
        if (collectors.size() > 1) {
            // purge old days
            collectors.clear();
            collectors.put(key, listOfDay);
        }

        logger.info("TrackerAccess :--------------------------  Collector [" + collectors.size() + "] Day[" + key + "] nbInDay[" + collectors.get(key).size()
                + "]");
        return new FlushResult(flushEvent);
    }

    /**
     * get all Records
     *
     * @return
     */
    public List<Record> getRecords() {
        final Logger logger = Logger.getLogger("org.bonitasoft");
        final Calendar currentDate = Calendar.getInstance();
        final String key = String.valueOf(currentDate.get(Calendar.YEAR)) + String.valueOf(currentDate.get(Calendar.DAY_OF_YEAR));
        final List<Record> listRecord = collectors.get(key);
        logger.info("ConnectorTrackRecordEvents _____________________________________: " + this + " Collector [" + collectors.size() + "] key[" + key
                + "] nbInDay["
                + (collectors.get(key) == null ? "null" : collectors.get(key).size()) + "]");

        return listRecord == null ? new ArrayList<Record>() : listRecord;
    }

    /**
     * clear all records to keep back memory
     */
    public void clear() {
        collectors.clear();
    }

}
