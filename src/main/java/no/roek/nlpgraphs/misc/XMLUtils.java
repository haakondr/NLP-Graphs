package no.roek.nlpgraphs.misc;

import java.io.FileInputStream;
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
				if(row.getAttribute("name").getValue().equals("plagiarism") || row.getAttribute("name").getValue().equals("detected-plagiarism")) {
					String type = row.getAttributeValue("type");
					String obfuscation = row.getAttributeValue("obfuscation");
					String language = row.getAttributeValue("this_language");
					String offset = row.getAttributeValue("this_offset");
					String length = row.getAttributeValue("this_length");
					String sourceReference = row.getAttributeValue("source_reference");
					String sourceLanguage = row.getAttributeValue("source_language");
					String sourceOffset = row.getAttributeValue("source_offset");
					String sourceLength = row.getAttributeValue("source_length");
					
					plagiarisms.add(new PlagiarismReference(type, obfuscation, language, offset, length, sourceReference, sourceLanguage, sourceOffset, sourceLength));
					return plagiarisms;
				}
			}
			
			} catch (JDOMException | IOException e) {
				e.printStackTrace();
			}
		return plagiarisms;
		}
	}
