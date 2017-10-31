import java.io.*;
import java.util.Scanner;

public class Driver {
	
	static Scanner userInput = new Scanner(System.in);
	public static void main(String[] args) {
				
		try 
		{

			System.out.println("****Naive Bayes Learning****");
			new ProcessData("/home/subhadip/Downloads/aclImdb/train",false,false).test("/home/subhadip/Downloads/aclImdb/test");
			
			System.out.println("");
			System.out.println("****Naive Bayes Learning with stopwords removed****");
			new ProcessData("/home/subhadip/Downloads/aclImdb/train",true,false).test("/home/subhadip/Downloads/aclImdb/test");
			
			System.out.println("");
			System.out.println("****Binary Naive Bayes****");
			new ProcessData("/home/subhadip/Downloads/aclImdb/train",false,true).test("/home/subhadip/Downloads/aclImdb/test");
			
			System.out.println("");
			System.out.println("****Binary Naive Bayes with stop words removed****");
			new ProcessData("/home/subhadip/Downloads/aclImdb/train",true,true).test("/home/subhadip/Downloads/aclImdb/test");
			
		}			
		catch(Exception ioe)
		{
			ioe.printStackTrace();
		}			
	}

}

