package fca.org.uk.cf.client;

import fca.org.uk.cf.client.util.ReportCriteria;

/**
 * Created by Chivukula on 09/06/2016.
 */
public class CFRequestInput {

    private String start;
    private String end;
    /**
     * Only supported value is "true".
     * Use this to indicate if you want the past 30 minutes worth of statistics returned
     */
    private String latest="true";
    private String interval;

    private ReportCriteria criteria;

    public String getStart() {
        return start;
    }
    public void setStart(String start) {
        this.start = start;
    }
    public String getEnd() {
        return end;
    }
    public void setEnd(String end) {
        this.end = end;
    }
    public String getLatest() {
        return latest;
    }
    public void setLatest(String latest) {
        this.latest = latest;
    }
    public String getInterval() {
        return interval;
    }
    public void setInterval(String interval) {
        this.interval = interval;
    }
    public ReportCriteria getCriteria() {
        return criteria;
    }
    public void setCriteria(ReportCriteria criteria) {
        this.criteria = criteria;
    }
}
