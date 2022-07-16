package gaya.pe.kr.util;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class BomGeneratorUtil {

    public static String documentToString(org.w3c.dom.Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformer = tf.newTransformer();

            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter sw = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.getBuffer().toString();
        } catch (TransformerException ex) {
            return null;
        }
    }

    public static File writeToFile(File file, String xmlString) throws IOException {
        try (FileWriter writer = new FileWriter(file.getAbsolutePath())) {
            System.out.println(file.getAbsolutePath() + " 위치에 SBOM 생성 완료");
            writer.write(xmlString);
        }
        return file;
    }


}
