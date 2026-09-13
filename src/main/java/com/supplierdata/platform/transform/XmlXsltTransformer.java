package com.supplierdata.platform.transform;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies an XSLT stylesheet to convert supplier XML payloads into the
 * internal schema, then parses the transformed XML into normalized DTOs.
 * Direct equivalent of the "XSLT transformations to convert XML ... payloads
 * into the required internal schema" step from the original .NET pipeline.
 */
@Component
public class XmlXsltTransformer {

    private static final String STYLESHEET_PATH = "xslt/supplier-xml-to-internal.xsl";

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
            var element = (org.w3c.dom.Element) nodes.item(i);
            records.add(new NormalizedSupplierRecord(
                    text(element, "SupplierCode"),
                    text(element, "SourceFormat"),
                    text(element, "CruiseLine"),
                    text(element, "ShipName"),
                    text(element, "SailingDate"),
                    text(element, "FareCode")
            ));
        }
        return records;
    }

    private String text(org.w3c.dom.Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        return nl.getLength() > 0 ? nl.item(0).getTextContent() : null;
    }
}
