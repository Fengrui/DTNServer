package info.fshi.dtnserver.context;

import info.fshi.dtnserver.data.DbManager;
import info.fshi.dtnserver.data.Device;
import info.fshi.dtnserver.data.Packet;
import info.fshi.dtnserver.data.Phone;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import com.amaxilatis.orion.OrionClient;
import com.amaxilatis.orion.model.OrionContextElement;

public class ContextManager {

	private final String ENTITY_URI = "urn:oc:entity:london:dtn:";
	private OrionClient client;
	private String serverUrl;
	private String token;
	private DbManager mDbManager;
	private int counter;

	public ContextManager(){
		final Properties properties = new Properties();
		try {
			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("connection.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		counter = 0;

		serverUrl = properties.getProperty("serverUrl");
		token = properties.getProperty("token");

		client = new OrionClient(serverUrl, token);
		mDbManager = new DbManager();
		try {
			mDbManager.open();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getEntityUri(int type){
		StringBuffer sb = new StringBuffer();
		switch(type){
		case Device.DEVICE_TYPE_PHONE:
			sb.append("phone:");
			break;
		case Device.DEVICE_TYPE_SENSOR:
			sb.append("sensor:");
			break;
		default:
			break;
		}
		sb.append(counter ++);
		return ENTITY_URI.concat(sb.toString());
	}

	public int registerResource(Device device){

		int deviceId = 0;
		String elementType = null;
		// register on local databases
		switch(device.getType()){
		case Device.DEVICE_TYPE_PHONE:
			deviceId = mDbManager.insertPhone((Phone)device);
			elementType = Device.TYPE_PHONE;
			break;
		case Device.DEVICE_TYPE_SENSOR:
			elementType = Device.TYPE_SENSOR;
			break;
		default:
			break;
		}
		
		// register to orion
		OrionContextElement element = new OrionContextElement();

		element.setId(device.getEntityid());
		element.setType(elementType);

		for (int i=0; i<device.getAttributes().length(); i++)
		{
			try {
				JSONObject attribute = (JSONObject) device.getAttributes().get(i);

				element.getAttributes().add(OrionClient.createAttribute(
						attribute.getString(Packet.KEY_ATTRIBUTE_NAME),
						attribute.getString(Packet.KEY_ATTRIBUTE_TYPE), 
						attribute.getString(Packet.KEY_ATTRIBUTE_VALUE)));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		try {
			final String entity = client.postContextEntity(device.getEntityid(), element);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			OrionContextElementWrapper entity = client.getContextEntity(device.getEntityid());
			
			for (Map.Entry<String, Object> entry : entity.getContextElement().getAttributes().get(0).entrySet())
			{
			    System.out.println(entry.getKey() + "/" + (String) entry.getValue());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		return deviceId;
	}

	public void updateResource(Device device){
		System.out.println("update resource");
		
		String elementType = null;
		// register on local databases
		switch(device.getType()){
		case Device.DEVICE_TYPE_PHONE:
			Phone phone = mDbManager.getPhone(device.getId());
			device.setEntityid(phone.getEntityid());
			mDbManager.updatePhone((Phone)device);
			elementType = Device.TYPE_PHONE;
			break;
		case Device.DEVICE_TYPE_SENSOR:
			elementType = Device.TYPE_SENSOR;
			break;
		default:
			break;
		}
		
		// register to orion
		OrionContextElement element = new OrionContextElement();

		element.setId(device.getEntityid());
		element.setType(elementType);

		for (int i=0; i<device.getAttributes().length(); i++)
		{
			try {
				JSONObject attribute = (JSONObject) device.getAttributes().get(i);

				element.getAttributes().add(OrionClient.createAttribute(
						attribute.getString(Packet.KEY_ATTRIBUTE_NAME),
						attribute.getString(Packet.KEY_ATTRIBUTE_TYPE), 
						attribute.getString(Packet.KEY_ATTRIBUTE_VALUE)));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		try {
			final String entity = client.updateFromContextEntity(element);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			OrionContextElementWrapper entity = client.getContextEntity(device.getEntityid());
			
			for (Map.Entry<String, Object> entry : entity.getContextElement().getAttributes().get(0).entrySet())
			{
			    System.out.println(entry.getKey() + "/" + (String) entry.getValue());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	public void deleteResource(Device device){
		String elementType = null;
		// register on local databases
		switch(device.getType()){
		case Device.DEVICE_TYPE_PHONE:
			Phone phone = mDbManager.getPhone(device.getId());
			device.setEntityid(phone.getEntityid());
			mDbManager.deletePhone(phone);
			mDbManager.deleteDevice(phone.getAddr());
			elementType = Device.TYPE_PHONE;
			break;
		case Device.DEVICE_TYPE_SENSOR:
			elementType = Device.TYPE_SENSOR;
			break;
		default:
			break;
		}
		
		// register to orion
		OrionContextElement element = new OrionContextElement();

		element.setId(device.getEntityid());
		element.setType(elementType);

		/**
		try {
			final String entity = client.deleteFromContextEntity(device.getEntityid(), element);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	public void getAllResources(){
		
	}
	
	public HashMap<String, String> getDeviceList(){
		return mDbManager.getDeviceList();
	}
	
	public void storeTransactionData(JSONObject data){
		try {
			mDbManager.insertTransactionData(data.getString(Packet.KEY_DATA_SOURCE), 
					data.getString(Packet.KEY_DATA_DESTINATION), 
					data.getString(Packet.KEY_DATA_PACKETID), 
					data.getString(Packet.KEY_DATA_PATH), 
					data.getInt(Packet.KEY_DATA_PACKETSIZE));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getResource(String entityId){
		/**
		try {
			OrionContextElementWrapper entity = client.getContextEntity(entityId);
			System.out.println();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

}
