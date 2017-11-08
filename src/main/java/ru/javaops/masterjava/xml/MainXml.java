package ru.javaops.masterjava.xml;

import com.google.common.base.Splitter;
import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.*;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import static com.google.common.base.Strings.nullToEmpty;


/**
 * Created by eekrupin on 03.11.2017.
 */
public class MainXml {

    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);
    private static final Comparator<User> USER_COMPARABLE = Comparator.comparing(User::getFullName).thenComparing(User::getEmail);

    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));
    }

    public static void main(String[] args) throws Exception {
        printUsersByProject("TopJava");
        //printUsersByProject("MasterJava");
        //createHTMLUsersByProject("TopJava");
    }

    public static void printUsersByProject(String project) throws Exception {
        Consumer<Object> printer = System.out::println;
        URL payloadUrl = Resources.getResource("payload.xml");

        try (InputStream is = payloadUrl.openStream()){
            StaxStreamProcessor processor = new StaxStreamProcessor(is);
            final Set<String> groupNames = new HashSet<>();

            while (processor.startElement("Group", "Groups")){
                String groupName = processor.getAttribute("name");
                if (project.equals(processor.getElementValue("project"))){
                    groupNames.add(groupName);
                 }
            }

            if (groupNames.isEmpty()){
                throw new IllegalArgumentException("Invalid "+ project + "or no groups");

            }

            Set<User> users = new TreeSet<>(USER_COMPARABLE);

            JaxbParser parser = new JaxbParser(User.class);
            while (processor.doUntil(XMLEvent.START_ELEMENT, "User")){

                User user = new User();
                user.setFlag(FlagType.valueOf(processor.getAttribute("flag").toUpperCase()));
                user.setEmail(processor.getAttribute("email"));
                user.setFullName(processor.getElementValue("fullName"));

                String groupRefs = processor.getElementValue("group");
                if (!Collections.disjoint(groupNames, Splitter.on(' ').splitToList(nullToEmpty(groupRefs)))){
                    users.add(user);
                }
            }

            users.forEach( user -> printer.accept(String.format("%s/%s", user.getFullName(), user.getEmail())) );

        }


//        Payload payload = JAXB_PARSER.unmarshal(
//                Resources.getResource("payload.xml").openStream());
//
//        payload.getUsers().getUser().stream()
//                .filter(user -> user.getGroup().stream()
//                        .filter(group -> project.equals( ( (ProjectType) ((GroupType) group).getProject() ).getName() )).count() > 0
//                )
//                .forEach( user -> printer.accept(String.format("%s/%s", user.getFullName(), user.getEmail())) );

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
