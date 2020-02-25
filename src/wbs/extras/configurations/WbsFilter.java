package wbs.extras.configurations;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public class WbsFilter extends AbstractFilter {
	
	public void addIgnoreString(String ignore) {
		filterStrings.add(ignore);
	}
	
	private List<String> filterStrings = new LinkedList<>();
			
	private Result isAllowed(String message) {

		String messageString = message.toLowerCase();
		for (String filterString : filterStrings) {
			if (messageString.contains(filterString.toLowerCase())) {
	        	return Filter.Result.DENY;
			}
		}
        return Filter.Result.NEUTRAL;
	}
	
	@Override
    public Result filter(LogEvent event) {
		return event == null ? Result.NEUTRAL : isAllowed(event.getMessage().getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return isAllowed(msg.getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return isAllowed(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return msg == null ? Result.NEUTRAL : isAllowed(msg.toString());
    }
}
