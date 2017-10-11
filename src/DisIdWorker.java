import java.util.HashMap;
import java.util.Map;

/**
 * 采用snowflake的思想，生成分布式系统中趋势递增的全局唯一ID，并采用自定义配置方式
 * Created by Vincent on 2017/10/11.
 */
public class DisIdWorker {

    //开始时间戳，由配置文件获取
    private long startTimeStamp;

    //毫秒数位数
    private int millisBits;

    //序列号位数
    private int sequenceBits;

    //存储Id中间字段<字段名称，位数>
    private Map<String, Integer> idMidFactors;

    //Id中间字段传入值（例如机房号：1，机架号：8，机器号：413）
    private Map<String, Long> midFactorsValue;

    //当前序列号
    private long sequence = 0L;

    //序列号掩码（如12位序列号的掩码为0b11111111111=0xfff=4095）
    private long sequenceMask;

    //上次生成Id的时间戳
    private long lastTimeStamp = -1L;

    private void init() {
        idMidFactors = new HashMap<>();
        HashMap<String, String> properties = PropertiesUtil.getAllProperties("IdConfig.properties");
        startTimeStamp = Long.parseLong(properties.get("startTimeStamp"));
        millisBits = Integer.parseInt(properties.get("millisBits"));
        sequenceBits = Integer.parseInt(properties.get("sequenceBits"));
        sequenceMask = -1L ^ (-1L << sequenceBits);
        String[] midFactors = properties.get("factors").split("&");
        for(String factor : midFactors) {
            idMidFactors.put(factor.trim(), Integer.parseInt(properties.get(factor.trim()+"Bits")));
        }
    }

    public DisIdWorker(Map<String, Long> midFactorsValue) {
        init();
        this.midFactorsValue = midFactorsValue;
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

        long ret = sequence;

        int offset = sequenceBits;
        //注意，采用HashMap后，无法保证中间字段是符合原来的顺序（但是并不影响）
        for(Map.Entry<String, Integer> entry : idMidFactors.entrySet()) {
            int bits = entry.getValue();
            ret = ret | (midFactorsValue.get(entry.getKey()) << offset);
            offset += bits;
        }
        ret = ret | (((long)(timeStamp - startTimeStamp)) << offset);

        return ret;
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
