/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.events;

import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;

/**
 * @author Tomas Muller
 */
public abstract class FilterBoxBackend<T extends FilterRpcRequest> extends EventAction<T, FilterRpcResponse> {

	@Override
	public FilterRpcResponse execute(T request, EventContext context) {
		
		FilterRpcResponse response = new FilterRpcResponse();
		
		if (context.isAuthenticated())
			request.setOption("user", context.getUser().getExternalUserId());
		
		switch (request.getCommand()) {
			case LOAD:
				load(request, response, context);
				break;
			case SUGGESTIONS:
				suggestions(request, response, context);
				break;
			case ENUMERATE:
				enumarate(request, response, context);
				break;
		}
		
		return response;
	}
	
	public abstract void load(T request, FilterRpcResponse response, EventContext context);
	
	public abstract void suggestions(T request, FilterRpcResponse response, EventContext context);
	
	public abstract void enumarate(T request, FilterRpcResponse response, EventContext context);	

}
