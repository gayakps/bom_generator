package gaya.pe.kr.api;

import gaya.pe.kr.exception.IllegalGithubURLException;
import gaya.pe.kr.exception.OptionFileNotFoundException;
import gaya.pe.kr.type.KeyType;
import org.cyclonedx.BomGeneratorFactory;
import org.cyclonedx.CycloneDxSchema;
import org.cyclonedx.exception.GeneratorException;
import org.cyclonedx.generators.xml.BomXmlGenerator;
import org.cyclonedx.model.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

import static gaya.pe.kr.util.BomGeneratorUtil.documentToString;
import static gaya.pe.kr.util.BomGeneratorUtil.writeToFile;


/**
 * @author KimSeonwoo
 * @see gaya.pe.kr.util.BomGeneratorUtil
 */
public class BomGenerator {

    public static Bom createBom(String url) throws IllegalGithubURLException, OptionFileNotFoundException, IOException {
        if ( url.contains("github") ) {
            System.out.printf("Load Repository : %s%n", url);
            Document document = Jsoup.connect(url).get();
            Elements elementsMain = document
                    .getElementsByClass("Details-content--hidden-not-important js-navigation-container js-active-navigation-container d-md-block");
            // Github Repo Root 에 있는 모든 파일 목록

            for (Element element : elementsMain) {
                for (Element elementsByClass : element.getElementsByClass("js-navigation-open Link--primary")) {
                    // 모든 Github 파일은 링크를 통해 접근이 가능하기 때문에 하이퍼링크가 있는 요소들을 추출함
                    Attributes attributes = elementsByClass.attributes();
                    // 하이퍼링크를 가지고 있는 모든 요소의 Attribute 를 가져옴
                    for (Attribute attribute : attributes) {
                        String value = attribute.getValue();
                        String key = attribute.getKey();
                        if (value.equalsIgnoreCase("pom.xml") && key.equals("title")) { // Repository 의 모든 파일 중 Pom.xml 파일만 탐색
                            String href = "https://github.com/" + attributes.get("href");
                            System.out.printf("Load Pom.xml : %s%n", href);

                            Document pomDocument = Jsoup.connect(href).get(); // Pom.xml 로 접근
                            Elements pomContents = pomDocument.getElementsByClass("highlight tab-size js-file-line-container js-code-nav-container js-tagsearch-file js-search-container");
                            // pom xml의 모든 태그를 탐색

                            Bom bom = new Bom(); // Bom Object 형성
                            bom.setComponents(new ArrayList<>());
                            Component component = null; // 컴포넌트 초기화
                            License license = null; // 라이선스 초기화
                            LicenseChoice licenseChoice = new LicenseChoice();
                            Metadata metadata = new Metadata();
                            bom.setMetadata(metadata); // bom 메타 데이터 설정
                            bom.getMetadata().setLicenseChoice(licenseChoice); // 메타 데이터에 라이선스 부여

                            for (Element pomContent : pomContents) { // Pom.xml 내용을 한 줄 씩 읽음
                                Elements subNodeElements = pomContent.select("tr");
                                KeyType nowKeyType = KeyType.NONE; // 현재 탐색 중인 태그 이름 NONE 일 경우 탐색을 시작해야함
                                for (Element subNode : subNodeElements) {
                                    String text = subNode.text();
                                    String nowKeyTypeName = nowKeyType.name().toLowerCase(Locale.ROOT);

                                    if (nowKeyType.equals(KeyType.NONE)) {
                                        // 현재 탐색중인 Key Type 을 찾아야함
                                        for (KeyType keyType : KeyType.values()) {
                                            if (!keyType.equals(KeyType.NONE)) {
                                                String keyTypeName = keyType.name().toLowerCase(Locale.ROOT);
                                                if (text.equals(String.format("<%s>", keyTypeName))) { // 새로운 태그를 찾은 경우
                                                    nowKeyType = keyType; // Key Type 에 태그를 명시
                                                    switch (nowKeyType) {
                                                        case DEPENDENCY:
                                                            component = new Component();
                                                            break;
                                                        case LICENSE:
                                                            license = new License();
                                                            break;
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    } else { // key type
                                        // 탐색 진행중이고, 만일 text 중에 같은 타입 나오면 끝내야함
                                        if (text.equals(String.format("</%s>", nowKeyTypeName))) {
                                            switch (nowKeyType) {
                                                case DEPENDENCY: {
                                                    if (component != null) {
                                                        component.setType(Component.Type.LIBRARY);
                                                        bom.addComponent(component);
                                                    }
                                                    component = null;
                                                    break;
                                                }
                                                case LICENSE: {
                                                    if (license != null) {
                                                        licenseChoice.addLicense(license);
                                                    }
                                                    license = null;
                                                    break;
                                                }
                                            }
                                            nowKeyType = KeyType.NONE;

                                        } else {
                                            for (String tag : nowKeyType.getTag()) {
                                                if (text.contains(tag)) {
                                                    String tagValue = text.replace(String.format("<%s>", tag), "").replace(String.format("</%s>", tag), "");
                                                    switch (nowKeyType) {
                                                        case DEPENDENCY: {
                                                            switch (tag) {
                                                                case "groupId":
                                                                    component.setGroup(tagValue);
                                                                    break;
                                                                case "artifactId":
                                                                    component.setName(tagValue);
                                                                    break;
                                                                case "version":
                                                                    component.setVersion(tagValue);
                                                                    break;
                                                            }
                                                            break;
                                                        }
                                                        case LICENSE: {
                                                            switch (tag) {
                                                                case "name":
                                                                    license.setName(tagValue);
                                                                    break;
                                                                case "url":
                                                                    license.setUrl(tagValue);
                                                                    break;
                                                            }
                                                            break;
                                                        }
                                                    }
                                                }

                                            }
                                        }

                                    }

                                }
                            }
                            return bom;
                        }
                    }


                }

            }

            // pom 을 발견했다면 return 을 통해 bom 이 반환됨
            throw new OptionFileNotFoundException(String.format("%s의 Repository 에서 pom.xml을 발견하지 못했습니다", url));
        }
        else {
            throw new IllegalGithubURLException(String.format("%s은 Github 링크가 아닙니다", url));
        }


    }

    public static File createBomFile(String url) throws IllegalGithubURLException, OptionFileNotFoundException, IOException, GeneratorException, ParserConfigurationException {
        Bom bom = createBom(url);
        /**
         *             Bom bom = BomGenerator.createBom("https://github.com/CycloneDX/cyclonedx-core-java");
         *             BomXmlGenerator generator = BomGeneratorFactory.createXml(CycloneDxSchema.Version.VERSION_14, bom);
         *             org.w3c.dom.Document doc = generator.generate();
         *             documentToString(doc);
         *             writeToFile(tempFile, generator.toXmlString());
         */
        BomXmlGenerator generator = BomGeneratorFactory.createXml(CycloneDxSchema.Version.VERSION_14, bom);
        org.w3c.dom.Document doc = generator.generate();
        documentToString(doc);
        Path path = Files.createTempDirectory("cyclonedx-core-java-seonwoos");
        return writeToFile(new File(path.toFile(), "bom.xml"), generator.toXmlString());
    }






}
