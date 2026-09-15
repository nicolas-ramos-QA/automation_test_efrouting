package com.automation_test_efrouting.question;

import com.automation_test_efrouting.userinterface.SelectorConstant;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.questions.Text;
import net.serenitybdd.screenplay.targets.Target;

public class LaneMileage implements Question<Integer> {

    private final String origin;
    private final String destination;

    public LaneMileage(String origin, String destination) {
        this.origin = origin;
        this.destination = destination;
    }

    @Override
    public Integer answeredBy(Actor actor) {
        String xpath = String.format(SelectorConstant.LANE_MILEAGE_TEMPLATE, origin, destination);
        String text = Text.of(Target.the("Millas de " + origin + " a " + destination).locatedBy(xpath))
                .answeredBy(actor);
        String digits = text.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    public static LaneMileage between(String origin, String destination) {
        return new LaneMileage(origin, destination);
    }
}
