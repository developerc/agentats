package ru.rtk.tih;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;



public class MenuFrame extends JFrame{
	public static final int DEFAULT_WIDTH=500;
    public static final int DEFAULT_HEIGHT=300;
    JMenuBar menuBar;
    JMenu fileMenu;
    JMenu optionsMenu;
    Properties mySettings;
    File f = null;
    public static String wfChannelNum;
    static DefaultListModel listModel = new DefaultListModel();
    
    public MenuFrame() throws Exception{
        setTitle("����� ��� AXE-10");
        setSize(DEFAULT_WIDTH,DEFAULT_HEIGHT);
        Image image = Toolkit.getDefaultToolkit().createImage("binoculars.png");
        setIconImage(image);
        
      //������ ���������� ������������ � ���� ���� ���������� ����� ������ 90
        //ActionListener �� �������� ����������
        ActionListener exitAL = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        
        if(SystemTray.isSupported()){
            PopupMenu pm = new PopupMenu();
               MenuItem miExit = new MenuItem("�����");
               miExit.addActionListener(exitAL);
               MenuItem miRestore = new MenuItem("������������");
               miRestore.addActionListener(new ActionListener() {
                   @Override
                   public void actionPerformed(ActionEvent e) {
                       restoreWindow();
                   }
               });
               pm.add(miRestore);
               pm.addSeparator();
               pm.add(miExit);

               SystemTray st = SystemTray.getSystemTray();
               TrayIcon ti = new TrayIcon(image,"������������ ������� ������", pm);
               ti.setImageAutoSize(true);
               ti.addMouseListener(new TrayMouseListener());
               try {
                   st.add(ti);
                   addWindowListener(new WindowMinimizeListener());
               } catch (AWTException ex) {
                   ex.printStackTrace();
               }
           } else
               System.out.println("-----System tray not supported!");
        //����� ����������� ������������ ���� � ���� ������ ������ 54
        
      //������� ����
        menuBar=new JMenuBar();
        fileMenu = new JMenu("File");
        fileMenu.add(new AbstractAction("Exit") {
        
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
        });
        optionsMenu = new JMenu("Options");
        optionsMenu.add(new AbstractAction("Settings"){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				try {
					new SettingsAction();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        	
        });
        	                
        menuBar.add(fileMenu);
        menuBar.add(optionsMenu);
        setJMenuBar(menuBar);
        //����� �������� ����
        
      //���������  List
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(5,5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JList list = new JList(listModel);
        list.setSelectedIndex(0);
        mainPanel.add(new JScrollPane(list), BorderLayout.CENTER);
        
        getContentPane().add(mainPanel);
        
      //��������� ���� �� ���� ��������
      		f = new File("mysettings.ini");
      		if(f.exists()){
      			setMyOptions();
      		} else {
      			JOptionPane.showMessageDialog(this, "�������� ����������� ���� mysettings.ini");
      		}
        
        // ����������� � ����
        DerbyConnect derbyconnect = new DerbyConnect("alarmDB");
        if (derbyconnect.connected()){               
      //-------- � ��������� ������ ��������� ��������� ������ ���� WinFiol
        Runnable r = new WFoutputListener();
        Thread t = new Thread(r);
        t.start();
        
      //-------- � ��������� ������ ��������� ���������� ������������� ������� ���������
        Runnable r2 = new OutputHandler();
        Thread t2 = new Thread(r2);
        t2.start();
        
      //-------- � ��������� ������ ��������� HTTP ������ ������� ����� �������� ��������� NAGIOS-�
        Runnable r3 = new HttpResponse();
        Thread t3 = new Thread(r3);
        t3.start();
        } else System.out.println("Can't connect to base");
    }
    
  //������ ������� � ������� ��� ������������ ���� � ���� ����� ������ 100
    //��������������� ���� ��� ������� ����� �� ������ � ����
    private void restoreWindow() {
        setVisible(true);
        setExtendedState(getExtendedState() & (JFrame.ICONIFIED ^ 0xFFFF));
        requestFocus();
    }
    
    //��������� �������� ������� ���� �� ������ � ����
    public class TrayMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                restoreWindow();
            }
        }
    }
    
    //��������� ������� ���� ���������� ������������ � ��������
    class WindowMinimizeListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            hideWindow();
        }
        @Override
        public void windowIconified(WindowEvent e) {
            hideWindow();
        }
    }
    
    //����� ����������� ���� ����������
    private void hideWindow() {
        setVisible(false);
    }
    //����� ������� � ������� ������������ ���� � ���� ������ ������ 66
    
    private void setMyOptions() throws IOException {
		// ������������� ���������� �������� ����� ��������
    	mySettings = new Properties();
    	f = new File("mysettings.ini");
    	FileInputStream in = new FileInputStream("mysettings.ini");
        mySettings.load(in);
        in.close();
        wfChannelNum = mySettings.getProperty("wfchannel");
        System.out.println(mySettings.getProperty("wfchannel"));
    }
    
    public static String curdate()
	{
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		return timeStamp;
	}
}
