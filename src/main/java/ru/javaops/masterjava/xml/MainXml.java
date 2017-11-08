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
//        printUsersByProject("TopJava");
//        printUsersByProject("MasterJava");
        createHTMLUsersByProject("TopJava");
    }

    public static void printUsersByProject(String project) throws Exception {
        Consumer<Object> printer = System.out::println;
        Payload payload = JAXB_PARSER.unmarshal(
                Resources.getResource("payload.xml").openStream());

        payload.getUsers().getUser().stream()
                .filter(user -> user.getGroup().stream()
                        .filter(group -> project.equals( ( (ProjectType) ((GroupType) group).getProject() ).getName() )).count() > 0
                )
                .forEach( user -> printer.accept(String.format("%s/%s", user.getFullName(), user.getEmail())) );

//        String strPayload = JAXB_PARSER.marshal(payload);
//        JAXB_PARSER.validate(strPayload);
//        System.out.println(strPayload);
    }

    public static void createHTMLUsersByProject(String project) throws Exception {
        Consumer<Object> printer = System.out::println;
        Payload payload = JAXB_PARSER.unmarshal(
                Resources.getResource("payload.xml").openStream());

        String sep = System.getProperty("line.separator");
        StringBuilder builder = new StringBuilder();
        builder.append("<table border = \"0\">");
        builder.append(sep);
        builder.append("<tr>")
            .append("<th>User</th>")
            .append("<th>Mail</th>")
            .append("</tr>");
        builder.append(sep);

        payload.getUsers().getUser().stream()
                .filter(user -> user.getGroup().stream()
                        .filter(group -> project.equals( ( (ProjectType) ((GroupType) group).getProject() ).getName() )).count() > 0
                )
                .forEach( user -> builder.append("<tr>")
                                .append("<td>")
                                .append(user.getFullName())
                                .append("</td>")
                                .append("<td>")
                                .append(user.getEmail())
                                .append("</td>")
                                .append("</tr>")
                         );

        builder.append(sep);
        builder.append("</table>");

        printer.accept(builder.toString());

    }

}
