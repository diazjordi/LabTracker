package htmlhandling;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import htmlhandling.HTMLParser;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Jordi Diaz on 12/22/14.
 * Designed to open browser, navigate to status web page, find HTML code for current status of PC's
 * then fetch that specific HTML div and save it locally for parsing by separate class
 */
public class HTMLScraper {

    // URLs for library PC status pages
    private static String libN1 = "http://10.84.89.162/MapViewerTemplateLib.html";

    public static void main(String[] args) throws IOException, SQLException {
        // Run first floor, pass in URL
        getHtmlFromPage(libN1);
        // Run htmlhandling.HTMLParser on output from first floor
        HTMLParser parser = new HTMLParser();
        parser.run();
    }

    // Takes URL for map page, loads map page into memory
    // searches for status div "the-pieces" and saves relevant html locally
    // for parsing
    private static void getHtmlFromPage(String url) throws IOException {
        // Gets Page, will currently only load script output for Firefox 24
        // will throw warnings and alerts, should be sorted out before production testing
        WebClient mapClient = new WebClient(BrowserVersion.FIREFOX_24);
        HtmlPage mapPage = mapClient.getPage(url);
        try {
            // Sleep is necessary for JS on page to execute
            Thread.sleep(3000);
            // Create file to save HTML
            File lib1 = new File("data/raw/libN1");
            // Add HTML to string and write to file
            String libString = mapPage.getElementById("the-pieces").asXml();
            FileUtils.writeStringToFile(lib1, libString);
        } catch (FailingHttpStatusCodeException ex) {
            System.out.println("Error downloading page: " + ex.getMessage());
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Close current web client
        mapClient.closeAllWindows();
    }


}
