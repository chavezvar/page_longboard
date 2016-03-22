package com.bonitasoft.custompage.longboard.connectortracker;

/**
 * this class is the factory to get the different information from the tacker
 * 
 * @author pierre-yves
 */
public class ConnectorTrackFactory {

    private static ConnectorTrackFactory connectorTrackFactory = new ConnectorTrackFactory();

    private static ConnectorTrackRecordEvents connectorTrackRecordEvents = new ConnectorTrackRecordEvents();

    /**
     * get the instance of the factory
     * 
     * @return
     */
    public static ConnectorTrackFactory getInstance()
    {
        return connectorTrackFactory;
    }

    /**
     * get the instance of the connectorRecordEvent
     * 
     * @return
     */
    public ConnectorTrackRecordEvents getConnectorRecordEvent2()
    {
        return connectorTrackRecordEvents;
    }
}
