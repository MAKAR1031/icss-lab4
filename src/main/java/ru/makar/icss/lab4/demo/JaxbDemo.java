package ru.makar.icss.lab4.demo;

import org.xml.sax.SAXException;
import ru.makar.icss.lab4.model.Info;
import ru.makar.icss.lab4.report.ReportGenerator;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class JaxbDemo {

    private static final String SOURCES_XSD_PATH = "xsd/schema.xsd";
    private static final String SOURCES_XML_PATH = "xml/sample-data.xml";

    public void run() {
        try {
            URL xsdUrl = getClass().getClassLoader().getResource(SOURCES_XSD_PATH);
            URL xmlUrl = getClass().getClassLoader().getResource(SOURCES_XML_PATH);

            if (xsdUrl == null || xmlUrl == null) {
                throw new IllegalStateException("One or both of these files were not found:\n  " +
                        SOURCES_XML_PATH +
                        "\n  " +
                        SOURCES_XSD_PATH);
            }

            JAXBContext context = JAXBContext.newInstance("ru.makar.icss.lab4.model");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(xsdUrl);
            unmarshaller.setSchema(schema);
            StreamSource streamSource = new StreamSource(new File(xmlUrl.toURI()));
            JAXBElement<Info> jaxbElement = unmarshaller.unmarshal(streamSource, Info.class);
            Info info = jaxbElement.getValue();
            processBirthDates(info);
            File out = new File("out");
            ReportGenerator reportGenerator = new ReportGenerator();
            reportGenerator.setGroupsInfo(info);
            reportGenerator.setTargetName("group-info.html");
            File report = reportGenerator.generate(out);
            if (report != null) {
                System.out.println("Unmarshalling complete. Report file: " + report.getAbsolutePath());
            } else {
                System.err.println("Report generate fail");
            }

            info.setStudents(null);
            Marshaller marshaller = context.createMarshaller();
            QName qName = new QName("http://makar.ru/icss/lab4/xsd", "info");
            JAXBElement<Info> groups = new JAXBElement<Info>(qName, Info.class, info);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            File groupsOnlyXsd = new File(out, "groups-only.xml");
            marshaller.marshal(groups, groupsOnlyXsd);
            System.out.println("Marshaling complete. XML file: " + groupsOnlyXsd.getAbsolutePath());
        } catch (JAXBException | SAXException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void processBirthDates(Info info) {
        info.getStudents().getStudent().forEach(s -> {
            if (s.getAge() != null) {
                s.setBirthDate(LocalDate.now().minus(s.getAge(), ChronoUnit.YEARS));
            } else {
                s.setAge((int) LocalDate.now().until(s.getBirthDate(), ChronoUnit.YEARS));
            }
        });
    }
}
