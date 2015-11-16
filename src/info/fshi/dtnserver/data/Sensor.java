package info.fshi.dtnserver.data;

public class Sensor extends Device {

	public Sensor(String id){
		super(id);
		this.type = Device.DEVICE_TYPE_SENSOR;
	}
	
}
