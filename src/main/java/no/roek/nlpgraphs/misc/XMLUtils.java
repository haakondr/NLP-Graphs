package no.roek.nlpgraphs.misc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


import no.roek.nlpgraphs.document.PlagiarismReference;

public class XMLUtils {

	public static List<PlagiarismReference> getPlagiarismReferences(String annotationFile) {
		List<PlagiarismReference> plagiarisms = new ArrayList<>();

		SAXBuilder builder = new SAXBuilder();
		InputStream is;
		try {
			is = new FileInputStream(annotationFile);
			Document document = builder.build(is);
			Element root = document.getRootElement();
			List<Element> elements = root.getChildren("feature");
			for (int i = 0; i < elements.size(); i++) {
				Element row = elements.get(i);
				if(row.getAttribute("name").equals("plagiarism")) {
					String type = row.getAttributeValue("type");
					String obfuscation = row.getAttributeValue("obfuscation");
					String language = row.getAttributeValue("this_language");
					int offset = row.getAttribute("this_offset").getIntValue();
					int length = row.getAttribute("this_length").getIntValue();
					String sourceReference = row.getAttributeValue("source_reference");
					String sourceLanguage = row.getAttributeValue("source_language");
					int sourceOffset = row.getAttribute("source_offset").getIntValue();
					int sourceLength = row.getAttribute("source_length").getIntValue();
					
					plagiarisms.add(new PlagiarismReference(type, obfuscation, language, offset, length, sourceReference, sourceLanguage, sourceOffset, sourceLength));
				}
			}
			
			} catch (JDOMException | IOException e) {
				e.printStackTrace();
			}

			return plagiarisms;
		}
	}
