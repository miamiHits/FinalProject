package FinalProject.BL.Agents;

import FinalProject.BL.DataObjects.*;

import java.util.Arrays;
import java.util.List;

public class AlgoTestUtils {
    static void makeProp(List<PropertyWithData> props) {
        PropertyWithData tempHot = new PropertyWithData();
        tempHot.setName("temp_hot");
        tempHot.setMin(36);
        tempHot.setMax(73);
        tempHot.setTargetValue(59);
        tempHot.setPrefix(Prefix.AT);
        tempHot.setRt(RelationType.LEQ);
        tempHot.setTargetTick(3);
        tempHot.setDeltaWhenWork(13.56);
        tempHot.setPowerConsumedInWork(11.52);
        tempHot.setDeltaWhenWorkOffline(0);
        tempHot.setCachedSensor(30);
        tempHot.setLoaction(false);
        Actuator Roomba = new Actuator("Roomba", "", "room",
                Arrays.asList(new Action("off", 0, Arrays.asList(new Effect("temp_hot", 0))),
                        new Action("cleaness", 11.52,
                                Arrays.asList(new Effect("temp_hot", 13.56)))));
        Sensor Roomba_sens = new Sensor("Roomba_sens", "", "Roomba", 40,
                Arrays.asList("temp_hot"));
        tempHot.setActuator(Roomba);
        tempHot.setSensor(Roomba_sens);
        props.add(tempHot);
    }
}
