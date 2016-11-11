package org.orienteer.inclogger.client;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authorization.UnauthorizedActionException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Exceptions;
import org.orienteer.inclogger.IncidentLogger;
import org.orienteer.inclogger.core.interfaces.ILogger;

import com.orientechnologies.orient.core.exception.OSchemaException;
import com.orientechnologies.orient.core.exception.OSecurityException;
import com.orientechnologies.orient.core.exception.OValidationException;

/**
 * Wicket {@link IRequestCycleListener} for handle exceptions to {@link IncidentLogger}}
 *
 */
public class OIncidentExceptionListener extends AbstractRequestCycleListener{

	@Override
	public IRequestHandler onException(RequestCycle cycle, Exception ex) {
		Throwable th = null;
		if((th=Exceptions.findCause(ex, OSecurityException.class))!=null
				|| (th=Exceptions.findCause(ex, OValidationException.class))!=null
				|| (th=Exceptions.findCause(ex, OSchemaException.class))!=null
				|| (th=Exceptions.findCause(ex, UnauthorizedActionException.class))!=null
				|| (th=Exceptions.findCause(ex, IllegalStateException.class))!=null && Exceptions.findCause(ex, WicketRuntimeException.class)==null)
		{
			return null;
		}
		else
		{
			ILogger logger = IncidentLogger.get().makeLogger();
			logger.getData().set("type", "exception");//later, can make interface method logger.setType(type)
			logger.incident(ex);
			return null;
		}
	}
	

}
