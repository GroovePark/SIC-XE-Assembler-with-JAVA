package project1b;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�. <br>
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ� �̸� ��ũ��Ų��.<br>
 * section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit ������ �������� ���� ���� */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
	InstTable instTab;
	
	
	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList;
	
	/**
	 * �ʱ�ȭ�ϸ鼭 symTable�� instTable�� ��ũ��Ų��.
	 * @param symTab : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param instTab : instruction ���� ���ǵ� instTable
	 */
	public TokenTable(SymbolTable symTab, InstTable instTab) {
		//...
		tokenList = new ArrayList<Token>();
	}
	
	/**
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line) {
		
		tokenList.add(new Token(line));
		
	}
	
	/**
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 �������� ����Ѵ�.
	 * instruction table, symbol table ���� �����Ͽ� objectcode�� �����ϰ�, �̸� �����Ѵ�.
	 * @param index
	 */
	public void makeObjectCode(int index){
		//...
	}
	
	/** 
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
}

/**
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ ��  �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. 
 * �ǹ� �ؼ��� ������ pass2���� object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token{
	//�ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location;
	String label;
	String operator;
	String[] operand = new String[4];
	String comment;
	char nixbpe=0;
	
	// object code ���� �ܰ迡�� ���Ǵ� ������ 
	String objectCode;
	int byteSize;
	
	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�. 
	 * @param line ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line) {
		//initialize �߰�
		parsing(line);
		
	}
	
	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * @param line ��������� ����� ���α׷� �ڵ�.
	 */
	public void parsing(String line) {
		//tab������ ��ū�Ѵ�.
		StringTokenizer tokens = new StringTokenizer(line,"\t");
		int check = 0;
		//�����ϱ��� ���̺� �ʱ�ȭ
		label = " ";
		operator =" ";
		operand[0] =" ";
		comment = " ";
		
		//Label���� �ִ� ����
		//���ʷ� ���� �־��ش�.
		if(line.charAt(0) != ' ' && line.charAt(0)!='\t' && line.charAt(0) != '.')
		{	
			while(tokens.hasMoreTokens()) 
			{ 	
				if(check == 0) {
					
					label = tokens.nextToken();
					check++;
				}
				else if(check == 1) {
					
					operator = tokens.nextToken();
					check++;
				}
				//','�������� operand���� ��ū�Ѵ�. ','�ƴ� ���й��ڴ� pass���� �ؼ� ��ū�Ұ��̴�.
				else if(check == 2) {
					int opnum = 0;
					operand[opnum] = tokens.nextToken();
					StringTokenizer opr = new StringTokenizer(operand[opnum],",");
					
					while(opr.hasMoreTokens())
					{
						operand[opnum] = opr.nextToken();
						opnum++;
					}
					
					check++;
				}
				else if(check == 3) {
					comment = tokens.nextToken();
					check++;
				}
			}
		}
		
		//Label���� ���� ����
		if (line.charAt(0) == '\t' || line.charAt(0) == ' ')
		{	
			//label�� ������ �־��ص� �׵ڷ� ���� ���ʷ� �־��ش�.
			label = " ";
			while(tokens.hasMoreTokens()) 
			{
				if(check == 0) {
					operator = tokens.nextToken();
					check++;
				}
				else if(check == 1) {
					int opnum = 0;
					operand[opnum] = tokens.nextToken();
					StringTokenizer opr = new StringTokenizer(operand[opnum],",");
					while(opr.hasMoreTokens())
					{
						operand[opnum] = opr.nextToken();
						opnum++;
					}
					check++;
				}
				else if(check == 2) {
					comment = tokens.nextToken();
					check++;
				}
			}
		}
		//subroutine �����ϴ� ����
		if(line.charAt(0) == '.')	
		{
			label = ".";
			operator =" ";
			operand[0] =" ";
			comment = " ";
			
		}	
		comment = " ";
	}

	
	/** 
	 * n,i,x,b,p,e flag�� �����Ѵ�. <br><br>
	 * 
	 * ��� �� : setFlag(nFlag, 1); <br>
	 *   �Ǵ�     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : ���ϴ� ��Ʈ ��ġ
	 * @param value : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
	 */
	public void setFlag(int flag, int value) {
		//...
		if(value == 1)
			nixbpe = (char) (nixbpe | flag);
	}
	
	/**
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ� <br><br>
	 * 
	 * ��� �� : getFlag(nFlag) <br>
	 *   �Ǵ�     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
	
	
	
}
