package fca.org.uk.cf.client.util;

public enum ReportCriteria {

	LATEST("latest"), INTERVAL("interval"), START("start"), END("end"), STAT_END(
			"start_end"), ;

	private String criteria;

	private ReportCriteria(String criteria) {
		this.criteria = criteria;
	}
	
	public String getValue(){
		return criteria;
	}
}
