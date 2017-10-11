/**
 * Created by Vincent on 2017/10/11.
 */
public class IdGen {

    private final long startTimeStamp = 1420041600000L;

    private final long workerIdBits = 5L;

    private final long dataCenterBits = 5L;

    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    private final long maxDataCenterId = -1L ^ (-1L << dataCenterBits);

    private final long sequenceBits = 12L;

    private final long workerIdOffset = sequenceBits;

    private final long dataCenterIdOffset = sequenceBits + workerIdBits;

    private final long timeStampOffset = sequenceBits + workerIdBits + dataCenterBits;

    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private long workerId;

    private long dataCenterId;

    private long sequence = 0L;

    private long lastTimeStamp = -1L;

    public IdGen(long workerId, long dataCenterId) {
        if(workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if(dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("dataCenter Id can't be greater than %d or less than 0", maxDataCenterId));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    public synchronized long getNextId() {
        //返回当前时间（毫秒）
        long timeStamp = System.currentTimeMillis();

        //如果当前时间小于上一次生成Id的时间戳，说明系统时钟的问题
        if(timeStamp < lastTimeStamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", 1));
        }

        if(timeStamp == lastTimeStamp) {
            sequence = (sequence +1) & sequenceMask;
            //毫秒内序列溢出
            if(sequence == 0) {
                //获得新的（更大的）时间戳
                timeStamp = getNextMillis(timeStamp);
            }
        }
        else {
            //时间戳发生变化，该毫秒内序列重置
            sequence = 0L;
        }

        //更新上次生成Id的时间戳
        lastTimeStamp = timeStamp;

        return ((timeStamp - startTimeStamp) << timeStampOffset) | (dataCenterId << dataCenterIdOffset) | (workerId << workerIdOffset) | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param timeStamp 上次生成Id的时间戳
     * @return 当前时间戳
     */
    protected long getNextMillis(long timeStamp) {
        long currTimeStamp = System.currentTimeMillis();
        while(currTimeStamp <= timeStamp) {
            currTimeStamp = System.currentTimeMillis();
        }
        return currTimeStamp;
    }
}
