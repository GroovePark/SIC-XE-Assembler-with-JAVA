package project1b;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.print.attribute.IntegerSyntax;


/**
 * Assembler : 
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. <br>
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. <br>
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. <br>
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) <br>
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) <br>
 * 
 * <br><br>
 * 작성중의 유의사항 : <br>
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.<br>
 *  2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br><br>
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간*/
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간*/
	ArrayList<TokenTable> TokenList;
	// literal처리를 위해 리터럴들과 주소값을 모아놓은 공간 //
	HashMap<String, Integer> literaltable;
	// 각 sect의 크기를 저장한 공간 //
	ArrayList<Integer> sectsize;
	// modify레코드를 작성하기 위해 수정해야할 곳의 주소, 주소로부터 문자몇개를 수정해야하는지, 연산해줘야할 값의 변수이름을 저장 ///
	ArrayList<Integer> modifytableloc;
	ArrayList<Integer> modifytablenum;
	ArrayList<String> modifytablename;
	/** 
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간. <br>
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;
	
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	int base;
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름. 
	 * @throws IOException 
	 */
	public Assembler(String instFile) throws IOException {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
		sectsize = new ArrayList<Integer>();
		literaltable = new HashMap<String, Integer>();
	
		modifytableloc = new ArrayList<Integer>();
		modifytablenum = new ArrayList<Integer>();
		modifytablename = new ArrayList<String>();
		
	}

	/** 
	 * 어셈블러의 메인 루틴
	 * @throws IOException 
	 * @throws CloneNotSupportedException 
	 */
	public static void main(String[] args) throws IOException, CloneNotSupportedException {
		Assembler assembler = new Assembler("inst.dat");
		
		
		assembler.loadInputFile("input.txt");
		
		assembler.pass1();
		assembler.printSymbolTable("symtab_20142362.txt");
		
		assembler.pass2();
		assembler.printObjectCode("output_20142362.txt");
		
	}

	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.<br>
	 * @param fileName : 저장되는 파일 이름
	 * @throws IOException 
	 */
	private void printObjectCode(String fileName) throws IOException {
		// TODO Auto-generated method stub
		int i;
		PrintWriter writer = new PrintWriter(fileName);
		for(i=0;i<codeList.size();i++)
				writer.println(codeList.get(i));
		
		writer.close();
	}
	
	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.<br>
	 * @param fileName : 저장되는 파일 이름
	 * @throws IOException 
	 */
	private void printSymbolTable(String fileName) throws IOException {
		// TODO Auto-generated method stub
		int i,j;
		PrintWriter writer = new PrintWriter(fileName);
		for(i=0;i<symtabList.size();i++)
		{
			for(j=0;j<symtabList.get(i).symbolList.size();j++)
			{
				String s = String.format("%X",symtabList.get(i).locationList.get(j));
				writer.printf("%6s        %s",symtabList.get(i).symbolList.get(j),s);
				writer.println("");
			}
			writer.println("");
		}
		
		writer.close();
		
		
	}

	/** 
	 * pass1 과정을 수행한다.<br>
	 *   1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성<br>
	 *   2) label을 symbolTable에 정리<br>
	 *   <br><br>
	 *    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 * @throws CloneNotSupportedException 
	 */
	private void pass1(){
		// TODO Auto-generated method stub
	int i,j,k; // 반복문 처리용
	int locctr,flag;//현재위치주소,리터럴중복처리방지플래그
	
	//각 섹터별로 토큰테이블 선언 토큰테이블에 해당 섹터의 토큰들이 다 차면 토큰리스트로 add해준다
	TokenTable tokentable = new TokenTable(null, instTable);
	TokenTable tokentable1 = new TokenTable(null, instTable);
	TokenTable tokentable2 = new TokenTable(null, instTable);
	TokenTable tokentable3 = new TokenTable(null, instTable);
	
	//일단 input파일의 첫줄부터 끝줄까지 저장해준다
	for(i=0; i<lineList.size(); i++)
	{	
			tokentable.putToken(lineList.get(i));
	}
	int sectnum = 0;
	
	
	//토큰테이블의 섹터를 나누는 과정
	for(i=0; i<lineList.size();i++)
	{	
		// 각 섹터 사이에 있는 .. subroutine to read.... 제거 과정
		if(lineList.get(i).charAt(0) != '.')
		{	
			////////////////////////////////////////////////////////////////////////////////////////
			if(sectnum == 0) //sectnum 변수를 통해 섹터분할
			{
			tokentable1.putToken(lineList.get(i));
				//CSECT를 기준으로 섹터를 나눈다.
				if(tokentable.tokenList.get(i).operator.equals("CSECT") || tokentable.tokenList.get(i).operator.equals("END"))
				{	
				tokentable1.tokenList.remove(tokentable1.tokenList.size()-1);
				tokentable2.putToken(lineList.get(i));	
				TokenList.add(tokentable1);
				sectnum++;
				
				}
			}
			////////////////////////////////////////////////////////////////////////////////////////
			else if(sectnum == 1)
			{
			tokentable2.putToken(lineList.get(i));
			
				if(tokentable.tokenList.get(i).operator.equals("CSECT") || tokentable.tokenList.get(i).operator.equals("END"))
				{	
				tokentable2.tokenList.remove(tokentable2.tokenList.size()-1);
				tokentable3.putToken(lineList.get(i));	
				TokenList.add(tokentable2);
				sectnum++;
				
				}
			}
			////////////////////////////////////////////////////////////////////////////////////////
			else if(sectnum == 2)
			{
			tokentable3.putToken(lineList.get(i));
			
				if(tokentable.tokenList.get(i).operator.equals("CSECT") || tokentable.tokenList.get(i).operator.equals("END"))
				{	
				
				TokenList.add(tokentable3);
				sectnum++;
				
				}
			}
		}
	}
	
	//토큰리스트에 있는 operator값을 inst.dat에 저장되있는 opcode와 매칭시켜주는 과정
	//매칭된 opcode는 tokentable의 comment자리에 들어가고 opcode가 없는 operator에는 
	//comment자리에 공백들어감(TokenTable 클라스 참고)
	for(i=0;i<TokenList.size();i++)
	{	
		for(j=0; j<TokenList.get(i).tokenList.size(); j++)
		{	
			if(instTable.instMap.containsKey(TokenList.get(i).tokenList.get(j).operator))
			{	
				
				TokenList.get(i).tokenList.get(j).comment = 
						instTable.instMap.get(TokenList.get(i).tokenList.get(j).operator).opcode;
			}
			else if(TokenList.get(i).tokenList.get(j).operator.charAt(0) == '+')
			{
				String format4 = TokenList.get(i).tokenList.get(j).operator;
				StringTokenizer st = new StringTokenizer(TokenList.get(i).tokenList.get(j).operator,"+");
				TokenList.get(i).tokenList.get(j).operator = st.nextToken();
				TokenList.get(i).tokenList.get(j).comment = 
						instTable.instMap.get(TokenList.get(i).tokenList.get(j).operator).opcode;
				TokenList.get(i).tokenList.get(j).operator = format4;
			}
		}
	}
	
	//섹터분할이 끝났으므로 섹터별로 그리고 라인별로 분석을 통해 locctr값을 이용 symboltable을 생성하는 구간이다.
	//각 섹터별로 심볼테이블을 선언해주고, 심볼테이블이 다 차면 심볼리스트에 add해준다.
	SymbolTable symboltable1 = new SymbolTable();
	SymbolTable symboltable2 = new SymbolTable();
	SymbolTable symboltable3 = new SymbolTable();
	sectnum = 0;
	//섹터별 분석
	for(i=0;i<TokenList.size();i++)
	{	
		
		locctr = 0;
		////////////////////////////////////////////////////////////////////////////////////////
		if(sectnum==0)
		{
		//라인별 분석
		for(j=0; j<TokenList.get(i).tokenList.size(); j++)
		{	
			//label값이 있으면 현재주소와 라벨 이름을 심볼테이블에 저장한다
			if(!TokenList.get(i).tokenList.get(j).label.equals(" "))
			{	
				
				symboltable1.putSymbol(TokenList.get(i).tokenList.get(j).label, locctr);
			}
			//Start일때 operand에 있는 시작주소 값을 integer형을 캐스팅하고 시작locctr에 넣어준다
			if(TokenList.get(i).tokenList.get(j).operator.equals("Start"))
			{
				locctr = Integer.parseInt(TokenList.get(i).tokenList.get(j).operand[0]);
				TokenList.get(i).tokenList.get(j).location = locctr;
			}
			//operator RESW일때 할당된 워드 x3큼 locctr 증가
			else if(TokenList.get(i).tokenList.get(j).operator.equals("RESW"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + 3*Integer.parseInt(TokenList.get(i).tokenList.get(j).operand[0]);
			}
			//operator RESB일때
			else if(TokenList.get(i).tokenList.get(j).operator.equals("RESB"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + Integer.parseInt(TokenList.get(i).tokenList.get(j).operand[0]);
			}
			//operator BYTE일때
			else if(TokenList.get(i).tokenList.get(j).operator.equals("BYTE"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + 1;
			}
			//operator WORD일때
			else if(TokenList.get(i).tokenList.get(j).operator.equals("WORD"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + 3;
			}
			
			// operator CSECT일때 locctr값 0으로 초기화
			else if(TokenList.get(i).tokenList.get(j).operator.equals("CSECT"))
			{
				locctr = 0;
				TokenList.get(i).tokenList.get(j).location = locctr;
			}
			
			// operator LTORG일때
			else if(TokenList.get(i).tokenList.get(j).operator.equals("LTORG"))
			{
				for(k=0;k<j;k++)
				{ 	//LTORG 전에 '='값이 있는 것들을 찾아서 literal_table에 넣어준다.
					if(TokenList.get(i).tokenList.get(k).operand[0].charAt(0) == '=')
					{
						flag = 0;
						//리터럴 테이블 값을 검사해서 값이 중복저장되지 않게 한다
						if(literaltable.containsKey(TokenList.get(i).tokenList.get(k).operand[0]))
							flag = 1;
						if(flag == 0)
						{	// 캐릭터 값인 경우
							if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'C')
							{
								literaltable.put(TokenList.get(i).tokenList.get(k).operand[0], locctr);
								locctr = locctr + TokenList.get(i).tokenList.get(k).operand[0].length() - 4;
								//-4를 해주는 이유는 =,C,',' 이렇게 4개의 문자 제외하기 때문
							}
							// 16진수 값인 경우 
							else if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'X')
							{
								literaltable.put(TokenList.get(i).tokenList.get(k).operand[0], locctr);
								locctr = locctr + (TokenList.get(i).tokenList.get(k).operand[0].length() - 4) / 2;
								//마찬가지로 4빼주고 16진수는 2문자당 1바이트이므로 2로 나눠줌
							}
						}
							
					}
				}
			}
			
			//심볼테이블에 absolute expression 값을 저장하는 부분
			else if(TokenList.get(i).tokenList.get(j).operator.equals("EQU") && !TokenList.get(i).tokenList.get(j).operand[0].equals("*"))
			{
				int index,numofoperand=0 , numofoperator = 0;
				int value = 0;
				String data;
				StringTokenizer st = new StringTokenizer(TokenList.get(i).tokenList.get(j).operand[0],"+-/*",true);
				String[] operator = new String[3];
				//각 연산자와 피연산자를 구분하여 토큰해준다. 연산자는 오퍼레이터 배열에 넣어줌
				while(st.hasMoreTokens())
				{
					data = st.nextToken();
					if(data.length() == 1)
					{
						operator[numofoperator] = data;
						numofoperator++;
					}
					else
					{
						TokenList.get(i).tokenList.get(j).operand[numofoperand] = data;
						numofoperand++;
					}
				}
				// 각 피연산자 갯수만큼 루프를 돌면서 값을 계산해준다
				for(k=0;k<numofoperand;k++)
				{	
					if(k==0)
					{
						index = symboltable1.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
						value = symboltable1.locationList.get(index);
					}
					else
					{
						if(operator[k-1].equals("+"))
						{
							index = symboltable1.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
							value = value + symboltable1.locationList.get(index);
						}
						if(operator[k-1].equals("-"))
						{
							index = symboltable1.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
							value = value - symboltable1.locationList.get(index);
						}
						if(operator[k-1].equals("/"))
						{
							index = symboltable1.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
							value = value / symboltable1.locationList.get(index);
						}
						if(operator[k-1].equals("*"))
						{
							index = symboltable1.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
							value = value - symboltable1.locationList.get(index);
						}
						
					}
				}
				symboltable1.modifySymbol(TokenList.get(i).tokenList.get(j).label, value);
			}
			//END인 경우 처음부터 루프를 돌면서 '='인 것들을 찾아주고 이것들중
			//literal_table에 없는 것들을 테이블에 넣어준다.
			//(literal_table에 있는 것은 LTORG에서 이미 처리
			else if(TokenList.get(i).tokenList.get(j).operator.equals("END"))
			{
				for(k=0;k<j;k++)
				{ 
					if(TokenList.get(i).tokenList.get(k).operand[0].charAt(0) == '=')
					{
						flag = 0;
						if(literaltable.containsKey(TokenList.get(i).tokenList.get(k).operand[0]))
							flag = 1;
						if(flag == 0)
						{
							if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'C')
							{
								literaltable.put(TokenList.get(i).tokenList.get(k).operand[0], locctr);
								locctr = locctr + TokenList.get(i).tokenList.get(k).operand[0].length() - 4;
							}
							else if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'X')
							{
								literaltable.put(TokenList.get(i).tokenList.get(k).operand[0], locctr);
								locctr = locctr + (TokenList.get(i).tokenList.get(k).operand[0].length() - 4) / 2;
							}
						}
							
					}
				}
			}
			//그외의 경우 (opcode가 있는 경우)
			else
			{	
				TokenList.get(i).tokenList.get(j).location = locctr;
				//4형식일시
				if(TokenList.get(i).tokenList.get(j).operator.charAt(0) == '+')
					locctr = locctr + 4;
				else
				{	String format = null;
					if(instTable.instMap.containsKey(TokenList.get(i).tokenList.get(j).operator))
					{
					format = instTable.instMap.get(TokenList.get(i).tokenList.get(j).operator).format;
					//3형식
					if(format.equals("3/4"))
						locctr = locctr + 3;
					//2형식
					else if(format.equals("2"))
						locctr = locctr + 2;
					}
				}
			}
			//섹터의 마지막 줄이 경우 
			if(j == TokenList.get(i).tokenList.size()-1)
			{	
				
				symtabList.add(symboltable1);
				sectnum++;
				sectsize.add(locctr);
		}
			
			
		}
		}
		//sectnum을 1증가시키고 위와 똑같은 과정을 반복한다.
		////////////////////////////////////////////////////////////////////////////////////////
		else if(sectnum==1)
		{
		for(j=0; j<TokenList.get(i).tokenList.size(); j++)
		{	
			if(!TokenList.get(i).tokenList.get(j).label.equals(" "))
			{	
				
				symboltable2.putSymbol(TokenList.get(i).tokenList.get(j).label, locctr);
			}
			
			
			
			if(TokenList.get(i).tokenList.get(j).operator.equals("Start"))
			{
				locctr = Integer.parseInt(TokenList.get(i).tokenList.get(j).operand[0]);
				TokenList.get(i).tokenList.get(j).location = locctr;
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("RESW"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + 3*Integer.parseInt(TokenList.get(i).tokenList.get(j).operand[0]);
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("RESB"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + Integer.parseInt(TokenList.get(i).tokenList.get(j).operand[0]);
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("BYTE"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + 1;
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("WORD"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + 3;
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("CSECT"))
			{
				locctr = 0;
				TokenList.get(i).tokenList.get(j).location = locctr;
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("LTORG"))
			{
				for(k=0;k<j;k++)
				{ 
					if(TokenList.get(i).tokenList.get(k).operand[0].charAt(0) == '=')
					{
						flag = 0;
						if(literaltable.containsKey(TokenList.get(i).tokenList.get(k).operand[0]))
							flag = 1;
						if(flag == 0)
						{
							if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'C')
							{
								literaltable.put(TokenList.get(i).tokenList.get(k).operand[0], locctr);
								locctr = locctr + TokenList.get(i).tokenList.get(k).operand[0].length() - 4;
							}
							else if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'X')
							{
								literaltable.put(TokenList.get(i).tokenList.get(k).operand[0], locctr);
								locctr = locctr + (TokenList.get(i).tokenList.get(k).operand[0].length() - 4) / 2;
							}
						}
							
					}
				}
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("EQU") && !TokenList.get(i).tokenList.get(j).operand[0].equals("*"))
			{
				int index,numofoperand=0 , numofoperator = 0;
				int value = 0;
				String data;
				StringTokenizer st = new StringTokenizer(TokenList.get(i).tokenList.get(j).operand[0],"+-/*",true);
				String[] operator = new String[3];
				while(st.hasMoreTokens())
				{
					data = st.nextToken();
					if(data.length() == 1)
					{
						operator[numofoperator] = data;
						numofoperator++;
					}
					else
					{
						TokenList.get(i).tokenList.get(j).operand[numofoperand] = data;
						numofoperand++;
					}
				}
				
				for(k=0;k<numofoperand;k++)
				{	
					if(k==0)
					{
						index = symboltable2.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
						value = symboltable2.locationList.get(index);
					}
					else
					{
						if(operator[k-1].equals("+"))
						{
							index = symboltable2.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
							value = value + symboltable2.locationList.get(index);
						}
						if(operator[k-1].equals("-"))
						{
							index = symboltable2.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
							value = value - symboltable2.locationList.get(index);
						}
						if(operator[k-1].equals("/"))
						{
							index = symboltable2.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
							value = value / symboltable2.locationList.get(index);
						}
						if(operator[k-1].equals("*"))
						{
							index = symboltable2.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
							value = value - symboltable2.locationList.get(index);
						}
						
					}
				}
				symboltable2.modifySymbol(TokenList.get(i).tokenList.get(j).label, value);
				
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("END"))
			{
				for(k=0;k<j;k++)
				{ 
					if(TokenList.get(i).tokenList.get(k).operand[0].charAt(0) == '=')
					{
						flag = 0;
						if(literaltable.containsKey(TokenList.get(i).tokenList.get(k).operand[0]))
							flag = 1;
						if(flag == 0)
						{
							if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'C')
							{
								literaltable.put(TokenList.get(i).tokenList.get(k).operand[0], locctr);
								locctr = locctr + TokenList.get(i).tokenList.get(k).operand[0].length() - 4;
							}
							else if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'X')
							{
								literaltable.put(TokenList.get(i).tokenList.get(k).operand[0], locctr);
								locctr = locctr + (TokenList.get(i).tokenList.get(k).operand[0].length() - 4) / 2;
							}
						}
							
					}
				}
			}
			else
			{	
				TokenList.get(i).tokenList.get(j).location = locctr;
				if(TokenList.get(i).tokenList.get(j).operator.charAt(0) == '+')
					locctr = locctr + 4;
				else
				{	String format = null;
					if(instTable.instMap.containsKey(TokenList.get(i).tokenList.get(j).operator))
					{
					format = instTable.instMap.get(TokenList.get(i).tokenList.get(j).operator).format;
					
					if(format.equals("3/4"))
						locctr = locctr + 3;
					else if(format.equals("2"))
						locctr = locctr + 2;
					}
				}
			}
			
			if(j == TokenList.get(i).tokenList.size()-1)
			{
				symtabList.add(symboltable2);
				sectnum++;
				sectsize.add(locctr);
			}
		}
		}
		//sectnum을 1늘리고 위와 같은 과정 반복
		////////////////////////////////////////////////////////////////////////////////////////
		else if(sectnum==2)
		{
		for(j=0; j<TokenList.get(i).tokenList.size(); j++)
		{	
			if(!TokenList.get(i).tokenList.get(j).label.equals(" "))
			{	
				symboltable3.putSymbol(TokenList.get(i).tokenList.get(j).label, locctr);
			}
			if(TokenList.get(i).tokenList.get(j).operator.equals("Start"))
			{
				locctr = Integer.parseInt(TokenList.get(i).tokenList.get(j).operand[0]);
				TokenList.get(i).tokenList.get(j).location = locctr;
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("RESW"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + 3*Integer.parseInt(TokenList.get(i).tokenList.get(j).operand[0]);
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("RESB"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + Integer.parseInt(TokenList.get(i).tokenList.get(j).operand[0]);
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("BYTE"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + 1;
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("WORD"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + 3;
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("CSECT"))
			{
				locctr = 0;
				TokenList.get(i).tokenList.get(j).location = locctr;
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("LTORG"))
			{
				for(k=0;k<j;k++)
				{ 
					if(TokenList.get(i).tokenList.get(k).operand[0].charAt(0) == '=')
					{
						flag = 0;
						if(literaltable.containsKey(TokenList.get(i).tokenList.get(k).operand[0]))
							flag = 1;
						if(flag == 0)
						{
							if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'C')
							{
								literaltable.put(TokenList.get(i).tokenList.get(k).operand[0], locctr);
								locctr = locctr + TokenList.get(i).tokenList.get(k).operand[0].length() - 4;
							}
							else if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'X')
							{
								literaltable.put(TokenList.get(i).tokenList.get(k).operand[0], locctr);
								locctr = locctr + (TokenList.get(i).tokenList.get(k).operand[0].length() - 4) / 2;
							}
						}
							
					}
				}
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("EQU") && !TokenList.get(i).tokenList.get(j).operand[0].equals("*"))
			{
				int index,numofoperand=0 , numofoperator = 0;
				int value = 0;
				String data;
				StringTokenizer st = new StringTokenizer(TokenList.get(i).tokenList.get(j).operand[0],"+-/*",true);
				String[] operator = new String[3];
				while(st.hasMoreTokens())
				{
					data = st.nextToken();
					if(data.length() == 1)
					{
						operator[numofoperator] = data;
						numofoperator++;
					}
					else
					{
						TokenList.get(i).tokenList.get(j).operand[numofoperand] = data;
						numofoperand++;
					}
				}
				
				for(k=0;k<numofoperand;k++)
				{	
					if(k==0)
					{
						index = symboltable3.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
						value = symboltable3.locationList.get(index);
					}
					else
					{
						if(operator[k-1].equals("+"))
						{
							index = symboltable3.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
							value = value + symboltable3.locationList.get(index);
						}
						if(operator[k-1].equals("-"))
						{
							index = symboltable1.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
							value = value - symboltable3.locationList.get(index);
						}
						if(operator[k-1].equals("/"))
						{
							index = symboltable1.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
							value = value / symboltable3.locationList.get(index);
						}
						if(operator[k-1].equals("*"))
						{
							index = symboltable3.symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
							value = value - symboltable3.locationList.get(index);
						}
						
					}
				}
				symboltable3.modifySymbol(TokenList.get(i).tokenList.get(j).label, value);
				
			}
			else if(TokenList.get(i).tokenList.get(j).operator.equals("END"))
			{
				for(k=0;k<j;k++)
				{ 
					if(TokenList.get(i).tokenList.get(k).operand[0].charAt(0) == '=')
					{
						flag = 0;
						if(literaltable.containsKey(TokenList.get(i).tokenList.get(k).operand[0]))
							flag = 1;
						if(flag == 0)
						{
							if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'C')
							{
								literaltable.put(TokenList.get(i).tokenList.get(k).operand[0], locctr);
								locctr = locctr + TokenList.get(i).tokenList.get(k).operand[0].length() - 4;
							}
							else if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'X')
							{
								literaltable.put(TokenList.get(i).tokenList.get(k).operand[0], locctr);
								locctr = locctr + (TokenList.get(i).tokenList.get(k).operand[0].length() - 4) / 2;
							}
						}
							
					}
				}
			}
			else
			{	
				TokenList.get(i).tokenList.get(j).location = locctr;
				if(TokenList.get(i).tokenList.get(j).operator.charAt(0) == '+')
					locctr = locctr + 4;
				else
				{	String format = null;
					if(instTable.instMap.containsKey(TokenList.get(i).tokenList.get(j).operator))
					{
					format = instTable.instMap.get(TokenList.get(i).tokenList.get(j).operator).format;
					
					if(format.equals("3/4"))
						locctr = locctr + 3;
					else if(format.equals("2"))
						locctr = locctr + 2;
					}
				}
			}
			
			if(j == TokenList.get(i).tokenList.size()-1)
			{
				symtabList.add(symboltable3);
				sectnum++;
				sectsize.add(locctr);
			}
			
		}
		}
		
	}
	}
	
	/**
	 * pass2 과정을 수행한다.<br>
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
	//반복문, symboltable인덱스,텍스트레코드관련플래그 ,리터럴 중복방지할때 쓰는 변수들
	int i,j,k,m,index,textflag,buffersize,literalflag,usedlitnum;
	String code = null; //오브젝트 코드생성할때 쓰는 문자열
	String buffer = null; //오브젝트 코드 생성할때 쓰는 문자열2(code보다 작은 단위로)
	textflag = 1;
	literalflag = 0;
	usedlitnum = 0;
	String[] used_literal = new String[3]; //리터럴방지를 위해 사용한문자는 이 배열에 넣어줌
	String[] extref = new String[4]; // modify레코드 작성위한 용도 현재 섹터의 extref값
	
	//초기화과정//
	for(i=0;i<3;i++)
		used_literal[i] = "";
	for(i=0;i<4;i++)
		extref[i] = "";
	
	//섹터별분석
	for(i=0;i<TokenList.size();i++)
	{	
		textflag = 1;
		buffersize = 0;
		//어셈블리 코드를 라인별로 순차적으로 분석한다.
		//라인별로 if문을 통해 경우를 설정 각 경우에 알맞은 머신코드를 생성한다.
		for(j=0;j<TokenList.get(i).tokenList.size();j++)
		{	
			// operand값을 검사하여 참조된 값이면 modify레코드에 써준다.
			for(m=0;m<4;m++)
				if(TokenList.get(i).tokenList.get(j).operand[0].equals(extref[m]))
				{
					modifytableloc.add(TokenList.get(i).tokenList.get(j).location+1);
					modifytablenum.add(5);
					modifytablename.add("+"+TokenList.get(i).tokenList.get(j).operand[0]);
				}
			//start 또는 섹터의 첫시작일때 헤더레코드를 작성한다.
			if(TokenList.get(i).tokenList.get(j).operator.equals("START") || j == 0)
			{
				code = "H";
				buffer = String.format("%-6s", TokenList.get(i).tokenList.get(j).label);
				code = code.concat(buffer);
				buffer = String.format("%06X",TokenList.get(i).tokenList.get(j).location);
				code = code.concat(buffer);
				buffer = String.format("%06X",sectsize.get(i));
				code = code.concat(buffer);
				codeList.add(code);
			}
			//EXTDEF일때 D레코드를 작성한다.
			else if(TokenList.get(i).tokenList.get(j).operator.equals("EXTDEF"))
			{
				code = "D";
				for(k=0;k<4;k++)
					if(TokenList.get(i).tokenList.get(j).operand[k] != null)
					{
						code = code.concat(TokenList.get(i).tokenList.get(j).operand[k]);
						index = symtabList.get(i).symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[k]);
						buffer = String.format("%06X",symtabList.get(i).locationList.get(index));
						code = code.concat(buffer);
					}
				codeList.add(code);
			}
			//EXTREF일때 R레코드를 작성한다.
			else if(TokenList.get(i).tokenList.get(j).operator.equals("EXTREF"))
			{	
				code = "R";
				for(k=0;k<4;k++)
					if(TokenList.get(i).tokenList.get(j).operand[k] != null)
					{
						extref[k]=TokenList.get(i).tokenList.get(j).operand[k];
						buffer = String.format("%-6s",TokenList.get(i).tokenList.get(j).operand[k]);
						code = code.concat(buffer);
					}
				codeList.add(code);
			}
			//코멘트에 매칭된 opcode 값이 들어 있을때
			else if(!TokenList.get(i).tokenList.get(j).comment.equals(" "))
			{	
				if(textflag == 1) // 첫번째 줄이라 레코드의 헤더를 작성해줘야할때 플래그
				{
					code = "T";
					buffer = String.format("%06X", TokenList.get(i).tokenList.get(j).location);
					code = code.concat(buffer);
					textflag = 0;
				}
				//각 연산자별로 나누어서 출력한다.
				if(TokenList.get(i).tokenList.get(j).operator.charAt(0) == '+')
				{
					buffer = format4(i,j);
					buffersize = buffer.length();
					
				}
				else if(instTable.instMap.get(TokenList.get(i).tokenList.get(j).operator).format.equals("2"))
				{
					buffer = format2(i,j);
					buffersize = buffer.length();
					
				}	
				else if(instTable.instMap.get(TokenList.get(i).tokenList.get(j).operator).format.equals("3/4"))
				{
					buffer = format3(i,j);
					buffersize = buffer.length();
					
				}
				// 머신코드를 쓰기전에 만약 쓰여지게 되면 60문자를 초과하는지 검사한다.
				// 67인 이유는 앞의 시작주소값와 'h'문자를 더해줘서 그렇다.
				if((code.length()+buffersize)>67)
				{	
					StringBuffer put_length = new StringBuffer(code);
					//column의 길이를 length에 넣어주고
					String length = String.format("%02X",(code.length()-7)/2);
					//시작주소값뒤에 넣어준다.
					put_length.insert(7, length);
					code = String.format("%s",put_length);
					//codelist에 추가 codelist는 한줄씩 나누어져있다.
					codeList.add(code);
					//써주려고 햇던 머신코드 값을 다음줄에써준다
					code = "T";
					code = code.concat(String.format("%06X", TokenList.get(i).tokenList.get(j).location));
					code = code.concat(buffer);
				}
				//60초과 안할시 그냥 써준다.
				else
					code = code.concat(buffer);
			
			}
			//base일때 base값을 구해준다. 심볼테이블에서 찾음.
			if(TokenList.get(i).tokenList.get(j).operator.equals("BASE"))
			{	
				index = symtabList.get(i).symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[0]);
				base = symtabList.get(i).locationList.get(index);
			}
			
			//LTORG일때 처리과정 C인지 X인지 나눠서 처리
			if(TokenList.get(i).tokenList.get(j).operator.equals("LTORG"))
			{	
				for(k=0;k<j;k++)
				{
					if(TokenList.get(i).tokenList.get(k).operand[0].charAt(0)=='=')
					{	
						//이미 처리한 literal인지 확인하는 과정이다. 사용시 flag 1로 세팅
						for(m=0;m<3;m++)
						{
							if(TokenList.get(i).tokenList.get(k).operand[0].equals(used_literal[m]))
								literalflag = 1;
						}
						//새로운 literal이라서 처리해줘야할때
						if(literalflag==0)
						{	
							used_literal[usedlitnum] = TokenList.get(i).tokenList.get(k).operand[0];
							usedlitnum++;
							/*캐릭터형인 경우 C,=,' 을 없애주고 나머지 문자를 
							16진수 아스키형을 바꿔서 오브젝트프로그램에 써주는 과정*/
							if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'C')
							{
								String str = TokenList.get(i).tokenList.get(k).operand[0];
								String asc;
								//맨앞의 3문자 c=' 맨뒤의 ' 제거
								str = str.substring(3,str.length()-1);
								for(m=0;m<str.length();m++)
								{	
									asc = String.format("%X",(int) str.charAt(m));
									if(m==0)
										buffer = asc;
									else
										buffer = buffer.concat(asc);
									
								}
							}
							//16진수 형은 X,=,' 을 없애주고 그대로 오브젝트프로그램에 써주면 됨
							if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'X')
							{
								String str = TokenList.get(i).tokenList.get(k).operand[0];
								str = str.substring(3,str.length()-1);
								buffer = str;
							}
							
							code = code.concat(buffer);
							
						}
					}
				}
				
			}
			// BYTE선언의 오브젝트 코드를 생성하는 부분
			// 캐릭터 타입은 아스키 코드값으로 출력해야하고
			// 16진수 타입은 그냥 출력 하면 된다. (위의 LTORG와 같은 기법)
			if(TokenList.get(i).tokenList.get(j).operator.equals("BYTE"))
			{
				if(TokenList.get(i).tokenList.get(j).operand[0].charAt(0) == 'C')
				{
					String str = TokenList.get(i).tokenList.get(j).operand[0];
					String asc;
					str = str.substring(2,str.length()-1);
					for(m=0;m<str.length();m++)
					{	
						asc = String.format("%X",(int) str.charAt(m));
						if(m==0)
							buffer = asc;
						else
							buffer = buffer.concat(asc);
						
					}
				}
				if(TokenList.get(i).tokenList.get(j).operand[0].charAt(0) == 'X')
				{
					String str = TokenList.get(i).tokenList.get(j).operand[0];
					str = str.substring(2,str.length()-1);
					buffer = str;
				}
				
				code = code.concat(buffer);
			}
			/*WORD인 경우 오퍼랜드 값을 '-+/*'기준으로 나눠주고 
			처리되지 않은 modify_table을 처리해줌
			각자값이 EXREF에 선언되어있으면 modify 테이블에 써준다.*/
			if(TokenList.get(i).tokenList.get(j).operator.equals("WORD"))
			{	
				int numofoperand=0 , numofoperator = 0;;
				String data;
				StringTokenizer st = new StringTokenizer(TokenList.get(i).tokenList.get(j).operand[0],"+-/*",true);
				String[] operator = new String[3];
				//피연산자 토큰과 더불어 연산자들은 operator배열에 넣어준다.
				while(st.hasMoreTokens())
				{
					data = st.nextToken();
					if(data.length() == 1)
					{
						operator[numofoperator] = data;
						numofoperator++;
					}
					else
					{
						TokenList.get(i).tokenList.get(j).operand[numofoperand] = data;
						numofoperand++;
					}
				}
				for(k=0;k<4;k++)
				{	//extref[] 에 값이 있어 Absolute Expression을 해야 할때
					if(TokenList.get(i).tokenList.get(j).operand[0].equals(extref[k]))
					{	
						for(m=0;m<numofoperand;m++)
						{	
							//첫번째 피연산자는 +이므로
							if(m==0)
							{
							modifytablename.add("+"+TokenList.get(i).tokenList.get(j).operand[m]);
							modifytablenum.add(6);
							modifytableloc.add(TokenList.get(i).tokenList.get(j).location);
							}
							//그뒤로는 operator배열에 있는 연산자들은 써준다.
							else
							{
								modifytablename.add(operator[m-1]+TokenList.get(i).tokenList.get(j).operand[m]);
								modifytablenum.add(6);
								modifytableloc.add(TokenList.get(i).tokenList.get(j).location);
							}
						}
						//참조되어서 현재 값을 알수없으므로 000000으로 오브젝트코드 써줌
						code = code.concat("000000");
						
					}
				}
			}
			//END일때
			//LTORG뒤에 나온 리터럴 처리해준다.
			if(TokenList.get(i).tokenList.get(j).operator.equals("END"))
			{
				for(k=0;k<j;k++)
				{
					if(TokenList.get(i).tokenList.get(k).operand[0].charAt(0)=='=')
					{	
						for(m=0;m<3;m++)
						{
							if(TokenList.get(i).tokenList.get(k).operand[0].equals(used_literal[m]))
								literalflag = 1;
						}
						if(literalflag==0)
						{	
							used_literal[usedlitnum] = TokenList.get(i).tokenList.get(k).operand[0];
							usedlitnum++;
							if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'C')
							{
								String str = TokenList.get(i).tokenList.get(k).operand[0];
								String asc;
								str = str.substring(3,str.length()-1);
								for(m=0;m<str.length();m++)
								{	
									asc = String.format("%X",(int) str.charAt(m));
									if(m==0)
										buffer = asc;
									else
										buffer = buffer.concat(asc);	
								}
							}
							if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'X')
							{
								String str = TokenList.get(i).tokenList.get(k).operand[0];
								str = str.substring(3,str.length()-1);
								buffer = str;
							}
							
							code = code.concat(buffer);
							
						}
					}
				}
			}
			
			//섹터의 마지막 줄일때  modify테이블을 보고 modify레코드를 작성해준뒤 E레코드도 작성해준다
			if(j==(TokenList.get(i).tokenList.size()) - 1) 
			{	
				StringBuffer put_length = new StringBuffer(code);
				String length = String.format("%02X",(code.length()-7)/2);
				put_length.insert(7, length);
				code = String.format("%s",put_length);
				codeList.add(code);
				for(m=0;m<modifytableloc.size();m++)
				{
					buffer = String.format("M%06X%02X%s",modifytableloc.get(m),
							modifytablenum.get(m),modifytablename.get(m));
					codeList.add(buffer);
				}
			//작성후 modify테이블 extref배열 초기화(다음섹터에서 사용해야하므로)
			modifytableloc.clear();
			modifytablenum.clear();
			modifytablename.clear();
			for(m=0;m<4;m++)
				extref[m]="";
			//E레코드 써줌
			buffer = "E";
			codeList.add(buffer);
			codeList.add("");
			
			}
		}
	}
	
	}
	
	/**
	 * inputFile을 읽어들여서 lineList에 저장한다.<br>
	 * @param inputFile : input 파일 이름.
	 * @throws IOException 
	 */
	private void loadInputFile(String inputFile) throws IOException {
		// TODO Auto-generated method stub
	    BufferedReader reader = new BufferedReader(new FileReader(inputFile));
	    String line = "";
	    while((line = reader.readLine()) != null) {
	    	lineList.add(line);
	    }
	    reader.close();
	}
	
	//format2함수 operator가 2형식일때 머신코드 생성
	public String format2(int sectnum,int linenum)
	{
		int opnum;
		String buffer;
		opnum = 0;
		//operand 갯수 구하는 과정
		while(TokenList.get(sectnum).tokenList.get(linenum).operand[opnum] != null)
			opnum++;
		//operand갯수가 1개일때
		if(opnum == 1)
		{
			buffer = String.format("%s%X0",
					TokenList.get(sectnum).tokenList.get(linenum).comment,
					RegisterToDecnum(TokenList.get(sectnum).tokenList.get(linenum).operand[0]));
		}
		// operand 갯수가 2개 일때 기계어 코드 구하는 과정
		else 
		{
			buffer = String.format("%s%X%X",
					TokenList.get(sectnum).tokenList.get(linenum).comment,
					RegisterToDecnum(TokenList.get(sectnum).tokenList.get(linenum).operand[0]),
					RegisterToDecnum(TokenList.get(sectnum).tokenList.get(linenum).operand[1]));
		}
		return buffer;
	}
	// formatt3 함수
	// operator가 3형식일때 라인넘버에서 생성되는 머신코드 
	//  값을 머신코드 포인터에 저장한다.
	public String format3(int sectnum,int linenum)
	{
		int a,b,nDisp,index;
		String buffer = null;
		String buffer2;
		String buffer3;
		
		if(TokenList.get(sectnum).tokenList.get(linenum).operand[1] != null)
		{		
			if(TokenList.get(sectnum).tokenList.get(linenum).operand[1].equals("X"))
			{	//index 값이 있을 때
				TokenList.get(sectnum).tokenList.get(linenum).setFlag(nFlag, 1);
				TokenList.get(sectnum).tokenList.get(linenum).setFlag(iFlag, 1);
				TokenList.get(sectnum).tokenList.get(linenum).setFlag(xFlag, 1);
				TokenList.get(sectnum).tokenList.get(linenum).setFlag(pFlag, 1);
				a = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
				b = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
				index = symtabList.get(sectnum).symbolList.indexOf(TokenList.get(sectnum).tokenList.get(linenum).operand[0]);
				nDisp = symtabList.get(sectnum).locationList.get(index) - 
						TokenList.get(sectnum).tokenList.get(linenum+1).location;
				if(nDisp<0)
				{	// nDisp가 음수일경우... nDisp 16진수 값을 문자열로 바꾼뒤
					// 앞에 5자리가 불필요하게 'F'로 채워져 나오므로 제거하고
					// 앞에 제거하고 남은 3자리만 출력하게 해줌
					buffer2 = String.format("%X",nDisp);
					buffer3 = buffer2.substring(5,8);
					buffer = String.format("%02X%X%s",
							Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
							b,buffer3);
				}
				else if(nDisp < -2048 && nDisp >2048)
				{	//Base relative 를 사용해야할때
					nDisp = TokenList.get(sectnum).tokenList.get(linenum).location - base;
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(nFlag, 1);
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(iFlag, 1);
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(xFlag, 1);
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(bFlag, 1);
					a = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
					b = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
					buffer = String.format("%02X%X%03X",
							Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
							b,nDisp);
				}
				else
				{
					buffer = String.format("%02X%01X%03X",
							Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
							b, nDisp);
				}
				
			}
		}
		else //index값이 없을 때
		{	
			if(TokenList.get(sectnum).tokenList.get(linenum).operand[0].charAt(0) == '#')
			{	 // immediate 처리
				StringTokenizer st = new StringTokenizer(TokenList.get(sectnum).tokenList.get(linenum).operand[0],"#");
				TokenList.get(sectnum).tokenList.get(linenum).setFlag(iFlag, 1);
				a = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
				b = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
					buffer = String.format("%02X%01X%03X",
							Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
							b, Integer.parseInt(st.nextToken()));
			}
			else if(TokenList.get(sectnum).tokenList.get(linenum).operand[0].charAt(0) == '=')
			{		// literal 처리
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(nFlag, 1);
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(iFlag, 1);
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(pFlag, 1);
					a = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
					b = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
					nDisp = literaltable.get(TokenList.get(sectnum).tokenList.get(linenum).operand[0])
							- TokenList.get(sectnum).tokenList.get(linenum+1).location;
					buffer = String.format("%02X%01X%03X",
							Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
							b, nDisp);
			}
			else if(TokenList.get(sectnum).tokenList.get(linenum).operand[0].charAt(0) == '@')
			{		// indirect 처리
					StringTokenizer st = new StringTokenizer(TokenList.get(sectnum).tokenList.get(linenum).operand[0],"@");
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(nFlag, 1);
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(pFlag, 1);
					a = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
					b = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
					index = symtabList.get(sectnum).symbolList.indexOf(st.nextToken());
					nDisp = symtabList.get(sectnum).locationList.get(index) - 
							TokenList.get(sectnum).tokenList.get(linenum+1).location;
					buffer = String.format("%02X%01X%03X",
							Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
							b, nDisp);
					
			}
			else if(TokenList.get(sectnum).tokenList.get(linenum).operand[0].equals(" "))
			{		//operand 값이 없을 때 처리
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(nFlag, 1);
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(iFlag, 1);
					a = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
					b = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
					buffer = String.format("%02X%01X000",
							Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a, b);
			}
			else
			{		// operand 값이 있는 simple adressing 처리
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(nFlag, 1);
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(iFlag, 1);
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(pFlag, 1);
					a = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
					b = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
					index = symtabList.get(sectnum).symbolList.indexOf(TokenList.get(sectnum).tokenList.get(linenum).operand[0]);
					nDisp = symtabList.get(sectnum).locationList.get(index) - 
							TokenList.get(sectnum).tokenList.get(linenum+1).location;
				if(nDisp<0)
				{		// nDisp가 음수일경우... nDisp 16진수 값을 문자열로 바꾼뒤
						// 앞에 5자리가 불필요하게 'F'로 채워져 나오므로 제거하고
						// 앞에 제거하고 남은 3자리만 출력하게 해줌
						buffer2 = String.format("%X",nDisp);
						buffer3 = buffer2.substring(5,8);
						buffer = String.format("%02X%X%s",
								Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
								b,buffer3);
				}
				else if(nDisp < -2048 && nDisp >2048)
				{		//Base relative 를 사용해야할때
						nDisp = TokenList.get(sectnum).tokenList.get(linenum).location - base;
						TokenList.get(sectnum).tokenList.get(linenum).setFlag(nFlag, 1);
						TokenList.get(sectnum).tokenList.get(linenum).setFlag(iFlag, 1);
						TokenList.get(sectnum).tokenList.get(linenum).setFlag(xFlag, 1);
						TokenList.get(sectnum).tokenList.get(linenum).setFlag(bFlag, 1);
						a = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
						b = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
						buffer = String.format("%02X%X%03X",
								Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
								b,nDisp);
				}
				else
				{
						buffer = String.format("%02X%01X%03X",
								Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
								b, nDisp);
				}
			}
				
		}
		return buffer;
	}
	
	//foramt4 함수 ,operator가 4형식일때 라인넘버에서 생성되는 머신코드 
	//  값을 머신코드 포인터에 저장한다.
	public String format4(int sectnum,int linenum)
	{	
		int a,b;
		String buffer;
		if(TokenList.get(sectnum).tokenList.get(linenum).operand[0].charAt(0) =='#')
		{	//IMMEDIATE를 사용한 4형식인 경우
			StringTokenizer st = new StringTokenizer(TokenList.get(sectnum).tokenList.get(linenum).operand[0],"#");
			TokenList.get(sectnum).tokenList.get(linenum).setFlag(iFlag, 1);
			TokenList.get(sectnum).tokenList.get(linenum).setFlag(eFlag, 1);
			a = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
			b = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
			buffer = String.format("%02X%01X%05X",
					Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
					b, Integer.parseInt(st.nextToken()));
		}
		else
		{	//IMMEDIATE가 아닌 일반 4형식인경우
			TokenList.get(sectnum).tokenList.get(linenum).setFlag(nFlag, 1);
			TokenList.get(sectnum).tokenList.get(linenum).setFlag(iFlag, 1);
			TokenList.get(sectnum).tokenList.get(linenum).setFlag(eFlag, 1);
			a = ((int)TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
			b = ((int)TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
			buffer = String.format("%02X%01X00000",
					Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
					b);
		}
		//operand를 검사해서 INDEX 값이 존재하는 경우 
		if(TokenList.get(sectnum).tokenList.get(linenum).operand[1] != null)
			if(TokenList.get(sectnum).tokenList.get(linenum).operand[1].equals("X"))
			{	
			TokenList.get(sectnum).tokenList.get(linenum).setFlag(nFlag, 1);
			TokenList.get(sectnum).tokenList.get(linenum).setFlag(iFlag, 1);
			TokenList.get(sectnum).tokenList.get(linenum).setFlag(xFlag, 1);
			TokenList.get(sectnum).tokenList.get(linenum).setFlag(eFlag, 1);
			a = ((int)TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
			b = ((int)TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
			buffer = String.format("%02X%01X00000",
					Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
					b);
			}		
	return buffer;
	}
	
	//RegisterToDecnum 함수
	//Register 캐릭터 문자를 받아와서 대응하는 숫자로 리턴해주는 함수
	public int RegisterToDecnum(String strReg)
	{
		if (strReg.equals("A")) return 0;
		else if (strReg.equals("X")) return 1;
		else if (strReg.equals("L")) return 2;
		else if (strReg.equals("B")) return 3;	
		else if (strReg.equals("S")) return 4;
		else if (strReg.equals("T")) return 5;
		else if (strReg.equals("F")) return 6;
		else if (strReg.equals("PC")) return 8;
		else if (strReg.equals("SW")) return 9;
		else
		{
			// 여기에 적절치 못한 Register가 올경우 에러 처리해줘야함
			System.out.println("error : Register Not found!!!\n");
			return -1;
		}
	}
}
