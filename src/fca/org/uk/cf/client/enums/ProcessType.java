package fca.org.uk.cf.client.enums;

/**
 * Created by Chivukula on 10/06/2016.
 */
public enum ProcessType {

    WRITE_TO_FILE(1),WRITE_TO_SPLUNK(2),WRITE_TO_SPLUNK_FROM_FILE(3);

    @SuppressWarnings("unused")
	private int type;

    private ProcessType(int type){
        this.type = type;
    }

    public  static ProcessType getProcessType(int type){
        ProcessType pType = WRITE_TO_FILE;
        switch (type) {
            case 1:
                pType= ProcessType.WRITE_TO_FILE;
                break;
            case 2:
                pType= ProcessType.WRITE_TO_SPLUNK;
                break;
            case 3:
                pType= ProcessType.WRITE_TO_SPLUNK_FROM_FILE;
                break;
            default:
                pType= ProcessType.WRITE_TO_FILE;
        }
        return pType;
    }


}
