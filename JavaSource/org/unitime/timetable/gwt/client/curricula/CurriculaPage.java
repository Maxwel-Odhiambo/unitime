/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.curricula;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.Client.GwtPageChangeEvent;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.curricula.CurriculumEdit.EditFinishedEvent;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicAreaInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.DepartmentInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Tomas Muller
 */
public class CurriculaPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);

	private CurriculumFilterBox iFilter = null;
	private AriaButton iSearch = null;
	private AriaButton iNew = null;
	private AriaButton iPrint = null;
	private CurriculaTable iCurriculaTable = null;
	
	private VerticalPanel iCurriculaPanel = null;
	
	private SimplePanel iPanel = null;
	private HorizontalPanel iFilterPanel = null;
	
	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);
	
	private CurriculumEdit iCurriculumPanel = null;
	private ClassificationsEdit iClassificationsEdit = null;
	
	public CurriculaPage() {
		iPanel = new SimplePanel();
		
		iCurriculaPanel = new VerticalPanel();
		
		iFilterPanel = new HorizontalPanel();
		iFilterPanel.setSpacing(3);
		
		Label filterLabel = new Label(MESSAGES.propFilter());
		iFilterPanel.add(filterLabel);
		iFilterPanel.setCellVerticalAlignment(filterLabel, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iFilter = new CurriculumFilterBox();
		iFilterPanel.add(iFilter);
		
		iSearch = new AriaButton(MESSAGES.buttonSearch());
		iSearch.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iSearch);		
		
		iPrint = new AriaButton(MESSAGES.buttonPrint());
		iPrint.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iPrint);		

		iNew = new AriaButton(MESSAGES.buttonAddNew());
		iNew.setEnabled(false);
		iNew.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iNew);
		iService.canAddCurriculum(new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(Boolean result) {
				iNew.setEnabled(result);
			}
		});
				
		iCurriculaPanel.add(iFilterPanel);
		iCurriculaPanel.setCellHorizontalAlignment(iFilterPanel, HasHorizontalAlignment.ALIGN_CENTER);
		
		iCurriculaTable = new CurriculaTable();
		iCurriculaTable.getElement().getStyle().setMarginTop(10, Unit.PX);
		iFilterPanel.add(iCurriculaTable.getOperations());
		iCurriculaTable.getOperations().setEnabled(false);
		
		iCurriculaPanel.add(iCurriculaTable);
		
		iCurriculaPanel.setWidth("100%");
		
		iPanel.setWidget(iCurriculaPanel);
		
		iCurriculumPanel = new CurriculumEdit(new CurriculumEdit.NavigationProvider() {
			@Override
			public CurriculumInterface previous(CurriculumInterface curriculum) {
				return iCurriculaTable.previous(curriculum == null ? null : curriculum.getId());
			}
			@Override
			public CurriculumInterface next(CurriculumInterface curriculum) {
				return iCurriculaTable.next(curriculum == null ? null : curriculum.getId());
			}
			@Override
			public void onChange(CurriculumInterface curriculum) {
				if (curriculum.getId() != null)
					History.newItem("detail=" + curriculum.getId(), false);
			}
		});
		
		iClassificationsEdit = new ClassificationsEdit();
		
		initWidget(iPanel);
		
		iSearch.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				loadCurricula();
			}
		});
		
		/*
		iFilter.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					loadCurricula();
			}
		});
		*/
		
		iPrint.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.print();
			}
		});
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				iFilter.setFocus(true);
			}
		});

		if (Window.Location.getParameter("q") != null) {
			iFilter.setValue(Window.Location.getParameter("q"), true);
			loadCurricula();
		} else {
			showLoading(MESSAGES.waitLoadingCurricula());
			iService.lastCurriculaFilter(new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String result) {
					if (iFilter.getValue().isEmpty()) {
						iFilter.setValue(result, true);
						loadCurricula();
					}
					hideLoading();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					iCurriculaTable.setError(MESSAGES.failedToLoadCurricula(caught.getMessage()));
					UniTimeNotifications.error(MESSAGES.failedToLoadCurricula(caught.getMessage()), caught);
					hideLoading();
					ToolBox.checkAccess(caught);
				}
				
			});
		}
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (event.getValue() == null || "0:0".equals(event.getValue())) return;
				String command = event.getValue();
				if (command.startsWith("detail=")) {
					showLoading(MESSAGES.waitLoadingCurriculum());
					iService.loadCurriculum(Long.parseLong(command.substring("detail=".length())), new AsyncCallback<CurriculumInterface>() {
						@Override
						public void onFailure(Throwable caught) {
							hideLoading();
						}
						@Override
						public void onSuccess(CurriculumInterface result) {
							iCurriculumPanel.edit(result, true);
							iPanel.setWidget(iCurriculumPanel);
							hideLoading();
							Client.fireGwtPageChanged(new GwtPageChangeEvent());
						}
					});
				} else if ("new".equals(command)) {
					UniTimePageLabel.getInstance().setPageName(MESSAGES.pageAddCurriculum());
					iCurriculumPanel.addNew();
					iPanel.setWidget(iCurriculumPanel);
					Client.fireGwtPageChanged(new GwtPageChangeEvent());
				} else {
					if (!"requests".equals(command))
						iFilter.setValue(command.replace("%20", " "), true);
					loadCurricula();
					if (iCurriculumPanel.isVisible()) {
						UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCurricula());
						iPanel.setWidget(iCurriculaPanel);
						iCurriculaTable.scrollIntoView();
						Client.fireGwtPageChanged(new GwtPageChangeEvent());
					}
				}
			}
		});

		
		iCurriculaTable.addCurriculumClickHandler(new CurriculaTable.CurriculumClickHandler() {
			@Override
			public void onClick(CurriculaTable.CurriculumClickedEvent evt) {
				showLoading(MESSAGES.waitLoadingCurriculumWithName(evt.getCurriculum().getName()));
				iService.loadCurriculum(evt.getCurriculum().getId(), new AsyncCallback<CurriculumInterface>() {

					@Override
					public void onFailure(Throwable caught) {
						hideLoading();
					}

					@Override
					public void onSuccess(CurriculumInterface result) {
						History.newItem("detail=" + result.getId(), false);
						iCurriculumPanel.edit(result, true);
						iPanel.setWidget(iCurriculumPanel);
						hideLoading();
						Client.fireGwtPageChanged(new GwtPageChangeEvent());
					}
				});
			}
		});
		
		iNew.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				History.newItem("new", false);
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageAddCurriculum());
				iCurriculumPanel.addNew();
				iPanel.setWidget(iCurriculumPanel);
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
			}
		});
		
		iCurriculaTable.setEditClassificationHandler(new CurriculaTable.EditClassificationHandler() {
			
			@Override
			public void doEdit(List<CurriculumInterface> curricula) {
				iClassificationsEdit.setData(curricula);
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCurriculumRequestedEnrollments());
				iPanel.setWidget(iClassificationsEdit);
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem("requests", false);

			}
		});
		
		iCurriculumPanel.addEditFinishedHandler(new CurriculumEdit.EditFinishedHandler() {
			
			@Override
			public void onSave(EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCurricula());
				iPanel.setWidget(iCurriculaPanel);
				loadCurricula();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getValue(), false);
			}
			
			@Override
			public void onDelete(EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCurricula());
				iPanel.setWidget(iCurriculaPanel);
				loadCurricula();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getValue(), false);
			}
			
			@Override
			public void onBack(EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCurricula());
				iPanel.setWidget(iCurriculaPanel);
				iCurriculaTable.scrollIntoView();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getValue(), false);
			}
		});
		
		iClassificationsEdit.addEditFinishedHandler(new ClassificationsEdit.EditFinishedHandler() {
			
			@Override
			public void onSave(ClassificationsEdit.EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCurricula());
				iPanel.setWidget(iCurriculaPanel);
				loadCurricula();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getValue(), false);
			}
			
			@Override
			public void onBack(ClassificationsEdit.EditFinishedEvent evt) {
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCurricula());
				iPanel.setWidget(iCurriculaPanel);
				iCurriculaTable.scrollIntoView();
				Client.fireGwtPageChanged(new GwtPageChangeEvent());
				History.newItem(iFilter.getValue(), false);
			}
		});
		

		iService.loadAcademicAreas(new AsyncCallback<TreeSet<AcademicAreaInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(TreeSet<AcademicAreaInterface> result) {
				iCurriculumPanel.setupAreas(result);
			}
		});
		iService.loadDepartments(new AsyncCallback<TreeSet<DepartmentInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(TreeSet<DepartmentInterface> result) {
				iCurriculumPanel.setupDepartments(result);
			}
		});
		iService.loadAcademicClassifications(new AsyncCallback<TreeSet<AcademicClassificationInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(TreeSet<AcademicClassificationInterface> result) {
				iCurriculumPanel.setupClassifications(result);
				iCurriculaTable.setup(new ArrayList<AcademicClassificationInterface>(result));
			}
		});
	}
	
	private void loadCurricula() {
		if (!iSearch.isEnabled()) return;
		iSearch.setEnabled(false);
		iPrint.setEnabled(false);
		iCurriculaTable.getOperations().setEnabled(false);
		final boolean newEnabled = iNew.isEnabled();
		if (newEnabled)
			iNew.setEnabled(false);
		History.newItem(iFilter.getValue(), false);
		iCurriculaTable.query(iFilter.getElementsRequest(), new Command() {
			@Override
			public void execute() {
				iSearch.setEnabled(true);
				iPrint.setEnabled(true);
				iCurriculaTable.getOperations().setEnabled(true);
				if (newEnabled)
					iNew.setEnabled(true);
			}
		});
	}

	public void showLoading(String message) { LoadingWidget.getInstance().show(message); }
	
	public void hideLoading() { LoadingWidget.getInstance().hide(); }	
}
