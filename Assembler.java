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
 * �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���� ��ƾ�̴�.
 * ���α׷��� ���� �۾��� ������ ����. <br>
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�. <br>
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�. <br>
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1) <br>
 * 4) �м��� ������ �������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2) <br>
 * 
 * <br><br>
 * �ۼ����� ���ǻ��� : <br>
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� �ȵȴ�.<br>
 *  2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br><br>
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */
public class Assembler {
	/** instruction ���� ������ ���� */
	InstTable instTable;
	/** �о���� input ������ ������ �� �� �� �����ϴ� ����. */
	ArrayList<String> lineList;
	/** ���α׷��� section���� symbol table�� �����ϴ� ����*/
	ArrayList<SymbolTable> symtabList;
	/** ���α׷��� section���� ���α׷��� �����ϴ� ����*/
	ArrayList<TokenTable> TokenList;
	// literaló���� ���� ���ͷ���� �ּҰ��� ��Ƴ��� ���� //
	HashMap<String, Integer> literaltable;
	// �� sect�� ũ�⸦ ������ ���� //
	ArrayList<Integer> sectsize;
	// modify���ڵ带 �ۼ��ϱ� ���� �����ؾ��� ���� �ּ�, �ּҷκ��� ���ڸ�� �����ؾ��ϴ���, ����������� ���� �����̸��� ���� ///
	ArrayList<Integer> modifytableloc;
	ArrayList<Integer> modifytablenum;
	ArrayList<String> modifytablename;
	/** 
	 * Token, �Ǵ� ���þ ���� ������� ������Ʈ �ڵ���� ��� ���·� �����ϴ� ����. <br>
	 * �ʿ��� ��� String ��� ������ Ŭ������ �����Ͽ� ArrayList�� ��ü�ص� ������.
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
	 * Ŭ���� �ʱ�ȭ. instruction Table�� �ʱ�ȭ�� ���ÿ� �����Ѵ�.
	 * 
	 * @param instFile : instruction ���� �ۼ��� ���� �̸�. 
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
	 * ������� ���� ��ƾ
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
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.<br>
	 * @param fileName : ����Ǵ� ���� �̸�
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
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.<br>
	 * @param fileName : ����Ǵ� ���� �̸�
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
	 * pass1 ������ �����Ѵ�.<br>
	 *   1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū������ �и��� �� ��ū���̺� ����<br>
	 *   2) label�� symbolTable�� ����<br>
	 *   <br><br>
	 *    ���ǻ��� : SymbolTable�� TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 * @throws CloneNotSupportedException 
	 */
	private void pass1(){
		// TODO Auto-generated method stub
	int i,j,k; // �ݺ��� ó����
	int locctr,flag;//������ġ�ּ�,���ͷ��ߺ�ó�������÷���
	
	//�� ���ͺ��� ��ū���̺� ���� ��ū���̺� �ش� ������ ��ū���� �� ���� ��ū����Ʈ�� add���ش�
	TokenTable tokentable = new TokenTable(null, instTable);
	TokenTable tokentable1 = new TokenTable(null, instTable);
	TokenTable tokentable2 = new TokenTable(null, instTable);
	TokenTable tokentable3 = new TokenTable(null, instTable);
	
	//�ϴ� input������ ù�ٺ��� ���ٱ��� �������ش�
	for(i=0; i<lineList.size(); i++)
	{	
			tokentable.putToken(lineList.get(i));
	}
	int sectnum = 0;
	
	
	//��ū���̺��� ���͸� ������ ����
	for(i=0; i<lineList.size();i++)
	{	
		// �� ���� ���̿� �ִ� .. subroutine to read.... ���� ����
		if(lineList.get(i).charAt(0) != '.')
		{	
			////////////////////////////////////////////////////////////////////////////////////////
			if(sectnum == 0) //sectnum ������ ���� ���ͺ���
			{
			tokentable1.putToken(lineList.get(i));
				//CSECT�� �������� ���͸� ������.
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
	
	//��ū����Ʈ�� �ִ� operator���� inst.dat�� ������ִ� opcode�� ��Ī�����ִ� ����
	//��Ī�� opcode�� tokentable�� comment�ڸ��� ���� opcode�� ���� operator���� 
	//comment�ڸ��� �����(TokenTable Ŭ�� ����)
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
	
	//���ͺ����� �������Ƿ� ���ͺ��� �׸��� ���κ��� �м��� ���� locctr���� �̿� symboltable�� �����ϴ� �����̴�.
	//�� ���ͺ��� �ɺ����̺��� �������ְ�, �ɺ����̺��� �� ���� �ɺ�����Ʈ�� add���ش�.
	SymbolTable symboltable1 = new SymbolTable();
	SymbolTable symboltable2 = new SymbolTable();
	SymbolTable symboltable3 = new SymbolTable();
	sectnum = 0;
	//���ͺ� �м�
	for(i=0;i<TokenList.size();i++)
	{	
		
		locctr = 0;
		////////////////////////////////////////////////////////////////////////////////////////
		if(sectnum==0)
		{
		//���κ� �м�
		for(j=0; j<TokenList.get(i).tokenList.size(); j++)
		{	
			//label���� ������ �����ּҿ� �� �̸��� �ɺ����̺� �����Ѵ�
			if(!TokenList.get(i).tokenList.get(j).label.equals(" "))
			{	
				
				symboltable1.putSymbol(TokenList.get(i).tokenList.get(j).label, locctr);
			}
			//Start�϶� operand�� �ִ� �����ּ� ���� integer���� ĳ�����ϰ� ����locctr�� �־��ش�
			if(TokenList.get(i).tokenList.get(j).operator.equals("Start"))
			{
				locctr = Integer.parseInt(TokenList.get(i).tokenList.get(j).operand[0]);
				TokenList.get(i).tokenList.get(j).location = locctr;
			}
			//operator RESW�϶� �Ҵ�� ���� x3ŭ locctr ����
			else if(TokenList.get(i).tokenList.get(j).operator.equals("RESW"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + 3*Integer.parseInt(TokenList.get(i).tokenList.get(j).operand[0]);
			}
			//operator RESB�϶�
			else if(TokenList.get(i).tokenList.get(j).operator.equals("RESB"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + Integer.parseInt(TokenList.get(i).tokenList.get(j).operand[0]);
			}
			//operator BYTE�϶�
			else if(TokenList.get(i).tokenList.get(j).operator.equals("BYTE"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + 1;
			}
			//operator WORD�϶�
			else if(TokenList.get(i).tokenList.get(j).operator.equals("WORD"))
			{
				TokenList.get(i).tokenList.get(j).location = locctr;
				locctr = locctr + 3;
			}
			
			// operator CSECT�϶� locctr�� 0���� �ʱ�ȭ
			else if(TokenList.get(i).tokenList.get(j).operator.equals("CSECT"))
			{
				locctr = 0;
				TokenList.get(i).tokenList.get(j).location = locctr;
			}
			
			// operator LTORG�϶�
			else if(TokenList.get(i).tokenList.get(j).operator.equals("LTORG"))
			{
				for(k=0;k<j;k++)
				{ 	//LTORG ���� '='���� �ִ� �͵��� ã�Ƽ� literal_table�� �־��ش�.
					if(TokenList.get(i).tokenList.get(k).operand[0].charAt(0) == '=')
					{
						flag = 0;
						//���ͷ� ���̺� ���� �˻��ؼ� ���� �ߺ�������� �ʰ� �Ѵ�
						if(literaltable.containsKey(TokenList.get(i).tokenList.get(k).operand[0]))
							flag = 1;
						if(flag == 0)
						{	// ĳ���� ���� ���
							if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'C')
							{
								literaltable.put(TokenList.get(i).tokenList.get(k).operand[0], locctr);
								locctr = locctr + TokenList.get(i).tokenList.get(k).operand[0].length() - 4;
								//-4�� ���ִ� ������ =,C,',' �̷��� 4���� ���� �����ϱ� ����
							}
							// 16���� ���� ��� 
							else if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'X')
							{
								literaltable.put(TokenList.get(i).tokenList.get(k).operand[0], locctr);
								locctr = locctr + (TokenList.get(i).tokenList.get(k).operand[0].length() - 4) / 2;
								//���������� 4���ְ� 16������ 2���ڴ� 1����Ʈ�̹Ƿ� 2�� ������
							}
						}
							
					}
				}
			}
			
			//�ɺ����̺� absolute expression ���� �����ϴ� �κ�
			else if(TokenList.get(i).tokenList.get(j).operator.equals("EQU") && !TokenList.get(i).tokenList.get(j).operand[0].equals("*"))
			{
				int index,numofoperand=0 , numofoperator = 0;
				int value = 0;
				String data;
				StringTokenizer st = new StringTokenizer(TokenList.get(i).tokenList.get(j).operand[0],"+-/*",true);
				String[] operator = new String[3];
				//�� �����ڿ� �ǿ����ڸ� �����Ͽ� ��ū���ش�. �����ڴ� ���۷����� �迭�� �־���
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
				// �� �ǿ����� ������ŭ ������ ���鼭 ���� ������ش�
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
			//END�� ��� ó������ ������ ���鼭 '='�� �͵��� ã���ְ� �̰͵���
			//literal_table�� ���� �͵��� ���̺� �־��ش�.
			//(literal_table�� �ִ� ���� LTORG���� �̹� ó��
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
			//�׿��� ��� (opcode�� �ִ� ���)
			else
			{	
				TokenList.get(i).tokenList.get(j).location = locctr;
				//4�����Ͻ�
				if(TokenList.get(i).tokenList.get(j).operator.charAt(0) == '+')
					locctr = locctr + 4;
				else
				{	String format = null;
					if(instTable.instMap.containsKey(TokenList.get(i).tokenList.get(j).operator))
					{
					format = instTable.instMap.get(TokenList.get(i).tokenList.get(j).operator).format;
					//3����
					if(format.equals("3/4"))
						locctr = locctr + 3;
					//2����
					else if(format.equals("2"))
						locctr = locctr + 2;
					}
				}
			}
			//������ ������ ���� ��� 
			if(j == TokenList.get(i).tokenList.size()-1)
			{	
				
				symtabList.add(symboltable1);
				sectnum++;
				sectsize.add(locctr);
		}
			
			
		}
		}
		//sectnum�� 1������Ű�� ���� �Ȱ��� ������ �ݺ��Ѵ�.
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
		//sectnum�� 1�ø��� ���� ���� ���� �ݺ�
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
	 * pass2 ������ �����Ѵ�.<br>
	 *   1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
	//�ݺ���, symboltable�ε���,�ؽ�Ʈ���ڵ�����÷��� ,���ͷ� �ߺ������Ҷ� ���� ������
	int i,j,k,m,index,textflag,buffersize,literalflag,usedlitnum;
	String code = null; //������Ʈ �ڵ�����Ҷ� ���� ���ڿ�
	String buffer = null; //������Ʈ �ڵ� �����Ҷ� ���� ���ڿ�2(code���� ���� ������)
	textflag = 1;
	literalflag = 0;
	usedlitnum = 0;
	String[] used_literal = new String[3]; //���ͷ������� ���� ����ѹ��ڴ� �� �迭�� �־���
	String[] extref = new String[4]; // modify���ڵ� �ۼ����� �뵵 ���� ������ extref��
	
	//�ʱ�ȭ����//
	for(i=0;i<3;i++)
		used_literal[i] = "";
	for(i=0;i<4;i++)
		extref[i] = "";
	
	//���ͺ��м�
	for(i=0;i<TokenList.size();i++)
	{	
		textflag = 1;
		buffersize = 0;
		//����� �ڵ带 ���κ��� ���������� �м��Ѵ�.
		//���κ��� if���� ���� ��츦 ���� �� ��쿡 �˸��� �ӽ��ڵ带 �����Ѵ�.
		for(j=0;j<TokenList.get(i).tokenList.size();j++)
		{	
			// operand���� �˻��Ͽ� ������ ���̸� modify���ڵ忡 ���ش�.
			for(m=0;m<4;m++)
				if(TokenList.get(i).tokenList.get(j).operand[0].equals(extref[m]))
				{
					modifytableloc.add(TokenList.get(i).tokenList.get(j).location+1);
					modifytablenum.add(5);
					modifytablename.add("+"+TokenList.get(i).tokenList.get(j).operand[0]);
				}
			//start �Ǵ� ������ ù�����϶� ������ڵ带 �ۼ��Ѵ�.
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
			//EXTDEF�϶� D���ڵ带 �ۼ��Ѵ�.
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
			//EXTREF�϶� R���ڵ带 �ۼ��Ѵ�.
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
			//�ڸ�Ʈ�� ��Ī�� opcode ���� ��� ������
			else if(!TokenList.get(i).tokenList.get(j).comment.equals(" "))
			{	
				if(textflag == 1) // ù��° ���̶� ���ڵ��� ����� �ۼ�������Ҷ� �÷���
				{
					code = "T";
					buffer = String.format("%06X", TokenList.get(i).tokenList.get(j).location);
					code = code.concat(buffer);
					textflag = 0;
				}
				//�� �����ں��� ����� ����Ѵ�.
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
				// �ӽ��ڵ带 �������� ���� �������� �Ǹ� 60���ڸ� �ʰ��ϴ��� �˻��Ѵ�.
				// 67�� ������ ���� �����ּҰ��� 'h'���ڸ� �����༭ �׷���.
				if((code.length()+buffersize)>67)
				{	
					StringBuffer put_length = new StringBuffer(code);
					//column�� ���̸� length�� �־��ְ�
					String length = String.format("%02X",(code.length()-7)/2);
					//�����ּҰ��ڿ� �־��ش�.
					put_length.insert(7, length);
					code = String.format("%s",put_length);
					//codelist�� �߰� codelist�� ���پ� ���������ִ�.
					codeList.add(code);
					//���ַ��� �޴� �ӽ��ڵ� ���� �����ٿ����ش�
					code = "T";
					code = code.concat(String.format("%06X", TokenList.get(i).tokenList.get(j).location));
					code = code.concat(buffer);
				}
				//60�ʰ� ���ҽ� �׳� ���ش�.
				else
					code = code.concat(buffer);
			
			}
			//base�϶� base���� �����ش�. �ɺ����̺��� ã��.
			if(TokenList.get(i).tokenList.get(j).operator.equals("BASE"))
			{	
				index = symtabList.get(i).symbolList.indexOf(TokenList.get(i).tokenList.get(j).operand[0]);
				base = symtabList.get(i).locationList.get(index);
			}
			
			//LTORG�϶� ó������ C���� X���� ������ ó��
			if(TokenList.get(i).tokenList.get(j).operator.equals("LTORG"))
			{	
				for(k=0;k<j;k++)
				{
					if(TokenList.get(i).tokenList.get(k).operand[0].charAt(0)=='=')
					{	
						//�̹� ó���� literal���� Ȯ���ϴ� �����̴�. ���� flag 1�� ����
						for(m=0;m<3;m++)
						{
							if(TokenList.get(i).tokenList.get(k).operand[0].equals(used_literal[m]))
								literalflag = 1;
						}
						//���ο� literal�̶� ó��������Ҷ�
						if(literalflag==0)
						{	
							used_literal[usedlitnum] = TokenList.get(i).tokenList.get(k).operand[0];
							usedlitnum++;
							/*ĳ�������� ��� C,=,' �� �����ְ� ������ ���ڸ� 
							16���� �ƽ�Ű���� �ٲ㼭 ������Ʈ���α׷��� ���ִ� ����*/
							if(TokenList.get(i).tokenList.get(k).operand[0].charAt(1) == 'C')
							{
								String str = TokenList.get(i).tokenList.get(k).operand[0];
								String asc;
								//�Ǿ��� 3���� c=' �ǵ��� ' ����
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
							//16���� ���� X,=,' �� �����ְ� �״�� ������Ʈ���α׷��� ���ָ� ��
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
			// BYTE������ ������Ʈ �ڵ带 �����ϴ� �κ�
			// ĳ���� Ÿ���� �ƽ�Ű �ڵ尪���� ����ؾ��ϰ�
			// 16���� Ÿ���� �׳� ��� �ϸ� �ȴ�. (���� LTORG�� ���� ���)
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
			/*WORD�� ��� ���۷��� ���� '-+/*'�������� �����ְ� 
			ó������ ���� modify_table�� ó������
			���ڰ��� EXREF�� ����Ǿ������� modify ���̺� ���ش�.*/
			if(TokenList.get(i).tokenList.get(j).operator.equals("WORD"))
			{	
				int numofoperand=0 , numofoperator = 0;;
				String data;
				StringTokenizer st = new StringTokenizer(TokenList.get(i).tokenList.get(j).operand[0],"+-/*",true);
				String[] operator = new String[3];
				//�ǿ����� ��ū�� ���Ҿ� �����ڵ��� operator�迭�� �־��ش�.
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
				{	//extref[] �� ���� �־� Absolute Expression�� �ؾ� �Ҷ�
					if(TokenList.get(i).tokenList.get(j).operand[0].equals(extref[k]))
					{	
						for(m=0;m<numofoperand;m++)
						{	
							//ù��° �ǿ����ڴ� +�̹Ƿ�
							if(m==0)
							{
							modifytablename.add("+"+TokenList.get(i).tokenList.get(j).operand[m]);
							modifytablenum.add(6);
							modifytableloc.add(TokenList.get(i).tokenList.get(j).location);
							}
							//�׵ڷδ� operator�迭�� �ִ� �����ڵ��� ���ش�.
							else
							{
								modifytablename.add(operator[m-1]+TokenList.get(i).tokenList.get(j).operand[m]);
								modifytablenum.add(6);
								modifytableloc.add(TokenList.get(i).tokenList.get(j).location);
							}
						}
						//�����Ǿ ���� ���� �˼������Ƿ� 000000���� ������Ʈ�ڵ� ����
						code = code.concat("000000");
						
					}
				}
			}
			//END�϶�
			//LTORG�ڿ� ���� ���ͷ� ó�����ش�.
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
			
			//������ ������ ���϶�  modify���̺��� ���� modify���ڵ带 �ۼ����ص� E���ڵ嵵 �ۼ����ش�
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
			//�ۼ��� modify���̺� extref�迭 �ʱ�ȭ(�������Ϳ��� ����ؾ��ϹǷ�)
			modifytableloc.clear();
			modifytablenum.clear();
			modifytablename.clear();
			for(m=0;m<4;m++)
				extref[m]="";
			//E���ڵ� ����
			buffer = "E";
			codeList.add(buffer);
			codeList.add("");
			
			}
		}
	}
	
	}
	
	/**
	 * inputFile�� �о�鿩�� lineList�� �����Ѵ�.<br>
	 * @param inputFile : input ���� �̸�.
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
	
	//format2�Լ� operator�� 2�����϶� �ӽ��ڵ� ����
	public String format2(int sectnum,int linenum)
	{
		int opnum;
		String buffer;
		opnum = 0;
		//operand ���� ���ϴ� ����
		while(TokenList.get(sectnum).tokenList.get(linenum).operand[opnum] != null)
			opnum++;
		//operand������ 1���϶�
		if(opnum == 1)
		{
			buffer = String.format("%s%X0",
					TokenList.get(sectnum).tokenList.get(linenum).comment,
					RegisterToDecnum(TokenList.get(sectnum).tokenList.get(linenum).operand[0]));
		}
		// operand ������ 2�� �϶� ���� �ڵ� ���ϴ� ����
		else 
		{
			buffer = String.format("%s%X%X",
					TokenList.get(sectnum).tokenList.get(linenum).comment,
					RegisterToDecnum(TokenList.get(sectnum).tokenList.get(linenum).operand[0]),
					RegisterToDecnum(TokenList.get(sectnum).tokenList.get(linenum).operand[1]));
		}
		return buffer;
	}
	// formatt3 �Լ�
	// operator�� 3�����϶� ���γѹ����� �����Ǵ� �ӽ��ڵ� 
	//  ���� �ӽ��ڵ� �����Ϳ� �����Ѵ�.
	public String format3(int sectnum,int linenum)
	{
		int a,b,nDisp,index;
		String buffer = null;
		String buffer2;
		String buffer3;
		
		if(TokenList.get(sectnum).tokenList.get(linenum).operand[1] != null)
		{		
			if(TokenList.get(sectnum).tokenList.get(linenum).operand[1].equals("X"))
			{	//index ���� ���� ��
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
				{	// nDisp�� �����ϰ��... nDisp 16���� ���� ���ڿ��� �ٲ۵�
					// �տ� 5�ڸ��� ���ʿ��ϰ� 'F'�� ä���� �����Ƿ� �����ϰ�
					// �տ� �����ϰ� ���� 3�ڸ��� ����ϰ� ����
					buffer2 = String.format("%X",nDisp);
					buffer3 = buffer2.substring(5,8);
					buffer = String.format("%02X%X%s",
							Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
							b,buffer3);
				}
				else if(nDisp < -2048 && nDisp >2048)
				{	//Base relative �� ����ؾ��Ҷ�
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
		else //index���� ���� ��
		{	
			if(TokenList.get(sectnum).tokenList.get(linenum).operand[0].charAt(0) == '#')
			{	 // immediate ó��
				StringTokenizer st = new StringTokenizer(TokenList.get(sectnum).tokenList.get(linenum).operand[0],"#");
				TokenList.get(sectnum).tokenList.get(linenum).setFlag(iFlag, 1);
				a = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
				b = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
					buffer = String.format("%02X%01X%03X",
							Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
							b, Integer.parseInt(st.nextToken()));
			}
			else if(TokenList.get(sectnum).tokenList.get(linenum).operand[0].charAt(0) == '=')
			{		// literal ó��
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
			{		// indirect ó��
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
			{		//operand ���� ���� �� ó��
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(nFlag, 1);
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(iFlag, 1);
					a = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
					b = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
					buffer = String.format("%02X%01X000",
							Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a, b);
			}
			else
			{		// operand ���� �ִ� simple adressing ó��
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(nFlag, 1);
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(iFlag, 1);
					TokenList.get(sectnum).tokenList.get(linenum).setFlag(pFlag, 1);
					a = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
					b = ((int) TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
					index = symtabList.get(sectnum).symbolList.indexOf(TokenList.get(sectnum).tokenList.get(linenum).operand[0]);
					nDisp = symtabList.get(sectnum).locationList.get(index) - 
							TokenList.get(sectnum).tokenList.get(linenum+1).location;
				if(nDisp<0)
				{		// nDisp�� �����ϰ��... nDisp 16���� ���� ���ڿ��� �ٲ۵�
						// �տ� 5�ڸ��� ���ʿ��ϰ� 'F'�� ä���� �����Ƿ� �����ϰ�
						// �տ� �����ϰ� ���� 3�ڸ��� ����ϰ� ����
						buffer2 = String.format("%X",nDisp);
						buffer3 = buffer2.substring(5,8);
						buffer = String.format("%02X%X%s",
								Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
								b,buffer3);
				}
				else if(nDisp < -2048 && nDisp >2048)
				{		//Base relative �� ����ؾ��Ҷ�
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
	
	//foramt4 �Լ� ,operator�� 4�����϶� ���γѹ����� �����Ǵ� �ӽ��ڵ� 
	//  ���� �ӽ��ڵ� �����Ϳ� �����Ѵ�.
	public String format4(int sectnum,int linenum)
	{	
		int a,b;
		String buffer;
		if(TokenList.get(sectnum).tokenList.get(linenum).operand[0].charAt(0) =='#')
		{	//IMMEDIATE�� ����� 4������ ���
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
		{	//IMMEDIATE�� �ƴ� �Ϲ� 4�����ΰ��
			TokenList.get(sectnum).tokenList.get(linenum).setFlag(nFlag, 1);
			TokenList.get(sectnum).tokenList.get(linenum).setFlag(iFlag, 1);
			TokenList.get(sectnum).tokenList.get(linenum).setFlag(eFlag, 1);
			a = ((int)TokenList.get(sectnum).tokenList.get(linenum).nixbpe) >> 4;
			b = ((int)TokenList.get(sectnum).tokenList.get(linenum).nixbpe) & 15;
			buffer = String.format("%02X%01X00000",
					Integer.parseInt(TokenList.get(sectnum).tokenList.get(linenum).comment,16)+a,
					b);
		}
		//operand�� �˻��ؼ� INDEX ���� �����ϴ� ��� 
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
	
	//RegisterToDecnum �Լ�
	//Register ĳ���� ���ڸ� �޾ƿͼ� �����ϴ� ���ڷ� �������ִ� �Լ�
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
			// ���⿡ ����ġ ���� Register�� �ð�� ���� ó���������
			System.out.println("error : Register Not found!!!\n");
			return -1;
		}
	}
}
