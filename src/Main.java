public class Main {

    public static void main(String[] args) {
        IdGen idGen = new IdGen(0,0);

        for(int i=0;i<50;i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int j=0;j<200;j++) {
                        long id = idGen.getNextId();
                        System.out.println(Thread.currentThread().getName() + "  " + id);
                    }
                }
            }, "Thread-"+i).start();
        }
    }
}
