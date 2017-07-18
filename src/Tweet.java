/**
 *
 * @author H2O
 * Adpat From Tutorial Slides
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tweet implements Runnable {
    private final String buffer;    
    static String timeStart = "\"created_at\":\"";
    static String timeEnd = "\",\"id\"";
    static String textStart = "\"text\":\"";
    static String textEnd = "\",\"source\":";
    static final ConcurrentHashMap<String, String> hashtable = new ConcurrentHashMap<String, String>();
    static List<String> items = new ArrayList<String>();
    static volatile boolean isComplete = false;
    public Tweet(String buffer){            
        this.buffer = buffer;
    }  
    
    public void run() {
        StringTokenizer st = new StringTokenizer(buffer," \n");
    } 
     public static void main(String args[]) throws java.io.IOException {
        
        int numCores = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(numCores);
        pool.submit(new MyRunnable(args[1], timeStart,timeEnd,textStart,textEnd));    
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
        Pattern timePattern, textPattern;
        String timeStart, timeEnd, textStart,textEnd ;
        String temp = "";
        int counter = 0, state = 0, match = 0;
        int len_textS, len_textE, len_timeS, len_timeE;
        public MyRunnable(String path, String timeStart, String timeEnd,String textStart,String textEnd){
            outputPath = path;
            this.timeStart = timeStart;
            this.timeEnd = timeEnd;
            this.textStart = textStart;
            this.textEnd = textEnd;            
            len_timeS = timeStart.length();
            len_timeE = timeEnd.length();
            len_textS = textStart.length();
            len_textE = textEnd.length();
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
                while(!isComplete){
                    while(!items.isEmpty()){
                    temp = items.remove(0);
                    if (temp == null){
                        continue;
                    }
                    char[] c = temp.toCharArray();
                    state = 0;
                    StringBuilder timeSb = new StringBuilder();
                    StringBuilder textSb = new StringBuilder();
                    timeSb.append(" (timestamp: ");
                    match = 0;
                    int a;
                    for(a  = 0; a< c.length;a++){         
                        if(state == 0){
                            if(c[a] == timeStart.charAt(match)){
                                match++;
                                if(match == len_timeS){
                                    state = 1;
                                    match = 0;
                                }      
                            }
                            else{match=0;}
                                                  
                        }                        
                        else if(state == 1){                            
                                if(c[a] == timeEnd.charAt(match)){
                                    match++;
                                    if(match == len_timeE){
                                    state = 2;
                                    timeSb.delete(timeSb.length() - match+1, timeSb.length());
                                    timeSb.append(')');
                                    match = 0;
                                }
                                }
                                else{match =0;}
                                timeSb.append(c[a]);
                                
                        }
                        else if(state == 2){
                            if(c[a] == textStart.charAt(match)){
                                match++;
                                if(match == len_textS){
                                    state = 3;
                                    match = 0;
                                }                                    
                            }
                            else{match=0;}
                                 
                        }
                        else if(state == 3){
                            if(c[a] == textEnd.charAt(match)){match++;}
                                else{match =0;}
                                textSb.append(c[a]);
                                if(match == len_textE){
                                    state = 2;
                                    textSb.delete(textSb.length() - match, textSb.length());
                                    match = 0;
                                    String clean = textSb.toString().replaceAll("\\\\u[A-Fa-f\\d]{4}", "");
                                    bw.append(clean);
                                    if(clean.length() != textSb.length()){  counter++;  
                                    }
                                    bw.append(timeSb.toString());
                                    bw.newLine();
                                    break;
                                }
                        
                        }
                        
                    }
                    // Matcher  used
                    /*String result = "";
                    if(temp != null){
                        Matcher matcher = textPattern.matcher(temp);
                        if(matcher.find()){
                            result = matcher.group(1);
                        //Replace unicode with empty string
                            String clean = result.replaceAll("\\\\u[A-Fa-f\\d]{4}", "");
                            if(clean.length() != result.length()){
                                counter++;
                            }
                            bw.append(clean);
                        }
                        matcher = timePattern.matcher(temp);
                        if(matcher.find()){
                            result = matcher.group(1);                                    
                            bw.append(" (");
                            bw.append(result);
                            bw.append(')');
                            bw.newLine();
                        }                    
                    }*/
                }                
            }
                bw.append(counter + " tweets contained unicode.");
                bw.close();
                fw.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }          
    } 
    }     
}

