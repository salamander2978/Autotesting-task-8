package com.example;

import java.time.Duration;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class ParasoftFooterNavigationTest {

    private static final String BASE_URL =
            "https://parabank.parasoft.com/parabank/index.htm";

    private static final By LOGO_HOME_ICON =
            By.cssSelector("img[title='ParaBank']");
    private static final By CUSTOMER_LOGIN_HEADING =
            By.xpath("//h2[normalize-space()='Customer Login']");
    private static final By FOOTER_ABOUT_US =
            By.xpath("//div[@id='footerPanel']//a[normalize-space()='About Us']");
    private static final By FOOTER_HOME =
            By.xpath("//div[@id='footerPanel']//a[normalize-space()='Home']");
    private static final By FOOTER_PARASOFT =
            By.xpath("//div[@id='footerPanel']//a[@target='_blank' and contains(@href,'parasoft.com')]");
    private static final By ABOUT_HEADING =
            By.xpath("//div[@id='rightPanel']//h1[contains(translate(.,'PARASOFT DEMO WEBSITE','parasoft demo website'),'demo website')]");

    private WebDriver driver;
    private WebDriverWait wait;
    private String mainWindowHandle;

    @BeforeClass
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(description = "Footer navigation, second-window handling and return to the main page")
    public void footerNavigationScenario() {

        driver.get(BASE_URL);
        mainWindowHandle = driver.getWindowHandle();
        assertMainPageVisible("Step 1: the test must start on the main page");

        scrollToBottom();

        wait.until(ExpectedConditions.elementToBeClickable(FOOTER_ABOUT_US)).click();

        WebElement aboutHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(ABOUT_HEADING));
        assertTrue(aboutHeading.getText().toLowerCase().contains("demo website"),
                "Step 4: the 'ParaSoft Demo Website' panel heading is not shown");
        assertEquals(driver.getWindowHandles().size(), 1,
                "Step 4: About Us must open in the same window, no new window expected");
        assertEquals(driver.getWindowHandle(), mainWindowHandle,
                "Step 4: still expected to be on the first/original window");
        assertTrue(driver.getCurrentUrl().contains("about.htm"),
                "Step 4: URL is expected to be the About page");

        scrollToBottom();
        wait.until(ExpectedConditions.elementToBeClickable(FOOTER_HOME)).click();

        assertEquals(driver.getWindowHandles().size(), 1,
                "Step 6: still a single window expected");
        assertEquals(driver.getWindowHandle(), mainWindowHandle,
                "Step 6: still expected to be on the first window");
        assertMainPageVisible("Step 6: the main page must be visible after clicking Home");

        scrollToBottom();
        wait.until(ExpectedConditions.elementToBeClickable(FOOTER_PARASOFT)).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        String secondWindowHandle = getNewWindowHandle(mainWindowHandle);
        driver.switchTo().window(secondWindowHandle);
        wait.until(d -> d.getCurrentUrl().contains("parasoft.com"));
        assertTrue(driver.getCurrentUrl().contains("parasoft.com"),
                "Step 8: the second window must show parasoft.com, but was: " + driver.getCurrentUrl());
        assertTrue(!driver.getCurrentUrl().contains("parabank"),
                "Step 8: the second window must NOT be the ParaBank page");

        driver.switchTo().window(mainWindowHandle);

        assertEquals(driver.getWindowHandle(), mainWindowHandle,
                "Step 10: must be back on the first window");
        assertMainPageVisible("Step 10: the main page must still be available in the first window");

        driver.switchTo().window(secondWindowHandle);
        driver.close();
        wait.until(ExpectedConditions.numberOfWindowsToBe(1));
        driver.switchTo().window(mainWindowHandle);
        assertEquals(driver.getWindowHandles().size(), 1,
                "Step 11: only the first window should remain after closing the second one");

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        wait.until(ExpectedConditions.elementToBeClickable(LOGO_HOME_ICON)).click();

        assertEquals(driver.getWindowHandle(), mainWindowHandle,
                "Step 13: still expected to be on the first window");
        assertMainPageVisible("Step 13: the main page must still be visible without corruptions");
        assertTrue(driver.findElement(By.cssSelector("img[title='ParaBank']")).isDisplayed(),
                "Step 13: the ParaBank logo must be present (page not corrupted)");
    }

    private void assertMainPageVisible(String context) {
        wait.until(ExpectedConditions.titleContains("ParaBank"));
        assertTrue(driver.getCurrentUrl().contains("index.htm"),
                context + " - expected the main page URL (index.htm), but was: " + driver.getCurrentUrl());
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(CUSTOMER_LOGIN_HEADING)).isDisplayed(),
                context + " - the 'Customer Login' panel must be visible on the main page");
    }

    private void scrollToBottom() {
        ((JavascriptExecutor) driver)
                .executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    private String getNewWindowHandle(String knownHandle) {
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(knownHandle)) {
                return handle;
            }
        }
        throw new IllegalStateException("No second window handle was found");
    }
}
