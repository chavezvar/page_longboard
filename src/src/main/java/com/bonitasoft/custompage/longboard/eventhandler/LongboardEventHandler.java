package com.bonitasoft.custompage.longboard.eventhandler;

import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SNamedElement;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateCatchEventInstance;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.events.model.SHandlerExecutionException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;

public class LongboardEventHandler implements SHandler<SEvent> {

    /** Bonita technical logger */
    private final TechnicalLoggerService technicalLog;

    private long tenantId;

    public LongboardEventHandler(final TechnicalLoggerService technicalLog, final long tenantId) {
        this.technicalLog = technicalLog;
        this.tenantId = tenantId;
    }

    private TenantServiceAccessor getTenantServiceAccessor() throws SHandlerExecutionException {
        try {

            ServiceAccessorFactory serviceAccessorFactory = ServiceAccessorFactory.getInstance();
            return serviceAccessorFactory.createTenantServiceAccessor(tenantId);
        } catch (Exception e) {
            throw new SHandlerExecutionException(e.getMessage(), null);
        }
    }

    public void execute(SEvent event) throws SHandlerExecutionException {
        // public void execute(SEvent event) throws SHandlerExecutionException {

        try {
            Object eventObject = event.getObject();
            if (eventObject instanceof SHumanTaskInstance) {
                SHumanTaskInstance humanTaskInstance = (SHumanTaskInstance) eventObject;

                if (humanTaskInstance.getStateName() == ActivityStates.READY_STATE) {
                    TenantServiceAccessor tenantServiceAccessor;

                } else if (humanTaskInstance.getStateName() == ActivityStates.COMPLETED_STATE) {
                    Long executionTime;

                    SUser assignee = getTenantServiceAccessor().getIdentityService().getUser(humanTaskInstance.getAssigneeId());

                }

            } else if (eventObject instanceof SIntermediateCatchEventInstance) {
            }
        } catch (SBonitaException e) {
            throw new SHandlerExecutionException(e.getMessage(), e);
        } catch (Exception ex) {
            throw new SHandlerExecutionException(ex.getMessage(), null);
        } finally {

        }
    }

    public boolean isInterested(SEvent event) {
        boolean isInterested = false;

        // Get the object associated with the event
        Object eventObject = event.getObject();
        if (eventObject instanceof SNamedElement) {
        }
        // Check that event is related to a task
        if (eventObject instanceof SHumanTaskInstance) {
            isInterested = true;
        } else if (eventObject instanceof SIntermediateCatchEventInstance) {
            isInterested = true;

        }

        return isInterested;
    }

    public String getIdentifier() {
        return "LongBoard Handler";
    }

}
