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

public class SuccessMessageRender implements Question<String> {

    @Override
    public String answeredBy(Actor actor) {

        actor.attemptsTo(
            WaitUntil.the(SelectorConstant.HOME_TEXT_INPUT, isVisible()).forNoMoreThan(20).seconds(),
            Scroll.to(Target.the( JsonTextSelector.HOME_VALUE ).located(By.xpath(SelectorConstant.HOME_TEXT_INPUT.getCssOrXPathSelector()))),
            WaitUntil.the(Target.the(JsonTextSelector.HOME_VALUE)
                        .located(By.xpath(SelectorConstant.HOME_TEXT_INPUT.getCssOrXPathSelector())),isVisible())
                .forNoMoreThan(10).seconds()
        );
        return Text.of(SelectorConstant.HOME_TEXT_INPUT).answeredBy(actor);
    }

    public static Question<String> successMessageRendered() {
        return new SuccessMessageRender();
    }

}
