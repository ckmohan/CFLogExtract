package fca.org.uk.cf.client;

import fca.org.uk.cf.client.enums.ProcessType;
import fca.org.uk.cf.client.util.ReportCriteria;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by Chivukula on 09/06/2016.
 * Calls the CFLogProcessor to extract ip stats data for a given time intervals (start/end time)
 */
public class CFApplication {

    @SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
        // create a scanner so we can read the command-line input
    	if(args ==null || args[0].equalsIgnoreCase("help")) {
    		
        	System.out.println("To extact data from cloudflare enter string EXTRACT \n");
        	System.out.println("To get List of Zone tags for your organisation enter string TAGS \n");
        	
			Scanner scanner = new Scanner(System.in);
        	String serviceReq = scanner.next();
        	if(serviceReq != null && serviceReq.equalsIgnoreCase("EXTRACT")) {
                CFRequestInput input = new CFRequestInput();
                input.setCriteria(ReportCriteria.STAT_END);
                //  prompt for the user's name
                System.out.print("*** Please enter log extract start time in Format dd/MM/yyyy HH:mm:ss (ex: 20/03/2016 23:00:00 ) *** \n");
                input.setStart(getEpochTime(scanner.next()+" "+scanner.next()));

                System.out.print("*** Log extract Endtime current time ? Y/N \n");
                
                if("Y".equalsIgnoreCase(scanner.next())) {
                    input.setEnd(Instant.now().getEpochSecond()+"");
                } else {
                    System.out.println("*** Please enter log extract End time in Format dd/MM/yyyy HH:mm:ss (ex: 20/03/2016 23:59:59) *** \n");
                    input.setEnd(getEpochTime(scanner.next()+" "+scanner.next()));
                }
                System.out.println("*** Select Required Report Type *** \n");
                System.out.println("1: Write to file \n2: Write to Splunk \n3: Read from file & write to splunk \n");
                int type = scanner.nextInt();
                System.out.println(String.format("Log Start time %s, and End Time is %s", input.getStart(), input.getEnd()));
                CFLogProcessor logProcessor = new CFLogProcessor(input);
                logProcessor.processRequest(ProcessType.getProcessType(type));
        	}else if(serviceReq != null && serviceReq.equalsIgnoreCase("TAGS")) {
        		CFLogProcessor logProcessor = new CFLogProcessor(null);
                System.out.println(logProcessor.getZoneListIds());
        	}
        }
        System.out.println("Good bye");            
    }

    /**
     * Returns epoch time
     * @param starTime
     * @return
     * @throws ParseException
     */
    private static String getEpochTime(String starTime) throws ParseException {
        //System.out.println(Instant.now().getEpochSecond());
        SimpleDateFormat df = new SimpleDateFormat();
        df.applyPattern("dd/MM/yyyy HH:mm:ss");
        Date date = df.parse(starTime);
        return date.getTime()/1000l+"";
    }
}
