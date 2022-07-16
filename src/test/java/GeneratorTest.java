import gaya.pe.kr.api.BomGenerator;
import gaya.pe.kr.exception.IllegalGithubURLException;
import gaya.pe.kr.exception.OptionFileNotFoundException;
import org.cyclonedx.BomGeneratorFactory;
import org.cyclonedx.CycloneDxSchema;
import org.cyclonedx.exception.GeneratorException;
import org.cyclonedx.generators.xml.BomXmlGenerator;
import org.cyclonedx.model.Bom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.naming.OperationNotSupportedException;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
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
import java.nio.file.Files;
import java.nio.file.Path;

import static gaya.pe.kr.util.BomGeneratorUtil.documentToString;
import static gaya.pe.kr.util.BomGeneratorUtil.writeToFile;

public class GeneratorTest {

    private File tempFile;

    @BeforeEach
    public void before() throws IOException {
        Path path = Files.createTempDirectory("cyclonedx-core-java-seonwoos");
        this.tempFile = new File(path.toFile(), "bom.xml");
    }

    public void after() {
        System.out.printf("테스트 성공 경로 명 : %s", tempFile.getAbsolutePath());
    }

    @Test
    public void createCycloneDXBom() throws Exception {
        try {
            Bom bom = BomGenerator.createBom("https://github.com/CycloneDX/cyclonedx-core-java");
            BomXmlGenerator generator = BomGeneratorFactory.createXml(CycloneDxSchema.Version.VERSION_14, bom);
            org.w3c.dom.Document doc = generator.generate();
            documentToString(doc);
            writeToFile(tempFile, generator.toXmlString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void createCycloneDXFileBom() throws Exception {
        try {
            File bom = BomGenerator.createBomFile("https://github.com/CycloneDX/cyclonedx-core-java");
            System.out.println(bom.toPath() + " << 생성잼");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}
