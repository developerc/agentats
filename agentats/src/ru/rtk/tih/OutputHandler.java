package ru.rtk.tih;

public class OutputHandler implements Runnable{
	int indexBegin=0;
	int indexEnd  =0;
	String attributeBegin = "*** ALARM";  //признак начала аварийного сообщения
	String attributeEnd = "END";      //признак конца
	boolean isAlarm = false;
	
	@Override
	public void run() {
		//периодически проверяем динамический массив сообщений 
		while(true){
			try {  
				//проверили на вхождение признака начала и конца аварийного сообщения
				for(int i=0; i<WFoutputListener.listOutputs.size(); i++){
					if(WFoutputListener.listOutputs.get(i).contains(attributeBegin)) {
						isAlarm = true;
						indexBegin = i;
						MenuFrame.listModel.add(0, MenuFrame.curdate() + WFoutputListener.listOutputs.get(i));
					}
					//if(WFoutputListener.listOutputs.get(i).contains(attributeEnd)){
					if(WFoutputListener.listOutputs.get(i).trim().equals(attributeEnd)){
						indexEnd = i;
						break;
					}
				}
				if(indexEnd > 0){ //если есть полное аварийное сообщение записываем его в базу
					System.out.println("indexBegin="+indexBegin);
					System.out.println("indexEnd="+indexEnd);
					
					if(isAlarm){  //если это аварийное сообщение
					DerbyConnect.insAlarm(indexBegin, indexEnd);
					}
					for(int i=0; i<=indexEnd; i++){  //удаляем сообщение из массива
						WFoutputListener.listOutputs.remove(0);
					}
					//WFoutputListener.listOutputs.remove(1);
					
					indexBegin=0;
					indexEnd  =0;
					isAlarm = false;
					for(String str : WFoutputListener.listOutputs){
						System.out.println("str="+str);
					}
				}
				
				
				
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
