package org.unitime.timetable.gwt.client.events;

import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.ApproveEventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class ApproveDialog<T> extends UniTimeDialogBox {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private SimpleForm iForm;
	private TextArea iNotes;
	private HasValue<List<T>> iTable;
	private ListBox iStandardNotes;
	private UniTimeFileUpload iFileUpload;
	private UniTimeHeaderPanel iFooter;
	
	public ApproveDialog(HasValue<List<T>> table) {
		super(true, false);
		iTable = table;
		
		iForm = new SimpleForm();
		
		if (iTable instanceof Widget) {
			ScrollPanel scroll = new ScrollPanel((Widget)iTable);
			ToolBox.setMaxHeight(scroll.getElement().getStyle(), "200px");
			iForm.addRow(MESSAGES.propMeetings(), scroll);
		}
		
		iStandardNotes = new ListBox();
		iStandardNotes.setVisibleItemCount(4);
		iStandardNotes.setWidth("480px");
		iForm.addRow(MESSAGES.propStandardNotes(), iStandardNotes);
		iStandardNotes.addDoubleClickHandler(new DoubleClickHandler() {
			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				String text = iNotes.getText();
				if (!text.isEmpty() && !text.endsWith("\n"))
					text += "\n";
				text += iStandardNotes.getItemText(iStandardNotes.getSelectedIndex());
				iNotes.setText(text);
			}
		});
		iStandardNotes.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					String text = iNotes.getText();
					if (!text.isEmpty() && !text.endsWith("\n"))
						text += "\n";
					text += iStandardNotes.getItemText(iStandardNotes.getSelectedIndex());
					iNotes.setText(text);
					event.preventDefault();
					event.stopPropagation();
				}
			}
		});
		
		iNotes = new TextArea();
		iNotes.setStyleName("unitime-TextArea");
		iNotes.setVisibleLines(5);
		iNotes.setCharacterWidth(80);
		iForm.addRow(MESSAGES.propNotes(), iNotes);
		
		iFileUpload = new UniTimeFileUpload();
		iForm.addRow(MESSAGES.propAttachement(), iFileUpload);
		
		iFooter = new UniTimeHeaderPanel();
		iFooter.addButton("approve", MESSAGES.opApprove(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onSubmit(ApproveEventRpcRequest.Operation.APPROVE, iTable.getValue(), iNotes.getText());
				hide();
			}
		});
		iFooter.addButton("inquire", MESSAGES.opInquire(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onSubmit(ApproveEventRpcRequest.Operation.INQUIRE, iTable.getValue(), iNotes.getText());
				hide();
			}
		});
		iFooter.addButton("reject", MESSAGES.opReject(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onSubmit(ApproveEventRpcRequest.Operation.REJECT, iTable.getValue(), iNotes.getText());
				hide();
			}
		});
		iFooter.addButton("cancel", MESSAGES.onCancel(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		iForm.addBottomRow(iFooter);
		
		/*
		setEnterToSubmit(new Command() {
			@Override
			public void execute() {
				if (iFooter.isEnabled("approve"))
					onApprove();
				else if (iFooter.isEnabled("reject"))
					onReject();
				else
					onInquire();
				hide();
			}
		});
		*/
		
		setWidget(iForm);
	}
	
	protected void onSubmit(ApproveEventRpcRequest.Operation operation, List<T> items, String message) {}
	
	public void reset(EventPropertiesRpcResponse properties) {
		iNotes.setText("");
		iStandardNotes.clear();
		iForm.getRowFormatter().setVisible(iForm.getRow(MESSAGES.propNotes()), properties != null && properties.hasStandardNotes());
		if (properties != null && properties.hasStandardNotes()) {
			for (String note: properties.getStandardNotes())
				iStandardNotes.addItem(note);
		}
		iFileUpload.reset();
	}
	
	public void show(List<T> meetings, ApproveEventRpcRequest.Operation operation) {
		iTable.setValue(meetings);
		if (iTable instanceof CanHideUnimportantColumns)
			((CanHideUnimportantColumns)iTable).hideUnimportantColumns();
		setText(MESSAGES.dialogApprove());
		iFooter.setEnabled("approve", operation == ApproveEventRpcRequest.Operation.APPROVE);
		iFooter.setEnabled("reject", operation == ApproveEventRpcRequest.Operation.REJECT);
		iFooter.setEnabled("inquire", operation == ApproveEventRpcRequest.Operation.INQUIRE);
		iFileUpload.check();
		center();
		if (iStandardNotes.getItemCount() == 0)
			iNotes.setFocus(true);
		else
			iStandardNotes.setFocus(true);
	}
	
	public void showApprove(List<T> meetings) {
		show(meetings, ApproveEventRpcRequest.Operation.APPROVE);
	}
	
	public void showReject(List<T> meetings) {
		show(meetings, ApproveEventRpcRequest.Operation.REJECT);
	}

	public void showInquire(List<T> meetings) {
		show(meetings, ApproveEventRpcRequest.Operation.INQUIRE);
	}
	
	public String getNote() {
		return iNotes.getText();
	}
	
	public static interface CanHideUnimportantColumns {
		public void hideUnimportantColumns();
	}
}
