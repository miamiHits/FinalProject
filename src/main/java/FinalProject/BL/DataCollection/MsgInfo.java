package FinalProject.BL.DataCollection;

public class MsgInfo {
    private int msgsNum;
    private long msgsSize;

    public MsgInfo(int msgsNum, long msgsSize) {
        this.msgsNum = msgsNum;
        this.msgsSize = msgsSize;
    }

    public int getMsgsNum() {
        return msgsNum;
    }

    public void setMsgsNum(int msgsNum) {
        this.msgsNum = msgsNum;
    }

    public long getMsgsSize() {
        return msgsSize;
    }

    public void setMsgsSize(long msgsSize) {
        this.msgsSize = msgsSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MsgInfo msgInfo = (MsgInfo) o;

        if (msgsNum != msgInfo.msgsNum) return false;
        return msgsSize == msgInfo.msgsSize;
    }

    @Override
    public int hashCode() {
        int result = msgsNum;
        result = 31 * result + (int) (msgsSize ^ (msgsSize >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "MsgInfo{" +
                "msgsNum=" + msgsNum +
                ", msgsSize=" + msgsSize +
                '}';
    }
}
