import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.IOException;

public class ProcessData
{
	int numofPositiveExamples = 0;
	int numOfNegativeExamples = 0;
	int V = 0;	
	double numberOfNegativeWords = 0;
	double numberOfPositiveWords = 0;
	HashMap<String, Integer> positiveVocabulary = new HashMap<String, Integer>();
	HashMap<String, Integer> negativeVocabulary = new HashMap<String, Integer>();		
	String dataFolder;
	
	public ProcessData(String dataFolder) throws IOException
	{
		this.dataFolder = dataFolder;
		calculateVocabularyHashMaps_Wrapper();
	}
	
	public Boolean findSentiment(String query)
	{
		query = query.replaceAll("[!?,]", "");
		String[] queryWords = query.split(" ");
		for (int i = 0; i < queryWords.length; i++)
		{
			queryWords[i] = queryWords[i].replaceAll("[^\\w]", "");
			//System.out.println(queryWords[i]);
		}
		
		double negativeProbability=1.0, positiveProbability=1.0;
		
		for(String word : queryWords)
		{
			double negCount=0,posCount=0;
			if(negativeVocabulary.get(word)!=null)
			{
				negCount = negativeVocabulary.get(word);
			}
			
			if(positiveVocabulary.get(word)!=null)
			{
				posCount = positiveVocabulary.get(word);
			}
						
			if(negCount!=0 || posCount!=0)
			{
				negativeProbability = ((negCount+1)/(numberOfNegativeWords + V))*negativeProbability;
				positiveProbability = ((posCount+1)/(numberOfPositiveWords + V))*positiveProbability;
			}
		}

		negativeProbability = negativeProbability*(numOfNegativeExamples/(double)(numOfNegativeExamples+numofPositiveExamples));
		positiveProbability = positiveProbability*(numofPositiveExamples/(double)(numofPositiveExamples + numOfNegativeExamples));
		
		System.out.println("Negative Sentiment Confidence : " + negativeProbability);
		System.out.println("Positive Sentiment Confidence : " + positiveProbability);
		
		if(negativeProbability>positiveProbability) return false;
		else return true;

		
	}
	
	private int calculateVocabularyHashMaps_Wrapper() throws IOException
	{
		V = calculateVocabularyHashMaps(0,dataFolder + "/neg") + calculateVocabularyHashMaps(1,dataFolder + "/pos");
		return V;
	}
	
	private int calculateVocabularyHashMaps(int mode,String path) throws IOException
	{
		int count = 0;
		File dataFile = new File(path);
		System.out.println(path);
		for(String fileName : dataFile.list())
		{
			if(mode==0) numOfNegativeExamples++;
			if(mode==1) numofPositiveExamples++;
				
			List<String> lines = Files.readAllLines(Paths.get(path + "/" + fileName));
			for(String line : lines)
			{
				line = line.replaceAll("[!?,]", "");
				String[] tokenisedString = line.split(" ");
				
				for (int i = 0; i < tokenisedString.length; i++) tokenisedString[i] = tokenisedString[i].replaceAll("[^\\w]", "");
				
				for(String word : tokenisedString)
				{
					if(mode==0)
					{
						numberOfNegativeWords++;
						if(negativeVocabulary.get(word)==null)
						{
							negativeVocabulary.put(word, 1);
							count++;
						}
						else
						{
							int x = negativeVocabulary.get(word);
							negativeVocabulary.put(word,x+1);
						}						
					}
					else
					{
						numberOfPositiveWords++;
						if(positiveVocabulary.get(word)==null)
						{
							positiveVocabulary.put(word, 1);
							if(negativeVocabulary.get(word)==null) count++;
						}
						else
						{
							int x = positiveVocabulary.get(word);
							positiveVocabulary.put(word,x+1);
						}
					}
				}
			}
		}
		return count;
	}
	
	public void test(String path) throws IOException
	{
		int numOfPosExamples = 0;
		int numOfNegExamples = 0;
		
		int correctlyClassifiedPositiveExamples = 0;
		int correctlyClassifiedNegativeExamples = 0;
		
		//positive classification
		File dataFile = new File(path + "/pos" );
		for(String fileName : dataFile.list())
		{
			numOfPosExamples++;
			String query = "";
			List<String> lines = Files.readAllLines(Paths.get(path + "/pos/" + fileName));
			for(String line : lines)
			{
				query = query + " " + line;
			}
			System.out.println(query);
			
			System.out.println("****\n");
			if(findSentiment(query)) correctlyClassifiedPositiveExamples++;
			
		}
		
		//negative classification
		dataFile = new File(path + "/neg" );
		for(String fileName : dataFile.list())
		{	
			numOfNegExamples++;
			String query = "";
			List<String> lines = Files.readAllLines(Paths.get(path + "/neg/" + fileName));
			for(String line : lines)
			{
				query = query + " " + line;
			}
			//if(!findSentiment(query)) correctlyClassifiedNegativeExamples++;
		}
		
		double x = 100*((correctlyClassifiedPositiveExamples+correctlyClassifiedNegativeExamples)/(double)(numOfPosExamples+numOfNegExamples));
		System.out.println(x);
	}
	
	
	
}
