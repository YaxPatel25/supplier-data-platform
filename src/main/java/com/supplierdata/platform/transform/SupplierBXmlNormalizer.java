package com.supplierdata.platform.transform;

import com.supplierdata.platform.dto.NormalizedSupplierRecord;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Normalizer strategy for Supplier B XML feeds (`SUP-XML-B`).
 * Handles attribute-driven XML structure:
 * <VoyageFeed supplier="SUP-XML-B"><Sailing line="Viking River" ship="Viking Sky" date="2026-10-10"><Pricing code="VIK-EARLY"/></Sailing></VoyageFeed>
 */
@Component
public class SupplierBXmlNormalizer implements SupplierNormalizer {

    @Override
    public String getSupplierCode() {
        return "SUP-XML-B";
    }

    @Override
    public String getSupportedFormat() {
        return "XML";
    }

    @Override
    public List<NormalizedSupplierRecord> normalize(Reader reader) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(reader));

        List<NormalizedSupplierRecord> results = new ArrayList<>();
        Element root = doc.getDocumentElement();
        String supplierCode = root.hasAttribute("supplier") ? root.getAttribute("supplier") : "SUP-XML-B";

        NodeList sailings = doc.getElementsByTagName("Sailing");
        for (int i = 0; i < sailings.getLength(); i++) {
            Element sailing = (Element) sailings.item(i);
            String line = sailing.getAttribute("line");
            String ship = sailing.getAttribute("ship");
            String date = sailing.getAttribute("date");

            NodeList pricings = sailing.getElementsByTagName("Pricing");
            String fareCode = "STANDARD";
            if (pricings.getLength() > 0) {
                Element pricing = (Element) pricings.item(0);
                if (pricing.hasAttribute("code")) {
                    fareCode = pricing.getAttribute("code");
                }
            }

            results.add(new NormalizedSupplierRecord(supplierCode, "XML", line, ship, date, fareCode));
        }
        return results;
    }
}
