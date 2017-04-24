package ru.rtk.tih;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DerbyConnect { 
	String dbName=null;  // the database name 
	public static Connection conn = null;
    ArrayList<Statement> statements = new ArrayList<Statement>(); // list of Statements, PreparedStatements
    public static PreparedStatement psInsert;
    static PreparedStatement psUpdate;
    public static Statement s;
    static ResultSet rs = null;
    String protocol = "jdbc:derby:";
    public static String alarmState = "0|NO ALARMS";
    public static int pluginAlarmCat = 0;
	
	public DerbyConnect(String dbName){
		this.dbName = dbName;
	}

	public boolean connected(){
		try {
			conn = DriverManager.getConnection(protocol + dbName + ";create=true");
			MenuFrame.listModel.add(0, MenuFrame.curdate() +  "  Connected to and created database " + dbName);
			System.out.println("Connected to and created database " + dbName);
			s = conn.createStatement();
			if(!tableExist()) {
				if(createTable()) {
					MenuFrame.listModel.add(0, MenuFrame.curdate() +  "  Table alarms created sucsessfully");
					System.out.println("table alarms created sucsessfully");
				} else {
					MenuFrame.listModel.add(0, MenuFrame.curdate() +  "  Table alarms not created");
					System.out.println("table alarms not created");
				}
					
				
			} else {
				MenuFrame.listModel.add(0, MenuFrame.curdate() +  "  Table alarms alredy exist");
				System.out.println("table alarms alredy exist");
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	private boolean tableExist() throws SQLException {
		boolean existence = false;
		try {
			rs = conn.getMetaData().getTables(null, null, "ALARMS", null);
			if(rs.next()) {
				existence = true;
			} else {
				existence = false;
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
			String theError = (sqle).getSQLState();
			System.out.println("theError="+theError);			
			throw sqle;						
		}
		
		return existence;
	}
	
	private boolean createTable(){
		boolean result = false;
		String createString = "CREATE TABLE alarms  "
				   +  "(RECORD_ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY," 
				   +  " BEGIN_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
				   +  " ALARM_ID INT NOT NULL,"
				   +  " ALARM_HEADER VARCHAR(100),"
				   +  " ALARM_DESCRIPTION VARCHAR(100),"
				   +  " ALARM_BODY VARCHAR(200),"
				   +  " CEASING_HEADER VARCHAR(100) DEFAULT null) " ;
		System.out.println("create table . . .");
		try {
			s.execute(createString);
			result = true;
		} catch (SQLException sqle) {
			// если неудача при создании таблицы
			result = false;
			System.out.println("sqle="+sqle);
		}
		
		return result;
	}
	
	public static void insAlarm(int indexBegin, int indexEnd){
		String alarmString = null;
		int numAlarm = 0;
		String [] partsAlarm;
		String bodyAlarm ="";
		int recordid=0;
		try {
			alarmString = WFoutputListener.listOutputs.get(indexBegin).trim();
			alarmString.trim();
			partsAlarm = alarmString.split(" ");
			if(alarmString.contains("CEASING")){  //если авария ушла
			numAlarm = Integer.parseInt(partsAlarm[3]);
			System.out.println("numAlarm="+partsAlarm[3]);	
			//проверяем есть ли авария с таким номером за сегодняшнее число
			rs = s.executeQuery("SELECT * FROM alarms where ALARM_ID="+partsAlarm[3]
					//+" AND DATE(BEGIN_DATE)=CURRENT_DATE");
			);
			if (rs.next())
	         {
				recordid = rs.getInt("RECORD_ID");
				System.out.println("RECORD_ID="+rs.getString("RECORD_ID"));
				psUpdate = conn.prepareStatement(
						"update alarms set CEASING_HEADER=? where RECORD_ID=?");
				psUpdate.setString(1, WFoutputListener.listOutputs.get(indexBegin+1));
				psUpdate.setInt(2, recordid);
				psUpdate.executeUpdate();
				System.out.println("Изменена запись");
	         }
			rs.close();
			
			
			} else {  //если авария наступила
				numAlarm = Integer.parseInt(partsAlarm[2]);
				for(int i=indexBegin+2; i<=indexEnd; i++) {
					bodyAlarm = bodyAlarm + WFoutputListener.listOutputs.get(i);
				}
			System.out.println("bodyAlarm="+bodyAlarm);
			System.out.println("numAlarm="+partsAlarm[2]);
			psInsert = conn.prepareStatement(
			        "insert into alarms (ALARM_ID, ALARM_HEADER, ALARM_DESCRIPTION, ALARM_BODY) values (?, ?, ?, ?)");
			psInsert.setInt(1, numAlarm);
			psInsert.setString(2, WFoutputListener.listOutputs.get(indexBegin));
			psInsert.setString(3, WFoutputListener.listOutputs.get(indexBegin+1));
			psInsert.setString(4, bodyAlarm);
			//psInsert.executeUpdate();
			int x = psInsert.executeUpdate();
			if(!(x==1)) MenuFrame.listModel.add(0, "ERROR INSERTING INTO TABLE ALARM!"); 
			System.out.println("Результат вставки=" + x);
			}
		//Записали в базу все как надо
		//теперь формируем строку состояния аварий которая будет передаваться в ответ на запрос HTTP
			
			rs = s.executeQuery("SELECT * FROM alarms where CEASING_HEADER IS NULL"
				//	+" AND DATE(BEGIN_DATE)=CURRENT_DATE"
					                                                                );
			alarmState = "";
			
			
			//	alarmState = alarmState + rs.getString("ALARM_DESCRIPTION").trim();	
			while (rs.next())
	         {
				alarmState = alarmState + getCatAlarm(rs.getString("ALARM_HEADER")).trim() + "_" +rs.getString("ALARM_DESCRIPTION").trim() + "|";				
				//System.out.println("alarmState="+alarmState);
	         }
			rs.close();
			if(alarmState.equals("")) alarmState = "NO ALARMS";
			alarmState = pluginAlarmCat + "|" + alarmState;
			System.out.println("alarmState="+alarmState);
			pluginAlarmCat = 0;
			
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
			System.out.println("sqle="+sqle);
		}
	}
	
	private static String getCatAlarm(String alarmStr){
		String alarmCat = null;
		String [] partsAlarm;
		//System.out.println("alarmStr"+alarmStr);
		
		partsAlarm = alarmStr.trim().split(" ");
		if(alarmStr.contains("CEASING")){
			alarmCat = partsAlarm[4];
		} else {
			alarmCat = partsAlarm[3];
		}
		//формируем категорию аварии в плагине
		if (alarmCat.contains("A1")) pluginAlarmCat = 2;
		else {
			if (alarmCat.contains("A2") && pluginAlarmCat<2) pluginAlarmCat = 1;	
		}
		return alarmCat;
	}
}
