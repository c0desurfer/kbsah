package ch.c0desurfer.kbsah.ui.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@UtilityClass
@Slf4j
public class RecordFormatter {

    public boolean isXmlValid(String string) {
        if (string == null || string.isBlank()) {
            return false;
        }

        try {
            SAXParserFactory.newDefaultInstance().newSAXParser().getXMLReader().parse(new InputSource(new StringReader(string)));
            return true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.debug("Not XML content, SAXParser can't parse (ignoring exception).");
            return false;
        }
    }

    public boolean isJson(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }

        try {
            new JSONObject(json);
        } catch (JSONException jse) {
            try {
                new JSONArray(json);
            } catch (JSONException je) {
                return false;
            }
        }
        return true;
    }

    public String formatJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.toString(4);
        } catch (JSONException e) {
            log.debug("Can't prettify JSON. Maybe not JSON.");
            return json;
        }
    }

    public String formatXml(String xml) {
        try {
            // turn xml string into a document
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            Document document = documentBuilderFactory
                .newDocumentBuilder()
                .parse(new InputSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));

            // remove whitespaces outside tags
            document.normalize();
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']",
                document,
                XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }

            TransformerFactory transformerFactory = TransformerFactory.newDefaultInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // return pretty print xml string
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
            return stringWriter.toString();
        } catch (Exception e) {
            log.debug("Can't prettify XML. Maybe not XML.");
        }
        return xml;
    }
}
