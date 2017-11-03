package ru.javaops.masterjava.xml;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.*;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;
import java.util.function.Consumer;


/**
 * Created by eekrupin on 03.11.2017.
 */
public class MainXml {

    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));
    }

    public static void main(String[] args) throws Exception {
        printUsersByProject("TopJava");
        printUsersByProject("MasterJava");
    }

    public static void printUsersByProject(String project) throws Exception {
        Consumer<Object> printer = System.out::println;
        Payload payload = JAXB_PARSER.unmarshal(
                Resources.getResource("payload.xml").openStream());

        payload.getUsers().getUser().stream()
                .filter(user -> user.getGroup().stream()
                        .filter(group -> project.equals(((ProjectType) ((GroupType) group).getProject()).getName())).count() > 0
                )
                .forEach(user -> printer.accept(user.getFullName()));

//        String strPayload = JAXB_PARSER.marshal(payload);
//        JAXB_PARSER.validate(strPayload);
//        System.out.println(strPayload);
    }

}
