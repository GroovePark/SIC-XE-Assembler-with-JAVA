package project1b;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;


/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ�. <br>
 * ���� instruction ���� ����, ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/** 
	 * inst.data ������ �ҷ��� �����ϴ� ����.
	 *  ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap;
	
	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * @param instFile : instuction�� ���� ���� ����� ���� �̸�
	 * @throws IOException 
	 */
	public InstTable(String instFile) throws IOException {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	   
	}
	
	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
	 * @throws IOException 
	 */
	public void openFile(String fileName) throws IOException {
		//...
	    FileInputStream in = new FileInputStream(fileName);
	
	    int c,i,inst_line;
	    String data = "";
	    String line_data ="";
	    String keyword = "";
	    //���ڵ� �ϳ��� ��������� �����Ⱚ�� ����־��µ� �� ������ �����ִ� �����̴�.		
	    while((c=in.read()) != -1)
	    	if((65<= c && c<=90) || (47<=c && c<=57) )
	    	data = data + (char)c;
	    	else
	    		data = data + " ";
	    
	    //������üũ�⸦ ���ڵ� �ϳ��� ũ��� ������ ���ڵ� ���� �����ش�.
	    inst_line = data.length() / 19;
	
	    for(i=0;i<inst_line;i++)
	    {	
	    	//inst.dat������ ���ڵ� ������ �����ϴ� �����̴�. 
	    	//fixed length ������� ����������Ƿ� 19����Ʈ ������ ��� ��ū�� �ǽ��ѵ� instMap�� �־��ش�.
	    	line_data = data.substring(i*19+7,(i+1)*19);
	    	keyword = data.substring(i*19,i*19+7);
	    	StringTokenizer st  =  new StringTokenizer(keyword," ");
			String  key  =  st.nextToken();
	    	Instruction inst =new Instruction(line_data);
	    	instMap.put(key,inst);
	    }
	    
	  in.close();
	}
	
	//get, set, search ���� �Լ��� ���� ����

}
/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����.
 * instruction�� ���õ� �������� �����ϰ� �������� ������ �����Ѵ�.
 */
class Instruction {
	/* 
	 * ������ inst.data ���Ͽ� �°� �����ϴ� ������ �����Ѵ�.
	 *  
	 * ex)
	 * String instruction;
	 * int opcode;
	 * int numberOfOperand;
	 * String comment;
	 */
	String opcode;
	String numberofOperand;
	
	/** instruction�� �� ����Ʈ ��ɾ����� ����. ���� ���Ǽ��� ���� */
	String format;
	
	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		// TODO Auto-generated method stub
		//inst.dat���� ���κ��� �Է¹��� ���ڿ��� ����� �������� ��ū�Ѵ�.
		StringTokenizer st  =  new StringTokenizer(line," ");
		String  a  =  st.nextToken();              
		String  b  =  st.nextToken();              
		String  c  =  st.nextToken();   
		
		format = a;
		opcode = b;
		numberofOperand = c;
	}
}
