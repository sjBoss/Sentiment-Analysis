import java.io.*;
import java.util.Scanner;

public class Driver {
	
	static Scanner userInput = new Scanner(System.in);
	public static void main(String[] args) {
		
		//String query = userInput.nextLine();
		try 
		{
			
			ProcessData  p = new ProcessData("/home/subhadip/Downloads/aclImdb/train",false);	
			p.test("/home/subhadip/Downloads/aclImdb/test");
			
		}			
		catch(Exception ioe)
		{
			ioe.printStackTrace();
		}			
	}

}

