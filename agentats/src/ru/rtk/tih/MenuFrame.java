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
        setTitle("јгент ј“— AXE-10");
        setSize(DEFAULT_WIDTH,DEFAULT_HEIGHT);
        Image image = Toolkit.getDefaultToolkit().createImage("binoculars.png");
        setIconImage(image);
        
      //начало ќрганизуем сворачивание в трей окна приложени€ конец строка 90
        //ActionListener на закрытие приложени€
        ActionListener exitAL = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        
        if(SystemTray.isSupported()){
            PopupMenu pm = new PopupMenu();
               MenuItem miExit = new MenuItem("¬ыход");
               miExit.addActionListener(exitAL);
               MenuItem miRestore = new MenuItem("¬осстановить");
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
               TrayIcon ti = new TrayIcon(image,"¬осстановить двойным кликом", pm);
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
        //конец организации сворачивани€ окна в трей начало строка 54
        
      //создаем меню
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
        //конец создани€ меню
        
      //ƒобавл€ем  List
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(5,5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JList list = new JList(listModel);
        list.setSelectedIndex(0);
        mainPanel.add(new JScrollPane(list), BorderLayout.CENTER);
        
        getContentPane().add(mainPanel);
        
      //провер€ем есть ли файл настроек
      		f = new File("mysettings.ini");
      		if(f.exists()){
      			setMyOptions();
      		} else {
      			JOptionPane.showMessageDialog(this, "—оздайте настроечный файл mysettings.ini");
      		}
        
        // коннектимс€ к базе
        DerbyConnect derbyconnect = new DerbyConnect("alarmDB");
        if (derbyconnect.connected()){               
      //-------- ¬ отдельном потоке запускаем слушатель вывода окна WinFiol
        Runnable r = new WFoutputListener();
        Thread t = new Thread(r);
        t.start();
        
      //-------- ¬ отдельном потоке запускаем обработчик динамического массива сообщений
        Runnable r2 = new OutputHandler();
        Thread t2 = new Thread(r2);
        t2.start();
        
      //-------- ¬ отдельном потоке запускаем HTTP сервер который будет отдавать сообщени€ NAGIOS-у
        Runnable r3 = new HttpResponse();
        Thread t3 = new Thread(r3);
        t3.start();
        } else System.out.println("Can't connect to base");
    }
    
  //начало методов и классов дл€ сворачивани€ окна в трей конец строка 100
    //восстанавливаем окно при двойном клике на иконке в трее
    private void restoreWindow() {
        setVisible(true);
        setExtendedState(getExtendedState() & (JFrame.ICONIFIED ^ 0xFFFF));
        requestFocus();
    }
    
    //слушатель двойного нажати€ мыши на иконке в трее
    public class TrayMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                restoreWindow();
            }
        }
    }
    
    //слушатель событий окна приложени€ сворачивани€ и закрыти€
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
    
    //метод спр€тывани€ окна приложени€
    private void hideWindow() {
        setVisible(false);
    }
    //конец методов и классов сворачивани€ окна в трей начало строка 66
    
    private void setMyOptions() throws IOException {
		// устанавливаем переменные согласно файла настроек
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
