package com.automation_test_efrouting.question;

import com.automation_test_efrouting.userinterface.SelectorConstant;
import com.automation_test_efrouting.utils.JsonTextSelector;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.actions.Scroll;
import net.serenitybdd.screenplay.questions.Text;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;
import org.openqa.selenium.By;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class RoutesSectionRender implements Question<String> {

    @Override
    public String answeredBy(Actor actor) {

        actor.attemptsTo(
            WaitUntil.the(SelectorConstant.ROUTES_SECTION, isVisible()).forNoMoreThan(30).seconds(),
            Scroll.to(Target.the( JsonTextSelector.ROUTES_VALUE ).located(By.xpath(SelectorConstant.ROUTES_SECTION.getCssOrXPathSelector()))),
            WaitUntil.the(Target.the(JsonTextSelector.ROUTES_VALUE)
                        .located(By.xpath(SelectorConstant.ROUTES_SECTION.getCssOrXPathSelector())),isVisible())
                .forNoMoreThan(10).seconds()
        );
        return Text.of(SelectorConstant.ROUTES_SECTION).answeredBy(actor);
    }

    public static Question<String> routesSectionRendered() {
        return new RoutesSectionRender();
    }

}
