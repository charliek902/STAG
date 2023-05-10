package edu.uob;

import com.alexmerz.graphviz.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class ActionsParserAndLoader {

    private final HashMap<String, HashSet<GameAction>> actionsMap;

    public ActionsParserAndLoader(File actionsFile) throws IOException, ParserConfigurationException, RuntimeException, SAXException, FileNotFoundException {
        actionsMap = new HashMap<>();

        try (InputStream inputStream = new FileInputStream(actionsFile)) {

            // make methods out of this ...
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            Element root = doc.getDocumentElement();
            NodeList actionNodes = root.getElementsByTagName("action");

            for (int i = 0; i < actionNodes.getLength(); i++) {
                Element actionNode = (Element) actionNodes.item(i);
                List<String> triggers = getChildElementTextList(actionNode, "triggers", "keyphrase");
                List<String> subjects = getChildElementTextList(actionNode, "subjects", "entity");
                List<String> consumedEntities = getChildElementTextList(actionNode, "consumed", "entity");
                List<String> producedEntities = getChildElementTextList(actionNode, "produced", "entity");
                String narration = getChildElementText(actionNode, "narration");
                GameAction action = new GameAction(triggers, subjects, consumedEntities, producedEntities, narration);

                for (String trigger : triggers) {
                    HashSet<GameAction> actionsForTrigger = actionsMap.computeIfAbsent(trigger, k -> new HashSet<>());
                    actionsForTrigger.add(action);
                }
            }
        }
    }

    private List<String> getChildElementTextList(Element parentElement, String childElementName, String childNodeName) {
        List<String> list = new ArrayList<>();
        Element childElement = (Element) parentElement.getElementsByTagName(childElementName).item(0);
        NodeList childNodes = childElement.getElementsByTagName(childNodeName);

        for (int i = 0; i < childNodes.getLength(); i++) {
            Element childNode = (Element) childNodes.item(i);
            list.add(childNode.getTextContent());
        }
        return list;
    }

    private String getChildElementText(Element parentElement, String childElementName) {
        Element childElement = (Element) parentElement.getElementsByTagName(childElementName).item(0);
        return childElement.getTextContent();
    }

    public HashMap<String, HashSet<GameAction>> getActionsMap() {
        return this.actionsMap;
    }
}
