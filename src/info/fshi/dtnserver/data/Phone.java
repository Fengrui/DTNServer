package info.fshi.dtnserver.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Phone extends Device {

	
	public Phone(String id){
		super(id);
		this.type = Device.DEVICE_TYPE_PHONE;
	}

	@Override
	public void setAttributes(JSONArray attr) {
		// TODO Auto-generated method stub
		super.setAttributes(attr);
		try {
			address = ((JSONObject) attr.get(0)).getString(Packet.KEY_ATTRIBUTE_VALUE);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
