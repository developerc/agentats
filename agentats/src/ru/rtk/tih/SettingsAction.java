package ru.rtk.tih;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SettingsAction extends JFrame{
	private JComboBox winFiolChannel;
	public Properties mySettings;
	
	
	public SettingsAction() throws Exception{
		setTitle("Settings");
		setSize(400,250);
	
		//добавляем ComboBox
		winFiolChannel = new JComboBox();
		winFiolChannel.addItem("Channel #1");
		winFiolChannel.addItem("Channel #2");
		winFiolChannel.addItem("Channel #3");
		winFiolChannel.addItem("Channel #4");
		winFiolChannel.addItem("Channel #5");
		winFiolChannel.addItem("Channel #6");
		winFiolChannel.addItem("Channel #7");
		winFiolChannel.addItem("Channel #8");
		winFiolChannel.addItem("Channel #9");
		winFiolChannel.addItem("Channel #10");
		
		JPanel channelPanel = new JPanel();
		channelPanel.add(winFiolChannel);
		add(channelPanel, BorderLayout.CENTER);
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mySettings = new Properties();
				mySettings.setProperty("wfchannel", winFiolChannel.getSelectedItem().toString());
				FileOutputStream out;
				try {
					out = new FileOutputStream("mysettings.ini");
					mySettings.store(out, null);
					out.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					System.out.println("exception="+e);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				MenuFrame.listModel.add(0, MenuFrame.curdate() + "  Настроен канал WinFiol = "+winFiolChannel.getSelectedItem().toString());
				System.out.println("Сохраняем настройки="+winFiolChannel.getSelectedItem().toString());
				setVisible(false);
			}
		});
		
		JButton canButton = new JButton("Cancel");
		canButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
			
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		buttonPanel.add(canButton);
		add(buttonPanel,BorderLayout.SOUTH);
		
		setVisible(true);
	}

}
