package ru.rtk.tih;

public class OutputHandler implements Runnable{
	int indexBegin=0;
	int indexEnd  =0;
	String attributeBegin = "*** ALARM";  //������� ������ ���������� ���������
	String attributeEnd = "END";      //������� �����
	boolean isAlarm = false;
	
	@Override
	public void run() {
		//������������ ��������� ������������ ������ ��������� 
		while(true){
			try {  
				//��������� �� ��������� �������� ������ � ����� ���������� ���������
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
				if(indexEnd > 0){ //���� ���� ������ ��������� ��������� ���������� ��� � ����
					System.out.println("indexBegin="+indexBegin);
					System.out.println("indexEnd="+indexEnd);
					
					if(isAlarm){  //���� ��� ��������� ���������
					DerbyConnect.insAlarm(indexBegin, indexEnd);
					}
					for(int i=0; i<=indexEnd; i++){  //������� ��������� �� �������
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
