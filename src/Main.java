import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        HashMap<String, Long> config = new HashMap<>();
        config.put("engineRoom", 1L);
        config.put("frame", 3L);
        config.put("worker", 410L);
        DisIdWorker idWorker = new DisIdWorker(config);

        for(int i=0;i<50;i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int j=0;j<200;j++) {
                        long id = idWorker.getNextId();
                        System.out.println(Thread.currentThread().getName() + "  " + id);
                    }
                }
            }, "Thread-"+i).start();
        }
    }
}
