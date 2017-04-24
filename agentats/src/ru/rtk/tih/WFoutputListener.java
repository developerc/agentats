package ru.rtk.tih;

import java.util.ArrayList;

import com.google.code.jdde.client.Advise;
import com.google.code.jdde.client.ClientConversation;
import com.google.code.jdde.client.DdeClient;
import com.google.code.jdde.client.event.AdviseDataListener;
import com.google.code.jdde.event.AdviseDataEvent;

public class WFoutputListener implements Runnable{
	String service = "WinFiol";
	//String topic = "Channel #1";
	String topic = MenuFrame.wfChannelNum;
	String item  = "Output";
	DdeClient ddeclient;
	ClientConversation conversation = null;
	Advise advise;
	static public ArrayList<String> listOutputs = new ArrayList<String>();  //динамический массив строк сообщений из Output Window
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		ddeclient = new DdeClient();
		conversation = ddeclient.connect(service, topic);
		conversation.getClient().setDefaultTimeout(3000);
		
		if (conversation != null) {
			MenuFrame.listModel.add(0, MenuFrame.curdate() +  "  Successfully connected to WinFiol");
            System.out.println("successfully connected to WinFiol");
		advise = conversation.startAdvise(item, new AdviseDataListener(){

			@Override
			public void valueChanged(AdviseDataEvent event) {
				// Когда приходит новая строка в WinFiol-e 
				System.out.println(new String(event.getData()));
				listOutputs.add(new String(event.getData()));
			}
        	   
           });
		}else{
        	System.out.println("Not connected");
        }
	}

}
