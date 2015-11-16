package info.fshi.dtnserver;

import info.fshi.dtnserver.context.ContextManager;
import info.fshi.dtnserver.data.Device;
import info.fshi.dtnserver.data.Packet;
import info.fshi.dtnserver.data.Phone;
import info.fshi.dtnserver.utils.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class ServerThread extends Thread {

	HttpServer mServer;
	ServerHandler mHandler;
	String pathDeviceList = "/list";
	String pathData = "/data";
	String pathSensorData = "/sensordata";
	String pathRegister = "/register";
	String pathUpdate = "/update";
	String pathDelete = "/delete";
	ContextManager mContextManager;

	public ServerThread(){
		try {
			mServer = HttpServer.create(new InetSocketAddress(8000), 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mContextManager = new ContextManager();
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		mHandler = new ServerHandler();

		mServer.createContext(pathRegister, mHandler);
		mServer.createContext(pathData, mHandler);
		mServer.createContext(pathUpdate, mHandler);
		mServer.createContext(pathSensorData, mHandler);
		mServer.createContext(pathDelete, mHandler);
		mServer.createContext(pathDeviceList, mHandler);
		mServer.createContext(pathData, mHandler);
		mServer.setExecutor(null); // creates a default executor
		mServer.start();
	}

	class ServerHandler implements HttpHandler {
		
		@Override
		public void handle(com.sun.net.httpserver.HttpExchange t)
				throws IOException {

			if(t.getHttpContext().getPath().equalsIgnoreCase(pathDeviceList)){
				HashMap<String, String> deviceList = mContextManager.getDeviceList();
				String response = new JSONObject(deviceList).toString();
				t.sendResponseHeaders(200, response.length());
				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.close();
			}
			// handle phone message
			else if(t.getHttpContext().getPath().equalsIgnoreCase(pathData)){
				
				JSONObject requestBody = null;

				BufferedReader streamReader = new BufferedReader(new InputStreamReader(t.getRequestBody(), "UTF-8")); 
				StringBuilder strBuilder = new StringBuilder();

				String inputStr;
				while ((inputStr = streamReader.readLine()) != null)
					strBuilder.append(inputStr);
				try {
					requestBody = new JSONObject(strBuilder.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(requestBody != null){
					try {
						System.out.println(requestBody.get(Packet.KEY_DATA_SOURCE));
						
						String topic = "/data/hydeparkcomms";
						String content = "{ \"source\": \"" + String.valueOf(requestBody.get(Packet.KEY_DATA_SOURCE)) + "\","
								+ "\"destination\": \""+ String.valueOf(requestBody.get(Packet.KEY_DATA_DESTINATION)) + "\","
								+ "\"path\": \""+ String.valueOf(requestBody.get(Packet.KEY_DATA_PATH)) + "\","
								+ "\"packetid\": \""+ String.valueOf(requestBody.get(Packet.KEY_DATA_PACKETID)) + "\","
								+ "\"packetsize\": \""+ String.valueOf(requestBody.get(Packet.KEY_DATA_PACKETSIZE)) +"\"}";
						sendMqtt(topic, content);
						
						// insert data to database
						mContextManager.storeTransactionData(requestBody);
						// use context manager to update data
						
						String response = String.valueOf(1); // 1 is success, 0 is fail
						t.sendResponseHeaders(200, response.length());
						OutputStream os = t.getResponseBody();
						os.write(response.getBytes());
						os.close();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else if(t.getHttpContext().getPath().equalsIgnoreCase(pathSensorData)){
				
				JSONObject requestBody = null;

				BufferedReader streamReader = new BufferedReader(new InputStreamReader(t.getRequestBody(), "UTF-8")); 
				StringBuilder strBuilder = new StringBuilder();

				String inputStr;
				while ((inputStr = streamReader.readLine()) != null)
					strBuilder.append(inputStr);
				try {
					requestBody = new JSONObject(strBuilder.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(requestBody != null){
					try {
						System.out.println(requestBody.get(Packet.KEY_DATA_CONTENT));
						if(String.valueOf(requestBody.get(Packet.KEY_DATA_CONTENT)).length() > 2){
							String topic = "/data/hydeparkdata";
							String content = String.valueOf(requestBody.get(Packet.KEY_DATA_CONTENT));
							sendMqtt(topic, content);
						}
						String response = String.valueOf(1); // 1 is success, 0 is fail
						t.sendResponseHeaders(200, response.length());
						OutputStream os = t.getResponseBody();
						os.write(response.getBytes());
						os.close();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else if(t.getHttpContext().getPath().equalsIgnoreCase(pathRegister)){

				JSONObject requestBody = null;

				int resourceId = 0;
				
				BufferedReader streamReader = new BufferedReader(new InputStreamReader(t.getRequestBody(), "UTF-8")); 
				StringBuilder strBuilder = new StringBuilder();

				String inputStr;
				while ((inputStr = streamReader.readLine()) != null)
					strBuilder.append(inputStr);
				try {
					requestBody = new JSONObject(strBuilder.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(requestBody != null){
					try {
						System.out.println(requestBody.get(Packet.KEY_ENTITY_TYPE));
						switch(requestBody.getInt(Packet.KEY_ENTITY_TYPE)){
						case Device.DEVICE_TYPE_PHONE:
							Phone phone = new Phone(mContextManager.getEntityUri(Device.DEVICE_TYPE_PHONE));
							JSONArray entityAttributes = requestBody.getJSONArray(Packet.KEY_ENTITY_ATTRIBUTES);
							phone.setAttributes(entityAttributes);
							resourceId = mContextManager.registerResource(phone);
							break;
						case Device.DEVICE_TYPE_SENSOR:
							break;
						case Device.DEVICE_TYPE_SINK:
							resourceId = 11;
						default:
							break;
						}

						String response = String.valueOf(resourceId);
						t.sendResponseHeaders(200, response.length());
						OutputStream os = t.getResponseBody();
						os.write(response.getBytes());
						os.close();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if(t.getHttpContext().getPath().equalsIgnoreCase(pathUpdate)){
				JSONObject requestBody = null;

				BufferedReader streamReader = new BufferedReader(new InputStreamReader(t.getRequestBody(), "UTF-8")); 
				StringBuilder strBuilder = new StringBuilder();

				String inputStr;
				while ((inputStr = streamReader.readLine()) != null)
					strBuilder.append(inputStr);
				try {
					requestBody = new JSONObject(strBuilder.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(requestBody != null){
					try {
						System.out.println(requestBody.get(Packet.KEY_ENTITY_TYPE));
						switch(requestBody.getInt(Packet.KEY_ENTITY_TYPE)){
						case Device.DEVICE_TYPE_PHONE:
							Phone phone = new Phone(null);
							phone.setId(requestBody.getInt(Packet.KEY_ID));
							JSONArray entityAttributes = requestBody.getJSONArray(Packet.KEY_ENTITY_ATTRIBUTES);
							phone.setAttributes(entityAttributes);
							mContextManager.updateResource(phone);
							break;
						case Device.DEVICE_TYPE_SENSOR:
							break;
						default:
							break;
						}

						String response = "OK";
						t.sendResponseHeaders(200, response.length());
						OutputStream os = t.getResponseBody();
						os.write(response.getBytes());
						os.close();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else if(t.getHttpContext().getPath().equalsIgnoreCase(pathDelete)){
				JSONObject requestBody = null;

				BufferedReader streamReader = new BufferedReader(new InputStreamReader(t.getRequestBody(), "UTF-8")); 
				StringBuilder strBuilder = new StringBuilder();

				String inputStr;
				while ((inputStr = streamReader.readLine()) != null)
					strBuilder.append(inputStr);
				try {
					requestBody = new JSONObject(strBuilder.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(requestBody != null){
					try {
						System.out.println(requestBody.get(Packet.KEY_ENTITY_TYPE));
						switch(requestBody.getInt(Packet.KEY_ENTITY_TYPE)){
						case Device.DEVICE_TYPE_PHONE:
							Phone phone = new Phone(null);
							phone.setId(requestBody.getInt(Packet.KEY_ID));
							mContextManager.deleteResource(phone);
							break;
						case Device.DEVICE_TYPE_SENSOR:
							break;
						default:
							break;
						}

						String response = "Deleted";
						t.sendResponseHeaders(200, response.length());
						OutputStream os = t.getResponseBody();
						os.write(response.getBytes());
						os.close();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else{
				System.out.println("bad request");
			}
		}
		
		public void sendMqtt(String topic, String content) {
			// QoS supported at level 0 and 1
			int qos             = 0;

			// Connection URI (ssl is also supported)
			String broker = Constants.MQTT_BROKER_ADDR;
			// Possible values ["tcp://api.cloudplugs.com:1883","ssl://api.cloudplugs.com:8883"]

			try {
				MemoryPersistence persistence = new MemoryPersistence();
				MqttClient sampleClient = new MqttClient(broker,MqttClient.generateClientId(), persistence);
				MqttConnectOptions	connOpts = new MqttConnectOptions();
				connOpts.setCleanSession(true);

				sampleClient.connect(connOpts);

				MqttMessage message = new MqttMessage(content.getBytes());
				message.setQos(qos);
				sampleClient.publish(topic, message);
				sampleClient.disconnect();

			} catch(MqttException me) {
				System.out.println("reason "+me.getReasonCode());
				System.out.println("msg "+me.getMessage());
				System.out.println("loc "+me.getLocalizedMessage());
				System.out.println("cause "+me.getCause());
				System.out.println("excep "+me);
				me.printStackTrace();
			}
		}
	}
}
