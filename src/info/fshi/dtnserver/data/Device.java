package info.fshi.dtnserver.data;

import org.json.JSONArray;

public abstract class Device {

	protected int id;
	protected String entityId;
	protected int type;
	protected JSONArray attributes;
	protected String address;
	public static final int DEVICE_TYPE_SINK = 3;
	public static final int DEVICE_TYPE_PHONE = 2;
	public static final int DEVICE_TYPE_RELAY = 2;
	public static final int DEVICE_TYPE_SENSOR = 1;
	public static final String TYPE_PHONE = "phone";
	public static final String TYPE_SENSOR = "sensor";
	public static final String TYPE_SINK = "sink";
	
	public Device(String entityId){
		this.entityId = entityId;
	}

	public void setId(int id){
		this.id = id;
	}
	
	public void setEntityid(String entityId){
		this.entityId = entityId;
	}

	public String getEntityid(){
		return this.entityId;
	}

	public int getId(){
		return this.id;
	}

	//	public String getName(){
	//		return this.name;
	//	}

	public int getType(){
		return this.type;
	}

	public JSONArray getAttributes(){
		return this.attributes;
	}

	public void setAttributes(JSONArray attr){
		this.attributes = attr;
	}
	
	public String getAddr(){
		return address;
	}


}
