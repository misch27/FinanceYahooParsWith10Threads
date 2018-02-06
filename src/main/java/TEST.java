public class TEST extends Thread {
    private static  int  currentMAX=1;
    private static int b;
    private boolean a = true;
    private  int  mainid;
    private final Object waitObject;
    public TEST(int id, Object waitObject){
        this.mainid=id;
        this.waitObject=waitObject;
    }
    public static void main (String args[]){
        Object waitObject = new Object();
        for (int i = currentMAX; i<=10;++i){
            Thread thread = new TEST(i, waitObject);
            thread.start();
        }
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                System.out.println("Start" + mainid);
                synchronized (waitObject) {
                    while (mainid!=currentMAX) {
                        waitObject.wait();
                    }
                    b++;
                    currentMAX++;
                    if (currentMAX == 10) {
                        currentMAX = 1;
                    }
                    System.out.println("End" + mainid);
                    waitObject.notifyAll();
                    if (b > 10) {
                        interrupt();
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
