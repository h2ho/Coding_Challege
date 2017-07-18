/**
 *
 * @author H2O
 */
import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;


import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AverageDeg implements Runnable {
    private final String buffer;    
    static final ConcurrentHashMap<String, String> hashtable = new ConcurrentHashMap<String, String>();
    static List<String> items = new ArrayList<String>();
    static volatile boolean isComplete = false;
    public AverageDeg(String buffer){            
        this.buffer = buffer;
    }  
    
    public void run() {
        StringTokenizer st = new StringTokenizer(buffer," \n");
    } 
     public static void main(String args[]) throws java.io.IOException {
        
        int numCores = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(numCores);
        pool.submit(new MyRunnable(args[1]));    
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            //Scan input file line by line
            inputStream = new FileInputStream(args[0]);
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
                items.add(sc.nextLine());                        
            }
            isComplete = true;
            if (sc.ioException() != null) {
                throw sc.ioException();
            }        
        }
        finally {
                if (inputStream != null) {inputStream.close();}
                if (sc != null) {sc.close();}
        }
        pool.shutdown();
    }
     
     static class MyRunnable implements Runnable {   
        String outputPath = ""; 
        BufferedWriter bw = null;
        FileWriter fw = null;
        String temp = "";
        String time = "(timestamp: ";
        int counter = 0, state = 0, match = 0;
        final String TWITTER="EEE MMM dd HH:mm:ss Z yyyy";				//Date Formatter
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
        Map<Date, Set<Set<String>>> graph =new TreeMap<Date, Set<Set<String>>>();			//store tweets that are created at same time together
        public MyRunnable(String path){
            outputPath = path;    
            sf.setLenient(true);
        }
	public void run() {

            try{
                File file = new File(outputPath);
                if(!file.exists()){
                    file.createNewFile();
                }    
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);    
                String data = "";
                StringBuilder hashtag = new StringBuilder();
                StringBuilder timeStamp = new StringBuilder();
                
                while(!isComplete || !items.isEmpty()){
                    while(!items.isEmpty()){
                    timeStamp.setLength(0);
                    temp = items.remove(0);
                    if (temp == null){
                        continue;
                    }
                    Set<String> edges = new HashSet<String>();
                    char[] c = temp.toCharArray();      
                    int i;
                    state = 0;
                    match = 0;
                    for(i =0; i< c.length;i++){				
                        if(state == 0){				// find hashtag or "timestamp"
                            if(c[i] == '#'){			
                                hashtag.append(c[i]);
                                state = 1;
                                continue;
                            }
                            if(c[i] == time.charAt(match)){
                                match++;
                                if(match == time.length()){
                                    state = 2;
                                }
                            }
                            else{ match=0; }
                                
                        }
                        else if(state == 1){			//found hashtag, trying to find space 
                            if(c[i] != ' '){
                                hashtag.append(c[i]);
                            }
                            else{
                                edges.add(hashtag.toString());  
                                hashtag.setLength(0);
                                state = 0;
                            }
                        }
                        else if (state == 2){			//read timestamp in string
                            timeStamp.append(c[i]);
                        }                        
                    }        
                    if(edges.isEmpty()){
                        timeStamp.setLength(0);
                        continue;
                    }
                    timeStamp.delete(timeStamp.length()-2, timeStamp.length());
                    Date twTime = sf.parse(timeStamp.toString());
                    if(!graph.containsKey(twTime)){
                        Set<Set<String>> connectedEd = new HashSet<Set<String>>();
                        connectedEd.add(edges);
                        graph.put(twTime,connectedEd);
                    }
                    else{
                        graph.get(twTime).add(edges);
                    }
                    for(Iterator<Map.Entry<Date, Set<Set<String>>>> it = graph.entrySet().iterator(); it.hasNext(); ) {
                        Map.Entry<Date, Set<Set<String>>> entry = it.next();
                        if(entry.getKey().getTime() + 60000 < twTime.getTime()) {           //remove entry if key is 60s longer than newest tweet
                            bw.append(String.format("%.02f",countEdges()));
                            bw.newLine();     
                            it.remove();
                        }
                        else{break;}
                    }
                } 
                }
                bw.close();
                fw.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }          
    } 
         
	public float countEdges(){
		Map<String,Set<String>> connected = new HashMap<String,Set<String>>();
		for (Date dt : graph.keySet()){
                        int counter = 0;
			for (Set<String> s : graph.get(dt)){
                            if(s.size() == 1){continue;}
                            for(String st : s){         
                                for(String st2 : s){
                                    if(!st.equals(st2) ){		//Create edges with other vertices
                                        if(!connected.containsKey(st)){
                                            Set<String> vertices = new HashSet<String>();
                                            vertices.add(st2);
                                            connected.put(st, vertices);
                                        }
                                        else{
                                            connected.get(st).add(st2);
                                        }                                        
                                    }
                                }			
                            }
			}
                        
		}
                int degree = 0;
                for(String key :connected.keySet()){				//sum the number of edges that each vertex has
                    degree += connected.get(key).size();
                }
                float result = (float)degree / (float)connected.keySet().size();
		return result;
		
	}
     }
}

