import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.sql.SQLException;
import java.util.ArrayList;


public class Main extends Thread{
    private static final String fileLocation = System.getProperty("user.dir");
    private static int i;
    public static void main (String args[]) throws SQLException {

//JSOUP не умеет работать с javascript
//используется selenium для отображения всей таблицы
//т.к. парсить <script> не всегда удобно

        JDBCClass jdbcClass = new JDBCClass();
        String service = fileLocation+"\\chromedriver.exe";
        System.setProperty("webdriver.chrome.driver", service);
        WebDriver  driver = new ChromeDriver();
        
        String company[] = {"GOOG", "AAPL"};
        String urlPartOne = "https://finance.yahoo.com/quote/";
        String urlPartTwo = "/history?period1=1221426000&period2=1506114000&interval=1d&filter=history&frequency=1d";
        try {
            for (i = 0; i < company.length; i++) {
                driver.get(urlPartOne + company[i] + urlPartTwo);
                Scrolling(driver);
                Parsing(i, driver, jdbcClass);
            }
        }finally {
            jdbcClass.close();
            driver.close();
        }

    }
    //прокрутка страницы
    private static void Scrolling(WebDriver driver) {
        for(int j=1; j<60; j++) {
            ((JavascriptExecutor) driver).executeScript("scroll(0,1000000)");
        }
    }

    private static void Parsing(int idOfCompany, WebDriver driver, JDBCClass jdbcClass) throws SQLException {
        ArrayList<ArrayList<String>> arrOfRow = new ArrayList<>();
        String line = driver.getPageSource();
        Document document = Jsoup.parse(line);
        Element table = document.select("table").get(1);
        Elements rows = table.select("tr");
        rows.forEach(row -> {
            ArrayList<String> arrOfCol = new ArrayList<>();
            Elements cols = row.select("td");
            cols.forEach(col -> {
                arrOfCol.add(col.text());
            });
            if(arrOfCol.size()!=0) {
                arrOfRow.add(arrOfCol);
            }
        });
        workWithThreads(idOfCompany, arrOfRow, jdbcClass);
    }

private static void workWithThreads(int idOfCompany, ArrayList<ArrayList<String>> arrOfRow, JDBCClass jdbcClass){
        boolean isEndOfProgram = false;
        Object waitObject = new Object();
        int areaOfSum = 0;
        ArrayList <ThreadPul> tenThreads = new ArrayList<>();
        for(int numOfThread=0; numOfThread<10; numOfThread++){
            ThreadPul threadPul = new ThreadPul(idOfCompany, numOfThread, areaOfSum, waitObject, jdbcClass, arrOfRow);
            threadPul.setName(Integer.toString(numOfThread));
            tenThreads.add(threadPul);
            threadPul.start();
            areaOfSum = areaOfSum + 10;
            }
            while (!isEndOfProgram){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int count = 0;
                for (ThreadPul oneThread:tenThreads
                     ) {
                    if(!oneThread.isAlive()){
                        count++;
                    }
                    if (count==10){
                        isEndOfProgram=true;
                    }

                }
            }
        }


}
