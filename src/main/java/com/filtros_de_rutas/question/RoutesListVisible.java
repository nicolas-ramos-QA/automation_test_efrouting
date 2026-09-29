package com.filtros_de_rutas.question;

import com.filtros_de_rutas.userinterface.SelectorConstant;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RoutesListVisible implements Question<Boolean> {

    @Override
    public Boolean answeredBy(Actor actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        boolean hasNewRoute = !driver.findElements(By.xpath(SelectorConstant.NEW_ROUTE_BUTTON)).isEmpty();
        boolean hasFooter = !driver.findElements(By.xpath(SelectorConstant.FOOTER_TOTAL_ROUTES)).isEmpty();
        boolean hasResults = !driver.findElements(By.xpath(SelectorConstant.RESULTS_LABEL)).isEmpty();
        return hasNewRoute && (hasFooter || hasResults);
    }

    public static RoutesListVisible isShown() {
        return new RoutesListVisible();
    }
}
