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
	HashMap<String, Integer> stopWords = new HashMap<String, Integer>();
	String dataFolder; 
	
	public ProcessData(String dataFolder, Boolean removeStopWords, Boolean applyBinaryNB) throws IOException
	{		
		this.dataFolder = dataFolder;		
		createStopwordsHashMap();		
		calculateVocabularyHashMaps_Wrapper(removeStopWords,applyBinaryNB);
	}
	
	public Boolean findSentiment(String query)
	{
		query = query.replaceAll("[!?,]", "");
		String[] queryWords = query.split("<br /><br />|\\s");
		for (int i = 0; i < queryWords.length; i++)
		{
			queryWords[i] = queryWords[i].replaceAll("[^\\w]", "");
			queryWords[i] = queryWords[i].toLowerCase();
		}
		
		double negativeProbability=0.0, positiveProbability=0.0;
		
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
				negativeProbability = negativeProbability + Math.log(((negCount+1)/(numberOfNegativeWords + V)));
				positiveProbability = positiveProbability + Math.log(((posCount+1)/(numberOfPositiveWords + V)));
			}
	
		}

		negativeProbability = negativeProbability + Math.log((numOfNegativeExamples/(double)(numOfNegativeExamples+numofPositiveExamples)));
		positiveProbability = positiveProbability + Math.log((numofPositiveExamples/(double)(numofPositiveExamples + numOfNegativeExamples)));
		
		if(negativeProbability>positiveProbability) return false;
		else return true;

		
	}
	
	private void createStopwordsHashMap() throws IOException
	{
		File stopWordsFile = new File("/home/subhadip/Downloads/aclImdb/stopwords");
		FileReader fileReader = new FileReader(stopWordsFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		while((line = bufferedReader.readLine()) != null)
		{
			stopWords.put(line, 1);
		}
	}
	
	private int calculateVocabularyHashMaps_Wrapper(Boolean removeStopWords, Boolean applyBinaryNB) throws IOException
	{
		if(applyBinaryNB) V = calculateVocabularyHashMaps_BinaryNaiveBayes(0,dataFolder + "/neg",removeStopWords) + calculateVocabularyHashMaps_BinaryNaiveBayes(1,dataFolder + "/pos",removeStopWords);
		else V = calculateVocabularyHashMaps(0,dataFolder + "/neg",removeStopWords) + calculateVocabularyHashMaps(1,dataFolder + "/pos",removeStopWords);
		return V;
	}
	
	private int calculateVocabularyHashMaps(int mode,String path, Boolean removeStopWords) throws IOException
	{
		int count = 0;
		File dataFile = new File(path);
		for(String fileName : dataFile.list())
		{
			if(mode==0) numOfNegativeExamples++;
			if(mode==1) numofPositiveExamples++;
				
			List<String> lines = Files.readAllLines(Paths.get(path + "/" + fileName));
			for(String line : lines)
			{
				line = line.replaceAll("[!?,]", "");
				String[] tokenisedString = line.split("<br /><br />|\\s");
				
				for (int i = 0; i < tokenisedString.length; i++)
				{
					tokenisedString[i] = tokenisedString[i].replaceAll("[^\\w]", "");
					tokenisedString[i] = tokenisedString[i].toLowerCase();
				}
				
				for(String word : tokenisedString)
				{
					if(removeStopWords)
					{
						if(stopWords.get(word)!=null) continue;
					}
					
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
			if(!findSentiment(query)) correctlyClassifiedNegativeExamples++;
		}
		
		double accuracy = 100*((correctlyClassifiedPositiveExamples+correctlyClassifiedNegativeExamples)/(double)(numOfPosExamples+numOfNegExamples));
		System.out.println("Accuracy " + accuracy);
		
		double precision = 100*(correctlyClassifiedPositiveExamples/(double)(correctlyClassifiedPositiveExamples + (numOfNegExamples - correctlyClassifiedNegativeExamples)));
		System.out.println("Precision " + precision);
		
		double recall = 100*(correctlyClassifiedPositiveExamples/(double)(numOfPosExamples));
		System.out.println("Recall " + recall);
		
		double fMeasure = 2*(precision*recall)/(precision + recall);
		System.out.println("fMeasure " + fMeasure);
	}
	
	
	private int calculateVocabularyHashMaps_BinaryNaiveBayes(int mode,String path, Boolean removeStopWords) throws IOException
	{
		int count = 0;
		File dataFile = new File(path);
		HashSet<String> uniqueWordList;
		for(String fileName : dataFile.list())
		{
			uniqueWordList = new HashSet<String>();
			if(mode==0) numOfNegativeExamples++;
			if(mode==1) numofPositiveExamples++;
				
			List<String> lines = Files.readAllLines(Paths.get(path + "/" + fileName));
			for(String line : lines)
			{
				line = line.replaceAll("[!?,]", "");
				String[] tokenisedString = line.split("<br /><br />|\\s");
				
				for (int i = 0; i < tokenisedString.length; i++)
				{
					tokenisedString[i] = tokenisedString[i].replaceAll("[^\\w]", "");
					tokenisedString[i] = tokenisedString[i].toLowerCase();
				}
				
				for(String word : tokenisedString)
				{
					if(uniqueWordList.contains(word)) continue;
					else uniqueWordList.add(word);
					
					if(removeStopWords)
					{
						if(stopWords.get(word)!=null) continue;
					}
					
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
	
}

