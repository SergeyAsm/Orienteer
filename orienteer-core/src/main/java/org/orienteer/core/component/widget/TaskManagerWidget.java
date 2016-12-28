package org.orienteer.core.component.widget;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.Command;
import org.orienteer.core.component.structuretable.OrienteerStructureTable;
import org.orienteer.core.tasks.OTaskSession;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.record.impl.ODocument;

import ru.ydn.wicket.wicketorientdb.model.SimpleNamingModel;

/**
 * Widget for {@link TaskManagerModule}
 *
 */
@Widget(domain="document",selector=OTaskSession.TASK_SESSION_CLASS, id=TaskManagerWidget.WIDGET_TYPE_ID, order=20, autoEnable=true)
public class TaskManagerWidget extends AbstractWidget<ODocument>{

	public static final String WIDGET_TYPE_ID = "taskManager";
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(TaskManagerWidget.class);
	private Form form;
	
	public static final List<String> TASK_DATA_LIST = new ArrayList<String>();
	static
	{
		//TASK_DATA_LIST.add("name");
	}	


	public TaskManagerWidget(String id, IModel<ODocument> model, final IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);

		form = new Form<Void>("form");
        
        OrienteerStructureTable<ODocument, String> structuredTable = new OrienteerStructureTable<ODocument, String>("table", model, TASK_DATA_LIST) {
			private static final long serialVersionUID = 1L;
			@Override
			protected Component getValueComponent(String id, IModel<String> rowModel) {
				return new Label(id,new PropertyModel<>(getModel(), rowModel.getObject()));
			}
			@Override
			protected IModel<?> getLabelModel(Component resolvedComponent, IModel<String> rowModel) {
				return new SimpleNamingModel<String>("integration."+rowModel.getObject());
			}
		};
			
		
		form.add(structuredTable);
		structuredTable.addCommand(makeStopButton());
		
		form.setOutputMarkupId(true);

		add(form);
	}
	
	
	private Command makeStopButton() {
		return new Command("stop","tasks.stop") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				setIcon(FAIconType.stop);
				setBootstrapType(BootstrapType.DANGER);
				setChangingDisplayMode(true);
				OTaskSession taskSession = new OTaskSession(TaskManagerWidget.this.getModelObject());
				setEnabled(taskSession.isBreakable());
			}
			@Override
			public void onClick() {
				OTaskSession taskSession = new OTaskSession(TaskManagerWidget.this.getModelObject());
				taskSession.getCallback().stop();
				setEnabled(false);
			}
		};
	}	
	
	@Override
	protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.bars);
	}

	@Override
	protected IModel<String> getDefaultTitleModel() {
		return new ResourceModel("tasks.title");
	}
	
	@Override
	protected String getWidgetStyleClass() {
		return "strict";
	}

}
