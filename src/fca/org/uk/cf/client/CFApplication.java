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

    public static void main(String[] args) throws Exception {
        // create a scanner so we can read the command-line input

        if(args == null || args.length ==0) {
            CFRequestInput input = new CFRequestInput();
            input.setCriteria(ReportCriteria.STAT_END);
            @SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);

            //  prompt for the user's name
            System.out.print("Enter start time in Format dd/MM/yyyy HH:mm:ss (ex: 20/03/2016 23:00:00 ) \n");
            input.setStart(getConvertedDataTime(scanner.next()+" "+scanner.next()));

            System.out.print("Endtime current time ? Y/N \n");
            
            if("Y".equalsIgnoreCase(scanner.next())) {
                input.setEnd(Instant.now().getEpochSecond()+"");
            } else {
                System.out.println("Enter End time in Format dd/MM/yyyy HH:mm:ss (ex: 20/03/2016 23:59:59) \n");
                input.setEnd(getConvertedDataTime(scanner.next()+" "+scanner.next()));
            }
            System.out.println("Enter Required Report Type \n");
            System.out.println("1: Write to file \n2: Write to Splunk \n3: Read from file & write to splunk \n");
            int type = scanner.nextInt();
            System.out.println(String.format("Log Start time %s, and End Time is %s", input.getStart(), input.getEnd()));
            CFLogProcessor logProcessor = new CFLogProcessor(input);
            logProcessor.processRequest(ProcessType.getProcessType(type));
        }else {
            System.out.println("Good bye");            
        }
        System.out.println("Good bye");            
    }

    private static String getConvertedDataTime(String starTime) throws ParseException {
        //System.out.println(Instant.now().getEpochSecond());
        SimpleDateFormat df = new SimpleDateFormat();
        df.applyPattern("dd/MM/yyyy HH:mm:ss");
        Date date = df.parse(starTime);
        return date.getTime()/1000l+"";
    }
}
