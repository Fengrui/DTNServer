package info.fshi.dtnserver.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class DbManager {

	private Connection connection = null;

	public DbManager(){

	}

	public void open() throws ClassNotFoundException, SQLException{

		Class.forName("org.postgresql.Driver");
		connection = DriverManager.getConnection(
				"jdbc:postgresql://localhost:5432/oc",
				"oc", 
				"123456");
		connection.setAutoCommit(false);

		System.out.println("Opened database successfully");
	}

	public void close() throws SQLException{
		connection.close();
	}

	// insert a phone and retrieve the primary key for future reference
	public int insertPhone(Phone phone){
		System.out.println("insert phone");
		int row = getPhone(phone.address);
		if(row > 0){
			return row;
		}else{
			int rowId = 0;
			Statement stmt = null;
			try {
				stmt = connection.createStatement();
				String sql = "INSERT INTO PHONE (ENTITY_ID,ADDRESS) "
						+ "VALUES ('" + phone.getEntityid() + "' , '" + phone.getAddr() + "') RETURNING _ID;";
				ResultSet rs = stmt.executeQuery(sql);

				if(rs.next()){
					rowId = rs.getInt("_id");
				}
				stmt.close();
				connection.commit();
			} catch (Exception e) {
				System.err.println( e.getClass().getName()+": "+ e.getMessage() );
				System.exit(0);
			}
			System.out.println("Records created successfully " + rowId);
			insertDevice(phone.getAddr(), Device.DEVICE_TYPE_RELAY);
			return rowId;
		}
	}

	// update a phone information
	public void updatePhone(Phone phone){
		System.out.println("update");
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			String sql = "UPDATE PHONE SET ADDRESS = '"
					+ phone.getAddr() + "' WHERE _ID = '" + phone.getId() + "'";
			stmt.executeUpdate(sql);

			stmt.close();
			connection.commit();
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
			System.exit(0);
		}
		System.out.println("Records updated successfully");
	}

	// delete a phone according to the primary key
	public void deletePhone(Phone phone){
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			String sql = "DELETE FROM PHONE WHERE _ID = '"
					+ phone.getId() + "'";
			stmt.executeUpdate(sql);

			stmt.close();
			connection.commit();
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
			System.exit(0);
		}
		System.out.println("Records updated successfully");
	}

	public int getPhone(String mac){
		int rowId = 0;
		Statement stmt = null;
		try {
			stmt = connection.createStatement();

			String sql = "SELECT * FROM PHONE WHERE ADDRESS = '"
					+ mac + "';";
			ResultSet rs = stmt.executeQuery(sql);

			if(rs.next()){
				rowId = rs.getInt("_id");
			}
			stmt.close();
			connection.commit();
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
			System.exit(0);
		}
		System.out.println("Records retrieved successfully " + rowId);
		return rowId;
	}

	// get a phone according to the primary key
	public Phone getPhone(int id){
		Phone phone = null;
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			String sql = "SELECT * FROM PHONE WHERE _ID = '"
					+ id + "'";
			ResultSet rs = stmt.executeQuery(sql);

			if(rs.next()){
				phone = new Phone(rs.getString("entity_id"));
				phone.setId(rs.getInt("_id"));
				JSONObject attrAddress = new JSONObject();
				JSONArray attrs = new JSONArray();
				attrAddress.put(Packet.KEY_ATTRIBUTE_TYPE, "string");
				attrAddress.put(Packet.KEY_ATTRIBUTE_NAME, "address");
				attrAddress.put(Packet.KEY_ATTRIBUTE_VALUE, rs.getString("address"));
				attrs.put(attrAddress);
				phone.setAttributes(attrs);
			}
			rs.close();
			stmt.close();
			connection.commit();
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
			System.exit(0);
		}
		System.out.println("Records retrieved successfully");
		return phone;
	}

	// insert a device to the device list
	public int insertDevice(String mac, int type){
		int rowId = 0;
		Statement stmt = null;
		try {
			stmt = connection.createStatement();

			String sql = "INSERT INTO DEVICELIST (ADDRESS,TYPE) "
					+ "VALUES ('" + mac + "' , '" + type + "') RETURNING _ID;";
			ResultSet rs = stmt.executeQuery(sql);

			if(rs.next()){
				rowId = rs.getInt("_id");
			}
			stmt.close();
			connection.commit();
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
			System.exit(0);
		}
		System.out.println("Records created successfully " + rowId);
		return rowId;
	}

	// delete a certain device from the device list
	public void deleteDevice(String mac){
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			String sql = "DELETE FROM DEVICELIST WHERE ADDRESS = '"
					+ mac + "'";
			stmt.executeUpdate(sql);

			stmt.close();
			connection.commit();
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
			System.exit(0);
		}
		System.out.println("Records deleted successfully");
	}

	// get the entire device list
	public HashMap<String, String> getDeviceList(){

		HashMap<String, String> deviceList = new HashMap<String, String>();

		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			String sql = "SELECT * FROM DEVICELIST";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()){
				if(rs.getInt("type") == Device.DEVICE_TYPE_RELAY)
					deviceList.put(rs.getString("address"), String.valueOf(rs.getInt("type")) + ":" + String.valueOf(rs.getInt("_id") % 10 + 1));
				else
					deviceList.put(rs.getString("address"), String.valueOf(rs.getInt("type")) + ":" + String.valueOf(rs.getInt("_id")));
			}

			rs.close();
			stmt.close();
			connection.commit();
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
			System.exit(0);
		}
		System.out.println("Records retrieved successfully");
		return deviceList;
	}

	// insert a device to the device list
	public int insertTransactionData(String source, String destination, String packetId, String path, int packetSize){
		int rowId = 0;
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			String sql = "INSERT INTO DATA (SOURCE,DESTINATION, PACKETID, PATH, PACKETSIZE) "
					+ "VALUES ('" + source + "' , '" + destination + "' , '" + packetId + "', '" + path + "', '" + String.valueOf(packetSize) + "') RETURNING _ID;";
			ResultSet rs = stmt.executeQuery(sql);

			if(rs.next()){
				rowId = rs.getInt("_id");
			}
			stmt.close();
			connection.commit();
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
			System.exit(0);
		}
		System.out.println("Data created successfully " + rowId);
		return rowId;
	}
}
