package com.supplierdata.platform.transform;

import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultXmlNormalizer implements SupplierNormalizer {

    private static final String STYLESHEET_PATH = "xslt/supplier-xml-to-internal.xsl";

    @Override
    public String getSupplierCode() {
        return "DEFAULT";
    }

    @Override
    public String getSupportedFormat() {
        return "XML";
    }

    @Override
    public List<NormalizedSupplierRecord> normalize(Reader xmlReader) throws Exception {
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer(new StreamSource(new ClassPathResource(STYLESHEET_PATH).getInputStream()));

        DOMResult result = new DOMResult();
        transformer.transform(new StreamSource(xmlReader), result);
        Document transformedDoc = (Document) result.getNode();

        return extractRecords(transformedDoc);
    }

    private List<NormalizedSupplierRecord> extractRecords(Document doc) {
        List<NormalizedSupplierRecord> records = new ArrayList<>();
        NodeList nodes = doc.getElementsByTagName("SupplierRecord");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            records.add(new NormalizedSupplierRecord(
                    text(element, "SupplierCode"),
                    "XML",
                    text(element, "CruiseLine"),
                    text(element, "ShipName"),
                    text(element, "SailingDate"),
                    text(element, "FareCode")
            ));
        }
        return records;
    }

    private String text(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        return nl.getLength() > 0 ? nl.item(0).getTextContent() : null;
    }
}
