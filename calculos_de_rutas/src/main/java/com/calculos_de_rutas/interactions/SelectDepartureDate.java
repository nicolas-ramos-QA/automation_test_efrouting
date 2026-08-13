package com.calculos_de_rutas.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class SelectDepartureDate implements Interaction {

    private static final int DAYS_AHEAD = 3;

    @Override
    public <U extends Actor> void performAs(U actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();

        LocalDate targetDate = LocalDate.now().plusDays(DAYS_AHEAD);
        String targetCaption = targetDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
        String targetDay = String.valueOf(targetDate.getDayOfMonth());

        for (int attempt = 0; attempt < 15; attempt++) {
            WebElement caption = driver.findElement(By.cssSelector("div[aria-live='polite']"));
            if (caption.getText().trim().equalsIgnoreCase(targetCaption)) {
                break;
            }
            driver.findElement(By.cssSelector("button[name='next-month']")).click();
            pause();
        }

        List<WebElement> days = driver.findElements(By.cssSelector("button[role='gridcell']"));
        for (WebElement day : days) {
            String cssClass = day.getAttribute("class");
            if (day.getText().trim().equals(targetDay)
                    && day.isEnabled()
                    && (cssClass == null || !cssClass.contains("outside"))) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", day);
                day.click();
                break;
            }
        }
    }

    private void pause() {
        try {
            Thread.sleep(400L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static SelectDepartureDate futureDate() {
        return new SelectDepartureDate();
    }
}
