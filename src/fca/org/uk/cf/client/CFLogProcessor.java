package fca.org.uk.cf.client;

import com.splunk.*;
import fca.org.uk.cf.client.enums.ProcessType;
import fca.org.uk.cf.client.util.PropertiesCache;
import fca.org.uk.cf.client.util.ReportCriteria;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;

public class CFLogProcessor {

    private final CFRequestInput cfRequestInput;
    PropertiesCache propertiesCache = PropertiesCache.getInstance();

    public CFLogProcessor(CFRequestInput input) {
        this.cfRequestInput = input;
    }

    //Public methods

    public void processRequest(ProcessType processType) throws Exception {
        switch (processType){
            case WRITE_TO_FILE:
                writeDataToFile();
                break;
            case WRITE_TO_SPLUNK:
                writeDataToSplunk();
                break;
            case WRITE_TO_SPLUNK_FROM_FILE:
                wirteDataToSplunkFromFile(PropertiesCache.getInstance().getProperty("source.gz.file.location"));
                break;
        }
        System.out.println(processType);
    }

    /**
     * Returns list domain specific zone tags ids
     * @return
     * @throws IOException
     */
    public String getZoneListIds() throws IOException {
        String baseUrl = "https://api.cloudflare.com/client/v4/zones?status=active";
        HttpURLConnection con = (HttpURLConnection) new URL(baseUrl).openConnection();
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("X-Auth-Key", PropertiesCache.getInstance().getProperty("cloudflare.authKey"));
        con.setRequestProperty("X-Auth-Email", PropertiesCache.getInstance().getProperty("registered.emailId"));
      
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        
		String inputLine;
		StringBuilder response = new StringBuilder();
		while ((inputLine = in.readLine()) != null) 
			response.append(inputLine);
		
		in.close();
		
        return response.toString();

    }

    //Private methods


