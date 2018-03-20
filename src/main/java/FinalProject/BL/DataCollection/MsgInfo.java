package FinalProject.BL.DataCollection;

public class MsgInfo {
    private int msgsNum;
    private long msgsLength;

    public MsgInfo(int msgsNum, long msgsLength) {
        this.msgsNum = msgsNum;
        this.msgsLength = msgsLength;
    }
    
    public int getMsgsNum() {
        return msgsNum;
    }

    public void setMsgsNum(int msgsNum) {
        this.msgsNum = msgsNum;
    }

    public long getMsgsLength() {
        return msgsLength;
    }

    public void setMsgsLength(long msgsLength) {
        this.msgsLength = msgsLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MsgInfo msgInfo = (MsgInfo) o;

        if (msgsNum != msgInfo.msgsNum) return false;
        return msgsLength == msgInfo.msgsLength;
    }

    @Override
    public int hashCode() {
        int result = msgsNum;
        result = 31 * result + (int) (msgsLength ^ (msgsLength >>> 32));
        return result;
    }

}
