package org.orienteer.inclogger.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.inclogger.client.OIncident;
import org.orienteer.inclogger.core.interfaces.IData;
import org.orienteer.inclogger.core.interfaces.ILoggerData;

import com.google.gson.Gson;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * 
 */
public class OIncidentData implements IData{

	private Gson gson = new Gson();

	public OIncidentData() {
	}
	
	public void applyLoggerData(final ILoggerData<?> loggerData) {
		List<OIncident> incidents = (List<OIncident>) loggerData.get(); 
		
		for (OIncident incident : incidents){
			ODocument doc = new ODocument("OIncident");
			for(Entry<String, String> entry : incident.entrySet()) {
				doc.field(entry.getKey(),entry.getValue());
			}			
			doc.field("sended",0);
			doc.save();
		}
	}

	@Override
	public String getData() {
		return getData(IDataFlag.NOTHING);
	}

	@Override
	public String getData(IDataFlag flag) {
		ODatabaseDocument db = OrienteerWebApplication.get().getDatabase();//IncidentLoggerModule.db;//new ODatabaseDocumentTx(settings.getDBUrl());
		List<OIncident> data = new ArrayList<OIncident>();
		if (db.isActiveOnCurrentThread()){
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from OIncident where sended < ?");
			List<ODocument> queryData = db.command(query).execute(2);
			for (ODocument incidentDoc : queryData){
				data.add(new OIncident(incidentDoc));
				if (flag == IDataFlag.SENDED){
					incidentDoc.field("sended",1);
					incidentDoc.save();
				}
			}
		}
		return gson.toJson(data);
	}

	@Override
	public void mark(IDataFlag before, IDataFlag now) {
		if (before ==IDataFlag.SENDED && now == IDataFlag.SENDED_SUCCESSFULLY){
			ODatabaseDocument db = OrienteerWebApplication.get().getDatabase();
			if (db.isActiveOnCurrentThread()){
				db.command(new OCommandSQL("update OIncident set sended=2 where sended=1")).execute();
			}
		}
	}

	@Override
	public void applyData(String clientInfo,String newData) {
		List<Map<String,String>> anotherData = gson.fromJson(newData, ArrayList.class);
		for (Map<String,String> incident : anotherData){
			ODocument doc = new ODocument("OIncident");
			for(Entry<String, String> entry : incident.entrySet()) {
				doc.field(entry.getKey(),entry.getValue());
			}		
			doc.field("host",clientInfo);
			doc.field("sended",2);
			doc.field("recieved",1);
			doc.save();
			updateStructuredData(doc);
		}
	}
	
	private void updateStructuredData(ODocument newIncident) {
		ODatabaseDocument db = OrienteerWebApplication.get().getDatabase();
		
		String host = (String) newIncident.field("host");
		String application = (String) newIncident.field("application");
		String type = (String) newIncident.field("type");

		ODocument appDoc = getOrMakeByTypeAndName("OIncidentApplication",application,null);
		
		ODocument hostDoc = getOrMakeByTypeAndName("OIncidentHost",host,appDoc);

		ODocument typeDoc = getOrMakeByTypeAndName("OIncidentType",type,hostDoc);
		db.commit();
		db.begin();
		
		newIncident.field("typeLink",typeDoc.getIdentity());
		newIncident.save();
		
		((List)typeDoc.field("incidents")).add(newIncident.getIdentity());
		typeDoc.field("parent",hostDoc.getIdentity());
		typeDoc.save();

		((List)hostDoc.field("types")).add(typeDoc.getIdentity());
		typeDoc.field("parent",appDoc.getIdentity());
		hostDoc.save();

		((List)appDoc.field("hosts")).add(hostDoc.getIdentity());
		appDoc.save();

		db.commit();
	}
	
	private ODocument getOrMakeByTypeAndName(String type,String name,ODocument parent){
		ODatabaseDocument db = OrienteerWebApplication.get().getDatabase();
		
		OSQLSynchQuery<ODocument> query;
		List<ODocument> queryData;
		if (parent!=null){
			query = new OSQLSynchQuery<ODocument>("select from "+type+" where name=? and parent=? ");
			queryData = db.command(query).execute(name,parent.getIdentity());
		}else{
			query = new OSQLSynchQuery<ODocument>("select from "+type+" where name = ?");
			queryData = db.command(query).execute(name);
		}
		ODocument doc;
		if (queryData.isEmpty()){
			doc = new ODocument(type);
			doc.field("name",name);
			doc.save();
		}else{
			doc = queryData.get(0);
		}
		return doc;
	}
}