    /**
     * @throws Exception
     */
    private void writeDataToSplunk() throws Exception {

        //String urls = "https://api.cloudflare.com/client/v4/zones/a7d186c7fd4f587e5dd6f0f6d9abe1a5/logs/requests?start=1465373445&end=1465373805";
        /* Constructing query string to retrieve data ip stats */

        //https://api.cloudflare.com/client/v4/zones/a7d186c7fd4f587e5dd6f0f6d9abe1a5/logs/requests?start=1465373445&end=1465373805
        String baseUrl = getBaseUrl() + propertiesCache.getProperty("zonelist.operation") +
                propertiesCache.getProperty("cloudflare.zoneTag") +
                propertiesCache.getProperty("log.requests.operation")
                + perpareGetQueryParams();

        HttpURLConnection con = getHttpConnection(baseUrl);

        try {
            Service service = getSplunkService();
            TcpInput myInput = (TcpInput) service.getInputs().get(propertiesCache.getProperty("splunk.tcp.port"));
            // Open a socket
            Socket socket = myInput.attach();
            try {
                OutputStream ostream = socket.getOutputStream();
                Writer out = new OutputStreamWriter(ostream, "UTF8");
                GZIPInputStream gis = new GZIPInputStream(con.getInputStream());
                IOUtils.copy(gis, out);
                // Send events to the socket then close it
                out.flush();
            } finally {
                socket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @throws Exception
     */
    private void wirteDataToSplunkFromFile(String sourcefile) throws Exception {
        try {

            File targetFile = new File(sourcefile);
            FileInputStream inputStream = new FileInputStream(targetFile);

            Service service = getSplunkService();
            TcpInput myInput = (TcpInput) service.getInputs().get(PropertiesCache.getInstance().getProperty("splunk.tcp.port"));

            // Open a socket
            Socket socket = myInput.attach();
            try {
                OutputStream ostream = socket.getOutputStream();
                Writer out = new OutputStreamWriter(ostream);
                IOUtils.copy(inputStream, out);
                // Send events to the socket then close it
                out.flush();
            } finally {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @throws Exception
     */
    private void writeDataToFile() throws Exception {

    	  String baseUrl = getBaseUrl() + propertiesCache.getProperty("zonelist.operation") +
                  propertiesCache.getProperty("cloudflare.zoneTag") +
                  propertiesCache.getProperty("log.requests.operation")
                  + perpareGetQueryParams();
    	  HttpURLConnection con = getHttpConnection(baseUrl);
        try {
            String filename = new SimpleDateFormat("dd-MM-yyyy hh-mm-ss'.txt.gz'").format(new Date());
            File targetFile = new File(PropertiesCache.getInstance().getProperty("gz.file.location") +  filename);
            FileUtils.copyInputStreamToFile(con.getInputStream(), targetFile);            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Construct cloudflare base url
     *
     * @return cloudflare url
     */
    private String getBaseUrl() {
        //https://api.cloudflare.com/client/v4

        StringBuilder url = new StringBuilder();
        url.append(PropertiesCache.getInstance().getProperty("cloudflare.baseurl"));
        url.append(PropertiesCache.getInstance().getProperty("cloudflare.version"));
        return url.toString();
    }

    /**
     * @return Splunk service connection object
     */
    private Service getSplunkService() {

        HttpService.setSslSecurityProtocol(SSLSecurityProtocol.TLSv1_2);

        // Create a map of arguments and add login parameters
        ServiceArgs loginArgs = new ServiceArgs();
        loginArgs.setUsername(PropertiesCache.getInstance().getProperty("splunk.username"));
        loginArgs.setPassword(PropertiesCache.getInstance().getProperty("splunk.password"));
        loginArgs.setHost(PropertiesCache.getInstance().getProperty("splunk.host"));
        loginArgs.setPort(Integer.parseInt(PropertiesCache.getInstance().getProperty("splunk.port")));

        // Create a Service instance and log in with the argument map
        return Service.connect(loginArgs);
    }

    /**
     * Gets HttpURLS Connection
     * @param requestUrl
     * @return
     * @throws IOException
     */
    private HttpURLConnection getHttpConnection(String requestUrl) throws IOException {


    	HttpURLConnection con = null;
    	
    	//If proxy enabled
    	if(Boolean.parseBoolean(PropertiesCache.getInstance().getProperty("isProxyEnabled"))){
        	
    		Proxy proxy = new Proxy(Proxy.Type.HTTP, 
        			new InetSocketAddress(PropertiesCache.getInstance().getProperty("http.proxyPort")
        			, Integer.parseInt(PropertiesCache.getInstance().getProperty("http.proxyPort"))));
    		
            Authenticator authenticator = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return (new PasswordAuthentication(PropertiesCache.getInstance().getProperty("http.proxyUser"),
                    		PropertiesCache.getInstance().getProperty("http.proxyPassword").toCharArray()));
                }
            };
            Authenticator.setDefault(authenticator);
            
            con = (HttpURLConnection) new URL(requestUrl).openConnection(proxy);
            
    	}else {
    		con = (HttpURLConnection) new URL(requestUrl).openConnection();	
    	}

        con.setRequestProperty("Accept-Encoding", "gzip");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("X-Auth-Key", PropertiesCache.getInstance().getProperty("cloudflare.authKey"));
        con.setRequestProperty("X-Auth-Email", PropertiesCache.getInstance().getProperty("registered.emailId"));
        return con;
    }

    /**
     * Utility method to populate query string parameters
     */
    private String perpareGetQueryParams() {

        ReportCriteria criteria = cfRequestInput.getCriteria();
        StringBuilder queryParam = new StringBuilder();
        queryParam.append("?");

        if (ReportCriteria.STAT_END == criteria) {
            queryParam.append(ReportCriteria.START.getValue() +"="+ cfRequestInput.getStart() + "&");
            queryParam.append(ReportCriteria.END.getValue() +"="+ cfRequestInput.getEnd());
        } else {
            String value = "";
            if (ReportCriteria.INTERVAL == criteria) {
                value = cfRequestInput.getInterval();
            } else if (ReportCriteria.LATEST == criteria) {
                value = cfRequestInput.getLatest();
            } else if (ReportCriteria.START == criteria) {
                value = cfRequestInput.getStart();
            }
            queryParam.append(criteria.getValue() + value);
        }
        return queryParam.toString();
    }

    public static void main(String[] args) throws IOException {
        CFLogProcessor processor = new CFLogProcessor(null);
        System.out.println(processor.getZoneListIds());
    }
}

