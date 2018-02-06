import java.util.ArrayList;

public class ThreadPul extends Thread {
    private static volatile int currentMAX=0;
    private final JDBCClass jdbcClass;
    private final int numOfThread;
    private final Object waitObject;
    private final ArrayList<ArrayList<String>> arrOfRow;
    private int areaOfSum;
    private int idOfCompany;

    ThreadPul(int idOfCompany, int numOfThread, int areaOfSum,
              Object waitObject, JDBCClass jdbcClass, ArrayList<ArrayList<String>> arrOfRow){
        this.jdbcClass = jdbcClass;
        this.waitObject = waitObject;
        this.numOfThread = numOfThread;
        this.arrOfRow = arrOfRow;
        this.areaOfSum = areaOfSum;
        this.idOfCompany = idOfCompany;
    }


    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                int currentArea;
                for (currentArea = areaOfSum; currentArea <areaOfSum+10; currentArea++) {
                    if(arrOfRow.size()> currentArea) {
                        jdbcClass.batchSQL(idOfCompany, numOfThread, arrOfRow.get(currentArea));
                    }
                }
                areaOfSum = areaOfSum + 100;
                currentArea = areaOfSum;
                synchronized (waitObject) {
                    while (numOfThread != getCurrentMAX()) {
                        waitObject.wait();
                    }
                    jdbcClass.exequteNewNumbers(numOfThread);
                    if(arrOfRow.size()<= currentArea+100){
                        interrupt();
                        //System.out.println("Поток "+getName()+" отработал");
                    }
                    if(numOfThread==9){
                        updateCurrentMAX();
                    }else {
                        incrimentMAX();
                    }
                    waitObject.notifyAll();
                }
          }catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    private synchronized static int getCurrentMAX() {
        return currentMAX;
    }
    private synchronized static void incrimentMAX(){
        currentMAX++;
    }
    private synchronized static void updateCurrentMAX() {
        currentMAX = 0;
    }
}
