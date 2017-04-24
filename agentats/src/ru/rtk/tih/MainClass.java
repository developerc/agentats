package ru.rtk.tih;

import java.awt.EventQueue;

import javax.swing.JFrame;


//главный класс приложения AgentATS

public class MainClass {
	public static void main(String[] args) {
		// write your code here
	        EventQueue.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                MenuFrame frame;
					try {
						frame = new MenuFrame();
						frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		                frame.setVisible(true);
		              
		                
		                
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                
	            }
	        });
	    }
}
