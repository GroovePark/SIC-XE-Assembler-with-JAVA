package project1b;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;


/**
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다. <br>
 * 또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
 */
public class InstTable {
	/** 
	 * inst.data 파일을 불러와 저장하는 공간.
	 *  명령어의 이름을 집어넣으면 해당하는 Instruction의 정보들을 리턴할 수 있다.
	 */
	HashMap<String, Instruction> instMap;
	
	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * @param instFile : instuction에 대한 명세가 저장된 파일 이름
	 * @throws IOException 
	 */
	public InstTable(String instFile) throws IOException {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	   
	}
	
	/**
	 * 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
	 * @throws IOException 
	 */
	public void openFile(String fileName) throws IOException {
		//...
	    FileInputStream in = new FileInputStream(fileName);
	
	    int c,i,inst_line;
	    String data = "";
	    String line_data ="";
	    String keyword = "";
	    //레코드 하나당 빈공간에는 쓰레기값이 들어있었는데 이 값들을 없애주는 과정이다.		
	    while((c=in.read()) != -1)
	    	if((65<= c && c<=90) || (47<=c && c<=57) )
	    	data = data + (char)c;
	    	else
	    		data = data + " ";
	    
	    //파일전체크기를 레코드 하나당 크기로 나눠서 레코드 수를 구해준다.
	    inst_line = data.length() / 19;
	
	    for(i=0;i<inst_line;i++)
	    {	
	    	//inst.dat파일을 레코드 단위로 구분하는 과정이다. 
	    	//fixed length 방식으로 저장되있으므로 19바이트 단위로 끊어서 토큰을 실시한뒤 instMap에 넣어준다.
	    	line_data = data.substring(i*19+7,(i+1)*19);
	    	keyword = data.substring(i*19,i*19+7);
	    	StringTokenizer st  =  new StringTokenizer(keyword," ");
			String  key  =  st.nextToken();
	    	Instruction inst =new Instruction(line_data);
	    	instMap.put(key,inst);
	    }
	    
	  in.close();
	}
	
	//get, set, search 등의 함수는 자유 구현

}
/**
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다.
 * instruction과 관련된 정보들을 저장하고 기초적인 연산을 수행한다.
 */
class Instruction {
	/* 
	 * 각자의 inst.data 파일에 맞게 저장하는 변수를 선언한다.
	 *  
	 * ex)
	 * String instruction;
	 * int opcode;
	 * int numberOfOperand;
	 * String comment;
	 */
	String opcode;
	String numberofOperand;
	
	/** instruction이 몇 바이트 명령어인지 저장. 이후 편의성을 위함 */
	String format;
	
	/**
	 * 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public void parsing(String line) {
		// TODO Auto-generated method stub
		//inst.dat에서 라인별로 입력받은 문자열을 공배글 기준으로 토큰한다.
		StringTokenizer st  =  new StringTokenizer(line," ");
		String  a  =  st.nextToken();              
		String  b  =  st.nextToken();              
		String  c  =  st.nextToken();   
		
		format = a;
		opcode = b;
		numberofOperand = c;
	}
}
